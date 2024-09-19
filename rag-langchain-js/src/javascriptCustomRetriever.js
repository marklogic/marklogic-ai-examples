import { BaseRetriever } from "@langchain/core/retrievers";
import { Document } from "@langchain/core/documents";

export class JavaScriptCustomRetriever extends BaseRetriever {
  lc_namespace = ["langchain", "retrievers"];

  constructor(fields) {
  super(fields);
}

async _getRelevantDocuments(query) {
  return [
    new Document({
      pageContent: `Some document pertaining to ${query}`,
      metadata: {},
    }),
    new Document({
      pageContent: `Some other document pertaining to ${query}`,
      metadata: {},
    }),
  ];
}
}
