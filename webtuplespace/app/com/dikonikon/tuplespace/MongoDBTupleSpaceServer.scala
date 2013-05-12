package com.dikonikon.tuplespace

import scala.xml.Elem
import com.mongodb.casbah.Imports._
import java.security.MessageDigest
import javax.xml.ws.WebEndpoint

/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 04/05/13
 * Time: 20:52
 */


class MongoDBTupleSpaceServer(host: String = "localhost", port: Int = 27017, dbname: String = "test") extends TupleSpaceServer {

  val db = MongoConnection(host, port)(dbname)
  /**
   * Happy day scenarios only for now
   * @param tuple
   * @return
   */
  private def saveTuple(tuple: WebTuple): WebTuple = {
    val tuples = db("tuples")
    val t = MongoDBObject()
    tuples += t
    tuple.id = t._id.get.toString
    var i: Int = 0;
    for (e <- tuple.internal) {
      i = i + 1
      val elements = db("elements" + i)
      val element = MongoDBObject("tupleid" -> t._id, "type" -> e._1, "value" -> e._2, "hash" -> e._3)
      elements += element
    }
    tuple
  }

  override def out(tuple: WebTuple): WebTuple = {
    saveTuple(tuple)
  }

  override def in(pattern: WebTuple) = None

  override def start(pattern: WebTuple): String = {
    val subid: String = addSubscription(pattern)
    val (m, s) = getMatching(pattern)
    val builder = MongoDBObject.newBuilder
    val i: Int = 1;
    pattern.internal.map (e => {})
  }

  private def addSubscription(pattern: WebTuple): String = {
    ""
  }

  private def getMatching(pattern: WebTuple): (List[WebTuple], List[String]) = {
    (List[WebTuple](), List[String]())
  }

  override def end(sessionId: Long): Unit = {
    throw new NotImplementedError()
  }
}
