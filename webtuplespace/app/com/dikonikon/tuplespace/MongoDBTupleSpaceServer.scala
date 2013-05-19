package com.dikonikon.tuplespace

import scala.xml.Elem
import com.mongodb.casbah.Imports._
import java.security.MessageDigest
import javax.xml.ws.WebEndpoint
import com.mongodb.casbah.commons.ValidBSONType.ObjectId

/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 04/05/13
 * Time: 20:52
 */


class MongoDBTupleSpaceServer(host: String = "localhost", port: Int = 27017, dbname: String = "test") extends TupleSpaceServer {

  override def put(tuple: WebTuple): WebTuple = {
    createTuple(tuple)
  }

  // todo:
  override def takeOne(pattern: WebTuple): WebTuple = WebTuple()

  // todo:
  override def subscribe(pattern: WebTuple): String = {
    ""
  }

  override def unsubscribe(sessionId: Long): Unit = {
    throw new NotImplementedError()
  }

  private val db = MongoConnection(host, port)(dbname)

  /**
   * Happy day scenarios only for now
   * @param webtuple
   * @return
   */
  private def createTuple(webtuple: WebTuple): WebTuple = {
    var i = 1
    val builder = MongoDBObject.newBuilder
    val shardHashTarget = Array[Byte]()
    for (e <- webtuple.internal) {
      val element = MongoDBObject("type" -> e._1, "value" -> e._2, "hash" -> e._3)
      builder += "e" + i -> element
      i = i + 1
      shardHashTarget ++ e._3
    }
    builder += "shardHash" -> toHash(shardHashTarget)
    val tuples = db("tuples")
    val mongoTuple = builder.result
    tuples += mongoTuple
    webtuple.id = mongoTuple._id.get.toString
    webtuple
  }

  private def addSubscription(pattern: WebTuple): String = {
    ""
  }
}
