/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 25/04/13
 * Time: 19:26
 */

package com.dikonikon.tuplespace

import java.security.MessageDigest

import scala.xml.NodeSeq

/**
 * WebTuple transforms a variety of forms of inputs into the form required to com.dikonikon.tuplespace.store it in WebTupleSpace.
 * Forms supported:
 * An XML document with the following structure:
 * <code>
 * <Tuple>
 *   <Element>
 *     <Type>string</Type>
 *     <Value>value as base 64 encoded String</Value>
 *   </Element>
 *   ...
 * </Tuple>
 * </code>
 * JSON strings are of the following format:
 * {
 *  <other headers...tbc>
 *  data: [{type: string, value: value as base 64 encoded String}, ...]
 * }
 *
 * If the value is flagged as encoded it is converted to a byte array.
 *
 * ??? why unencode the values, either in the JSON string or the XML? They are used only to match on...
 *
 * significant or preserved, so if it is supported as a payload the ordering will need to be explicitly
 * represented in the data structure.
 */
trait WebTuple extends {
  var id: String = null
  var internal: List[(String, String, Array[Byte])] = Nil
  override def equals(obj: Any): Boolean = {
    val that = obj.asInstanceOf[WebTuple]
    that.internal == this.internal
  }
}

object WebTuple {

  private def toHash(x: Array[Byte]): Array[Byte] = {
    val m = MessageDigest.getInstance("SHA-256")
    m.update(x)
    m.digest()
  }

  class XMLWebTuple (var original: NodeSeq) extends WebTuple {
    internal = {
        List[(String, String, Array[Byte])]() ++ (original \\ "Element").map(x => {
          val t = x \\ "Type"
          val v = (x \\ "Value").text
          (t.text, v, toHash(v.getBytes))
        })
    }
  }

  def apply(tuple: NodeSeq): WebTuple = new XMLWebTuple(tuple)
}
