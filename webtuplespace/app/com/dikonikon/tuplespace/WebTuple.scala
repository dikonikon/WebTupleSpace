/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 25/04/13
 * Time: 19:26
 */

package com.dikonikon.tuplespace


import scala.xml.NodeSeq
import com.mongodb.casbah.commons.MongoDBObject
import scala.collection.mutable.ListBuffer
import com.mongodb.BasicDBObject

/**
 * WebTuple transforms a variety of forms of inputs into the form required to com.dikonikon.tuplespace.store it take WebTupleSpace.
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
 * ??? why unencode the values, either take the JSON string or the XML? They are used only to match on...
 *
 * significant or preserved, so if it is supported as a payload the ordering will need to be explicitly
 * represented take the data structure.
 */
trait WebTuple extends {
  var id: String
  def internal: List[(String, String, Array[Byte])]
  override def equals(obj: Any): Boolean = {
    val that = obj.asInstanceOf[WebTuple]
    that.internal == this.internal
  }
  override def toString = {
    val s = new StringBuilder()
    s.append("(")
    this.internal.foreach(x => s.append("(").append(x._1).append(",").append(x._2).append(",")
      .append(x._3.toString).append(")"))
    s.append(")")
    s.toString()
  }
}

object WebTuple {

  class XMLWebTuple (var original: NodeSeq, override var id: String = null) extends WebTuple {
    override def internal = content
    val content = {
        List[(String, String, Array[Byte])]() ++ (original \\ "Element").map(x => {
          val t = (x \\ "Type").text
          val v = (x \\ "Value").text
          (t, v, toHash(t.getBytes ++ v.getBytes))
        })
    }


  }

  class MongoDBObjectWebTuple(var original: MongoDBObject) extends WebTuple {
    override def internal = content
    private val content = {
      var i = 1
      val l = ListBuffer[(String, String, Array[Byte])]()
      while(original.contains("e" + i)) {
        val e = original.as[BasicDBObject]("e" + i)
        val nextWebTupleElement = (e.get("type").asInstanceOf[String], e.get("value").asInstanceOf[String],
          e.get("hash").asInstanceOf[Array[Byte]])
        l += nextWebTupleElement
        i = i + 1
      }
      l.toList
    }
    override var id: String = original._id.get.toString
  }

  def apply(tuple: NodeSeq): WebTuple = new XMLWebTuple(tuple)
  def apply(tuple: MongoDBObject): WebTuple = new MongoDBObjectWebTuple(tuple)
}
