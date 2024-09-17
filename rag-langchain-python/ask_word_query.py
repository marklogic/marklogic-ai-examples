# Based on example at
# https://python.langchain.com/docs/use_cases/question_answering/quickstart .

import os
import sys
from dotenv import load_dotenv
from langchain import hub
from langchain_openai import AzureChatOpenAI
from langchain.schema import StrOutputParser
from langchain.schema.runnable import RunnablePassthrough
from marklogic import Client
from word_query_retriever import WordQueryRetriever


def format_docs(docs):
    return "\n\n".join(doc.page_content for doc in docs)


question = sys.argv[1]

retriever = WordQueryRetriever.create(
    Client("http://localhost:8003", digest=("langchain-user", "password"))
)
retriever.collections = ["event"]
retriever.max_results = int(sys.argv[2]) if len(sys.argv) > 2 else 10

load_dotenv()

prompt = hub.pull("rlm/rag-prompt")
# Note that the Azure OpenAI API key, the Azure OpenAI Endpoint, and the OpenAI API
# Version are all read from the environment automatically.
llm = AzureChatOpenAI(
    model_name=os.getenv("AZURE_LLM_DEPLOYMENT_NAME"),
    azure_deployment=os.getenv("AZURE_LLM_DEPLOYMENT_NAME"),
    temperature=0,
    max_tokens=None,
    timeout=None,
)

rag_chain = (
        {"context": retriever | format_docs, "question": RunnablePassthrough()}
        | prompt
        | llm
        | StrOutputParser()
)

print(rag_chain.invoke(question))
