FROM openjdk:8-jre-alpine

RUN apk update
RUN apk add supervisor

RUN mkdir /kys
WORKDIR /kys
RUN touch mars.conf

COPY ./target/scala-2.12/mars.jar mars.jar

ADD mars.sv.conf /etc/supervisor/conf.d/

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/mars.sv.conf"]