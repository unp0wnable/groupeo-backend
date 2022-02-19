# Imagen de Java para compilar la aplicación
FROM adoptopenjdk/openjdk11:latest
# Directorio en el que copiar el ejecutable
RUN mkdir -p /software
# Copiar ejecutable .jar
ADD target/groupeo-*.jar /software/groupeo.jar
# Comando para arrancar aplicación
CMD java -Dserver.port=$PORT $JAVA_OPTS -jar /software/groupeo-*.jar
