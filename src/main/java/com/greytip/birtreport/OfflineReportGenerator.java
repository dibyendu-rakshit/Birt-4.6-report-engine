package com.greytip.birtreport;

import com.greytip.attachment.AttachmentService;
import com.greytip.birtreport.model.OfflineReportContext;
import com.greytip.birtreport.model.OfflineReportInfo;
import com.greytip.birtreport.model.ParameterInfo;
import com.greytip.birtreport.model.ParameterTypeDef;
import com.greytip.birtreport.util.DateUtils;
import com.greytip.birtreport.util.RenderOpsUtil;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.birt.report.engine.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dibyendu on 12/15/2017.
 */
@Service
public class OfflineReportGenerator {

    @Autowired
    private IReportEngine engine;
    @Autowired
    private AttachmentService attachmentService;

    @Value("${birt.report.output.file-loc}")
    private String outPutFileLoc;

    @Value("${aws.s3.bucket_name}")
    private String s3Bucket;

    private Map<String, OfflineReportInfo> reportInfoMap = new HashMap<String, OfflineReportInfo>();

    private static final String PREFIX = "reports";

    private static final Logger log = LoggerFactory.getLogger(OfflineReportGenerator.class);

    public Map<String, String> generateReport(OfflineReportContext reportContext, Connection connection) {
        Map<String, String> outPut = new HashMap<>();
        File designFile = new File(reportContext.getReportDesign());
        if (!designFile.exists()) {
            log.info("Unable to create report using design file {} .", reportContext.getFileName());
            return null;
        }
        IRunAndRenderTask task = null;

        try {
            IReportRunnable design = engine.openReportDesign(reportContext.getReportDesign());
            this.updateReportInfo(designFile, design);
            task = engine.createRunAndRenderTask(design);
            //set the parameters
            if (reportContext.getParams() != null) {
                OfflineReportInfo offlineReportInfo = reportInfoMap.get(designFile.getAbsolutePath());
                Map<String, Object> reportParameters = new HashMap<>();
                reportContext.getParams().forEach((k, v) -> setParameter(offlineReportInfo, k, v, reportParameters));
                task.setParameterValues(reportParameters);
            }

            // validate the parameters
            task.validateParameters();

            //set the output format
            final IRenderOption renderOption = RenderOpsUtil.reportGeneratedFormat(reportContext);
            final String attachmentFileName = getOutputFile(reportContext);
            final String outputFile = String.format("%s/%s", outPutFileLoc, attachmentFileName);
            renderOption.setOutputFileName(outputFile);
            task.setRenderOption(renderOption);

            //set the connection object
            task.getAppContext().put("OdaJDBCDriverPassInConnection", connection);
            // now generate the report
            task.run();

            outPut.put("attachment_name", attachmentFileName);

            // now delete the report generated file because of space issue
            File file = new File(outputFile);

            //First creating a folder into s3
            String attachmentId = attachmentService.createAttachment(s3Bucket, reportContext.getAccessId(), PREFIX, reportContext.getUserId());

            // then upload into S3 {bucketName}/{accessId}/reports/{attachmentId}
            attachmentService.addAttachmentFile(s3Bucket, reportContext.getAccessId(), attachmentId, file);

            String attachmentPathId = String.format("%s://%s", "s3", attachmentId);
            outPut.put("attachment_path", attachmentPathId);

            file.delete();

            return outPut;

        } catch (EngineException e) {
            log.error("could not able generate report by Birt4.6 report engine due to {} ", e);
            return null;
        } finally {
            if (task != null) {
                task.close();
            }
        }
    }

