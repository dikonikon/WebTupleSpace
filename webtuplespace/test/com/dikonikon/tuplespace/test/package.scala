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

//////////////////////////////////////////////////
// Verbs used to do set up and tear down for tests
//////////////////////////////////////////////////

package object test {
  def createTestWebTuples {
    createTuple(testTuples._1)
    createTuple(testTuples._2)
  }

  val testPattern = WebTuple(<Tuple><Element><Type>String</Type><Value>avalue1</Value></Element></Tuple>)

  val testTuples = (WebTuple(<Tuple>
    <Element><Type>String</Type><Value>avalue1</Value></Element>
    <Element><Type>Int</Type><Value>AES</Value></Element>
  </Tuple>), WebTuple(<Tuple>
    <Element><Type>String</Type><Value>avalue2</Value></Element>
    <Element><Type>Int</Type><Value>IBM</Value></Element>
  </Tuple>))

  val testEquivalentTuples = (WebTuple(<Tuple>
    <Element><Type>String</Type><Value>avalue1</Value></Element>
    <Element><Type>Int</Type><Value>AES</Value></Element>
  </Tuple>), WebTuple(<Tuple>
    <Element><Type>String</Type><Value>avalue2</Value></Element>
    <Element><Type>Int</Type><Value>IBM</Value></Element>
  </Tuple>))

  val similarTestTuples = (WebTuple(<Tuple>
    <Element><Type>String</Type><Value>avalue1</Value></Element>
    <Element><Type>Int</Type><Value>AES</Value></Element>
  </Tuple>), WebTuple(<Tuple>
    <Element><Type>String</Type><Value>avalue1</Value></Element>
    <Element><Type>Int</Type><Value>IBM</Value></Element>
  </Tuple>))

  def cleanTestDB {
    val tuples = db("tuples")
    val sessions = db("sessions")
    tuples.remove(MongoDBObject(), WriteConcern.FsyncSafe)
    sessions.remove(MongoDBObject(), WriteConcern.FsyncSafe)
  }

}
