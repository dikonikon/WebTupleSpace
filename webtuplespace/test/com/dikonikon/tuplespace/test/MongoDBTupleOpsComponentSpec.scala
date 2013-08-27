package com.dikonikon.tuplespace.test

/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 22/05/13
 * Time: 14:25
 */

import com.dikonikon.tuplespace._
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

import com.dikonikon.tuplespace.MongoDBTupleOps._
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.{MongoDBObject, MongoDBList}
import com.mongodb.{BasicDBList, DBObject}


class MongoDBTupleOpsComponentSpec extends Specification {

  "findMatchingTuples" should {

    "respond with one tuple" in  {
      cleanTestDB
      createTestWebTuples
      val pattern = testPattern
      val result: List[WebTuple] = findMatchingTuples(pattern)
      result.size should be equalTo(1)
      cleanTestDB
    }
  }
}
