FROM ubuntu:latest
LABEL authors="ijj28"

ENTRYPOINT ["top", "-b"]

# OpenJDK 17을 기반으로 사용
FROM openjdk:17

# 작업 디렉터리 설정
WORKDIR /app

# 현재 프로젝트의 JAR 파일을 컨테이너로 복사
COPY build/libs/*.jar app.jar

# 컨테이너에서 실행할 명령어
CMD ["java", "-jar", "app.jar"]
