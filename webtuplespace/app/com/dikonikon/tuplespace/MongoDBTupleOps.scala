package com.dikonikon.tuplespace

import com.mongodb.casbah.Imports._

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

  /**
   * Happy day scenarios only for now
   * @param webtuple
   * @return
   */
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

  def createSession(): MongoDBObject = {
    val sessions = database("sessions")
    val sessionObj = MongoDBObject()
    sessions += sessionObj
    sessionObj
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
        val subscriptions = x.as[MongoDBList]("subscriptions")
        subscriptions += subscription
        addNotifications(subscription)
        sessions.update(MongoDBObject("_id" -> x._id), x)
      }
    }
  }

  def addNotifications(subscription: MongoDBObject): Unit = {
    val tuples = database("tuples")
    val cursor = tuples.find(subscription.as[MongoDBObject]("query"))
    val notifications = subscription.as[MongoDBList]("notifications")
    for (t <- cursor) {
      if (!notifications.contains(t)) notifications += t
    }
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
  private def toMongoTuple(webtuple: WebTuple): MongoDBObject = {
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
    tupleObj += "shardKey" -> toHash(shardHashTarget)
  }

  private def toMongoQuery(pattern: WebTuple): MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    var i = 1
    for (e <- pattern.internal) {
      builder += ("e" + i + ".hash" -> e._3)
      i += 1
    }
    builder.result()
  }

  private def toSubscription(pattern: WebTuple): MongoDBObject = {
    val query = toMongoQuery(pattern)
    val mongoPattern = toMongoTuple(pattern)
    MongoDBObject("query" -> query, "pattern" -> mongoPattern)
  }

}

object MongoDBTupleOps extends MongoDBTupleOps {

}



