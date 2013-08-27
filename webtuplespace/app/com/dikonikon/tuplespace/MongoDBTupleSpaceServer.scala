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

  override def write(tuple: WebTuple): WebTuple = {
    createTuple(tuple)
  }

  override def read(pattern: WebTuple): List[WebTuple] = {
    findMatchingTuples(pattern)
  }

  override def take(pattern: WebTuple): List[WebTuple] = {
    findMatchingTuples(pattern, true)
  }
}