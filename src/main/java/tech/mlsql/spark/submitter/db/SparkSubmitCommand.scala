/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.mlsql.spark.submitter.db

import java.io.File

import net.csdn.common.logging.Loggers
import streaming.common.ParamsUtil

import scala.io.Source

/**
  * Created by allwefantasy on 12/7/2017.
  */
object ManagerConfiguration {
  var config: ParamsUtil = null
  val clean_check_interval = 30
  val submit_progress_check_interval = 2
  val submit_progress_check_expire_duration = 60 * 10
  val resubmit_try_interval = 60

  def yarnUrl = {
    config.getParam("yarnUrl")
  }

  def enableScheduler = {
    config.getBooleanParam("enableScheduler", true)
  }

  def afterLogCheckTimeout = {
    config.getIntParam("afterLogCheckTimeout", 60)
  }

  def sparkSqlServer = {
    config.getParam("sparkSqlServer", "")
  }

  def taskThreads = {
    config.getIntParam("taskThreads", 10)
  }

  def jdbcPath = {
    config.getParam("jdbcPath", "classpath:///jdbc.properties")
  }

  def env = {
    //eg. export SPARK_HOME=/opt/spark-2.1.1;export HADOOP_CONF_DIR=/etc/hadoop/conf;cd $SPARK_HOME;
    //eg. source /etc/profile ;cd $SPARK_HOME ;
    if (config.hasParam("envPath")) {
      Source.fromFile(new File(config.getParam("envPath"))).getLines().mkString("\n")
    } else {
      "export SPARK_HOME=/opt/spark-2.1.1;export HADOOP_CONF_DIR=/etc/hadoop/conf;cd $SPARK_HOME;"
    }

  }

  def liveness_check_interval = {
    if (config.hasParam("liveness_check_interval")) {
      config.getIntParam("liveness_check_interval")
    } else 30
  }

}

class SparkSubmitCommand {
  val logger = Loggers.getLogger(classOf[SparkSubmitCommand])


  val keywords = Set(
    "class",
    "master",
    "deploy-mode",
    "name",
    "queue",
    "executor-memory",
    "driver-memory",
    "num-executors",
    "executor-cores",
    "jars",
    "files")

  def process(params: Map[String, String]) = {
    var jarPath = params.getOrElse("jarPath", "")
    val beforeShell = params.getOrElse("beforeShell", "")
    val afterShell = params.getOrElse("afterShell", "")
    val sourceK = params.filter(f => f._1.startsWith("mmspark.")).map(f => (cut(f._1), f._2)).filter { f =>
      f._2 != null && !f._2.isEmpty
    }.map { f =>
      val paramKey = f._1
      val paramValue = f._2
      paramKey match {
        case pk if pk.startsWith("spark.") => s"""--conf "${paramKey}=${paramValue}" """
        case pk if keywords.contains(pk) => s"""--${paramKey} $paramValue """
        case pk if pk == "args" => s"""$paramValue """
        case pk if pk == "main_jar" =>
          jarPath = paramValue
          null
        case _ => s"""-${paramKey} $paramValue """
      }

    }.filter(f => f != null)

    var source = sourceK.filter(f => f.startsWith("--")).mkString(" \\\n") + " \\\n" +
      jarPath + " \\\n" +
      sourceK.filter(f => f.startsWith("-") && !f.startsWith("--")).mkString(" \\\n") + " \\\n" +
      sourceK.filter(f => !f.startsWith("-") && !f.startsWith("--")).mkString(" \\\n")
    source = source.substring(0, source.length - 2)
    logger.info(source)
    TSparkApplication.save("", ManagerConfiguration.yarnUrl, "spark-submit " + source, beforeShell, afterShell)
  }

  private def cut(str: String) = {
    val abc = str.split("\\.")
    abc.takeRight(abc.length - 1).mkString(".")
  }

  def name: String = "spark"
}


