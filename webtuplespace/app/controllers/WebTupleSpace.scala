package controllers

import play.api._
import play.api.mvc._
import com.dikonikon.tuplespace.WebTuple

object WebTupleSpace extends Controller {
  /**
   * puts a tuple into the TupleSpace
   * @return
   */
  def out = Action {
    request => {
      val tupleDoc = request.body.asXml
      val wsTuple = WebTuple(tupleDoc.get)
      Ok("working on it!").as("application/xml")
    }
  }

  def in = Action {
    request => {
      Ok("not implemented").as("application/xml")
    }
  }

}
