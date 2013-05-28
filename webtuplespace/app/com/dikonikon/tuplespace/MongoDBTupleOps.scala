package com.dikonikon.tuplespace

import com.mongodb.casbah.Imports._
import play.Logger
import com.mongodb.casbah.commons.ValidBSONType.ObjectId

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
    val tuples = database("tuples")
    tuples += tupleObj
    print(tupleObj.toString)
    webtuple.id = tupleObj._id.get.toString
    webtuple
  }

  def findMatchingTuples(pattern: WebTuple, remove: Boolean = false): List[WebTuple] = {
    val tuples = database("tuples")
    val builder = MongoDBObject.newBuilder
    var i = 1
    for (e <- pattern.internal) {
      builder += ("e" + i + ".hash" -> e._3)
      i += 1
    }
    val query = builder.result
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

  def endSession(sessionId: String): Unit = {
    val sessions = database("sessions")
    val id = new ObjectId(sessionId)
    sessions.remove(MongoDBObject("_id" -> id))
  }

  def db = this.database
}

object MongoDBTupleOps extends MongoDBTupleOps {

}

