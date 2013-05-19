package com.dikonikon.tuplespace

import scala.xml.Elem
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.query.Imports._

trait TupleSpaceServer {
  def takeOne(pattern: WebTuple)
  def put(tuple: WebTuple): WebTuple
  def subscribe(pattern: WebTuple): String
  def unsubscribe(sessionId: Long)
}



object TupleSpaceServer {
  def apply() = {

  }
}

