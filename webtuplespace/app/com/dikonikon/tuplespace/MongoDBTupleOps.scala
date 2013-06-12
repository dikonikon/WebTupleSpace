package com.dikonikon.tuplespace

import com.mongodb.casbah.Imports._
import scala.collection.mutable.ListBuffer

/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 20/05/13
 * Time: 09:48
 */
class MongoDBTupleOps() {
  private val conf = MongoDBConfig()
  private val database = MongoConnection(conf.host, conf.port)(conf.dbname)

  def createTuple(webtuple: WebTuple): WebTuple = {
    val mongoDbTuple = toMongoTuple(webtuple)
    val tuples = database("tuples")
    tuples += mongoDbTuple
    print(mongoDbTuple.toString)
    webtuple.id = mongoDbTuple._id.get.toString
    webtuple
  }

  def findMatchingTuples(pattern: WebTuple, remove: Boolean = false): List[WebTuple] = {
    val tuples = database("tuples")
    val query = toMongoQuery(pattern)
    val matches = tuples.find(query)
    val listOfMatches = List[DBObject]() ++ matches
    if (remove) tuples.remove(query, WriteConcern.FsyncSafe)
    listOfMatches.map(x => WebTuple(x))
  }

  def createSession(): String = {
    val sessions = database("sessions")
    val sessionObj = MongoDBObject()
    sessions += sessionObj
    sessionObj._id.get.toString
  }

  def deleteSession(sessionId: String): Unit = {
    val sessions = database("sessions")
    val id = new ObjectId(sessionId)
    sessions.remove(MongoDBObject("_id" -> id))
  }

  def addSubscription(pattern: WebTuple, sessionId: String): Unit = {
    val sessions = database("sessions")
    val subscription = toSubscription(pattern)
    val id = new ObjectId(sessionId)
    val session = sessions.findOne(MongoDBObject( "_id" -> id))
    session match {
      case None => throw NoSessionFoundException()
      case Some(x) => {
        val subscriptions = (x.getOrElse("subscriptions", new MongoDBList())).asInstanceOf[MongoDBList]
        subscriptions += subscription
        addNotifications(subscription)
        x += "subscriptions" -> subscriptions
        sessions.update(MongoDBObject("_id" -> x._id), x)
      }
    }
  }

  private def addNotifications(subscription: MongoDBObject): Unit = {
    val tuples = database("tuples")
    val query = toMongoQuery(subscription.as[DBObject]("pattern"))
    val cursor = tuples.find(query)
    val notifications = subscription.as[MongoDBList]("notifications")
    for (t <- cursor) {
      if (!notifications.contains(t._id)) notifications += t._id
    }
    subscription += "notifications" -> notifications
  }

  def readNotifications(sessionId: String): List[(WebTuple, List[WebTuple])] = {
    val sessions = database("sessions")
    // for each subscription, for each ObjectId in the subscription, create a tuple of the original pattern and
    // list of matching tuples, if they still exist.
    val session = sessions.findOne(MongoDBObject("_id" -> new ObjectId(sessionId)))
    session match {
      case None => throw NoSessionFoundException()
      case Some(s) => {
        val subscriptions = s.getOrElse("subscriptions", None)
        subscriptions match {
          case None => List[(WebTuple, List[WebTuple])]()
          case subs: BasicDBList => {
            val result = readNotificationsStillExisting(subs)
            sessions.update(MongoDBObject("_id" -> new ObjectId(sessionId)), s)
            result
          }
        }
      }
    }
  }

  private def readNotificationsStillExisting(subscriptions: MongoDBList): List[(WebTuple, List[WebTuple])] = {
    List[(WebTuple, List[WebTuple])]() ++ subscriptions.map((s) => {
      val subscription = s.asInstanceOf[BasicDBObject]
      val pattern: WebTuple = WebTuple(subscription.as[BasicDBObject]("pattern"))
      val notifications = subscription.as[MongoDBList]("notifications")
      val notificationHistory = subscription.as[MongoDBList]("notificationHistory")
      notificationHistory += notifications
      val results: List[WebTuple] = readTuplesWithIds(notifications)
      notifications.clear
      (pattern, results)
    })
  }

  private def readTuplesWithIds(ids: MongoDBList): List[WebTuple] = {
    val tuples = database("tuples")
    val query = "_id" $in ids
    val result = new ListBuffer[WebTuple]()
    val cursor = tuples.find(query)
    cursor.foreach(x => result += WebTuple(x))
    result.toList
  }

  def db = this.database

  /**
   * There are two different conversions from WebTuple to MongoDBObject:
   * 1. a 'full' conversion that pulls all of the data from the tuple including type, value and hash information
   * for each element as well as calculating the shardKey for the tuple
   * 2. see <code>toMongoQuery</code> that in effect creates a MongoDB query by pulling only the hashes
   * for each element into the DBObject. These are the fields that are used to match on.
   * @param webtuple
   * @return
   */
  private def toMongoTuple(webtuple: WebTuple, isPattern: Boolean = false): MongoDBObject = {
    var i = 1
    val tupleObj = MongoDBObject()
    val shardHashTarget = Array[Byte]()
    for (e <- webtuple.internal) {
      val element = MongoDBObject("type" -> e._1, "value" -> e._2, "hash" -> e._3)
      val key = "e" + i.toString
      tupleObj += key -> element
      i = i + 1
      shardHashTarget ++ e._3
    }
    if (!isPattern) tupleObj += "shardKey" -> toHash(shardHashTarget)
    tupleObj
  }

  private def toMongoQuery(pattern: WebTuple): MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    for (i <- 1 to pattern.internal.size) {
      builder += ("e" + i + ".hash" -> pattern.internal(i - 1)._3)
    }
    builder.result()
  }

  private def toMongoQuery(pattern: DBObject): MongoDBObject = {
    val builder = MongoDBObject.newBuilder

    for (i <- 1 to pattern.size) {
      val key = "e" + i
      builder += (key + ".hash" -> pattern(key))
    }
    builder.result()
  }

  private def toSubscription(pattern: WebTuple): MongoDBObject = {
    val mongoPattern = toMongoTuple(pattern, isPattern = true)
    val sub = MongoDBObject("pattern" -> mongoPattern)
    sub += "notifications" -> new MongoDBList()
    sub += "notificationHistory" -> new MongoDBList()
  }

}

object MongoDBTupleOps extends MongoDBTupleOps {

}



