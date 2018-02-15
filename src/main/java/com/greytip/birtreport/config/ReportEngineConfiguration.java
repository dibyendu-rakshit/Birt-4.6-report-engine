package com.greytip.birtreport.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greytip.attachment.AttachmentService;
import com.greytip.birtreport.model.ApplicationStartupContext;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.core.framework.PlatformConfig;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by Dibyendu on 12/14/2017.
 */
@Configuration
public class ReportEngineConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ReportEngineConfiguration.class);

    @Value("${cougar.public.schema.url}")
    private String publicSchemaUrl;

    @Value("${cougar.public.schema.username}")
    private String publicSchemaUsername;

    @Value("${cougar.public.schema.password}")
    private String publicSchemaPassword;

    private ApplicationStartupContext startupContext;

    @Value("${aws.region}")
    private String awsRegion;
    @Value("${aws.access_key}")
    private String awsAccessKey;
    @Value("${aws.access_secret}")
    private String awsAccessSecret;

    @Autowired
    public void setStartupContext(ApplicationStartupContext startupContext) {
        this.startupContext = startupContext;
    }

    @Bean
    public IReportEngine createReportEngine() throws BirtException, IOException {
        logger.info("creating report engine");

        EngineConfig config = new EngineConfig();
        HashMap<Object, Object> appContext = new HashMap<>();
        appContext.put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, this
                .getClass().getClassLoader());

        config.setAppContext(appContext);

        Platform.startup();

        IReportEngineFactory factory = (IReportEngineFactory) Platform
                .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        IReportEngine engine = factory.createReportEngine(config);

        logger.info("Report engine created, {} ", engine);
        return engine;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public AttachmentService attachmentService() {
        return new AttachmentService(awsAccessKey, awsAccessSecret, awsRegion);
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        restTemplate.getMessageConverters().add(converter);
        return restTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterApplicationStartup() {

        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("Where is your PostgreSQL JDBC Driver ? Include in your library path!!!!!!");
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection(publicSchemaUrl, publicSchemaUsername, publicSchemaPassword);
        } catch (SQLException e) {
            logger.error("Connection Failed! Check output console");
            e.printStackTrace();
        }
        this.startupContext.setPublicConnection(conn);
    }
}
