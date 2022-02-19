# Construción multifase del contenedor de Docker

# 1) Compilar proyecto mediante Maven
# Descargar imagen de Maven
FROM maven:3-jdk-11
# Copiar contenido a directorio especial
ADD . /software
WORKDIR /software
# Comprobar si todo se ha copiado correctamente
RUN ls -l
# Construir proyecto Maven (mvn build)
RUN mvn clean install -Dprod


# 2) Ejecutar el paquete construido en el paso 1)
# Descargar imagen de Java
FROM openjdk:11-jdk
MAINTAINER Unp0wnable
VOLUME /tmp
# Copiar el ejecutable .jar al contenedor actual
COPY --from=0 "/software/target/groupeo-*.jar" "groupeo-app.jar"
# Ejecutar app
CMD ["sh","-c", "java $JAVA_OPTS -Dserver.port=$PORT -jar /groupeo-app.jar"]
