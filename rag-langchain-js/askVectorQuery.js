import { config } from 'dotenv';
import { AzureChatOpenAI } from "@langchain/openai";

import {StringOutputParser} from "@langchain/core/output_parsers";
import {createStuffDocumentsChain} from "langchain/chains/combine_documents";
import * as hub from "langchain/hub";
import {createDatabaseClient} from "marklogic";
import {VectorQueryRetriever} from "./vectorQueryRetriever.js";

config({path: "../.env"});
const marklogicClient = createDatabaseClient({
  host:     'localhost',
  port:     '8003',
  user:     'ai-examples-user',
  password: 'password',
  authType: 'DIGEST'
});
const llm = new AzureChatOpenAI({ });
const prompt = await hub.pull("rlm/rag-prompt");
const vectorQueryRetriever = new VectorQueryRetriever({marklogicClient});

let question = "What disturbances has Jane Doe caused?";
if (process.argv.length > 2) {
  question = process.argv[2];
}
console.log(`Question: ${question}`);

const ragChain = await createStuffDocumentsChain({
  llm,
  prompt,
  outputParser: new StringOutputParser()
});
const chainResponse = await ragChain.invoke({
  question: question,
  context: await vectorQueryRetriever.invoke(question),
});
console.log("\n\nChatbot VectorQuery chainResponse: \n" + JSON.stringify(chainResponse));
