FROM ubuntu:22.04 AS builder

# 환경변수 설정

# 프로젝트 복사 및 빌드
WORKDIR /app
COPY . /app

# Gradle 권한 부여
RUN chmod +x gradlew

# 필수 라이브러리 설치
RUN apt-get update && apt-get install -y \
    build-essential zip unzip curl zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*

# sdkman 및 graalvm java 설치
RUN curl -s "https://get.sdkman.io" | bash \
    source "/root/.sdkman/bin/sdkman-init.sh" \
    sdk install java 21.0.2-graalce \
    sdk use java 21.0.2-graalce


