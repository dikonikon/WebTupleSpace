package com.dikonikon.tuplespace.test

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
    "when sent a valid Tuple, with objectid defined return a success response" in {
      running(FakeApplication()) {
        cleanTestDB
        val request = FakeRequest(PUT, "/webtuplespace/write").
          withXmlBody(<Tuple><Element><Type>String</Type> <Value>aldjsflajlajs</Value></Element></Tuple>).
          withHeaders(("Content-Type", "text/xml"))
        val response = route(request).get
        //print(contentAsString(response))
        status(response) must equalTo(OK)
        contentType(response) must beSome.which(_ == "text/xml")
        contentAsString(response) must contain ("<Status>Success</Status>")
        cleanTestDB
      }
    }

    "given an initially empty database\nwhen two tuples are added and then a matching pattern used in read, the same two tuples should be returned" in {
      running(FakeApplication()) {
        cleanTestDB
        val request = FakeRequest(PUT, "/webtuplespace/write").
          withXmlBody(<Tuple><Element><Type>String</Type> <Value>aldjsflajlajs</Value></Element></Tuple>).
          withHeaders(("Content-Type", "text/xml"))
        // add once
        var response = route(request).get
        status(response) must equalTo(OK)
        // add again
        response = route(request).get
        status(response) must equalTo(OK)

        // request using matching pattern
        val readRequest = FakeRequest(PUT, "/webtuplespace/read").
          withXmlBody(<Tuple><Element><Type>String</Type> <Value>aldjsflajlajs</Value></Element></Tuple>).
          withHeaders(("Content-Type", "text/xml"))
        val readResponse = route(readRequest).get

        // check read response
        status(readResponse) must equalTo(OK)
        contentType(readResponse) must beSome.which(_ == "text/xml")
        val responseBody = contentAsString(readResponse)
        responseBody must contain("<Status>Success</Status>")
        val xml = XML.loadString(responseBody)
        print(xml.toString())
        (xml \\ "Tuple").size must equalTo(2)
        cleanTestDB
      }
    }
  }
}