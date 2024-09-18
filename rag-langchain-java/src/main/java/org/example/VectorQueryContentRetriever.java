/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.expression.PlanBuilder;
import com.marklogic.client.row.RowManager;
import com.marklogic.client.row.RowRecord;
import com.marklogic.client.row.RowSet;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VectorQueryContentRetriever implements ContentRetriever {

    private final DatabaseClient databaseClient;
    private final EmbeddingModel embeddingModel;

    public VectorQueryContentRetriever(DatabaseClient client, EmbeddingModel embeddingModel) {
        this.databaseClient = client;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public List<Content> retrieve(Query query) {
        List<String> terms = Stream.of(query.text().split(" "))
            .filter(term -> term.length() > 3 || Character.isUpperCase(term.charAt(0)))
            .collect(Collectors.toList());

        Response<Embedding> response = embeddingModel.embed(query.text());

        RowManager rowManager = databaseClient.newRowManager();
        PlanBuilder op = rowManager.newPlanBuilder();
        PlanBuilder.ModifyPlan plan = op.fromSearchDocs(op.cts.andQuery(
                op.cts.wordQuery(op.xs.stringSeq(terms.toArray(new String[]{}))),
                op.cts.collectionQuery("events")
            ))
            .limit(100)
            .joinInner(
                op.fromView("example", "events", "", op.fragmentIdCol("vectorsDocId")),
                op.on(op.fragmentIdCol("fragmentId"), op.fragmentIdCol("vectorsDocId"))
            )
            .bind(op.as(op.col("similarity"), op.vec.cosineSimilarity(
                op.vec.vector(op.col("embedding")),
                op.vec.vector(op.xs.floatSeq(response.content().vector()))
            )))
            .orderBy(op.desc(op.col("similarity")))
            .select(op.col("uri"), op.col("text"))
            .limit(10);

        RowSet<RowRecord> rows = rowManager.resultRows(plan);
        List<Content> results = new ArrayList<>();
        rows.stream().forEach(row -> {
            String text = row.getString("text");
            Metadata metadata = new Metadata();
            metadata.put("uri", row.getString("uri"));
            Content content = new Content(new TextSegment(text, metadata));
            results.add(content);
        });
        return results;
    }
}
