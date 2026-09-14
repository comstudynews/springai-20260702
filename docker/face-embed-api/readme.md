## 이미지 로드 방법1: face-embed-api.jar로 image 로딩
image> docker load -i face-embed-api.tar

## 이미지 로드 방법2: 직접 docker image 로 빌드(시간 오래 걸림)
context> docker build -t face-embed-api:latest .

## 컨테이너 실행
docker run -d --name face-embed-api -p 50001:8000 face-embed-api:latest

## 파일이 수정되어 이미지 내용이 변경되었을 때, 이미지 빌드 후 TAR 파일로 다시 저장
context> docker build -t face-embed-api:latest .
image> face-embed-api.tar 삭제
image> docker save -o face-embed-api.tar face-embed-api:latest