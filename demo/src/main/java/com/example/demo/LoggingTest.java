package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggingTest.class);

    public static void main(String[] args) {
        logger.info("SLF4J and Logback are now working!");
        logger.error("This is an error message.");
    }
}

