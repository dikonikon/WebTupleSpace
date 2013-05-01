package com.dikonikon.tuplespace

trait TupleSpaceServer {
  def in[T](tuple: WSTuple[T])
  def out(pattern: WSTuplePattern)
}
