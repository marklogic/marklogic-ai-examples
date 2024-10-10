from typing import List
from langchain_core.documents import Document
from langchain_core.retrievers import BaseRetriever
from langchain_openai import AzureOpenAIEmbeddings
from marklogic import Client


class VectorQueryRetriever(BaseRetriever):
    client: Client
    embedding_generator: AzureOpenAIEmbeddings

    @classmethod
    def create(
        cls,
        client: Client,
        embedding_generator: AzureOpenAIEmbeddings
    ):
        return cls(
            client=client,
            embedding_generator=embedding_generator
        )

    def _build_eval_script(self, query, query_embedding):
        words = []
        for word in query.split():
            if len(word) > 2:
                words.append(word.lower().replace("?", ""))

        return """
const op = require('/MarkLogic/optic');

op.fromSearchDocs(
    cts.andQuery([cts.wordQuery({}), cts.collectionQuery('events')]),
    null, {{'scoreMethod': 'score-bm25'}}
  )
  .limit(100)
  .joinInner(
    op.fromView('example','events', '', op.fragmentIdCol('vectorsDocId')),
    op.on(
      op.fragmentIdCol('fragmentId'),
      op.fragmentIdCol('vectorsDocId')
    )
  )
  .bind(op.as('similarity', op.vec.cosineSimilarity(
    op.vec.vector(op.col('embedding')),
    op.vec.vector(vec.vector({}))
  )))
  .orderBy(op.desc(op.col('similarity')))
  .limit(10)
  .result()
        """.format(
            words,
            query_embedding
        )

    def _get_relevant_documents(self, query: str) -> List[Document]:
        query_embedding = self.embedding_generator.embed_query(query)
        eval_script = self._build_eval_script(query, query_embedding)
        results = self.client.eval(javascript=eval_script)

        print(f"Count of MarkLogic documents sent to the LLM: {len(results)}")
        for result in results:
            print(f"URI: {result['uri']}")
        return map(lambda doc: Document(page_content=doc["text"]), results)
