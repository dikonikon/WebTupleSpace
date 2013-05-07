package com.dikonikon.tuplespace

import scala.xml.Elem
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.query.Imports._

trait TupleSpaceServer {
  def in(tuple: WSTuple)
  def out(pattern: WSTuple)
}

object TupleSpaceServer {
  def apply() = {

  }
}

