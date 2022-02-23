# Imagen de Java para compilar la aplicación
FROM adoptopenjdk/openjdk11:latest

# Crear directorio con el código a desplegar
RUN mkdir -p /deploy

# Copiar ficheros al contenedor
COPY . /deploy

# Acceder al directorio que contiene el código
WORKDIR /deploy/target

# Comando para arrancar aplicación
CMD java -Dserver.port=$PORT $JAVA_OPTS -jar groupeo-*.jar -XX:+UseContainerSupport
