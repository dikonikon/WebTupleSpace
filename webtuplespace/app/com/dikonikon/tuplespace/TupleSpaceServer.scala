package com.dikonikon.tuplespace

import scala.xml.Elem
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.query.Imports._

trait TupleSpaceServer {
  def in(tuple: WebTuple)
  def out(pattern: WebTuple)
}

object TupleSpaceServer {
  def apply() = {

  }
}

