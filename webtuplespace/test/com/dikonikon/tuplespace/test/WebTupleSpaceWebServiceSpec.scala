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
class WebTupleSpaceWebServiceSpec extends Specification {
  
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
        val request = FakeRequest(POST, "/webtuplespace/write").
          withXmlBody(<Tuple><Element><Type>String</Type> <Value>aldjsflajlajs</Value></Element></Tuple>).
          withHeaders(("Content-Type", "text/xml"))
        val response = route(request).get
        status(response) must equalTo(OK)
        contentType(response) must beSome.which(_ == "text/xml")
        contentAsString(response) must contain ("<Tuple>")
        contentAsString(response) must contain ("<Id>")
        cleanTestDB
      }
    }

    """given an initially empty database
      |when two tuples are added and then a matching pattern used in read,
      |the same two tuples should be returned""" in {
      running(FakeApplication()) {
        cleanTestDB
        val request = FakeRequest(POST, "/webtuplespace/write").
          withXmlBody(<Tuple><Element><Type>String</Type> <Value>aldjsflajlajs</Value></Element></Tuple>).
          withHeaders(("Content-Type", "text/xml"))
        // add once
        var response = route(request).get
        status(response) must equalTo(OK)
        // add again
        response = route(request).get
        status(response) must equalTo(OK)

        // request using matching pattern
        val readRequest = FakeRequest(POST, "/webtuplespace/read").
          withXmlBody(<Tuple><Element><Type>String</Type> <Value>aldjsflajlajs</Value></Element></Tuple>).
          withHeaders(("Content-Type", "text/xml"))
        val readResponse = route(readRequest).get

        // check read response
        status(readResponse) must equalTo(OK)
        contentType(readResponse) must beSome.which(_ == "text/xml")
        val responseBody = contentAsString(readResponse)
        responseBody must contain("<Tuples>")
        val xml = XML.loadString(responseBody)
        println("expecting two tuples:")
        println(xml.toString())
        (xml \\ "Tuple").size must equalTo(2)
        cleanTestDB
      }
    }

    """given two tuples in the db,
      |when a take request is sent with a matching pattern
      |them two tuples are returned,
      |and then a subsequent read using the same pattern will return no tuples
    """ in {
      running(FakeApplication()) {
        cleanTestDB
        addTwoTuples
        // send take request
        val takeRequest = FakeRequest(POST, "/webtuplespace/take").
          withXmlBody(<Tuple><Element><Type>String</Type><Value>avalue1</Value></Element></Tuple>).
          withHeaders(("Content-Type", "text/xml"))
        val takeResponse = route(takeRequest).get
        status(takeResponse) must equalTo(OK)
        val takeContent = contentAsString(takeResponse)
        val xmlTakeContent = XML.loadString(takeContent)
        (xmlTakeContent \\ "Tuples").length must equalTo(1)
        (xmlTakeContent \\ "Tuple").length must equalTo(2)

        // read with same tuple
        val finalReadRequest = FakeRequest(POST, "/webtuplespace/read").
          withXmlBody(<Tuple><Element><Type>String</Type><Value>avalue1</Value></Element></Tuple>).
          withHeaders(("Content-Type", "text/xml"))
        val finalReadResponse = route(finalReadRequest).get
        status(finalReadResponse) must equalTo(OK)
        val finalContent = contentAsString(finalReadResponse)
        val xmlFinalContent = XML.loadString(finalContent)
        (xmlFinalContent \\ "Tuple").length must equalTo(0)
      }
    }
  }

  def addTwoTuples {
    // add two tuples to db
    val tupleWriteRequest1 = FakeRequest(POST, "/webtuplespace/write").
      withXmlBody(<Tuple>
      <Element><Type>String</Type><Value>avalue1</Value></Element>
      <Element><Type>Int</Type><Value>AES</Value></Element>
    </Tuple>).
      withHeaders(("Content-Type", "text/xml"))
    val tupleWriteResponse1 = route(tupleWriteRequest1).get
    status(tupleWriteResponse1) must equalTo(OK)

    val tupleWriteRequest2 = FakeRequest(POST, "/webtuplespace/write").
      withXmlBody(<Tuple>
      <Element><Type>String</Type><Value>avalue1</Value></Element>
      <Element><Type>Int</Type><Value>IBM</Value></Element>
    </Tuple>).
      withHeaders(("Content-Type", "text/xml"))
    val tupleWriteResponse2 = route(tupleWriteRequest2).get
    status(tupleWriteResponse2) must equalTo(OK)
  }
}