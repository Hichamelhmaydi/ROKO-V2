package com.example.roko.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
public class DatabaseConnectionVerifier {

    private final DataSource dataSource;

    public DatabaseConnectionVerifier(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void verifyConnection() {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println(" Database connection established successfully!");
            System.out.println("Database: " + connection.getMetaData().getDatabaseProductName());
        } catch (Exception e) {
            System.err.println("Failed to connect to database");
            throw new RuntimeException(e);
        }
    }
}