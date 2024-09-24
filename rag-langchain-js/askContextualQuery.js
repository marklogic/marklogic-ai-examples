import { config } from 'dotenv';
import { AzureChatOpenAI } from "@langchain/openai";

import {StringOutputParser} from "@langchain/core/output_parsers";
import {createStuffDocumentsChain} from "langchain/chains/combine_documents";
import * as hub from "langchain/hub";
import {createDatabaseClient} from "marklogic";
import {ContextualQueryRetriever} from "./contextualQueryRetriever.js";

config({path: "../.env"});
const marklogicClient = createDatabaseClient({
  host:     'localhost',
  port:     '8003',
  user:     'ai-examples-user',
  password: 'password',
  authType: 'DIGEST'
});
const contextualQueryRetriever = new ContextualQueryRetriever({marklogicClient});

const llm = new AzureChatOpenAI({ });
const prompt = await hub.pull("rlm/rag-prompt");

let question = "What disturbances has Jane Doe caused?";
if (process.argv.length > 2) {
  question = process.argv[2];
}
console.log(`Question: ${question}`);

const contextualQuery = {
  "query": {
    "queries": [
      {
        "value-query": {
          "json-property": "type",
          "text": "public intoxication"
        }
      }
    ]
  }
}

const ragChain = await createStuffDocumentsChain({
  llm,
  prompt,
  outputParser: new StringOutputParser()
});
const chainResponse = await ragChain.invoke({
  question: question,
  context: await contextualQueryRetriever.invoke({"question": question, "contextualQuery": contextualQuery}),
});
console.log("\n\nChatbot VectorQuery chainResponse: \n" + JSON.stringify(chainResponse));
