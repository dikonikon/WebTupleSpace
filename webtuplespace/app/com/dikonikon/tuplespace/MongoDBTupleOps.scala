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
  private val db = MongoConnection(conf.host, conf.port)(conf.dbname)

  /**
   * Happy day scenarios only for now
   * @param webtuple
   * @return
   */
  def createTuple(webtuple: WebTuple): WebTuple = {
    var i = 1
    val builder = MongoDBObject.newBuilder
    val shardHashTarget = Array[Byte]()
    for (e <- webtuple.internal) {
      val element = MongoDBObject("type" -> e._1, "value" -> e._2, "hash" -> e._3)
      builder += "e" + i -> element
      i = i + 1
      shardHashTarget ++ e._3
    }
    builder += "shardKey" -> toHash(shardHashTarget)
    val tuples = db("tuples")
    val mongoTuple = builder.result
    tuples += mongoTuple
    webtuple.id = mongoTuple._id.get.toString
    webtuple
  }

  def readMatchingTuples(pattern: WebTuple): List[WebTuple] = {
    val tuples = db("tuples")
    val query = MongoDBObject.newBuilder
    var i = 1
    for (e <- pattern.internal) {
      query += ("e" + i + ".hash" -> e._3)
      i += 1
    }
    val matches = tuples.find(query)
    val listOfMatches = List[DBObject]() ++ matches
    listOfMatches.map(x => WebTuple(x))
  }

  def addSubscription(pattern: WebTuple): String = {
    ""
  }
}

object MongoDBTupleOps extends MongoDBTupleOps {

}

