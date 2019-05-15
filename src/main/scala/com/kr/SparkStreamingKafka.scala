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
    val streamingContext: StreamingContext =
      new StreamingContext(new SparkConf(), Seconds(conf("spark.micro.batch.duration").toInt))
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
                kafkaProducer.send(message.value(), conf("kafka.output.topic"))
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

  private def loadConfig(): Map[String, String] = {
    ConfigFactory.parseResources("config.properties")
      .entrySet()
      .map(config => config.getKey -> config.getValue.unwrapped().toString)
      .toMap
  }
}
