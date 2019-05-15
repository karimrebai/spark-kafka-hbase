package com.kr

import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put, Table}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}

case class Record(rowKey: String, columnFamily: String, qualifier: String, value: String)

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
    val hConnection: Connection = ConnectionFactory.createConnection(HBaseConfiguration.create())
    new HBaseRepository(hConnection, configuration("hbase.namespace"))
  }
}
