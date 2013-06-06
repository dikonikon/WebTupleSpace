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
import com.dikonikon.tuplespace.NoSessionFoundException
import com.dikonikon.tuplespace.NoNotificationFound
import com.dikonikon.tuplespace.NoSubscriptionFound
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

  "create Session followed by add Subscription" should {
    "result in a session, subscription and notification" in {
      cleanTestDB
      createTestWebTuples
      val sessionId = createSession()
      addSubscription(testPattern, sessionId)
      val sessions = db("sessions")
      val session = (sessions.findOneByID(new ObjectId(sessionId))).getOrElse(throw NoSessionFoundException())
      val subscriptions = session.get("subscriptions").asInstanceOf[BasicDBList]
      subscriptions.size must equalTo(1)
      val subscription = subscriptions.get(0).asInstanceOf[DBObject]     // todo
      val notifications = subscription.get("notifications").asInstanceOf[BasicDBList]
      notifications.size must equalTo(1)
    }
  }

}
