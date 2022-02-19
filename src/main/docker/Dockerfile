# Imagen de Java para compilar la aplicación
FROM adoptopenjdk/openjdk11:latest

RUN ls -la

# Copiar ejecutable .jar
COPY **/groupeo**.jar .

# Comando para arrancar aplicación
CMD java -Dserver.port=$PORT $JAVA_OPTS -jar /software/groupeo-*.jar
