#!/bin/sh

export SPARK_MAJOR_VERSION=2

jobName=poc-spark-kafka

# Config files
keytab=config/doaat_app_nifi_dev.keytab
log4jConf=config/log4j.properties
hbaseSite=/etc/hbase/conf/hbase-site.xml
pocProperties=config/poc-spark-kafka.properties

jobDir=.

# Main jar and dependencies
mainJar=${jobDir}/spark-streaming-kafka-hbase-1.0-SNAPSHOT-jar-with-dependencies.jar
hbaseCommonJar=${jobDir}//hbase-common.jar
hbaseClientJar=${jobDir}/hbase-client.jar
hbaseServerJar=${jobDir}/hbase-server.jar
hbaseProtocolJar=${jobDir}/hbase-protocol.jar

spark-submit --master yarn-cluster \
--class com.kr.SparkStreamingKafka \
--files ${log4jConf},${keytab},${hbaseSite},${pocProperties} \
--name ${jobName} \
--conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=log4j.properties" \
--conf "spark.driver.extraJavaOptions=-Dlog4j.configuration=log4j.properties" \
--jars ${hbaseCommonJar},${hbaseClientJar},${hbaseServerJar},${hbaseProtocolJar} ${mainJar}
