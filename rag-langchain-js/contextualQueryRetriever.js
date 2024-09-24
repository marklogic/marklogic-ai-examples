import { BaseRetriever } from "@langchain/core/retrievers";
import { Document } from "@langchain/core/documents";

// This example is based on the LangChain guide at https://js.langchain.com/v0.1/docs/modules/data_connection/retrievers/custom/
// and the example at https://github.com/langchain-ai/langchainjs/blob/main/examples/src/retrievers/custom.ts
export class ContextualQueryRetriever extends BaseRetriever {
  lc_namespace = ["langchain", "retrievers"];
  marklogicClient;

  constructor(fields) {
    super(fields);
    this.marklogicClient = fields.marklogicClient;
  }

  #buildWordsArray(chatQuestion) {
    const words = [];
    chatQuestion.split(" ").forEach(word => {
      if (word.length > 2) words.push(word.toLowerCase().replace("?", ""))
    })
    return words;
  }

  async _getRelevantDocuments(
    questionContext, _callbacks,
  ) {
    const words = this.#buildWordsArray(questionContext.question);
    const wordQuery = {"term-query": {"text": words}};
    const contextualQuery = questionContext.contextualQuery;
    contextualQuery["query"]["queries"].push(wordQuery)
    console.log(JSON.stringify(contextualQuery))

    const results = await this.marklogicClient.documents.query(contextualQuery).result((queryResults) => {
      return queryResults;
    });

    const documents = [];
    results.forEach((result) => {
      console.log(result["uri"])
      documents.push(
        new Document({
          pageContent: result['content']['transcript'],
          metadata: {"uri": result["uri"]}
        })
      )
    });
    return documents;
  }
}
