-- pgvector 확장을 설치해 벡터 타입과 벡터 연산 기능을 추가
CREATE EXTENSION IF NOT EXISTS vector;

-- 키-값 저장소 기능을 제공하는 hstore 확장을 추가
CREATE EXTENSION IF NOT EXISTS hstore;

-- UUID 생성 함수를 제공하는 uuid-ossp 확장을 추가
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS public.face_vector_store (
  id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
  content text,
  metadata json,
  embedding vector(512)
);