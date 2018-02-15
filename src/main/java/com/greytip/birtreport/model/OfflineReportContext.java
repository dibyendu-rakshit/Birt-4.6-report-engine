package com.greytip.birtreport.model;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;


public class OfflineReportContext {
	public enum ReportType {
		PDF("pdf", "pdf", "pdf"),
		EXCEL("excel", "Excel", "xls"),
		WORD("word","Word", "doc"),
		HTML("html", "HTML", "html"),
		CSV("csv", "CSV","csv");

		private String id;
		private String description;
		private String extension;

		ReportType(String id, String desc, String extension) {
			this.id = id;
			this.description = desc;
			this.extension = extension;
		}

		public String getDescription() {
			return description;
		}

		public String getId() {
			return id;
		}

		public String getExtension() {
			return extension;
		}

		public static ReportType getType(String format) {
			if (StringUtils.equalsIgnoreCase(format, PDF.getId())) {
				return PDF;
			} else if (StringUtils.equalsIgnoreCase(format, EXCEL.getId())) {
				return EXCEL;
			} else if (StringUtils.equalsIgnoreCase(format, WORD.getId())) {
				return WORD;
			} else if (StringUtils.equalsIgnoreCase(format, HTML.getId())) {
				return HTML;
			}
			return null;
		}

		@Override
		public String toString() {
			return this.description;
		}
	};

	private String reportDesign;

	private ReportType reportType;

	private Map<String, Object> params = new HashMap<String, Object>();

	private String key;

	private String fileName;

	private String successMessage;

	private String failureMessage;

	private boolean success = true;

	private String accessId;

    private String userId;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public OfflineReportContext(String reportDesign, ReportType reportType,
			Map<String, Object> params, String key,String accessId, String userId) {
		this(reportDesign, reportType, params, key, "document."+ reportType.toString(),accessId,userId);
	}

	public OfflineReportContext(String reportDesign, ReportType reportType,
			Map<String, Object> params, String key, String fileName,String accessId, String userId) {
		super();
		this.reportDesign = reportDesign;
		this.reportType = reportType;
		this.params = params;
		this.key = key;
		this.fileName = fileName;
		this.accessId = accessId;
		this.userId = userId;
	}

	public void addParam(String key, Object value) {
		params.put(key, value);
	}

	public void removeParam(String key) {
		params.remove(key);
	}

	public Object getParam(String key) {
		return params.get(key);
	}

	public String getReportDesign() {
		return reportDesign;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public String getKey() {
		return key;
	}

	public ReportType getReportType() {
		return reportType;
	}

	public String getFileName() {
		return fileName;
	}

	public String getSuccessMessage() {
		return successMessage;
	}

	public void setSuccessMessage(String successMessage) {
		this.successMessage = successMessage;
	}

	public String getFailureMessage() {
		return failureMessage;
	}

	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}

	public String getAccessId() {
		return accessId;
	}

    public String getUserId() {
        return userId;
    }
}
