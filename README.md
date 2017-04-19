##
## The SparkSQL and Cassandra connection (using datastax) is demonstrated on Docker.
##

NOTICE1: This is based on 

	https://hub.docker.com/_/cassandra/
	https://gist.github.com/clakech/4a4568daba1ca108f03c
	https://coderwall.com/p/o9bkjg/setup-spark-with-cassandra-connector.
	
NOTICE2: Please refer to https://github.com/datastax/spark-cassandra-connector (datastax).

NOTICE3: This is also based on 

	https://docs.docker.com/engine/tutorials/dockervolumes 
	https://developers.google.com/protocol-buffers 
	http://findbugs.sourceforge.net
	http://spark.apache.org/docs/latest/submitting-applications.html

NOTICE4: Make sure that 2.0.3-SNAPSHOT (spark) is used instead of 2.0.3 (spark) for the sbt dependencies.

NOTICE5: 2 terminals are required (one server and one client)


Instructions are given below to run an example of sparkSQL-Cassandra.


[terminal 1] 

[1-1] open a terminal

[1-2] sudo docker run --name cassandra-server -d cassandra:2.2
	
	
	Unable to find image 'cassandra:2.2' locally
	2.2: Pulling from library/cassandra
	Status: Downloaded newer image for cassandra:2.2
	

[1-3] sudo docker ps -a
	
	
	PORTS                                         NAMES
	7000-7001/tcp, 7199/tcp, 9042/tcp, 9160/tcp   cassandra-server
	
[1-4] sudo docker inspect --format='{{ .NetworkSettings.IPAddress }}' cassandra-server
	
	172.17.0.2


	
[terminal 2]

[2-1] open another terminal

[2-2] git clone this-source-code-folder

[2-3] cd downloaded-source-code-folder

[2-4] sudo make BIND_DIR=.  shell

	wait ... wait ... wait ...  wait ... wait ... then a bash shell will be ready (root@2580de09e033).

[2-5] root@2580de09e033:/# cd /home/spark/


[2-6] root@2580de09e033:/home/spark# cqlsh cassandra

 
	Connected to Test Cluster at cassandra:9042.
	[cqlsh 5.0.1 | Cassandra 2.2.9 | CQL spec 3.3.1 | Native protocol v4]
	Use HELP for help.
	cqlsh> 

	
[2-7 make a table]


	cqlsh> CREATE KEYSPACE tkeyspace WITH replication = {'class':'SimpleStrategy','replication_factor':1};
	cqlsh> USE tkeyspace;
	cqlsh:tkeyspace> CREATE TABLE stable( key text PRIMARY KEY, value int);
	cqlsh:tkeyspace> INSERT INTO stable(key,value) VALUES('dog',1);
	cqlsh:tkeyspace> INSERT INTO stable(key,value) VALUES('cat',2);
	cqlsh:tkeyspace> INSERT INTO stable(key,value) VALUES('deer',3);
	cqlsh:tkeyspace> select * from stable;
	
	 key  | value
	------+-------
	  cat |     2
	  dog |     1
	 deer |     3
		
	cqlsh:tkeyspace> exit;


[2-8] root@3e3a5d228c9c:/home/spark# cd spark-2.0.3/
[2-9] root@3e3a5d228c9c:/home/spark/spark-2.0.3# sbt sbt-version


	Getting org.scala-sbt sbt 0.13.11  (this may take some time)...


[2-10] root@3e3a5d228c9c:/home/spark/spark-2.0.3# ./dev/change-scala-version.sh 2.11

[2-11] root@3e3a5d228c9c:/home/spark/spark-2.0.3# sbt -Dscala-2.11 -Pyarn -Phadoop-2.7 -Dhadoop.version=2.7.3 -Phive -Phive-thriftserver -Pmesos clean package

[2-12] root@3e3a5d228c9c:/home/spark/spark-2.0.3# sbt -mem 4096 -Dscala-2.11=true  publish-local 

[2-13] root@3e3a5d228c9c:/home/spark/spark-2.0.3# cd ..

