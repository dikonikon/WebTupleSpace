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

  val db = MongoConnection(host, port)(dbname)
  /**
   * Happy day scenarios only for now
   * @param tuple
   * @return
   */
  private def saveTuple(tuple: WebTuple): WebTuple = {
    val tupleid = newTuple
    tuple.id = tupleid.toString
    saveOrUpdateElements(tuple, tupleid)
    tuple
  }

  private def saveOrUpdateElements(tuple: WebTuple, tupleid: ObjectId): Unit = {
    var i: Int = 0;
    for (e <- tuple.internal) {
      i = i + 1
      val eDbObjOpt = findElementObject(e, i)
      eDbObjOpt match {
        case None => addNewElement(e, tupleid, i)
        case Some(eObj) => {
          addTupleIdToElement(eObj, tupleid, i)
        }
      }
    }
  }

  private def newTuple(): ObjectId = {
    val t = MongoDBObject()
    val tuples = db("tuples")
    tuples += t
    t._id.get
  }

  private def addTupleIdToElement(e: DBObject, tupleId: ObjectId, sequ: Int): Unit = {
    val elements = db("elements" + sequ)
    val newIds = MongoDBList()
    newIds ++ e("tupleids").asInstanceOf[MongoDBList]
    elements.update(MongoDBObject("_id" -> e._id), MongoDBObject("tupleids" -> newIds))
  }

  private def addNewElement(e: (String, String, Array[Byte]), tupleid: ObjectId, sequ: Int): Unit = {
    val elements = db("elements" + sequ)
    val tupleids = MongoDBList()
    tupleids += tupleid
    val element = MongoDBObject("tupleids" -> tupleids, "type" -> e._1, "value" -> e._2, "hash" -> e._3)
    elements += element
  }

  private def findElementObject(pattern: (String, String, Array[Byte]), sequ: Int): Option[DBObject] = {
    val elements = db("elements" + sequ)
    elements.findOne(MongoDBObject("hash" -> pattern._3))
  }

  private def findElements(pattern: WebTuple): List[MongoDBObject] = {

  }

  override def out(tuple: WebTuple): WebTuple = {
    saveTuple(tuple)
  }

  override def in(pattern: WebTuple) = None

  override def start(pattern: WebTuple): String = {
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
