package com.greytip.birtreport.model;


public class ParameterInfo {

    private String name;

    private ParameterTypeDef.DataType dataType;

    private ParameterTypeDef.ControlType contolType;

    private ParameterTypeDef.ParameterType parameterType;

    private boolean allowMultipleValues;

    public ParameterInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParameterTypeDef.DataType getDataType() {
        return dataType;
    }

    public void setDataType(ParameterTypeDef.DataType dataType) {
        this.dataType = dataType;
    }

    public ParameterTypeDef.ControlType getContolType() {
        return contolType;
    }

    public void setContolType(ParameterTypeDef.ControlType contolType) {
        this.contolType = contolType;
    }

    public ParameterTypeDef.ParameterType getParameterType() {
        return parameterType;
    }

    public void setParameterType(ParameterTypeDef.ParameterType parameterType) {
        this.parameterType = parameterType;
    }

    public boolean isAllowMultipleValues() {
        return allowMultipleValues;
    }

    public void setAllowMultipleValues(boolean allowMultipleValues) {
        this.allowMultipleValues = allowMultipleValues;
    }

}
