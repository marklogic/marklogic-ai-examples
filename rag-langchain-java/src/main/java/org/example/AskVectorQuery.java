/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import com.marklogic.client.DatabaseClient;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;

public class AskVectorQuery {

    public static void main(String[] args) {
        // A default question is here to simplify running this in an IDE.
        final String question = args.length > 0 ? args[0] : "What disturbances has Jane Doe caused?";

        ChatLanguageModel chatLanguageModel = ConfigUtil.newChatLanguageModel();
        EmbeddingModel embeddingModel = ConfigUtil.newEmbeddingModel();
        DatabaseClient databaseClient = ConfigUtil.newDatabaseClient();

        Assistant assistant = AiServices.builder(Assistant.class)
            .chatLanguageModel(chatLanguageModel)
            .contentRetriever(new VectorQueryRetriever(databaseClient, embeddingModel))
            .build();

        Result<String> result = assistant.chat(question);
        System.out.println("Question: " + question);
        result.sources().forEach(source -> System.out.println("Document URI: " + source.textSegment().metadata().getString("uri")));
        System.out.println("Answer:");
        System.out.println(result.content());
    }
}
