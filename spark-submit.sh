#!/bin/sh

export SPARK_MAJOR_VERSION=2

# Config files
keytab=doaat_app_nifi_dev.keytab
log4jConf=log4j.properties
hbaseSite=/etc/hbase/conf/hbase-site.xml
config=config.properties

# Main jar and dependencies
mainJar=lib/spark-streaming-kafka-hbase-1.0-SNAPSHOT-jar-with-dependencies.jar
hbaseCommonJar=lib/hbase-common.jar
hbaseClientJar=lib/hbase-client.jar
hbaseServerJar=lib/hbase-server.jar
hbaseProtocolJar=lib/hbase-protocol.jar

spark-submit --master yarn \
--deploy-mode cluster \
--class com.kr.SparkStreamingKafka \
--files ${log4jConf},${keytab},${hbaseSite},${config} \
--name poc-spark-kafka \
--conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=log4j.properties" \
--conf "spark.driver.extraJavaOptions=-Dlog4j.configuration=log4j.properties" \
--jars ${hbaseCommonJar},${hbaseClientJar},${hbaseServerJar},${hbaseProtocolJar} ${mainJar}
