package com.dikonikon.tuplespace

trait TupleSpaceServer {
  def in(tuple: WSTuple)
  def out(pattern: WSTuplePattern)
}
