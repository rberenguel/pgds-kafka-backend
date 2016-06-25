name := "minteressa.producer"

version := "1.0"

scalaVersion := "2.11.8"

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

libraryDependencies ++= Seq(
  "org.apache.kafka"           % "kafka_2.11"           % "0.10.0.0" exclude("javax.jms", "jms"),
  "org.slf4j"                  % "slf4j-api"            % "1.7.10",
  "org.slf4j"                  % "slf4j-api"            % "1.7.10",
  "org.twitter4j" % "twitter4j-stream" % "4.0.4"
)
