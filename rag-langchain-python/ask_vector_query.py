# Based on example at
# https://python.langchain.com/docs/use_cases/question_answering/quickstart .

import os
import sys
from dotenv import load_dotenv
from langchain import hub
from langchain_openai import AzureChatOpenAI, AzureOpenAIEmbeddings
from langchain.schema import StrOutputParser
from langchain.schema.runnable import RunnablePassthrough
from marklogic import Client
from vector_query_retriever import VectorQueryRetriever


def format_docs(docs):
    return "\n\n".join(doc.page_content for doc in docs)


load_dotenv("../.env")

retriever = VectorQueryRetriever.create(
    Client("http://localhost:8003", digest=("ai-examples-user", "password")),
    embedding_generator=AzureOpenAIEmbeddings(
        azure_deployment=os.environ["AZURE_EMBEDDING_DEPLOYMENT_NAME"]
    )
)

question = sys.argv[1]

fake_langsmith_api_key_to_avoid_warning = "not required for this example"
prompt = hub.pull("rlm/rag-prompt", api_key=fake_langsmith_api_key_to_avoid_warning)

# Note that the Azure OpenAI API key, the Azure OpenAI Endpoint, and the OpenAI API
# Version, are all read from the environment automatically.
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
