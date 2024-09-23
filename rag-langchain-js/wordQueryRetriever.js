import { BaseRetriever } from "@langchain/core/retrievers";
import { Document } from "@langchain/core/documents";
import pkg from 'marklogic';
const { ctsQueryBuilder, queryBuilder } = pkg;

export class WordQueryRetriever extends BaseRetriever {
  lc_namespace = ["langchain", "retrievers"];
  marklogicClient;

  constructor(fields) {
    super(fields);
    this.marklogicClient = fields.marklogicClient;
  }

  #buildWordsArray(chat_question) {
    const words = [];
    chat_question.split(" ").forEach(word => {
      if (word.length > 2) words.push(word.toLowerCase().replace("?", ""))
    })
    return words;
  }

  async _getRelevantDocuments(
    chat_question, _callbacks,
  ) {
    const words = this.#buildWordsArray(chat_question);
    const wordQuery = queryBuilder.where(ctsQueryBuilder.cts.wordQuery(words));
    const results = await this.marklogicClient.documents.query(wordQuery).result((queryResults) => {
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
