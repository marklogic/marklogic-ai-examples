/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * langchain4j provides Spring Boot integration - https://docs.langchain4j.dev/tutorials/spring-boot-integration -
 * and Quarkus integration - https://docs.langchain4j.dev/tutorials/quarkus-integration - to handle
 * configuring langchain4j objects via a properties file. To keep these examples simple - i.e. avoiding more
 * dependencies, as well as Java 17 - this utility class is used to read properties instead. You are of course
 * free to use any approach you want for reading configuration files.
 */
class ConfigUtil {

    static ChatLanguageModel newChatLanguageModel(String[] args) throws IOException {
        String apiKey;
        String serviceVersion;
        String endpoint;
        String deploymentName;

        if (args.length < 2) {
            // Program is being run in an IDE.
            try (FileReader reader = new FileReader("gradle.properties")) {
                Properties props = new Properties();
                props.load(reader);
                serviceVersion = props.getProperty("OPENAI_API_VERSION");
                endpoint = props.getProperty("AZURE_OPENAI_ENDPOINT");
                deploymentName = props.getProperty("AZURE_LLM_DEPLOYMENT_NAME");
            }
            try (FileReader reader = new FileReader("gradle-local.properties")) {
                Properties props = new Properties();
                props.load(reader);
                apiKey = props.getProperty("AZURE_OPENAI_API_KEY");
            }
        } else {
            // Program is being run by Gradle. First argument is the question
            apiKey = args[1];
            serviceVersion = args[2];
            endpoint = args[3];
            deploymentName = args[4];
        }

        return AzureOpenAiChatModel.builder()
            .apiKey(apiKey)
            .serviceVersion(serviceVersion)
            .endpoint(endpoint)
            .deploymentName(deploymentName)
            .build();
    }
}
