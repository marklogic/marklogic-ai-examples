import { AzureOpenAIEmbeddings } from "@langchain/openai";
import { BaseRetriever } from "@langchain/core/retrievers";
import { Document } from "@langchain/core/documents";
import {config} from "dotenv";
import pkg from 'marklogic';
const { ctsQueryBuilder, planBuilder } = pkg;

export class VectorQueryRetriever extends BaseRetriever {
  lc_namespace = ["langchain", "retrievers"];
  marklogicClient;
  embeddingModel;

  constructor(fields) {
    super(fields);
    this.marklogicClient = fields.marklogicClient;
    config({path: "../.env"});
    this.embeddingModel = new AzureOpenAIEmbeddings({
      azureOpenAIApiDeploymentName: process.env.AZURE_EMBEDDING_DEPLOYMENT_NAME,
    });
  }

  #buildWordsArray(chatQuestion) {
    const words = [];
    chatQuestion.split(" ").forEach(word => {
      if (word.length > 2) words.push('"' + word.toLowerCase().replace("?", "") + '"')
    })
    return words;
  }

  async #generateEmbedding(chatQuestion) {
    return this.embeddingModel.embedQuery(chatQuestion);
  }

  #buildOpticPlan(words, chatQuestionEmbedding) {
    return planBuilder
      .fromSearchDocs(ctsQueryBuilder.cts.andQuery([
        ctsQueryBuilder.cts.wordQuery(words),
        ctsQueryBuilder.cts.collectionQuery("events")
      ]))
      .limit(100)
      .joinInner(
        planBuilder.fromView("example", "events", "", planBuilder.fragmentIdCol("vectorsDocId")),
        planBuilder.on(planBuilder.fragmentIdCol("fragmentId"), planBuilder.fragmentIdCol("vectorsDocId"))
      )
      .bind(planBuilder.as(planBuilder.col("similarity"), planBuilder.vec.cosineSimilarity(
        planBuilder.vec.vector(planBuilder.col("embedding")),
        planBuilder.vec.vector(chatQuestionEmbedding)
      )))
      .orderBy(planBuilder.desc(planBuilder.col("similarity")))
      .select("uri", "text")
      .limit(10);
  }

  async _getRelevantDocuments(
    chatQuestion, _callbacks,
  ) {
    const words = this.#buildWordsArray(chatQuestion);
    const chatQuestionEmbedding = await this.#generateEmbedding(chatQuestion);

    const opticQuery = this.#buildOpticPlan(words, chatQuestionEmbedding);
    const opticQueryResults = await this.marklogicClient.documents.query(opticQuery).result((results) => {
      return results;
    });

    const documents = [];
    opticQueryResults.forEach((result) => {
      console.log(result["uri"])
      documents.push(
        new Document({
          pageContent: result["content"]["transcript"],
          metadata: {"uri": result["uri"]}
        })
      )
    });
    return documents;
  }
}
