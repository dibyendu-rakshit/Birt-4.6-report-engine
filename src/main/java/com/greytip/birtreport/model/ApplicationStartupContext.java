package com.greytip.birtreport.model;

import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Dibyendu on 1/17/2018.
 */
@Component
public class ApplicationStartupContext {

    private Connection publicConnection;

    public Connection getPublicConnection() {
        return publicConnection;
    }

    public void setPublicConnection(Connection publicConnection) {
        this.publicConnection = publicConnection;
    }

    @PreDestroy
    public void closeConnection() {
        try {
            if (publicConnection != null) {
                publicConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
