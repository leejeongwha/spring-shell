FROM ubuntu:22.04 AS builder

# 환경변수 설정
#ENV LANG=C.UTF-8
#ENV JAVA_HOME=/opt/graalvm-community-java21
#ENV PATH="${JAVA_HOME}/bin:${PATH}"

# GraalVM Native Image 설치
#RUN chmod +x /usr/local/bin/gu
#RUN gu install native-image

# 프로젝트 복사 및 빌드
WORKDIR /app
COPY . /app

# Gradle 권한 부여
RUN chmod +x gradlew

# 필수 라이브러리 설치
RUN apt-get update && apt-get install -y \
    build-essential \
    zip \
    unzip \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Native 이미지 컴파일 (Spring Boot 3.x 기준)
#RUN ./gradlew nativeCompile --stacktrace

# 네이티브 이미지 빌드 실행
#CMD ["./gradlew", "nativeCompile"]

#docker run -it 39cf9afedbf9 /bin/bash
