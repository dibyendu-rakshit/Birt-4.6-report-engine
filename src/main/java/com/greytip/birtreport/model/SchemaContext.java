package com.greytip.birtreport.model;

/**
 * Created by Dibyendu on 1/17/2018.
 */
public class SchemaContext {

    private String domainName;
    private String connectionUrl;
    private String userName;
    private String password;

    public SchemaContext(String domainName, String connectionUrl, String userName, String password) {
        this.domainName = domainName;
        this.connectionUrl = connectionUrl;
        this.userName = userName;
        this.password = password;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
