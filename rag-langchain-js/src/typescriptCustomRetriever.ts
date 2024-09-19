import {
  BaseRetriever,
  type BaseRetrieverInput,
} from "@langchain/core/retrievers";
import { Document } from "@langchain/core/documents";

export interface CustomRetrieverInput extends BaseRetrieverInput {}

export class TypeScriptCustomRetriever extends BaseRetriever {
  lc_namespace = ["langchain", "retrievers"];

  constructor(fields?: CustomRetrieverInput) {
  super(fields);
}

async _getRelevantDocuments(
  query: string,
): Promise<Document[]> {
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
