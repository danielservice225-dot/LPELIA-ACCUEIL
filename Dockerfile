FROM python:3.12-slim

ENV PYTHONUNBUFFERED=1 PIP_NO_CACHE_DIR=1 DATA_DIR=/app/data
WORKDIR /app

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY app.py knowledge.seed.json ./
COPY static ./static
RUN mkdir -p /app/data

EXPOSE 8000
CMD ["sh", "-c", "uvicorn app:app --host 0.0.0.0 --port ${PORT:-8000} --proxy-headers --forwarded-allow-ips='*'"]
