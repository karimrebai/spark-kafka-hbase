package com.kr

import java.lang

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent

class KafkaDirectStream(streamingContext: StreamingContext,
                        kafkaParams: Map[String, Object], inputTopics: Array[String]) {
  def getStream: InputDStream[ConsumerRecord[String, String]] = {
    KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](inputTopics, kafkaParams)
    )
  }
}

object KafkaDirectStream {
  def apply(streamingContext: StreamingContext, configuration: Map[String, String]): KafkaDirectStream = {
    val inputTopics = Array(configuration("kafka.input.topic"))
    new KafkaDirectStream(streamingContext, createConsumerConfig(configuration), inputTopics)
  }

  private def createConsumerConfig(configuration: Map[String, String]): Map[String, Object] = {
    Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> configuration("kafka.brokers"),
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> configuration("kafka.groupId"),
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: lang.Boolean),
      CommonClientConfigs.SECURITY_PROTOCOL_CONFIG -> "SASL_SSL",
      SaslConfigs.SASL_MECHANISM -> "GSSAPI",
      SaslConfigs.SASL_KERBEROS_SERVICE_NAME -> "kafka",
      SaslConfigs.SASL_JAAS_CONFIG -> configuration("kafka.jaas.config")
    )
  }
}
