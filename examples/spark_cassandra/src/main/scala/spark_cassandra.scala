

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
