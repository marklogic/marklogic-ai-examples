from typing import List
from langchain_core.documents import Document
from langchain_core.retrievers import BaseRetriever
from marklogic import Client

"""
Modeled after
https://github.com/langchain-ai/langchain/blob/master/libs/langchain/langchain/retrievers/elastic_search_bm25.py ,
which uses a `create` method instead of __init__.
"""


class WordQueryRetriever(BaseRetriever):
    client: Client
    max_results: int = 10
    collections: List[str] = []
    drop_words: List[str] = [
        "did",
        "the",
        "about",
        "a",
        "an",
        "is",
        "are",
        "what",
        "say",
        "do",
        "was",
        "that",
        "tell"
    ]

    @classmethod
    def create(cls, client: Client):
        return cls(client=client)

    def _get_relevant_documents(self, query: str) -> List[Document]:
        words = []
        for word in query.split():
            if word.lower() not in self.drop_words and len(word) > 2:
                words.append(word.lower().replace("?", ""))

        word_query = "<word-query xmlns='http://marklogic.com/cts'>"
        for word in words:
            word_query = f"{word_query}<text>{word}</text>"
        word_query = f"{word_query}</word-query>"

        print(f"Searching with query: {word_query}")
        results = self.client.documents.search(
            query=word_query,
            page_length=self.max_results,
            collections=self.collections,
        )
        print(f"Count of matching MarkLogic documents: {len(results)}")
        return map(lambda doc: Document(page_content=doc.content["transcript"]), results)
