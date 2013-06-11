package com.dikonikon.tuplespace

import scala.xml.Elem
import com.mongodb.casbah.Imports._
import java.security.MessageDigest
import javax.xml.ws.WebEndpoint
import com.mongodb.casbah.commons.ValidBSONType.ObjectId

/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 04/05/13
 * Time: 20:52
 */


class MongoDBTupleSpaceServer() extends TupleSpaceServer {

  import MongoDBTupleOps._

  override def write(tuple: WebTuple): WebTuple = {
    createTuple(tuple)
    // todo: does this now trigger a subscription?
  }

  override def read(pattern: WebTuple): List[WebTuple] = {
    findMatchingTuples(pattern)
  }

  override def take(pattern: WebTuple): List[WebTuple] = {
    findMatchingTuples(pattern, true)
  }

  override def startSession(): String = createSession()

  override def endSession(sessionId: String): Unit = {
    deleteSession(sessionId)
  }

  override def subscribe(pattern: WebTuple, sessionId: String): Unit = {
    addSubscription(pattern, sessionId)
  }

  override def unsubscribe(subscriptionId: String, sessionId: String): Unit = {
    throw new NotImplementedError()
  }

  // todo: implement
  override def readNotifications(sessionId: String): List[(WebTuple, List[WebTuple])] = {
    List[(WebTuple, List[WebTuple])]()
  }

  /**
   * Removes notifications from the Session that have
   * been previously read
   * @param sessionId
   */
  override def clearNotifications(sessionId: String): Unit = {

  }

}

case class NoSessionFoundException() extends RuntimeException

case class NoSubscriptionFound() extends RuntimeException

case class NoSubscriptionListFound() extends RuntimeException

case class NoNotificationFound() extends RuntimeException
