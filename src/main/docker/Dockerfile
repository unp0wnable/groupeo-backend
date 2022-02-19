# Descargar imagen de Java
FROM openjdk:11-jdk
MAINTAINER Unp0wnable

# Construir proyecto Maven (mvn build)
RUN mvn clean install -Dprod
# Comprobar si todo se ha copiado correctamente
RUN ls -l
# Copiar el ejecutable .jar al contenedor actual
COPY "/target/groupeo.jar" "groupeo.jar"
# Ejecutar app
CMD ["sh","-c", "java $JAVA_OPTS -Dserver.port=$PORT -jar /groupeo.jar"]
