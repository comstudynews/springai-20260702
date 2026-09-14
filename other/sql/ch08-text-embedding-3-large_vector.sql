-- pgvector 확장을 설치해 벡터 타입과 벡터 연산 기능을 추가
-- CREATE EXTENSION IF NOT EXISTS vector;

-- 키-값 저장소 기능을 제공하는 hstore 확장을 추가
-- CREATE EXTENSION IF NOT EXISTS hstore;

-- UUID 생성 함수를 제공하는 uuid-ossp 확장을 추가
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- text-embedding-3-larg 임베딩 모델을 사용할 경우 3072 차원 저장
-- CREATE TABLE IF NOT EXISTS public.vector_store (
--    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
--    content text,
--    metadata json,
--    embedding vector(3072)
-- );

-- ORDER BY에 코사인 거리 연산자를 사용할 경우(<=>) 사용될 인덱스
CREATE INDEX IF NOT EXISTS vector_store_embedding_ivfflat_cos 
  ON vector_store 
  USING ivfflat ((embedding::halfvec(3072)) halfvec_cosine_ops) 
  WITH (lists = 100);
-- Ex) SELECT id, content
-- FROM vector_store
-- ORDER BY (embedding::halfvec(3072)) <=> '[벡터]'::halfvec(3072)
-- LIMIT 5;

-- ORDER BY에 L2 거리 연산자를 사용할 경우(<->)
-- CREATE INDEX IF NOT EXISTS vector_store_embedding_ivfflat_l2 
--   ON vector_store 
--   USING ivfflat ((embedding::halfvec(3072)) halfvec_l2_ops)  
--   WITH (lists = 100);
-- Ex) SELECT id, content
-- FROM vector_store
-- ORDER BY (embedding::halfvec(3072)) <-> '[벡터]'::halfvec(3072)
-- LIMIT 5;