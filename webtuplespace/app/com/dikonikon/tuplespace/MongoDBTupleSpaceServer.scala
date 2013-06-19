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

  /**
   * note as a side effect can add a notification to a matching session->subscription. This is done close to
   * but not in the same transaction as the write - it can't really because the operations work on separate
   * collections.
   * To ensure that notifications cannot be lost there is a todo: run a periodic matching process that ensures that
   * any recent writes have authored notifications.
   * @param tuple
   * @return
   */
  override def write(tuple: WebTuple): WebTuple = {
    createTuple(tuple)
    //
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
  override def notifications(sessionId: String): List[(WebTuple, List[WebTuple])] = {
    readNotifications(sessionId)
  }

  /**
   * The notification history is the list of notifications that TupleSpaceServer thinks it has sent to the
   * client as a result of a call to notifications. Since it is possible for the read transaction to succeed
   * and the notifications still to fail to get to the client, the following strategy is employed:
   * - when notifications are read and returned to the client they are, as part of that transaction,
   * appended to the notification history.
   * - if the client experiences a system or communications failure during a read, it can read both the notifications
   * again and the notification history. If this fails again the notification history may be re-read as many times
   * as necessary - it is never deleted until the client calls clearNotificationHistory.
   * - once a call to notificationHistory succeeds the client can call clearNotificationHistory to remove it.
   * In this way it is possible that notifications will be received more than once - for example if notifications
   * succeeds and notificationHistory is subsequently called, but, subject to the transactional integrity of
   * the MongoDB server, notifications should not be lost.
   * @param sessionId
   * @return
   */
  override def notificationHistory(sessionId: String): List[(WebTuple,  List[WebTuple])] = {
    readNotificationHistory(sessionId)
  }

  /**
   * Removes notifications from the Session that have
   * been previously read
   * @param sessionId
   */
  override def notificationsReceived(sessionId: String): Unit = {
    clearNotificationHistory(sessionId)
  }

}

case class NoSessionFoundException() extends RuntimeException

case class NoSubscriptionFound() extends RuntimeException

case class NoSubscriptionListFound() extends RuntimeException

case class NoNotificationFound() extends RuntimeException
