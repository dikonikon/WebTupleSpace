package com.dikonikon.tuplespace

import scala.xml.Elem
import com.mongodb.casbah.Imports._
import java.security.MessageDigest
import javax.xml.ws.WebEndpoint
import com.mongodb.casbah.commons.ValidBSONType.ObjectId

/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 04/05/13
 * Time: 20:52
 */


class MongoDBTupleSpaceServer() extends TupleSpaceServer {

  import MongoDBTupleOps._

  override def put(tuple: WebTuple): WebTuple = {
    createTuple(tuple)
    // todo: does this now trigger a subscription?
  }

  // todo:
  override def takeOne(pattern: WebTuple): WebTuple = WebTuple()

  // todo:
  override def subscribe(pattern: WebTuple): String = {
    ""
  }

  override def unsubscribe(sessionId: Long): Unit = {
    throw new NotImplementedError()
  }
}
