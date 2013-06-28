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
  def take = Action(parse.xml) {
    request => {
      val tupleDoc = request.body
      Logger.debug("take request body: " + tupleDoc)
      val wsTuple = WebTuple(tupleDoc)
      val result = Server.take(wsTuple)
      val response =
        <Tuples>
          {result.map(x => x.toXML)}
        </Tuples>
      Logger.debug("take response: " + response.toString)
      Ok(response).as("text/xml")
    }
  }

  def read = Action(parse.xml) {
    request => {
      val tupleDoc = request.body
      Logger.debug("read request body: " + tupleDoc.toString)
      val wsTuple = WebTuple(tupleDoc)
      val result = Server.read(wsTuple)
      val response =
        <Tuples>
          {result.map(x => x.toXML)}
        </Tuples>
      Logger.debug("read response: " + response.toString)
      Ok(response).as("text/xml")
    }
  }

  def write = Action(parse.xml) {
    request => {
      val tupleDoc = request.body
      Logger.debug("write request body: " + tupleDoc.toString)
      val wsTuple = WebTuple(tupleDoc)
      val tupleResult = Server.write(wsTuple)
      val response = tupleResult.toXML
      Logger.debug("write response: " + response.toString)
      Ok(response).as("text/xml")
    }
  }

  def startSession = Action {
    request => {
      Logger.debug("start session received ")
      val sessionId = Server.startSession()
      val response =
          <SessionId>{sessionId}</SessionId>
      Logger.debug("response is: " + response.toString)
      Ok(response).as("text/xml")
    }
  }

  def endSession = Action {
    request => {
      Ok("not implemented").as("text/xml")
    }
  }

  def subscribe(sessionId: String) = Action(parse.xml) {
    request => {
      val tupleDoc = request.body
      Logger.debug("subscribe request received for: " + sessionId)
      Logger.debug("subscribe request body: " + tupleDoc.toString)
      val wsTuple = WebTuple(tupleDoc)
      Server.subscribe(wsTuple, sessionId)
      Ok
    }
  }

  def unsubscribe = Action {
    request => {
      Ok("not implemented").as("text/xml")
    }
  }

  def notifications(sessionId: String) = Action {
    request => {
      Logger.debug("notifications request received for session: " + sessionId)
      val notifications = Server.notifications(sessionId)
      val response = toXML(notifications)
      Logger.debug("notifications response is: " + response.toString)
      Ok(response).as("text/xml")
    }
  }

  def notificationHistory(sessionId: String) = Action {
    request => {
      Logger.debug("notifications history request received for session: " + sessionId)
      val history = Server.notificationHistory(sessionId)
      val response = toXML(history)
      Logger.debug("notifications history response is: " + response.toString)
      Ok(response).as("text/xml")
    }
  }

  private def toXML(list: List[(WebTuple, List[WebTuple])]) =

      <NotificationsSet>
        {list.map(x =>
          <Notifications>
            <Subscription>
              {x._1.toXML}
            </Subscription>
            {x._2.map(y => y.toXML)}
          </Notifications>)}
      </NotificationsSet>

  def notificationsReceived(sessionId: String) = Action {
    request => {
      Logger.debug("notifications received request received for session: " + sessionId)
      Server.notificationsReceived(sessionId)
      Logger.debug("successfully cleared notification history for session: " + sessionId)
      Ok
    }
  }

}
