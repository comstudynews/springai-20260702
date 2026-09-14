# 베이스 이미지로 Python 3.11 슬림 사용
FROM python:3.11-slim

# 시스템 패키지 설치 (OpenCV 의존성)
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      libgl1 libglib2.0-0 && \
    rm -rf /var/lib/apt/lists/*

# 작업 디렉터리 설정
WORKDIR /app

# Python 의존성 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 소스 복사
COPY . .

# API 포트 노출
EXPOSE 8000

# 컨테이너 시작 시 uvicorn 실행
CMD ["uvicorn", "face_embed_api:app", "--host", "0.0.0.0", "--port", "8000"]