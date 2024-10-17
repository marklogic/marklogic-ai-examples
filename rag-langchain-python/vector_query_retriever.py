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
    null,
    {{
      'scoreMethod': 'score-bm25',
      'bm25LengthWeight': 0.5
    }}
  )
  .limit(100)
  .bind(op.as('transcript', op.xpath('doc', '/transcript')))
  .joinInner(
    op.fromView('example','events', '', op.fragmentIdCol('vectorsDocId')),
    op.on(
      op.fragmentIdCol('fragmentId'),
      op.fragmentIdCol('vectorsDocId')
    )
  )
  .bind(op.as('cosineSim',
    op.vec.cosineSimilarity(
        op.vec.vector(op.col('embedding')),
        op.vec.vector(vec.vector({}))
    )
  ))
  .bind(op.as('hybridScore',
    op.vec.vectorScore(op.col('score'), op.col('cosineSim'), 0.1)
  ))
  .select(['uri', 'transcript', 'hybridScore'])
  .orderBy(op.desc(op.col('hybridScore')))
  .limit(10)
  .result()
        """.format(
            words,
            query_embedding
        )

    def _get_relevant_documents(self, query: str) -> List[Document]:
        query_embedding = self.embedding_generator.embed_query(query)
        eval_script = self._build_eval_script(query, query_embedding)
        optic_rows = self.client.eval(javascript=eval_script)
        print(optic_rows[1].keys())

        print(f"Count of MarkLogic chunks sent to the LLM: {len(optic_rows)}")
        for optic_row in optic_rows:
            print(f"URI: {optic_row['uri']}")
        return map(lambda optic_row: Document(page_content=optic_row["transcript"]), optic_rows)
