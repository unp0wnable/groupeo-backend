# Imagen de Java para compilar la aplicación
FROM adoptopenjdk/openjdk11:latest

RUN ls -la

# Directorio en el que almacenar el código a desplegar
WORKDIR /deploy

# Copiar ejecutable .jar
COPY **/groupeo**.jar .

# Comando para arrancar aplicación
CMD java -Dserver.port=$PORT $JAVA_OPTS -jar /software/groupeo-*.jar
