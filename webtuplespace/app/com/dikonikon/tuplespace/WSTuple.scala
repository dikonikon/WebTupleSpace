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
import org.json4s._


/**
 * WSTuple transforms a variety of forms of inputs into the form required to store it in WebTupleSpace.
 * Forms supported:
 * A Scala Product (which includes Scala Tuples)
 * A JSON string representation with the following structureL
 * {
 *    "string": "<value as stringified byte array>",
 *    ...
 * }
 *
 * An XML document with the following structure:
 * <code>
 * <Tuple>
 *   <Field>
 *     <Type>string</Type>
 *     <Value>value as stringified byte array</Value>
 *     ...
 *   </Field>
 * </Tuple>
 * </code>
 */
trait WSTuple {
  private var internal: List[(String, Array[Byte], Array[Byte])] = Nil
}

object WSTuple {

  class ProductWSTuple[T](val original: Product) extends WSTuple {

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

    internal = {
      val x = List[(String, Array[Byte], Array[Byte])]()

       x ++ original.productIterator.map(x => {
          val t = toBytes(x)
          val h = toHash(t)
          (x.getClass.toString, t, h)})
    }
  }

  def apply(tuple: Product): WSTuple = new ProductWSTuple(tuple)
}
