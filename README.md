# Kerberized Spark Streaming with Kafka and HBase

In this article, I'll explain how to create and run a Spark Streaming component in a Kerberized Hadoop cluster.

This component will stream messages from a kafka topic, save theses messages into HBase and finally push it in an another kafka topic :

![spark_streaming_kafka](https://user-images.githubusercontent.com/10392318/57933520-cfaec400-78bd-11e9-8a81-2e135bea1752.png)
