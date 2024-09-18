/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.io.IOException;

public class WordQueryExample {

    // See https://docs.langchain4j.dev/tutorials/rag for more information on RAG assistants.
    public interface Assistant {
        @UserMessage("You are an assistant for question-answering tasks. " +
            "Use the following pieces of retrieved context to answer the question. " +
            "If you don't know the answer, just say that you don't know. " +
            "Use six sentences maximum. " +
            "Question: {{question}}")
        Result<String> chat(@V("question") String question);
    }

    public static void main(String[] args) throws IOException {
        // A default question is here to simplify running this in an IDE.
        final String question = args.length > 0 ? args[0] : "Tell me about Jane Johnson";

        ChatLanguageModel chatLanguageModel = ConfigUtil.newChatLanguageModel(args);

        DatabaseClient client = DatabaseClientFactory.newClient("localhost", 8003,
            new DatabaseClientFactory.DigestAuthContext("ai-examples-user", "password"));

        Assistant assistant = AiServices.builder(Assistant.class)
            .chatLanguageModel(chatLanguageModel)
            .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
            .contentRetriever(new WordQueryContentRetriever(client))
            .build();

        Result<String> result = assistant.chat(question);
        System.out.println("Question: " + question);
        result.sources().forEach(source -> System.out.println("Document URI: " + source.textSegment().metadata().getString("uri")));
        System.out.println("Answer:");
        System.out.println(result.content());
    }
}
