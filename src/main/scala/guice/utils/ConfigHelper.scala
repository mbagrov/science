package guice.utils

import java.lang.Long
import java.util

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.io.FileUtils
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by Aleksey Voronets on 09.02.16.
  */
trait ConfigHelper {

  lazy val config = ConfigFactory.load()
  val logger: Logger = LoggerFactory.getLogger(getClass)

  def getString(key: String): String = config.getString(key)

  def getBoolean(key: String): Boolean = config.getBoolean(key)

  def getInt(key: String): Int = config.getInt(key)

  def getStringList(key: String): util.List[String] = config.getStringList(key)

  def getConfig(key: String): Config = config.getConfig(key)

  def getBytes(key: String): Long = config.getBytes(key)

  def getFileContent(key: String): Option[String] = {
    val file = getString(key)
    try {
      Some(FileUtils.readFileToString(new java.io.File(file)))
    } catch {
      case e: Exception => logger.error(s"can't load file, key: $key, value: $file", e); None
    }
  }
}
