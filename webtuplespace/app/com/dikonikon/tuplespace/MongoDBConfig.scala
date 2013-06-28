

/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 20/05/13
 * Time: 09:49
 */

package com.dikonikon.tuplespace

import com.typesafe.config._
import play.api._
import scala.collection.immutable.HashMap
import java.lang.Integer

case class MongoDBConfig() {

  val HOST_KEY = "mongodb.host"
  val HOST_PORT = "mongodb.port"
  val DBNAME = "mongodb.dbname"

  val _defaults = HashMap((HOST_KEY, "localhost"), (HOST_PORT, "27017"), (DBNAME, "test"))

  def host: String = {
    getConfigOrDefault(HOST_KEY)
  }

  def port: Int = {
    val portNo = getConfigOrDefault(HOST_PORT)
    Integer.parseInt(portNo)
  }

  def dbname: String = {
    getConfigOrDefault(DBNAME)
  }

  private def getConfigOrDefault(key: String) = {
    val config = ConfigFactory.load
    try {
      val conf = config.getString(key)
      Logger.info("retrieved for key: " + key + " value: " + conf)
      conf
    } catch {
      case ex: ConfigException.Missing => {
        Logger.info("no value found for: " + key + " using default: " + _defaults(key))
        _defaults(key)
      }
      case ex: ConfigException.WrongType => {
        Logger.info("could not convert config type for: " + HOST_KEY + " to String, using default: " + _defaults(key))
        _defaults(key)
      }
    }
  }
}