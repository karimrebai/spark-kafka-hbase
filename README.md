## Kerberized Spark Streaming with Kafka and HBase

### Description

This component stream messages from a kafka topic, save these into HBase and finally push these messages in an another kafka topic :

![spark_streaming_kafka](https://user-images.githubusercontent.com/10392318/57933520-cfaec400-78bd-11e9-8a81-2e135bea1752.png)

The component relies on 4 classes :
* [SparkStreamingKafka](src/main/scala/com/kr/SparkStreamingKafka.scala) implements the main streaming algorithm and depends on the 3 classes below
* [KafkaDirectStream](src/main/scala/com/kr/KafkaDirectStream.scala) configures Kafka consumer and provides a method to stream input messages from input topic
* [KafkaProducer](src/main/scala/com/kr/KafkaProducer.scala) configure Kafka producer and exposes a method to push messages to output topic
* [HBaseRepository](src/main/scala/com/kr/HBaseRepository.scala) allows to open a connection to HBase and provides methods to put data into a given table

### Prerequisites

#### Configuration

The only file to configure is [config.properties](config.properties) where all the %PARAMS% have to be replaced to target specific kafka broker and topics, a keytab and a HBase namespace :

```properties
kafka.brokers=%URL_1%:%PORT_1%,%URL_2%:%PORT_2%
kafka.groupId=poc-spark-kafka
kafka.input.topic=%INPUT_TOPIC%
kafka.output.topic=%OUTPUT_TOPIC%
kafka.jaas.config=com.sun.security.auth.module.Krb5LoginModule required useTicketCache=false useKeyTab=true principal="%PRINCIPAL%@%REALM%" keyTab="%PRINCIPAL%.keytab" renewTicket=true storeKey=true;

kerberos.principal=%PRINCIPAL%@%REALM%
kerberos.keytab=%PRINCIPAL%.keytab

hbase.namespace=%NAMESPACE%

spark.micro.batch.duration=5
```

The kerberos principal also has to be specified in [spark-submit shell script](spark-submit.sh).

#### HBase table

The component will try to put data into a table "employee", created as follows :

```sql
create table "employee"("ID" VARCHAR, "info"."NAME" VARCHAR, CONSTRAINT pk PRIMARY KEY(ID));
```

### Installation and deployment

```bash
$ mvn install
```
Then the spark job may be started with the provided [spark-submit shell script](spark-submit.sh) :
```bash
$ ./spark-submit.sh
```

### Produce and consume data via Kafka

First of all, jaas configuration has to be provided :
```bash
$ export KAFKA_OPTS="-Djava.security.auth.login.config=/etc/kafka/conf/kafka_client_jaas.conf"
```

Then you can put data in the %INPUT_TOPIC% via this command :
```bash
$ /usr/hdp/current/kafka-broker/bin/kafka-console-producer.sh --broker-list %URL_1%:%PORT_1%,%URL_2%:%PORT_2% --topic %INPUT_TOPIC% --security-protocol SASL_SSL --property security.protocol=SASL_SSL
```

If the job is working well, you should the event in the %OUTPUT_TOPIC% with the following command :
```bash
$ /usr/hdp/current/kafka-broker/bin/kafka-console-consumer.sh --new-consumer --bootstrap-server %URL_1%:%PORT_1%,%URL_2%:%PORT_2% --topic %OUTPUT_TOPIC% --security-protocol SASL_SSL --property security.protocol=SASL_SSL
```
