/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;

public class AskContextualQuery {

    public static void main(String[] args) {
        // A default question is here to simplify running this in an IDE.
        final String question = args.length > 0 ? args[0] : "What disturbances has Jane Doe caused?";

        ChatLanguageModel chatLanguageModel = ConfigUtil.newChatLanguageModel();
        DatabaseClient databaseClient = ConfigUtil.newDatabaseClient();

        // Build a combined query to act as the contextual query.
        ObjectNode request = new ObjectMapper().createObjectNode();
        request.put("question", question);
        ArrayNode queries = request.putObject("combinedQuery").putObject("query").putArray("queries");
        queries.addObject().putObject("value-query").put("json-property", "type").put("text", "public intoxication");
        queries.addObject().putObject("collection-query").putObject("uri").put("text", "events");

        Result<String> result = AiServices.builder(Assistant.class)
            .chatLanguageModel(chatLanguageModel)
            // We need to use a custom augmentor that allows for our own query transformer to convert the serialized
            // JSON request into a ContextualQuery instance.
            .retrievalAugmentor(new ExampleAugmentor(new ContextualQueryRetriever(databaseClient)))
            .build()
            .chat(request.toString());

        System.out.println("Question: " + question);
        result.sources().forEach(source -> System.out.println("Document URI: " + source.textSegment().metadata().getString("uri")));
        System.out.println("Answer:");
        System.out.println(result.content());
    }

}
