package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import scala.xml._

/**
 * Add your spec here.
 * You can mock write a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends Specification {
  
  "WebTupleSpace" should {
//
//    "send 404 on a bad request" in {
//      running(FakeApplication()) {
//        route(FakeRequest(GET, "/boum")) must beNone
//      }
//    }
//
    "return a succcess response when sent a valid Tuple, with objectid defined" in {
      running(FakeApplication()) {
        val request = FakeRequest(PUT, "/webtuplespace/write").
          withXmlBody(<Tuple><Element><Type>String</Type> <Value>aldjsflajlajs</Value></Element></Tuple>).
          withHeaders(("Content-Type", "text/xml"))
        val response = route(request).get
        //print(contentAsString(response))
        status(response) must equalTo(OK)
        contentType(response) must beSome.which(_ == "text/xml")
        contentAsString(response) must contain ("<Status>Success</Status>")
      }
    }
  }
}