    private void updateReportInfo(File designFile, IReportRunnable design) {
        boolean dirty = Boolean.TRUE;
        OfflineReportInfo offlineReportInfo = null;
        if (reportInfoMap.containsKey(designFile.getAbsolutePath())) {
            offlineReportInfo = reportInfoMap.get(designFile.getAbsolutePath());
            if (offlineReportInfo.getLastModified() != null
                    && designFile.lastModified() <= offlineReportInfo.getLastModified().getTime()) {
                dirty = Boolean.FALSE;
            }
        }

        if (dirty) {
            if (offlineReportInfo == null) {
                offlineReportInfo = new OfflineReportInfo();
                offlineReportInfo.setKey(designFile.getAbsolutePath());
                reportInfoMap.put(designFile.getAbsolutePath(), offlineReportInfo);
            }
            offlineReportInfo.setLastModified(new Date(designFile.lastModified()));

            IGetParameterDefinitionTask ptask = engine.createGetParameterDefinitionTask(design);
            Collection<IScalarParameterDefn> params = ptask.getParameterDefns(false);

            for (IScalarParameterDefn scalar : params) {
                ParameterInfo paramInfo = new ParameterInfo(scalar.getName());
                paramInfo.setDataType(ParameterTypeDef.DataType.getType(scalar.getDataType()));
                paramInfo.setContolType(ParameterTypeDef.ControlType.getType(scalar
                        .getControlType()));
                paramInfo.setParameterType(ParameterTypeDef.ParameterType.getType(scalar
                        .getParameterType()));
                paramInfo.setAllowMultipleValues(StringUtils.equals(
                        scalar.getScalarParameterType(),
                        ParameterTypeDef.SCALAR_PARAM_TYPE_MULTI_VALUE));
                offlineReportInfo.addParameter(scalar.getName(), paramInfo);
            }
        }
    }

    private Map<String, Object> setParameter(OfflineReportInfo offlineReportInfo, String paramName, Object paramValue, Map<String, Object> reportParameters) {
        if (log.isDebugEnabled()) {
            log.debug("Setting parameter: {} and value: {} ", paramName, paramValue);
        }

        ParameterInfo paramInfo = offlineReportInfo.getParameter(paramName);

        if (paramInfo != null && paramValue != null) {
            ParameterTypeDef.DataType dataType = paramInfo.getDataType();
            String paramString = paramValue.toString();
            if (paramValue instanceof Date) {
                paramString = DateUtils.formatDate((Date) paramValue);
            }

            if (dataType == ParameterTypeDef.DataType.BOOLEAN) {
                reportParameters.put(paramName, BooleanUtils.toBoolean(paramString));
            } else if (dataType == ParameterTypeDef.DataType.DATE) {
                if (paramValue instanceof Date) {
                    reportParameters.put(paramName, paramString);
                } else {
                    reportParameters.put(paramName, DateUtils.parseDate(paramString));
                }
            } else if (dataType == ParameterTypeDef.DataType.DATE_TIME || dataType == ParameterTypeDef.DataType.TIME) {
                if (paramValue instanceof Date) {
                    reportParameters.put(paramName, paramString);
                } else {
                    reportParameters.put(paramName, DateUtils.parseDateTime(paramString));
                }
            } else if (dataType == ParameterTypeDef.DataType.DECIMAL || dataType == ParameterTypeDef.DataType.FLOAT) {
                reportParameters.put(paramName, NumberUtils.toDouble(paramString));
            } else if (dataType == ParameterTypeDef.DataType.INTEGER) {
                reportParameters.put(paramName, NumberUtils.toInt(paramString));
            } else if (dataType == ParameterTypeDef.DataType.STRING) {
                if (paramInfo.isAllowMultipleValues()) {
                    if (paramValue instanceof String) {
                        reportParameters.put(paramName, paramString.split(","));
                    } else {
                        reportParameters.put(paramName, paramValue);
                    }
                } else {
                    reportParameters.put(paramName, paramString);
                }
            } else {
                reportParameters.put(paramName, paramValue);
            }
        } else {
            reportParameters.put(paramName, paramValue);
        }
        return reportParameters;
    }

    private String getOutputFile(OfflineReportContext context) {
        Long time = new Long(0);
        synchronized (this) {
            time = (new Date()).getTime();
        }

        String fileName = context.getFileName().replaceAll("[\\\\/:*?\"<>|]", "_") + "-" + time + "."
                + context.getReportType().getExtension();

        if (log.isDebugEnabled()) {
            log.debug("Creating temp file {} ", fileName);
        }
        return fileName;
    }
}
