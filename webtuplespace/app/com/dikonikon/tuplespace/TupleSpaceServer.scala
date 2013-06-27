package com.dikonikon.tuplespace

/**
 * The intention is that this interface should be wrapped by a client side proxy to provide
 * the features of the tuplespace, including asynchronous notification of the arrival of
 * tuples matching a subscription's pattern, but polling the server.
 * The two functions <code>notifications</code> and <code>notificationHistory</code> should
 * be used together to provide reliable delivery of notifications.
 * If a request for notifications fails then it should be retried in conjunction by a request to
 * notificationHistory. notificationHistory can be retried multiple times on its own.
 * Once a successful response to both requests is received the notification response should be either
 * equal to or a subset of the response to notificationHistory. If this is the case then the
 * notificationHistory response should be passed to the client of the proxy and a request made to
 * notificationsReceived, which will clear the history.
 * notificationsReceived may also be retried in isolation but only at the end of successful completion
 * of this sequence.
 */
trait TupleSpaceServer {
  def take(pattern: WebTuple): List[WebTuple]
  def read(pattern: WebTuple): List[WebTuple]
  def write(tuple: WebTuple): WebTuple
  def startSession(): String
  def endSession(sessionId: String)
  def subscribe(pattern: WebTuple, sessionId: String)
  def unsubscribe(subscriptionId: String, sessionId: String)
  def notifications(sessionId: String): List[(WebTuple, List[WebTuple])]
  def notificationHistory(sessionId: String): List[(WebTuple,  List[WebTuple])]
  def notificationsReceived(sessionId: String)
}



object TupleSpaceServer {

  abstract class AbstractTupleSpaceServerImpl {
    val server: TupleSpaceServer
  }

  def apply() = {

  }
}

