import { config } from 'dotenv';
import { AzureChatOpenAI } from "@langchain/openai";

import { WordQueryRetriever } from "./wordQueryRetriever.js";
import {StringOutputParser} from "@langchain/core/output_parsers";
import {createStuffDocumentsChain} from "langchain/chains/combine_documents";
import * as hub from "langchain/hub";
import {createDatabaseClient} from "marklogic";

config({path: "../.env"});
const marklogicClient = createDatabaseClient({
  host:     'localhost',
  port:     '8003',
  user:     'ai-examples-user',
  password: 'password',
  authType: 'DIGEST'
});
const wordQueryRetriever = new WordQueryRetriever({marklogicClient});

// We need to specify the environment variables since the Python and Java examples use different default variables.
const llm = new AzureChatOpenAI({
  azureOpenAIApiVersion: process.env.OPENAI_API_VERSION,
  azureOpenAIApiDeploymentName: process.env.AZURE_LLM_DEPLOYMENT_NAME
});
const prompt = await hub.pull("rlm/rag-prompt");

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
  context: await wordQueryRetriever.invoke(question),
});
console.log("\n\nChatbot WordQuery chainResponse: \n" + JSON.stringify(chainResponse));
