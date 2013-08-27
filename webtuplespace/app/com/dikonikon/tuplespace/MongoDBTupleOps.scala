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
class MongoDBTupleOps extends MongoDBConstants {
  private val conf = MongoDBConfig()
  private val database = MongoConnection(conf.host, conf.port)(conf.dbname)

  def createTuple(webtuple: WebTuple): WebTuple = {
    val mongoDbTuple = toMongoTuple(webtuple)
    val tuples = database(_tuples)
    tuples += mongoDbTuple
    webtuple.id = mongoDbTuple._id.get.toString
    webtuple
  }

  def findMatchingTuples(pattern: WebTuple, remove: Boolean = false): List[WebTuple] = {
    val tuples = database(_tuples)
    val query = toMongoQuery(pattern)
    val matches = tuples.find(query)
    val listOfMatches = List[DBObject]() ++ matches
    if (remove) tuples.remove(query, WriteConcern.FsyncSafe)
    listOfMatches.map(x => WebTuple(x))
  }

  private def readTuplesWithIds(ids: MongoDBList): List[WebTuple] = {
    val tuples = database(_tuples)
    val query = _id $in ids
    val result = new ListBuffer[WebTuple]
    val cursor = tuples.find(query)
    cursor.foreach(x => result += WebTuple(x))
    result.toList
  }

  private def createTuplesIdsOnly(ids: MongoDBList): List[WebTuple] = {
    List[WebTuple]() ++
      ids.map(x => WebTuple(x.asInstanceOf[ObjectId].toString))
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
      val element = MongoDBObject(_type -> e._1, _value -> e._2, _hash -> e._3)
      val key = "e" + i.toString
      tupleObj += key -> element
      i = i + 1
      shardHashTarget ++ e._3
    }
    if (!isPattern) tupleObj += _shardKey -> toHash(shardHashTarget)
    tupleObj
  }

  private def toMongoQuery(pattern: WebTuple): MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    for (i <- 1 to pattern.internal.size) {
      builder += (_e + i + _dothash -> pattern.internal(i - 1)._3)
    }
    builder.result()
  }

  private def toMongoQuery(pattern: DBObject): MongoDBObject = {
    val builder = MongoDBObject.newBuilder

    for (i <- 1 to pattern.size) {
      val key = _e + i
      val value: Array[Byte] = (pattern.as[DBObject](key)).as[Array[Byte]]("hash")
      builder += (key + _dothash -> value)
    }
    builder.result()
  }
}

object MongoDBTupleOps extends MongoDBTupleOps {

}

trait MongoDBConstants {
  val _pattern = "pattern"
  val _tuples = "tuples"
  val _e = "e"
  val _hash = "hash"
  val _dothash = ".hash"
  val _type = "type"
  val _value = "value"
  val _id = "_id"
  val _shardKey = "shardKey"
}


