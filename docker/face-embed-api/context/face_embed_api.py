from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import StreamingResponse, JSONResponse
from ultralytics import YOLO
import uvicorn
import cv2
import numpy as np
import io
import onnxruntime as ort

# FastAPI 애플리케이션 인스턴스 생성
app = FastAPI()

# -----------------------------------------------------------------------------
# 1) YOLO Face 모델 로드
#    - 얼굴 검출을 위해 Ultralytics YOLO 모델을 사용
#    - 모델 파일 다운로드: https://github.com/akanametov/yolo-face?tab=readme-ov-file
#    - yolov8n-face.pt: 작은 경량 모델
#    - pt: PyTorch에서 학습된 모델 파일 확장자
# -----------------------------------------------------------------------------
model = YOLO("yolov8n-face.pt")

# -----------------------------------------------------------------------------
# 2) ONNX 임베딩 세션 로드 (ArcFace 예시)
#    - ONNX 파일: 학습된 신경망 구조와 가중치를 담고 있는 파일
#    - ONNX 파일: 딥러닝 모델을 저장하고 교환하기 위한 표준화된 파일 형식, PyTorch, TensorFlow 등 다양한 프레임워크에서 내보내거나 불러올 수 있음
#    - 임베딩 세션 로드: 임베딩 모델(.onnx) 파일을 ONNX Runtime 같은 엔진으로 불러와서, 세션(Session)이라는 실행 컨텍스트를 만든다는 뜻
#    - ArcFace 모델 파일 다운로드: https://github.com/deepinsight/insightface/tree/master/model_zoo
#    - onnxruntime.InferenceSession: ONNX 모델을 실행하는 세션 생성
# -----------------------------------------------------------------------------
onnx_session = ort.InferenceSession("arcface_r50.onnx")

# onnx_input_name: ONNX 모델 입력 텐서 이름 조회
onnx_input_name = onnx_session.get_inputs()[0].name

# -----------------------------------------------------------------------------
def crop_face_bytes(image_bytes: bytes) -> bytes:
    """
    바이트 배열로 전달된 원본 이미지에서 첫 번째 얼굴만 검출·크롭하여
    JPEG 인코딩 바이트로 반환하는 함수

    Args:
        image_bytes (bytes): 원본 이미지의 바이너리 데이터

    Returns:
        bytes: 얼굴 영역을 JPEG로 인코딩한 바이너리 데이터

    Raises:
        ValueError: 이미지 디코딩, 얼굴 미검출, 인코딩 실패 시 예외 발생
    """
    # 1) 바이트 배열을 OpenCV 이미지로 디코딩
    nparr = np.frombuffer(image_bytes, np.uint8)  # 바이트 배열 -> NumPy 배열 (0~255 값을 갖는 1차원, (n,) shape)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)   # NumPy 배열 -> OpenCV 이미지 (H, W, C) shape
    if img is None:
        raise ValueError("이미지 디코딩 실패")

    # 2) YOLO 모델로 얼굴 검출 (results.boxes: 검출된 바운딩 박스 리스트)
    results = model(img)[0] # 하나의 이미지만 입력했으므로 0 인텍스의 결과 얻기
    if len(results.boxes) == 0:
        raise ValueError("얼굴을 찾지 못했습니다")

    # 3) 첫 번째 검출된 얼굴 영역 좌표 추출 (x1, y1, x2, y2)
    x1, y1, x2, y2 = map(int, results.boxes.xyxy[0].tolist())

    # 4) 원본 이미지에서 얼굴 영역 크롭
    face = img[y1:y2, x1:x2]

    # 5) 크롭된 얼굴 이미지를 JPEG로 인코딩
    success, encoded = cv2.imencode(".jpg", face) #encoded: JPEG로 인코딩된 이미지 (NumPy 배열)
    if not success:
        raise ValueError("얼굴 인코딩 실패")

    # 6) JPEG 바이트 반환
    return encoded.tobytes() # NumPy 배열 -> 바이트 배열

