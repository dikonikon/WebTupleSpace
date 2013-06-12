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
   * The notification history is the list of notifications that TupleSpaceServer thinks it has sent to the
   * client as a result of a call to readNotifications. Since it is possible for the read transaction to succeed
   * and the notifications still to fail to get to the client, the following strategy is employed:
   * - when notifications are read and returned to the client they are, as part of that transaction,
   * appended to the notification history.
   * - if the client experiences a system or communications failure during a read, it can read both the notifications
   * again and the notification history. If this fails again the notification history may be re-read as many times
   * as necessary - it is never deleted until the client calls clearNotificationHistory.
   * - once a call to readNotificationHistory succeeds the client can call clearNotificationHistory to remove it.
   * In this way it is possible that notifications will be received more than once - for example if readNotifications
   * succeeds and readNotificationHistory is subsequently called, but, subject to the transactional integrity of
   * the MongoDB server, notifications should not be lost.
   * @param sessionId
   * @return
   */
  override def readNotificationHistory(sessionId: String): List[(WebTuple,  List[WebTuple])] = {
    List[(WebTuple, List[WebTuple])]()
  }

  /**
   * Removes notifications from the Session that have
   * been previously read
   * @param sessionId
   */
  override def clearNotificationHistory(sessionId: String): Unit = {

  }

}

case class NoSessionFoundException() extends RuntimeException

case class NoSubscriptionFound() extends RuntimeException

case class NoSubscriptionListFound() extends RuntimeException

case class NoNotificationFound() extends RuntimeException
