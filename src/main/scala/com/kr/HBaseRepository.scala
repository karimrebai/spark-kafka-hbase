package com.kr

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put, Table}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}

class HBaseRepository(connection: Connection, nameSpace: String) {

  def getTable(tableName: String): Table = {
    connection.getTable(TableName.valueOf(s"$nameSpace:$tableName"))
  }

  def closeTable(table: Table): Unit = {
    table.close()
  }

  def put(table: Table, record: Record): Unit = {
    val put = new Put(Bytes.toBytes(record.rowKey))
    put.addColumn(Bytes.toBytes(record.columnFamily), Bytes.toBytes(record.qualifier), Bytes.toBytes(record.value))
    table.put(put)
  }

  def closeConnection(): Unit = {
    connection.close()
  }
}

object HBaseRepository {
  def apply(configuration: Map[String, String]): HBaseRepository = {
    val hConnection: Connection = ConnectionFactory.createConnection(createHBaseConfig(configuration))
    new HBaseRepository(hConnection, configuration("hbase.namespace"))
  }

  private def createHBaseConfig(configuration: Map[String, String]) = {
    val hBaseConfiguration: Configuration = HBaseConfiguration.create()
    hBaseConfiguration.addResource("core-site.xml")
    hBaseConfiguration.addResource("hbase-site.xml")
    hBaseConfiguration.set("hbase.rpc.controllerfactory.class", "org.apache.hadoop.hbase.ipc.RpcControllerFactory")
    hBaseConfiguration.set("hbase.zookeeper.quorum", configuration("zookeeper.url"))
    hBaseConfiguration.set("hbase.zookeeper.property.clientPort", configuration("zookeeper.clientPort"))
    hBaseConfiguration.set("hadoop.security.authentication", "kerberos")
    hBaseConfiguration
  }
}
