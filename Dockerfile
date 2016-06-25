FROM williamyeh/sbt

MAINTAINER Rberenguel

RUN mkdir -p /opt/twitter/src/

COPY src /opt/twitter/src/
COPY build.sbt /opt/twitter/

WORKDIR /opt/twitter/

RUN cat /etc/hosts > /dev/stderr

CMD ["run"]