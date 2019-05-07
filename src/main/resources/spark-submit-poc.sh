#!/bin/sh

jobName=poc-spark-kafka

# Config files
keytab=config/doaat_app_nifi_dev.keytab
log4jConf=config/log4j.properties
hbaseSite=/etc/hbase/conf/hbase-site.xml
pocProperties=config/poc-spark-kafka.properties

jobDir=.

# Main jar and dependencies
sparkStreamingKafkaJar=${jobDir}/spark-streaming-kafka_2.10-1.6.4-20170328.085640-1.jar
mainJar=${jobDir}/spark-streaming-kafka-hbase-1.0-SNAPSHOT-jar-with-dependencies.jar
hbaseCommonJar=${jobDir}//hbase-common.jar
hbaseClientJar=${jobDir}/hbase-client.jar
hbaseServerJar=${jobDir}/hbase-server.jar
hbaseProtocolJar=${jobDir}/hbase-protocol.jar

spark-submit --master yarn-cluster \
--class com.kr.Main \
--files ${log4jConf},${keytab},${hbaseSite},${pocProperties} \
--name ${jobName} \
--conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=log4j.properties" \
--conf "spark.driver.extraJavaOptions=-Dlog4j.configuration=log4j.properties" \
--jars ${sparkStreamingKafkaJar},${hbaseCommonJar},${hbaseClientJar},${hbaseServerJar},${hbaseProtocolJar} ${mainJar}
