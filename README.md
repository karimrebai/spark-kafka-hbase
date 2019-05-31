## Kerberized Spark Streaming with Kafka and HBase

### Description

This component stream messages from a kafka topic, save these into HBase and finally push these messages in an another kafka topic :

![spark_streaming_kafka](https://user-images.githubusercontent.com/10392318/57933520-cfaec400-78bd-11e9-8a81-2e135bea1752.png)

The component relies on 4 classes :
* [SparkStreamingKafka](src/main/scala/com/kr/SparkStreamingKafka.scala) implements the main streaming algorithm and depends on the 3 classes below
* [KafkaDirectStream](src/main/scala/com/kr/KafkaDirectStream.scala) configures Kafka consumer and provides a method to stream input messages from input topic
* [KafkaProducer](src/main/scala/com/kr/KafkaProducer.scala) configure Kafka producer and exposes a method to push messages to output topic
* [HBaseRepository](src/main/scala/com/kr/HBaseRepository.scala) allows to open a connection to HBase and provides methods to put data into a given table

### Installation

```bash
$ mvn install
```

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

#### HBase table

The component will try to put data into a table "employee", created as follows :

```sql
create table "employee"("ID" VARCHAR, "info"."NAME" VARCHAR, CONSTRAINT pk PRIMARY KEY(ID));
```

### Deployment

```bash
$ export KAFKA_OPTS="-Djava.security.auth.login.config=/etc/kafka/conf/kafka_client_jaas.conf -Dsun.security.krb5.debug=true"

$ /usr/hdp/current/kafka-broker/bin/kafka-console-consumer.sh --new-consumer --bootstrap-server noeyy3pu.noe.edf.fr:9096,noeyycgd.noe.edf.fr:9096,noeyycge.noe.edf.fr:9096 --topic fr.edf.doaat.nsi.zb-metadata-from-nifi-json-dev-inspect-2 --security-protocol SASL_SSL --property security.protocol=SASL_SSL

$ /usr/hdp/current/kafka-broker/bin/kafka-console-producer.sh --broker-list noeyy3pu.noe.edf.fr:9096,noeyycgd.noe.edf.fr:9096,noeyycge.noe.edf.fr:9096 --topic fr.edf.doaat.nsi.zb-metadata-from-nifi-json-dev-inspect-1 --security-protocol SASL_SSL --property security.protocol=SASL_SSL
```