package controllers

import play.api._
import play.api.mvc._

object WebTupleSpace extends Controller {
  def out = Action {
    request =>
      // extract tuple data and pass it to tuple space to add
      Ok("working on it!").as("application/xml")

  }

  def in = Action {
    request => {
      Ok("not implemented").as("application/xml")
    }
  }

}
