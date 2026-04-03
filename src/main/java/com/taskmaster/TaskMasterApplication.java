package com.taskmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * TaskMaster - Collaborative Task Tracking System
 * Main entry point for the Spring Boot application.
 */
@SpringBootApplication
@EnableAsync
public class TaskMasterApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskMasterApplication.class, args);
    }
}
