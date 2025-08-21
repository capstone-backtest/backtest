# Docker 사용 가이드

## 1. 개요

이 문서는 Backtesting API 서버의 Docker 설정을 설명합니다. 백엔드 서비스는 Dockerfile을 통해 컨테이너 이미지로 빌드되며, 프로젝트 루트의 `docker-compose.yml` 파일에 의해 다른 서비스들과 함께 관리됩니다.

## 2. Dockerfile

`backend/Dockerfile`은 Python 3.11을 기반으로 FastAPI 애플리케이션을 실행하기 위한 환경을 구성합니다.

**주요 단계:**

1.  **Base Image**: `python:3.11-slim`을 기반 이미지로 사용합니다.
2.  **Working Directory**: 컨테이너 내 작업 디렉터리를 `/app`으로 설정합니다.
3.  **Install Dependencies**: `requirements.txt`에 명시된 의존성을 설치합니다.
4.  **Copy Code**: 프로젝트 코드를 컨테이너로 복사합니다.
5.  **Expose Port**: 애플리케이션이 실행되는 `8000` 포트를 노출합니다.
6.  **Run Application**: `uvicorn`을 사용하여 FastAPI 애플리케이션을 실행합니다.

```dockerfile
# 공식 Python 런타임을 부모 이미지로 사용
FROM python:3.11-slim

# 컨테이너 내 작업 디렉터리 설정
WORKDIR /app

# requirements.txt 파일을 컨테이너의 /app에 복사
COPY ./requirements.txt /app/requirements.txt

# requirements.txt에 명시된 패키지 설치
RUN pip install --no-cache-dir -r requirements.txt

# 나머지 애플리케이션 코드를 컨테이너의 /app에 복사
COPY . /app

# 8000 포트를 컨테이너 외부로 노출
EXPOSE 8000

# 애플리케이션 실행
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

## 3. Docker Compose

프로젝트 루트의 `docker-compose.yml` 파일에서 `backend` 서비스가 어떻게 정의되어 있는지 확인할 수 있습니다. 이 설정은 전체 애플리케이션의 일부로 백엔드를 실행하는 데 사용됩니다.

```yaml
version: '3.8'

services:
  backend:
    build: ./backend
    ports:
      - "8000:8000"
    volumes:
      - ./backend:/app
    env_file:
      - ./backend/.env
    depends_on:
      - mysql
  # ... 다른 서비스들
```

*   **`build: ./backend`**: `backend` 디렉터리의 `Dockerfile`을 사용하여 이미지를 빌드합니다.
*   **`ports`**: 호스트의 `8000` 포트를 컨테이너의 `8000` 포트와 매핑합니다.
*   **`volumes`**: 로컬 `backend` 디렉터리를 컨테이너의 `/app` 디렉터리와 마운트하여 코드 변경 사항이 즉시 반영되도록 합니다. (개발 환경에 유용)
*   **`env_file`**: `backend/.env` 파일에 정의된 환경 변수를 컨테이너에 주입합니다.
*   **`depends_on`**: `mysql` 서비스가 시작된 후에 `backend` 서비스가 시작되도록 의존성을 설정합니다.

전체 애플리케이션 실행에 대한 자세한 내용은 프로젝트 루트의 [README.md](../README.md)를 참고하세요.