package com.dikonikon.tuplespace


trait TupleSpaceServer {
  def take(pattern: WebTuple): List[WebTuple]
  def read(pattern: WebTuple): List[WebTuple]
  def write(tuple: WebTuple): WebTuple
  def startSession(): String
  def endSession(sessionId: String)
  def subscribe(pattern: WebTuple, sessionId: String)
  def unsubscribe(subscriptionId: String, sessionId: String)
}



object TupleSpaceServer {
  def apply() = {

  }
}