[2-14] root@613538fbfd43:/home/spark# cd joda-time-2.9.3/

[2-15] root@613538fbfd43:/home/spark/joda-time-2.9.3# mvn clean install

[2-16] root@613538fbfd43:/home/spark/joda-time-2.9.3# cd ..

[2-17] root@3e3a5d228c9c:/home/spark# cd spark-cassandra-connector/

[2-18] root@3e3a5d228c9c:/home/spark/spark-cassandra-connector# sbt sbt-version


	Getting org.scala-sbt sbt 0.13.12  (this may take some time)...


[2-19] root@3e3a5d228c9c:/home/spark/spark-cassandra-connector# sbt -Dscala-2.11=true  assembly

[2-20] root@3e3a5d228c9c:/home/spark/spark-cassandra-connector# sbt -mem 4096 -Dscala-2.11=true  publish-local

	
	published ivy to /root/.ivy2/local/com.datastax.spark/spark-cassandra-connector_2.11/2.0.1-6-g07ec0a5/ivys/ivy.xml
	NOTE: take note of the installed version (2.0.1-6-g07ec0a5) for build.sbt in the example.
	for example, "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.1-6-g07ec0a5" 


[2-21] root@3e3a5d228c9c:/home/spark/spark-cassandra-connector# cd ..

[2-22] root@3e3a5d228c9c:/home/spark# /home/spark/spark-2.0.3/sbin/start-master.sh --host 0.0.0.0

	log information
	INFO Utils: Successfully started service 'sparkMaster' on port 7077.
	INFO Master: Starting Spark master at spark://0.0.0.0:7077
	INFO Master: Running Spark version 2.0.3-SNAPSHOT


[2-23] root@3e3a5d228c9c:/home/spark# /home/spark/spark-2.0.3/sbin/start-slave.sh  spark://0.0.0.0:7077 


[2-24] root@3e3a5d228c9c:/home/spark# cd examples/spark_cassandra


	NOTE: copy the connetor version (2.0.1-6-g07ec0a5 which is obtained at [2-20]) into build.sbt
	for example, "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.1-6-g07ec0a5"


[2-25] The example source code is as follows.


	import org.apache.spark
	import org.apache.spark._
	import org.apache.spark.SparkContext
	import org.apache.spark.SparkContext._
	import org.apache.spark.SparkConf
	import org.apache.spark.sql.SQLContext
	import org.apache.spark.sql.SparkSession
	import org.apache.spark.sql.cassandra
	import org.apache.spark.sql.cassandra._
	import com.datastax.spark
	import com.datastax.spark._
	import com.datastax.spark.connector
	import com.datastax.spark.connector._
	import com.datastax.spark.connector.cql
	import com.datastax.spark.connector.cql._
	import com.datastax.spark.connector.cql.CassandraConnector
	import com.datastax.spark.connector.cql.CassandraConnector._

	object spark_cassandra {

		def main(args: Array[String]):Unit = {
		

			println("... Scala SQL & Cassandra connection ...")

			//val conf = new SparkConf().setAppName(appName).setMaster(master)
			//The appName parameter is a name for your application to show on the cluster UI. 
			//master is a Spark, Mesos or YARN cluster URL, or a special “local” string to run in local mode
			val conf = new SparkConf(true).set("spark.cassandra.connection.host", "172.17.0.2")

			//val sc = new SparkContext("spark://0.0.0.0:7077", "test", conf) // error!
                	val sc = new SparkContext("local","test", conf)
		
			val mytable = sc.cassandraTable[(String, Int)]("tkeyspace", "stable").select("key", "value")
                	mytable.foreach(println)
				

		}
	}


[2-26] root@3e3a5d228c9c:/home/spark/examples/spark_cassandra# sbt sbt-version


	Getting org.scala-sbt sbt 0.13.15  (this may take some time)...


[2-27] root@3e3a5d228c9c:/home/spark/examples/spark_cassandra# sbt clean compile

