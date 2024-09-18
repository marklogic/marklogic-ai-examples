/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

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
        Config config = newConfig(args);
        return AzureOpenAiChatModel.builder()
            .apiKey(config.apiKey)
            .serviceVersion(config.serviceVersion)
            .endpoint(config.endpoint)
            .deploymentName(config.llmDeploymentName)
            .build();
    }

    static EmbeddingModel newEmbeddingModel(String[] args) throws IOException {
        Config config = newConfig(args);
        return AzureOpenAiEmbeddingModel.builder()
            .apiKey(config.apiKey)
            .serviceVersion(config.serviceVersion)
            .endpoint(config.endpoint)
            .deploymentName(config.embeddingDeploymentName)
            .build();
    }

    static Config newConfig(String[] args) throws IOException {
        Config config = new Config();
        if (args.length < 2) {
            // Program is being run in an IDE.
            try (FileReader reader = new FileReader("gradle.properties")) {
                Properties props = new Properties();
                props.load(reader);
                config.serviceVersion = props.getProperty("OPENAI_API_VERSION");
                config.endpoint = props.getProperty("AZURE_OPENAI_ENDPOINT");
                config.llmDeploymentName = props.getProperty("AZURE_LLM_DEPLOYMENT_NAME");
                config.embeddingDeploymentName = props.getProperty("AZURE_EMBEDDING_DEPLOYMENT_NAME");
            }
            try (FileReader reader = new FileReader("gradle-local.properties")) {
                Properties props = new Properties();
                props.load(reader);
                config.apiKey = props.getProperty("AZURE_OPENAI_API_KEY");
            }
        } else {
            // Program is being run by Gradle. First argument is the question
            config.apiKey = args[1];
            config.serviceVersion = args[2];
            config.endpoint = args[3];
            config.llmDeploymentName = args[4];
            config.embeddingDeploymentName = args[5];
        }
        return config;
    }

    static class Config {
        String apiKey;
        String serviceVersion;
        String endpoint;
        String llmDeploymentName;
        String embeddingDeploymentName;
    }
}
