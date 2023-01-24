FROM openjdk:19-slim AS build
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app/classes

FROM openjdk:19-slim
VOLUME /tmp
ENV JAVA_OPTS=""
ARG DEPENDENCY=target/dependency
COPY --from=build /app/lib /app/lib
COPY --from=build /app/META-INF /app/META-INF
COPY --from=build /app/classes /app

RUN useradd -u 3000 app
USER app
WORKDIR /

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -cp app:app/lib/* kz.shaykemelov.demo.DemoApplication"]
