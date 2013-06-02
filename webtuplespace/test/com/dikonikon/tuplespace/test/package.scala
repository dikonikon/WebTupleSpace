package com.dikonikon.tuplespace

import com.dikonikon.tuplespace.MongoDBTupleOps._
import com.mongodb.casbah.WriteConcern
import com.mongodb.casbah.commons.MongoDBObject

/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 22/05/13
 * Time: 14:54
 */
package object test {
  def createTestWebTuples {
    val webTuple1 = WebTuple(<Tuple>
      <Element><Type>String</Type><Value>avalue1</Value></Element>
      <Element><Type>Int</Type><Value>AES</Value></Element>
    </Tuple>)
    val webTuple2 = WebTuple(<Tuple>
      <Element><Type>String</Type><Value>avalue2</Value></Element>
      <Element><Type>Int</Type><Value>IBM</Value></Element>
    </Tuple>)
    createTuple(webTuple1)
    createTuple(webTuple2)
  }

  def cleanTestDB {
    val collection = db("tuples")
    collection.underlying.remove(MongoDBObject(), WriteConcern.FsyncSafe)
  }

}
