package com.dikonikon.tuplespace.test

/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 23/05/13
 * Time: 09:14
 */

import com.dikonikon.tuplespace.WebTuple
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class WebTupleUnitSpec extends Specification {

  "when constructed from valid XML with two Elements and WebTuple" should {
    "contain a list with two three-tuples" in {
      val webTuple = WebTuple(<Tuple><Element><Type>String</Type><Value>avalue1</Value></Element>
      <Element><Type>Int</Type><Value>3</Value></Element></Tuple>)
      val internal = webTuple.internal
      internal.size must equalTo(2)
      val firstType = internal(0)._1
      firstType must equalTo("String")
    }
  }

}
