-- pgvector 확장을 설치해 벡터 타입과 벡터 연산 기능을 추가
-- CREATE EXTENSION IF NOT EXISTS vector;

-- 키-값 저장소 기능을 제공하는 hstore 확장을 추가
-- CREATE EXTENSION IF NOT EXISTS hstore;

-- UUID 생성 함수를 제공하는 uuid-ossp 확장을 추가
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- OpenAI
-- CREATE TABLE IF NOT EXISTS public.spring_ai_chat_memory_vector_store (
--   id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
--   content text,
--   metadata json,
--   embedding vector(3072)
-- );

CREATE INDEX IF NOT EXISTS spring_ai_chat_memory_vector_store_idx
  ON public.spring_ai_chat_memory_vector_store 
  USING ivfflat ((embedding::halfvec(3072)) halfvec_cosine_ops) 
  WITH (lists = 100);

-- VertexAI Gemini
-- CREATE TABLE IF NOT EXISTS public.spring_ai_chat_memory_vector_store (
--   id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
--   content text,
--   metadata json,
--   embedding vector(768)
-- );
-- CREATE INDEX spring_ai_chat_memory_vector_store_idx
--   ON public.spring_ai_chat_memory_vector_store
--   USING hnsw (embedding vector_cosine_ops);