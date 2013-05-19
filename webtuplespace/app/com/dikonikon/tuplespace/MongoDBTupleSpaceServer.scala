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

  override def take(pattern: WebTuple) = None

  override def subscribe(pattern: WebTuple): String = {
    val (m, s) = findMatching(pattern)
    val builder = MongoDBObject.newBuilder
    val i: Int = 1;
    pattern.internal.map (e => {})
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
    val mongotupleid = createTuple
    webtuple.id = mongotupleid.toString
    createOrUpdateElements(webtuple, mongotupleid)
    webtuple
  }

  private def createOrUpdateElements(webtuple: WebTuple, mongotupleid: ObjectId): Unit = {
    var i: Int = 0
    val elementObjectIds = MongoDBList()
    for (e <- webtuple.internal) {
      i = i + 1
      val eDbObjOpt = findElementObject(e, i)
      eDbObjOpt match {
        case None => { elementObjectIds += createNewElement(e, mongotupleid, i); }
        case Some(eObj) => {
          updateElementWithTupleId(eObj, mongotupleid, i)
        }
      }
      updateTupleWithElementIds(mongotupleid, elementObjectIds)
    }
  }

  private def updateTupleWithElementIds(mongotupleid: ObjectId, elementObjectIds: MongoDBList): Unit = {
    val tuples = db("tuples")
    tuples.update(MongoDBObject("_id" -> mongotupleid), MongoDBObject("elementids" -> elementObjectIds))
  }

  private def createTuple(): ObjectId = {
    val t = MongoDBObject()
    val tuples = db("tuples")
    tuples += t
    t._id.get
  }

  private def updateElementWithTupleId(e: DBObject, tupleId: ObjectId, sequ: Int): Unit = {
    val elements = db("elements" + sequ)
    val newIds = MongoDBList()
    newIds ++ e("tupleids").asInstanceOf[MongoDBList]
    elements.update(MongoDBObject("_id" -> e._id), MongoDBObject("tupleids" -> newIds))
  }

  private def createNewElement(e: (String, String, Array[Byte]), tupleid: ObjectId, sequ: Int): ObjectId = {
    val elements = db("elements" + sequ)
    val tupleids = MongoDBList()
    tupleids += tupleid
    val element = MongoDBObject("tupleids" -> tupleids, "type" -> e._1, "value" -> e._2, "hash" -> e._3)
    elements += element
    element._id.get
  }

  private def findElementObject(pattern: (String, String, Array[Byte]), sequ: Int): Option[DBObject] = {
    val elements = db("elements" + sequ)
    elements.findOne(MongoDBObject("hash" -> pattern._3))
  }

  /*
  Iterate through each non-None element in the pattern, and find the matching elements if they exist.
  If at any point an element doesn't match abandon the search since if any element doesn't match
  the tuple doesn't match.
   */
  private def findMatchingElementObjects(pattern: WebTuple): List[DBObject] = {
    var sequ = 0
    var matchingElements = List[DBObject]()
    for (e <- pattern.internal) {
      sequ = sequ + 1;
      e match {
        case None =>
        case x => findElementObject(e, sequ) match {
          case None => return List[DBObject]()
          case Some(x) => matchingElements = x :: matchingElements
        }
      }
    }
    matchingElements
  }


  private def addSubscription(pattern: WebTuple): String = {
    ""
  }

  // TODO: hmmm... I may have this all wrong. Maybe should use one collection, sharded over hash
  // each tuple is an object with elements e1, e2, e3... and the collection is indexed on them all
  // each element is an embedded document of the form { "type", "value", "hash" }
  // need to work out whether this works - maybe its a lot simpler and its atomic

  /*
  1. Find the matching element for each non-None element take the pattern by looking up the hash
   */
  private def findMatching(pattern: WebTuple): (List[WebTuple]) = {
    val elementObjs = findMatchingElementObjects(pattern)
    val commonTupleIds = findCommonTupleIds(elementObjs)
  }

  private def findCommonTupleIds(elementObjs: List[DBObject]): List[ObjectId] = {
    val tupleRefLists: List[MongoDBList] = elementObjs.map(x => x("tupleids").asInstanceOf[MongoDBList])
    val firstList = tupleRefLists.head
    val otherLists = tupleRefLists.tail
    List[ObjectId]() ++ firstList.filter(inAllLists(_, otherLists)).map(x => x.asInstanceOf[ObjectId])
  }

  private def inAllLists(x: Any, list: List[MongoDBList]): Boolean = {
    for (l <- list) {
      if (!l.contains(x)) return false;
    }
    true
  }

}
