name := "spark_cassandra"
version := "1.0"
scalaVersion := "2.11.8"

javacOptions ++= Seq("-source", "1.8", "-target", "1.87")

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.0.3-SNAPSHOT",
  "org.apache.spark" %% "spark-sql" % "2.0.3-SNAPSHOT",
  "org.apache.spark" %% "spark-mllib" % "2.0.3-SNAPSHOT" ,
  "org.apache.spark" %% "spark-streaming" % "2.0.3-SNAPSHOT",
  "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.1-6-g07ec0a5",
  "com.codahale.metrics" % "metrics-core" % "3.0.2",
  "com.codahale.metrics" % "metrics-json" % "3.0.2",
  "org.json4s" % "json4s-ast_2.11" % "3.2.10",
  "org.eclipse.jetty" % "jetty-server" % "9.4.0.RC1",
  "joda-time" % "joda-time" % "2.9.3",
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.6.5",
  "com.fasterxml.jackson.module" % "jackson-module-jaxb-annotations" % "2.6.5",
  "com.fasterxml.jackson.module" % "jackson-module-paranamer" % "2.6.5",
  "com.thoughtworks.paranamer" % "paranamer" % "2.6.1"

 
)
