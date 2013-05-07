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


class MongoDBTupleSpaceServer extends TupleSpaceServer {

  /**
   * Happy day scenarios only for now
   * @param tuple
   * @return
   */
  private def saveTuple(tuple: WSTuple): WSTuple = {
    val tuples = MongoConnection()("test")("tuples")
    val t = MongoDBObject()
    tuples += t
    tuple.id = t.get("_id").toString
    var i: Int = 0;
    for (e <- tuple.internal) {
      i = i + 1
      val elements = MongoConnection()("test")("elements" + i)
      val element = MongoDBObject("tupleid" -> tuple.id, "type" -> e._1, "value" -> e._2, "hash" -> e._3)
      elements += element
    }
    tuple
  }

  override def out(tuple: WSTuple) = {

    val id = saveTuple(tuple)
    for (e <- tuple.internal) {
      // create object from each element together with id
      // how do we identify and fix inconsistencies if the save happens half way
    }
  }

  override def in(pattern: WSTuple) = None
}
