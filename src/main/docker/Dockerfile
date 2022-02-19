# Imagen de Java para compilar la aplicación
FROM adoptopenjdk/openjdk11:latest


# Directorio en el que almacenar el código a desplegar
WORKDIR /deploy
RUN ls -la

# Copiar ejecutable .jar
COPY ./target/groupeo-*.jar .

# Comando para arrancar aplicación
CMD java -Dserver.port=$PORT $JAVA_OPTS -jar groupeo-*.jar