[2-28] root@3e3a5d228c9c:/home/spark/examples/spark_cassandra# sbt clean package

[2-29] root@3e3a5d228c9c:/home/spark/examples/spark_cassandra# JAVA_OPTS="-Xmx4g" scala -cp /root/.ivy2/local/com.datastax.spark/spark-cassandra-connector_2.11/2.0.1-6-g07ec0a5/jars/spark-cassandra-connector_2.11.jar:/root/.ivy2/local/org.apache.spark/spark-core_2.11/2.0.3-SNAPSHOT/jars/spark-core_2.11.jar:/root/.ivy2/local/org.apache.spark/spark-sql_2.11/2.0.3-SNAPSHOT/jars/spark-sql_2.11.jar:/root/.ivy2/local/org.apache.spark/spark-mllib_2.11/2.0.3-SNAPSHOT/jars/spark-mllib_2.11.jar:/root/.ivy2/local/org.apache.spark/spark-streaming_2.11/2.0.3-SNAPSHOT/jars/spark-streaming_2.11.jar:/root/.ivy2/cache/org.apache.hadoop/hadoop-common/jars/hadoop-common-2.2.0.jar:/root/.ivy2/cache/org.apache.hadoop/hadoop-auth/jars/hadoop-auth-2.2.0.jar:/root/.ivy2/cache/org.apache.hadoop/hadoop-hdfs/jars/hadoop-hdfs-2.2.0.jar:/root/.ivy2/cache/org.apache.hadoop/hadoop-client/jars/hadoop-client-2.2.0.jar:/root/.ivy2/cache/com.google.guava/guava/bundles/guava-14.0.1.jar:/root/.ivy2/cache/org.apache.commons/commons-lang3/jars/commons-lang3-3.4.jar:/root/.ivy2/cache/org.slf4j/slf4j-api/jars/slf4j-api-1.7.7.jar:/root/.ivy2/cache/org.slf4j/slf4j-nop/jars/slf4j-nop-1.7.21.jar:/root/.ivy2/cache/commons-logging/commons-logging/jars/commons-logging-1.2.jar:/root/.ivy2/cache/commons-configuration/commons-configuration/jars/commons-configuration-1.6.jar:/root/.ivy2/cache/commons-lang/commons-lang/jars/commons-lang-2.6.jar:/root/.ivy2/local/org.apache.spark/spark-network-common_2.11/2.0.3-SNAPSHOT/jars/spark-network-common_2.11.jar:/root/.ivy2/local/org.apache.spark/spark-network-shuffle_2.11/2.0.3-SNAPSHOT/jars/spark-network-shuffle_2.11.jar:/root/.ivy2/local/org.apache.spark/spark-unsafe_2.11/2.0.3-SNAPSHOT/jars/spark-unsafe_2.11.jar:/root/.ivy2/cache/io.netty/netty/bundles/netty-3.8.0.Final.jar:/root/.ivy2/cache/io.netty/netty-all/jars/netty-all-4.0.33.Final.jar:/root/.ivy2/cache/com.esotericsoftware/kryo-shaded/bundles/kryo-shaded-3.0.3.jar:/root/.ivy2/cache/com.twitter/chill_2.11/jars/chill_2.11-0.8.0.jar:/root/.ivy2/cache/com.codahale.metrics/metrics-core/bundles/metrics-core-3.0.2.jar:/root/.ivy2/cache/org.json4s/json4s-core_2.11/jars/json4s-core_2.11-3.2.11.jar:/root/.ivy2/cache/org.json4s/json4s-jackson_2.11/jars/json4s-jackson_2.11-3.2.11.jar:/root/.ivy2/cache/org.json4s/json4s-ast_2.11/jars/json4s-ast_2.11-3.2.11.jar:/root/.ivy2/cache/javax.servlet/javax.servlet-api/jars/javax.servlet-api-3.1.0.jar::/root/.ivy2/cache/org.eclipse.jetty/jetty-util/jars/jetty-util-9.4.0.RC1.jar:/root/.ivy2/cache/org.eclipse.jetty/jetty-servlet/jars/jetty-servlet-9.2.16.v20160414.jar:/root/.ivy2/cache/org.eclipse.jetty/jetty-security/jars/jetty-security-9.2.16.v20160414.jar:/root/.ivy2/cache/org.eclipse.jetty/jetty-io/jars/jetty-io-9.4.0.RC1.jar::/root/.ivy2/cache/org.eclipse.jetty/jetty-xml/jars/jetty-xml-9.2.16.v20160414.jar:/root/.ivy2/cache/org.eclipse.jetty/jetty-continuation/jars/jetty-continuation-9.2.16.v20160414.jar:/root/.ivy2/cache/org.eclipse.jetty/jetty-server/jars/jetty-server-9.2.16.v20160414.jar:/root/.ivy2/cache/org.eclipse.jetty/jetty-http/jars/jetty-http-9.2.16.v20160414.jar:/root/.ivy2/cache/org.glassfish.jersey.containers/jersey-container-servlet-core/jars/jersey-container-servlet-core-2.22.2.jar:/root/.ivy2/cache/org.glassfish.jersey.containers/jersey-container-servlet/jars/jersey-container-servlet-2.22.2.jar:/root/.ivy2/cache/org.glassfish.jersey.core/jersey-server/jars/jersey-server-2.22.2.jar:/root/.ivy2/cache/org.glassfish.jersey.core/jersey-common/jars/jersey-common-2.22.2.jar:/root/.ivy2/cache/org.glassfish.jersey.core/jersey-client/jars/jersey-client-2.22.2.jar:/root/.ivy2/cache/org.eclipse.jetty/jetty-servlets/jars/jetty-servlets-9.2.16.v20160414.jar:/root/.ivy2/cache/org.eclipse.jetty/jetty-util/jars/jetty-util-9.2.16.v20160414.jar:/root/.ivy2/local/org.apache.spark/spark-launcher_2.11/2.0.3-SNAPSHOT/jars/spark-launcher_2.11.jar:/root/.ivy2/cache/com.fasterxml.jackson.core/jackson-databind/bundles/jackson-databind-2.6.5.jar:/root/.ivy2/cache/com.fasterxml.jackson.core/jackson-core/bundles/jackson-core-2.6.5.jar:/root/.ivy2/cache/com.fasterxml.jackson.core/jackson-annotations/bundles/jackson-annotations-2.6.5.jar:/root/.ivy2/cache/com.codahale.metrics/metrics-json/bundles/metrics-json-3.0.2.jar:/root/.ivy2/cache/commons-codec/commons-codec/jars/commons-codec-1.8.jar:/root/.ivy2/cache/org.apache.xbean/xbean-asm5-shaded/bundles/xbean-asm5-shaded-4.4.jar:/root/.ivy2/cache/joda-time/joda-time/jars/joda-time-2.9.3.jar:/root/.ivy2/cache/net.jpountz.lz4/lz4/jars/lz4-1.3.0.jar:/root/.ivy2/local/org.apache.spark/spark-catalyst_2.11/2.0.3-SNAPSHOT/jars/spark-catalyst_2.11.jar:/root/.ivy2/cache/com.fasterxml.jackson.module/jackson-module-scala_2.11/bundles/jackson-module-scala_2.11-2.6.5.jar:/root/.ivy2/cache/com.fasterxml.jackson.module/jackson-module-paranamer/bundles/jackson-module-paranamer-2.6.5.jar:/root/.ivy2/cache/com.thoughtworks.paranamer/paranamer/jars/paranamer-2.6.jar   ./target/scala-2.11/spark_cassandra_2.11-1.0.jar


[2-30] The output may look like

	... Scala SQL & Cassandra connection ...
	org.apache.hadoop.util.NativeCodeLoader <clinit>
	WARNING: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
	[Stage 0:>                                                          (0 + 1) / 4](dog,1)
	(deer,3)
	[Stage 0:=============================>                             (2 + 1) / 4](cat,2)


