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
        val request = FakeRequest(PUT, "/webtuplespace/write").
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
        responseBody must contain("<Tuples>")
        val xml = XML.loadString(responseBody)
        print(xml.toString())
        (xml \\ "Tuple").size must equalTo(2)
        cleanTestDB
      }
    }

    """given two tuples in the database
      |and a session and subscription that matches them both
      |two notifications should be returned to the client
      |and after they have been returned there should be no
      |current notifications and two notifications in history.
      |After sending notificationsreceived the notification
      |history should be empty.
    """ in {
      running(FakeApplication()) {
        cleanTestDB
        // add two tuples to db
        val tupleWriteRequest1 = FakeRequest(PUT, "/webtuplespace/write").
          withXmlBody(<Tuple>
          <Element><Type>String</Type><Value>avalue1</Value></Element>
          <Element><Type>Int</Type><Value>AES</Value></Element>
        </Tuple>).
          withHeaders(("Content-Type", "text/xml"))
        val tupleWriteResponse1 = route(tupleWriteRequest1).get
        status(tupleWriteResponse1) must equalTo(OK)

        val tupleWriteRequest2 = FakeRequest(PUT, "/webtuplespace/write").
          withXmlBody(<Tuple>
          <Element><Type>String</Type><Value>avalue1</Value></Element>
          <Element><Type>Int</Type><Value>IBM</Value></Element>
        </Tuple>).
          withHeaders(("Content-Type", "text/xml"))
        val tupleWriteResponse2 = route(tupleWriteRequest2).get
        status(tupleWriteResponse2) must equalTo(OK)

        // start a session
        val sessionStartRequest = FakeRequest(GET, "/webtuplespace/start")
        val sessionStartResponse = route(sessionStartRequest).get
        val sessionIdContent = contentAsString(sessionStartResponse)
        val xmlSessionIdContent = XML.loadString(sessionIdContent)
        val sessionId = (xmlSessionIdContent \\ "SessionId").text
        sessionId must beAnInstanceOf[String]

        // add a subscription
        val addSubscriptionRequest = FakeRequest(PUT, "/webtuplespace/subscribe/session/" + sessionId).
          withXmlBody(<Element><Type>String</Type><Value>avalue1</Value></Element>).
          withHeaders(("Content-Type", "text/xml"))
        val addSubscriptionResponse = route(addSubscriptionRequest).get
        status(addSubscriptionResponse) must equalTo(OK)

        // get notifications
        val getNotifsRequest = FakeRequest(GET, "/webtuplespace/notifications/session/" + sessionId)
        val getNotifsResponse = route(getNotifsRequest).get
        status(getNotifsResponse) must equalTo(OK)
        val notificationsContent = contentAsString(getNotifsResponse)
        val xmlNotificationsContent = XML.loadString(notificationsContent)
        (xmlNotificationsContent \\ "NotificationsSet").length must equalTo(1)
        (xmlNotificationsContent \\ "Notifications").length must equalTo(1)
        (xmlNotificationsContent \\ "Subscription").length must equalTo(1)
        (xmlNotificationsContent \\ "Tuple").length must equalTo(3) // the subscription plus two notifications

        // get notifications again - should be empty now - notifications should have been moved to history
        val secondNotifsRequest = FakeRequest(GET, "/webtuplespace/notifications/session/" + sessionId)
        val secondNotifsResponse = route(secondNotifsRequest).get
        status(secondNotifsResponse) must equalTo(OK)
        val secondNotsContent = contentAsString(secondNotifsResponse)
        val secondXmlNotifs = XML.loadString(secondNotsContent)
        (secondXmlNotifs \\ "NotificationsSet").length must equalTo(1)
        (secondXmlNotifs \\ "Notifications").length must equalTo(1)
        (secondXmlNotifs \\ "Subscription").length must equalTo(1)
        (secondXmlNotifs \\ "Tuple").length must equalTo(1) // just the subscription

        // test for notification history
        val notificationHistoryRequest = FakeRequest(GET, "/webtuplespace/notificationhistory/session/" + sessionId)
        val notificationHistoryResponse = route(notificationHistoryRequest).get
        status(notificationHistoryResponse) must equalTo(OK)
        val histContent = contentAsString(notificationHistoryResponse)
        val xmlHistory = XML.loadString(histContent)
        (xmlHistory \\ "NotificationsSet").length must equalTo(1)
        (xmlHistory \\ "Notifications").length must equalTo(1)
        (xmlHistory \\ "Subscription").length must equalTo(1)
        (xmlHistory \\ "Tuple").length must equalTo(3) // the pattern and the two historical matches

        // send notificationsreceived then check history is empty
        val notifsReceivedRequest = FakeRequest(GET, "/webtuplespace/notificationsreceived/session/" + sessionId)
        val notifsReceivedResponse = route(notifsReceivedRequest).get
        status(notifsReceivedResponse) must equalTo(OK)
        val secondNotificationHistoryResponse = route(notificationHistoryRequest).get
        status(secondNotificationHistoryResponse) must equalTo(OK)
        val emptyHistContent = contentAsString(secondNotificationHistoryResponse)
        val xmlEmptyHistCont = XML.loadString(emptyHistContent)
        (xmlEmptyHistCont \\ "NotificationsSet").length must equalTo(1)
        (xmlEmptyHistCont \\ "Notifications").length must equalTo(1)
        (xmlEmptyHistCont \\ "Subscription").length must equalTo(1)
        (xmlEmptyHistCont \\ "Tuple").length must equalTo(1)
        //cleanTestDB
      }
    }
  }
}