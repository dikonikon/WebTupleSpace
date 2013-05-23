package com.dikonikon.tuplespace.test

/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 22/05/13
 * Time: 14:25
 */

import com.dikonikon.tuplespace.WebTuple
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

import com.dikonikon.tuplespace.MongoDBTupleOps._



class MongoDBTupleOpsComponentSpec extends Specification {

  "findMatchingTuples" should {

    "respond with one tuple" in  {
      cleanTestDB
      createTestTuples
      val pattern = WebTuple(<Tuple><Element><Type>String</Type><Value>avalue1</Value></Element></Tuple>)
      val result: List[WebTuple] = findMatchingTuples(pattern)
      result.size should be equalTo(1)
      cleanTestDB
    }
  }

}
