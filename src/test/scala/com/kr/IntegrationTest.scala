package com.kr

import java.nio.file.Paths

import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.hadoop.hbase.{HBaseTestingUtility, NamespaceDescriptor}
//import org.apache.spark.SparkConf
//import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class IntegrationTest extends FlatSpec with Matchers with BeforeAndAfter with EmbeddedKafka {

//  private val master = "local[*]"
//  private val appName = "example-spark-streaming"
//  private val batchDuration = Seconds(1)
//
//  private var ssc: StreamingContext = _

  private val OsNameKey = "os.name"
  private val HadoopHomeDirPropertyKey = "hadoop.home.dir"
  private val HadoopDirName = "hadoop"
  private val HadoopDllPath = "bin/hadoop.dll"
  private val HadoopWinutilsPath = "bin/winutils.exe"
  private val TestBuildDataDirKey = "test.build.data.basedirectory"
  private val TestBuildDataDir = "C:/HBaseTestingUtility/"

  private var hBase: HBaseTestingUtility = _

  private val Namespace = "test"

  implicit val kafkaConfig: EmbeddedKafkaConfig = EmbeddedKafkaConfig(kafkaPort = 12345)

  before {
    if (isWindows) {
      System.setProperty(TestBuildDataDirKey, TestBuildDataDir)
      val hadoopHomeDir = Paths.get(getClass.getClassLoader.getResource(HadoopDirName).toURI).toString
      System.setProperty(HadoopHomeDirPropertyKey, hadoopHomeDir)
      System.load(s"$hadoopHomeDir/$HadoopDllPath")
      System.load(s"$hadoopHomeDir/$HadoopWinutilsPath")
    }
    hBase = new HBaseTestingUtility()
    hBase.startMiniCluster()
    hBase.getConnection.getAdmin.createNamespace(NamespaceDescriptor.create(Namespace).build)

//    val conf = new SparkConf()
//      .setMaster(master)
//      .setAppName(appName)
//    ssc = new StreamingContext(conf, batchDuration)
  }

  def isWindows: Boolean = {
    System.getProperty(OsNameKey).toLowerCase().indexOf("win") >= 0
  }

  after {
//    if (ssc != null) {
//      ssc.stop()
//    }
  }

  it should "be ok" in {
    withRunningKafka {
      publishStringMessageToKafka("test", "yo")
    }
  }
}
