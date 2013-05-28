package com.dikonikon.tuplespace

import scala.xml.Elem
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.query.Imports._

trait TupleSpaceServer {
  def take(pattern: WebTuple): WebTuple
  def read(pattern: WebTuple): List[WebTuple]
  def write(tuple: WebTuple): WebTuple
  def startSession(): String
  def endSession(sessionId: String)
  def subscribe(pattern: WebTuple, sessionId: String): String
  def unsubscribe(subscriptionId: String, sessionId: String)
}



object TupleSpaceServer {
  def apply() = {

  }
}

