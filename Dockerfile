FROM mozilla/sbt

WORKDIR /Producer

ADD . /Producer

CMD sbt run