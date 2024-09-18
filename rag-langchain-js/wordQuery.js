import { config } from 'dotenv';
import { AzureChatOpenAI } from "@langchain/openai";

config();

const model = new AzureChatOpenAI({ });

const prompt = ['Tell me a funny joke'];
const result = await model.invoke(prompt);

console.log(result);
