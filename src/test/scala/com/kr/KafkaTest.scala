package com.kr

import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.scalatest.{FlatSpec, Matchers}

class KafkaTest extends FlatSpec with Matchers with EmbeddedKafka {

  implicit val kafkaConfig: EmbeddedKafkaConfig = EmbeddedKafkaConfig(kafkaPort = 12345)

  behavior of "EmbeddedKafka"

  it should "be ok" in {
    withRunningKafka {
      publishStringMessageToKafka("abc", "yo")
    }
  }
}
