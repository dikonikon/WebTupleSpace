package com.dikonikon.tuplespace

import scala.xml.Elem
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.query.Imports._

trait TupleSpaceServer {
  def take(pattern: WebTuple): WebTuple
  def read(pattern: WebTuple): List[WebTuple]
  def write(tuple: WebTuple): WebTuple
  def subscribe(pattern: WebTuple): String
  def unsubscribe(sessionId: Long)
}



object TupleSpaceServer {
  def apply() = {

  }
}

