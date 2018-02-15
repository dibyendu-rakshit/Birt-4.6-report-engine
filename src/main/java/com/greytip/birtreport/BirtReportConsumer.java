package com.greytip.birtreport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greytip.birtreport.model.ApplicationStartupContext;
import com.greytip.birtreport.model.OfflineReportContext;
import com.greytip.birtreport.model.SchemaContext;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dibyendu on 12/15/2017.
 */
@Service
public class BirtReportConsumer {

    private static final Logger logger = LoggerFactory.getLogger(BirtReportConsumer.class);

    @Value("${birt.report.input.file-loc}")
    private String inputFileLoc;

    @Value("${birt.report.generator.username}")
    private String reportGeneratorUserName;

    @Value("${birt.report.generator.password}")
    private String reportGeneratorPassword;

    @Value("${cougar.dev-port:8080}")
    private int cougarDevPort;

    @Value("${spring.profiles.active}")
    private String envType;

    @Value("${cougar.domain.scheme}")
    private String reqScheme;

    @Autowired
    private OfflineReportGenerator reportGenerator;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private CougarService cougarService;
    @Autowired
    private ApplicationStartupContext startupContext;


    @KafkaListener(topics = "${kafka.topic}")
    public void onReceiveMessage(String payload, @Header(KafkaHeaders.OFFSET) Integer offset,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {

        logger.info("Processing key = {} ,topic = {}, partition = {}, offset = {}, payload = {}",
                key, topic, partition, offset, payload);

        String userNamePassWord = String.format("%s:%s", reportGeneratorUserName, reportGeneratorPassword);
        String domainName = null;
        /*
         * creating auth key that would be passed to authorization header
         * while calling cougar code to update queued_task table.
         */
        String authKey = new String(Base64.encodeBase64(userNamePassWord.getBytes()));
        Connection schemaConnection = null;

        try {
            //update the queued table that task it precessing
            Map<String, String> input = new HashMap<>();
            input.put("taskId", key);

            Map<String, Object> message = mapper.readValue(payload, Map.class);
            logger.info("message is {} ", message);

            String reportKey = (String) message.get("report");
            String accessId = (String) message.get("account");
            String userId = Integer.toString((Integer) message.get("user"));

            logger.info("accessId is {} ", accessId);

            SchemaContext schemaContext = getSchemaContext(accessId);

            logger.info("envType {} ", envType);

            if ("dev".equalsIgnoreCase(envType)) {
                domainName = String.format("%s://%s:%s/%s", "http", schemaContext.getDomainName(), cougarDevPort, "cougar");
            } else {
                if (reqScheme.equalsIgnoreCase("http")) {
                    domainName = String.format("%s://%s", "http", schemaContext.getDomainName());
                } else {
                    domainName = String.format("%s://%s", "https", schemaContext.getDomainName());
                }
            }

            logger.info("domain name is {} ", domainName);

            // update the status on queued_task table as processing
            cougarService.processTask(domainName, input, authKey);

            schemaConnection = getSchemaWiseConnection(schemaContext.getConnectionUrl(),
                    schemaContext.getUserName(), schemaContext.getPassword());

            Map<String, Object> filterParams = (Map<String, Object>) message.get("params");
            logger.info("filter params {} ", filterParams);

            OfflineReportContext.ReportType reportType = OfflineReportContext.ReportType.getType((String) message.get("report-type"));
            logger.info("report type {}", reportType.getExtension());

            OfflineReportContext reportContext = new OfflineReportContext(getRptDesignFile(reportKey), reportType, filterParams, null, reportKey, accessId,userId);
            Map<String, String> result = reportGenerator.generateReport(reportContext, schemaConnection);

            //update the queued_task table that job is completed.
            if (result != null && !result.isEmpty()) {
                result.put("taskId", key);
                cougarService.completeTask(domainName, result, authKey);
            }

        } catch (Exception e) {
            logger.error("Not able to generate report. Following reason {}", e);
        } finally {
            if (schemaConnection != null) {
                try {
                    schemaConnection.close();
                } catch (SQLException e) {
                    logger.error("Could not able to close schema wise connection {} ", e);
                }
            }
        }
    }

    private String getRptDesignFile(String reportKey) throws FileNotFoundException {
        logger.info("reportKey is {} ", reportKey);

        String rptFile = inputFileLoc + "/" + reportKey + "/" + reportKey + ".rptdesign";
        File file = new File(rptFile);
        if (!file.exists()) {
            throw new FileNotFoundException(rptFile + "is not found");
        }
        return rptFile;
    }

    private SchemaContext getSchemaContext(String accessId) {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        SchemaContext schemaContext = null;
        String query = "select ser.connectionurl url,\n" +
                "   acc.domainname,\n" +
                "   acc.schemausername,\n" +
                "   acc.schemapassword\n" +
                "  from tblaccountinfo acc \n" +
                "  inner join tblserverinfo ser on acc.dbserver = ser.dbcode\n" +
                "  and acc.accessid = ? ";

        try {
            dbConnection = startupContext.getPublicConnection();
            preparedStatement = dbConnection.prepareStatement(query);
            preparedStatement.setString(1, accessId);

            // execute select SQL statement
            ResultSet rs = preparedStatement.executeQuery();
            while (rs != null && rs.next()) {
                String domainName = rs.getString("domainname");
                String connectionUrl = rs.getString("url");
                String userName = rs.getString("schemausername");
                String password = rs.getString("schemapassword");
                schemaContext = new SchemaContext(domainName, connectionUrl, userName, password);
            }

        } catch (SQLException e) {
            logger.error("Could not able to create the connection for schema level. For following reason {}", e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return schemaContext;
    }

    private Connection getSchemaWiseConnection(String url, String userName, String password) {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("Where is your PostgreSQL JDBC Driver ? Include in your library path!!!!!!");
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection(url, userName, password);
        } catch (SQLException e) {
            logger.error("Connection Failed! Check output console {} ", e);
        }
        return conn;
    }
}
