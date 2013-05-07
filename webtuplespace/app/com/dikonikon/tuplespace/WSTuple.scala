/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 25/04/13
 * Time: 19:26
 */

package com.dikonikon.tuplespace

import java.io.{ObjectOutputStream, ByteArrayOutputStream}
import java.security.MessageDigest

import sun.misc.BASE64Decoder

import scala.xml.Elem

/**
 * WSTuple transforms a variety of forms of inputs into the form required to com.dikonikon.tuplespace.store it in WebTupleSpace.
 * Forms supported:
 * A Scala Product (which includes Scala Tuples)
 * An XML document with the following structure:
 * <code>
 * <Tuple>
 *   <Element>
 *     <Type>string</Type>
 *     <Value>value as base 64 encoded byte array</Value>
 *   </Element>
 *   ...
 * </Tuple>
 * </code>
 * JSON strings cannot be used in their vanilla form because the ordering of elements in a JSON string is not
 * significant or preserved, so if it is supported as a payload the ordering will need to be explicitly
 * represented in the data structure.
 */
trait WSTuple[T] {
  var id: String = null
  var internal: List[(String, Array[Byte], Array[Byte])] = Nil
  var original: T
}

object WSTuple {

  private val decoder = new BASE64Decoder()
  private def decode(data: String): Array[Byte] = {
    decoder.decodeBuffer(data)
  }

  private def toBytes(x: Any): Array[Byte] = {
    val t = x.asInstanceOf[scala.Serializable]
    val buffer = new ByteArrayOutputStream()
    val out = new ObjectOutputStream(buffer)
    out.writeObject(t)
    buffer.toByteArray
  }

  private def toHash(x: Array[Byte]): Array[Byte] = {
    val m = MessageDigest.getInstance("SHA-256")
    m.update(x)
    m.digest()
  }

  class ProductWSTuple[T <: Product](override var original: T) extends WSTuple[T] {
    internal = {
      List[(String, Array[Byte], Array[Byte])]() ++ original.productIterator.map(x => {
          val v = toBytes(x)
          val h = toHash(v)
          (x.getClass.toString, v, h)})
    }
  }

  class XMLWSTuple[T <: Elem] (override var original: T) extends WSTuple[T] {
    internal = {
        List[(String, Array[Byte], Array[Byte])]() ++ (original \\ "Element").map(x => {
          val t = x \\ "Type"
          val v = decode ((x \\ "Value").text)
          (t.text, v, toHash(v))
        })
    }
  }

  def apply(tuple: Product): WSTuple[Product] = new ProductWSTuple[Product](tuple)
  def apply(tuple: Elem): WSTuple[Elem] = new XMLWSTuple[Elem](tuple)
}
