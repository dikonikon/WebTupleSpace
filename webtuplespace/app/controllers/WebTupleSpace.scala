package controllers

import play.api._
import play.api.mvc._
import play.api.Logger
import com.dikonikon.tuplespace.WebTuple
import com.dikonikon.tuplespace.Server
import com.dikonikon.tuplespace.akka.SimpleOutActor
import play.api.libs.iteratee.{Iteratee, Concurrent}
import akka.actor.{ActorRef, Props, ActorSystem}



// todo: write interceptor to catch any exceptions and display 500 message
// todo: partially done: interceptor wraps as html, want an xml restful style response

object WebTupleSpace extends Controller {
  val system = ActorSystem("SessionManagement")



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

  def session = WebSocket.using[String] { request => {
      Logger.debug("received WebSocket request")
      var outActor: ActorRef = null
      val out = Concurrent.unicast[String]( channel => {
          Logger.debug("getting new Actor")
          outActor = system.actorOf(Props[SimpleOutActor[String]])
          Logger.debug("sending channel")
          outActor ! channel
          },
        () => Unit,
        (_, _) => Unit)
      val in = Iteratee.foreach[String]( x => {println(x); outActor ! "--" + x + "--" } ).mapDone { _ =>
        println("Disconnected")
      }
      (in, out)
    }
  }

  private def toXML(list: List[(WebTuple, List[WebTuple])]) =

      <Subscriptions>
        {list.map(x =>
          <Subscription>
            <Pattern>{x._1.toXML}</Pattern>
            <Notifications>{x._2.map(y => y.toXML)}</Notifications>
          </Subscription>)}
      </Subscriptions>
}
