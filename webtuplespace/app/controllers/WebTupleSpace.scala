package controllers

import play.api._
import play.api.mvc._
import com.dikonikon.tuplespace.WebTuple
import com.dikonikon.tuplespace.Server



// todo: write interceptor to catch any exceptions and display 500 message
// todo: partially done: interceptor wraps as html, want an xml restful style response

object WebTupleSpace extends Controller {
  /**
   * puts a webtuple into the TupleSpace
   * @return
   */
  def take = Action {
    request => {
      val tupleDoc = request.body.asXml
      val wsTuple = WebTuple(tupleDoc.get)

      Ok("working on it!").as("application/xml")
    }
  }

  def read = Action(parse.xml) {
    request => {
      val tupleDoc = request.body
      Logger.debug("read request body: " + request.body.toString)
      val wsTuple = WebTuple(tupleDoc)
      val result = Server.read(wsTuple)
      val response =
        <Response>
          <Status>Success</Status>
          {result.map(x => x.toXML)}
        </Response>
      Logger.debug("read response: " + response.toString)
      Ok(response).as("text/xml")
    }
  }

  def write = Action(parse.xml) {
    request => {
      val tupleDoc = request.body
      Logger.debug("write request body: " + request.body.toString)
      val wsTuple = WebTuple(tupleDoc)
      val tupleResult = Server.write(wsTuple)
      val response =
        <Response>
          <Status>Success</Status>
          {tupleResult.toXML}
        </Response>
      Logger.debug("write response: " + response.toString)
      Ok(response).as("text/xml")
    }
  }

  def startSession = Action {
    request => {
      Ok("not implemented").as("application/xml")
    }
  }

  def endSession = Action {
    request => {
      Ok("not implemented").as("application/xml")
    }
  }

  def subscribe = Action {
    request => {
      Ok("not implemented").as("application/xml")
    }
  }

  def unsubscribe = Action {
    request => {
      Ok("not implemented").as("application/xml")
    }
  }

  def notifications = Action {
    request => {
      Ok("not implemented").as("application/xml")
    }
  }

  def notificationHistory = Action {
    request => {
      Ok("not implemented").as("application/xml")
    }
  }

  def notificationsReceived = Action {
    request => {
      Ok("not implemented").as("application/xml")
    }
  }

}
