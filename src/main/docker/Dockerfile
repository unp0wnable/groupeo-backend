# Imagen de Java para compilar la aplicación
FROM adoptopenjdk/openjdk11:latest

# Crear directorio con el código a desplegar
RUN mkdir -p /deploy
RUN ls -la

# Copiar ficheros al contenedor
COPY . /deploy


WORKDIR /deploy
RUN ls -la

# Comando para arrancar aplicación
CMD java -Dserver.port=$PORT $JAVA_OPTS -jar groupeo-*.jar
