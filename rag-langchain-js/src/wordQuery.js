import { config } from 'dotenv';
import { AzureChatOpenAI } from "@langchain/openai";

import {TypeScriptCustomRetriever} from "./typescriptCustomRetriever.js";
import {JavaScriptCustomRetriever} from "./javascriptCustomRetriever.js";
config();

const model = new AzureChatOpenAI({ });

const prompt = ['Tell me a funny joke'];


const typeScriptRetriever = new TypeScriptCustomRetriever({});
const typeScriptRagDocs = await typeScriptRetriever.invoke("LangChain docs");
console.log("TypeScript RAG docs: \n" + JSON.stringify(typeScriptRagDocs));

const javaScriptRetriever = new JavaScriptCustomRetriever({});
const javaScriptRagDocs = await javaScriptRetriever.invoke("LangChain docs");
console.log("\n\nJavaScript RAG docs: \n" + JSON.stringify(javaScriptRagDocs));

const chatResponse = await model.invoke(prompt);
console.log("\n\nChatbot response: \n" + JSON.stringify(chatResponse));
