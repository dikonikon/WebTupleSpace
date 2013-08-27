package com.dikonikon.tuplespace


trait TupleSpaceServer {
  def take(pattern: WebTuple): List[WebTuple]
  def read(pattern: WebTuple): List[WebTuple]
  def write(tuple: WebTuple): WebTuple
}

object TupleSpaceServer {

  abstract class AbstractTupleSpaceServerImpl {
    val server: TupleSpaceServer
  }

  def apply() = {

  }
}

