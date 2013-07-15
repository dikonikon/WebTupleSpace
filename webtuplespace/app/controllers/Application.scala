package controllers

import play.api.Logger
import play.api.mvc._
import com.dikonikon.tuplespace.MongoDBTupleOps._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.WriteConcern

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def test = Action {
    Ok(views.html.testrunner())
  }

  def reset = Action {
    Logger.info("request to reset WebTupleSpace received")
    val tuples = db("tuples")
    val sessions = db("sessions")
    tuples.remove(MongoDBObject(), WriteConcern.FsyncSafe)
    sessions.remove(MongoDBObject(), WriteConcern.FsyncSafe)
    Logger.info("WebTupleSpace is reset")
    Ok(<Result>TupleSpace Reset</Result>).as("text/xml")
  }
}