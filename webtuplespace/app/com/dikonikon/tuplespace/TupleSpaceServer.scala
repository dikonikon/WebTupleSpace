package com.dikonikon.tuplespace

import scala.xml.Elem
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.query.Imports._

trait TupleSpaceServer {
  def in(pattern: WebTuple)
  def out(tuple: WebTuple): WebTuple
  def start(pattern: WebTuple): String
  def end(sessionId: Long)
}

object TupleSpaceServer {
  def apply() = {

  }
}