# -----------------------------------------------------------------------------
def embedded_face_arcface(face_bytes: bytes) -> list[float]:
    """
    얼굴 크롭된 JPEG 바이트를 받아 ArcFace ONNX 모델로 임베딩 벡터를 반환하는 함수

    Args:
        face_bytes (bytes): 크롭 후 JPEG로 인코딩된 얼굴 이미지 바이트 데이터

    Returns:
        list[float]: 길이가 512인 임베딩 벡터

    Raises:
        ValueError: 디코딩, ONNX 실행, 벡터 정규화 실패 시 예외 발생
    """
    # 1) JPEG 바이트를 OpenCV 이미지로 디코딩
    nparr = np.frombuffer(face_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    if img is None:
        raise ValueError("얼굴 이미지 디코딩 실패")

    # 2) BGR -> RGB 변환 (ArcFace 모델은 RGB를 입력해야 함)
    rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

    # 3) 전처리: 크기 조정, 스케일링, 정규화
    #    - 모델 입력 크기: 112x112
    #    - 255.0으로 나누어서 [0, 1] 범위로 변환(스케일링), 직관적, 일반 분류 모델에서 흔히 사용
    #    - 평균 0.5, 표준편차 0.5로 정규화([-1, 1] 범위로 변환), 평균이 0 근처, 얼굴 인식/생성 모델에서 주로 사용
    #    - 모델 학습 시 사용한 정규화 방식에 따라 입력도 반드시 동일하게 맞춰야 성능이 제대로 나옴
    face = cv2.resize(rgb, (112, 112)).astype(np.float32) / 255.0 
    face = (face - 0.5) / 0.5 # face*2 - 1과 동일, 0.0->-1.0, 1.0->1.0
    #    - 축(axis) 순서 변경: (H, W, C) -> (1, C, H, W), ONNX 모델 입력 형태(batch, C, H, W)에 맞춤
    inp = np.transpose(face, (2, 0, 1))[None, ...]

    # 4) ONNX Runtime 세션 실행
    try:
        outputs = onnx_session.run(None, {onnx_input_name: inp}) # None: 모든 출력 텐서 반환
    except Exception as ex:
        raise ValueError(f"ONNX 런타임 오류: {ex}") from ex

    # 5) 결과 벡터 추출 및 L2 노름(벡터 길이)을 1로 맞춰 정규화함(L2 정규화)
    raw = outputs[0].flatten()
    norm = np.linalg.norm(raw)
    if norm == 0:
        raise ValueError("임베딩 벡터의 크기가 0입니다.")
    normalized = raw / norm

    # 6) Python 리스트로 변환하여 반환
    return normalized.tolist()


@app.post("/get-face-image")
async def get_face_image(file: UploadFile = File(...)):
    """
    멀티파트 요청 본문에서 'file' 파라미터로 전송된 
    원본 이미지를 받아 얼굴만 크롭한 JPEG 바이너리를 반환하는 엔드포인트

    - HTTP Method: POST
    - Path: /get-face-image/
    - Request: multipart/form-data, 'file' 파라미터로 이미지 파일 업로드
    - Response: image/jpeg 스트리밍
    """
    # 업로드된 파일의 바이트 읽기
    data = await file.read()

    try:
        # 얼굴 크롭 및 JPEG 바이트 생성
        face_jpeg = crop_face_bytes(data)
    except ValueError as e:
        msg = str(e)
        raise HTTPException(status_code=500, detail=msg)

    # StreamingResponse로 JPEG 바이트 스트리밍
    return StreamingResponse(io.BytesIO(face_jpeg), media_type="image/jpeg")


@app.post("/get-face-vector")
async def get_face_vector(file: UploadFile = File(...)):
    """
    멀티파트 요청 본문에서 'file' 파라미터로 전송된 원본 이미지를 받아
    1) 얼굴만 크롭 (crop_face_bytes)
    2) 얼굴 임베딩 벡터 생성 (embedded_face_arcface)
    3) JSON 형식으로 벡터 반환

    - HTTP Method: POST
    - Path: /get-face-vector/
    - Request: multipart/form-data, 'file' 파라미터로 이미지 파일 업로드
    - Response: application/json
    """
    # 업로드된 파일의 바이트 읽기
    data = await file.read()

    try:
        # 1) 얼굴 크롭된 JPEG 바이트 획득
        face_bytes = crop_face_bytes(data)
        # 2) ArcFace 모델로 임베딩 벡터 생성
        vector = embedded_face_arcface(face_bytes)
    except ValueError as e:
        msg = str(e)
        raise HTTPException(status_code=500, detail=msg)

    # JSONResponse로 벡터 반환
    return JSONResponse(content={"vector": vector})


if __name__ == "__main__":
    # uvicorn을 사용하여 서버 실행
    # 모든 네트워크 인터페이스에서 들어오는 요청을 수신
    uvicorn.run(app, host="0.0.0.0", port=8000)
