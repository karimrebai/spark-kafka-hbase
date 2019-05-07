package com.kr

import java.util.Properties

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.config.SaslConfigs
import org.slf4j.{Logger, LoggerFactory}

class KafkaProducer(producer: Producer[Nothing, String], topic: String) extends Serializable {

  val Logger: Logger = LoggerFactory.getLogger(KafkaProducer.getClass)

  def send(msg: String, topic: String): Unit = {
    val record = new ProducerRecord(topic, msg)
    val callback = new Callback {
      override def onCompletion(metadata: RecordMetadata, e: Exception): Unit = {
        if (e != null) {
          Logger.error(s"Error pushing message : ${e.getMessage}", e)
        }
      }
    }
    producer.send(record, callback)
  }
}

object KafkaProducer {
  def apply(config: Map[String, String]): KafkaProducer = {
    new KafkaProducer(new org.apache.kafka.clients.producer.KafkaProducer[Nothing, String](
      createProducerConfig(config)), config("kafka.push.topic"))
  }

  private def createProducerConfig(config: Map[String, String]): Properties = {
    val props = new Properties
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config("kafka.brokers"))
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, config("kafka.push.maxRequestSize"))
    props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, config("kafka.push.compression"))
    props.put(ProducerConfig.ACKS_CONFIG, "1")
    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")
    props.put(SaslConfigs.SASL_MECHANISM, "GSSAPI")
    props.put(SaslConfigs.SASL_KERBEROS_SERVICE_NAME, "kafka")
    props.put(SaslConfigs.SASL_JAAS_CONFIG, config("kafka.jaas.config"))
    props
  }
}
