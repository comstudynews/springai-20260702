## 실행 중인 컨테이너 중지
# docker stop pgvector

## 컨테이너 제거
# docker rm pgvector

## 기존 볼륨 제거
# docker volume rm pgdata

## 컨테이너 생성
docker run `
--name pgvector `
-d `
-p 5432:5432 `
-e POSTGRES_USER=postgres `
-e POSTGRES_PASSWORD=postgres `
-e TZ=Asia/Seoul `
-v pgvector-volume:/var/lib/postgresql/data `
pgvector/pgvector:pg17 `
postgres -c max_connections=500