package com.dikonikon.tuplespace

import scala.xml.Elem
import com.mongodb.casbah.Imports._

/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 04/05/13
 * Time: 20:52
 */


class MongoDBTupleSpaceServer(host: String, port: Int, dbname: String) extends TupleSpaceServer {

  val db = MongoConnection(host, port)(dbname)
  /**
   * Happy day scenarios only for now
   * @param tuple
   * @return
   */
  private def saveTuple(tuple: WebTuple): WebTuple = {
    val tuples = db("tuples")
    val t = MongoDBObject("tuple" -> tuple.original)
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
}
