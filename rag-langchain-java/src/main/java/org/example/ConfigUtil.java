/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

/**
 * langchain4j provides Spring Boot integration - https://docs.langchain4j.dev/tutorials/spring-boot-integration -
 * and Quarkus integration - https://docs.langchain4j.dev/tutorials/quarkus-integration - to handle
 * configuring langchain4j objects via a properties file. To keep these examples simple - i.e. avoiding more
 * dependencies, as well as Java 17 - this utility class is used to read properties from a .env file instead. You are of
 * course free to use any approach you wish for reading configuration files.
 */
class ConfigUtil {

    static DatabaseClient newDatabaseClient() {
        return DatabaseClientFactory.newClient("localhost", 8003,
            new DatabaseClientFactory.DigestAuthContext("ai-examples-user", "password"));
    }

    static ChatLanguageModel newChatLanguageModel() {
        Config config = newConfig();
        return AzureOpenAiChatModel.builder()
            .apiKey(config.apiKey)
            .serviceVersion(config.serviceVersion)
            .endpoint(config.endpoint)
            .deploymentName(config.llmDeploymentName)
            .build();
    }

    static EmbeddingModel newEmbeddingModel() {
        Config config = newConfig();
        return AzureOpenAiEmbeddingModel.builder()
            .apiKey(config.apiKey)
            .serviceVersion(config.serviceVersion)
            .endpoint(config.endpoint)
            .deploymentName(config.embeddingDeploymentName)
            .build();
    }

    static Config newConfig() {
        Dotenv dotenv = loadDotenv();
        Config config = new Config();
        config.apiKey = dotenv.get("AZURE_OPENAI_API_KEY");
        config.serviceVersion = dotenv.get("OPENAI_API_VERSION");
        config.endpoint = dotenv.get("AZURE_OPENAI_ENDPOINT");
        config.llmDeploymentName = dotenv.get("AZURE_LLM_DEPLOYMENT_NAME");
        config.embeddingDeploymentName = dotenv.get("AZURE_EMBEDDING_DEPLOYMENT_NAME");
        return config;
    }

    static Dotenv loadDotenv() {
        try {
            return Dotenv.load();
        } catch (DotenvException e) {
            return Dotenv.configure().directory("..").load();
        }
    }

    static class Config {
        String apiKey;
        String serviceVersion;
        String endpoint;
        String llmDeploymentName;
        String embeddingDeploymentName;
    }
}
