FROM hub.c.163.com/library/java:8-alpine

ADD *.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
