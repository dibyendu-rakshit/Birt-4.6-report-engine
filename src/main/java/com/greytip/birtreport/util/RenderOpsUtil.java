package com.greytip.birtreport.util;

import com.greytip.birtreport.model.OfflineReportContext;
import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Dibyendu on 12/15/2017.
 */
public class RenderOpsUtil {

    private static final Logger log = LoggerFactory.getLogger(RenderOpsUtil.class);

    private RenderOpsUtil() {

    }

    public static IRenderOption reportGeneratedFormat(OfflineReportContext context) {
        IRenderOption options = null;
        log.info("Setting options for {} ", context.getReportType());
        if (context.getReportType().equals(OfflineReportContext.ReportType.PDF)) {
            options = getPdfOptions();
        } else if (context.getReportType().equals(OfflineReportContext.ReportType.WORD)) {
            options = getWordOptions();
        } else if (context.getReportType().equals(OfflineReportContext.ReportType.EXCEL)) {
            options = getExcelOptions();
        } else if (context.getReportType().equals(OfflineReportContext.ReportType.HTML)) {
            options = getHtmlOptions();
        } else if (context.getReportType().equals(OfflineReportContext.ReportType.CSV)) {
            options = getCsvOptions();
        }
        log.info("option format is {} ", options.getOutputFormat());
        return options;
    }


    private static IRenderOption getPdfOptions() {
        PDFRenderOption options = new PDFRenderOption();
        options.setOutputFormat(OfflineReportContext.ReportType.PDF.getExtension());
        return options;
    }

    private static IRenderOption getWordOptions() {
        EXCELRenderOption options = new EXCELRenderOption();
        options.setOutputFormat(OfflineReportContext.ReportType.WORD.getExtension());
        return options;
    }

    private static IRenderOption getExcelOptions() {
        EXCELRenderOption options = new EXCELRenderOption();
        options.setWrappingText(false);
        options.setOutputFormat(OfflineReportContext.ReportType.EXCEL.getExtension());
        return options;
    }

    private static IRenderOption getHtmlOptions() {
        HTMLRenderOption options = new HTMLRenderOption();
        options.setOutputFormat(OfflineReportContext.ReportType.HTML.getExtension());
        options.setHtmlPagination(false);
        options.setHtmlRtLFlag(false);
        options.setEmbeddable(false);
        return options;
    }

    private static IRenderOption getCsvOptions() {
        EXCELRenderOption options = new EXCELRenderOption();
        options.setOutputFormat(OfflineReportContext.ReportType.CSV.getExtension());
        return options;
    }
}
