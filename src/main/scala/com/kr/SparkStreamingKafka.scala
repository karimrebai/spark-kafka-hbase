package com.kr

import java.security.PrivilegedAction

import com.typesafe.config.ConfigFactory
import org.apache.hadoop.security.UserGroupInformation
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, HasOffsetRanges}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.JavaConversions._

object SparkStreamingKafka {

  def main(args: Array[String]): Unit = {
    val conf = loadConfig()
    val streamingContext: StreamingContext = createStreamingContext(conf)
    val stream = KafkaDirectStream(streamingContext, conf).getStream
    stream.foreachRDD(rdd => {
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd.foreachPartition(part => {
        val ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(
          conf("kerberos.principal"), conf("kerberos.keytab"))
        ugi.doAs(new PrivilegedAction[Void] {
          override def run(): Null = {
            val hBaseRepository = HBaseRepository(conf)
            val employeeTable = hBaseRepository.getTable("employee")
            try {
              val kafkaProducer = KafkaProducer(conf)
              part.foreach(message => {
                hBaseRepository.put(employeeTable, Record("42", "info", "NAME", message.value()))
                kafkaProducer.send(message.value(), conf("kafka.push.topic"))
              })
            } finally {
              hBaseRepository.closeTable(employeeTable)
              hBaseRepository.closeConnection()
            }
            null
          }
        })
      })
      stream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
    })
    streamingContext.start()
    streamingContext.awaitTermination()
  }

  private def createStreamingContext(conf: Map[String, String]): StreamingContext = {
    val sparkConf = new SparkConf()
    sparkConf.set("spark.hadoop.hadoop.security.authentication", "kerberos")
    sparkConf.set("spark.hadoop.hadoop.security.authorization", "true")
    sparkConf.set("spark.hadoop.dfs.namenode.kerberos.principal", conf("hdfs.url"))
    sparkConf.set("spark.hadoop.yarn.resourcemanager.principal", conf("yarn.url"))
    new StreamingContext(sparkConf, Seconds(conf("spark.micro.batch.duration").toInt))
  }

  private def loadConfig(): Map[String, String] = {
    ConfigFactory.parseResources("poc-spark-kafka.properties")
      .entrySet()
      .map(config => config.getKey -> config.getValue.unwrapped().toString)
      .toMap
  }
}
