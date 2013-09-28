

/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 23/08/13
 * Time: 13:36
 */

package com.dikonikon.tuplespace.akka

import akka.actor.Actor
import play.api.libs.iteratee.Concurrent.Channel
import play.api.Logger
import com.dikonikon.tuplespace.WebTuple
import scala.xml.{ NodeSeq }

class SessionInActor extends Actor {
  def receive = {
    case "bibble" =>
  }
}

//class SessionOutActor extends Actor {
//  def receive = {
//    case e: Enumerator =>
//  }
//}

class SimpleOutActor[T] extends Actor {
  var channel: Channel[T] = null

  def receive = {
    case c: Channel[T] => {
      Logger.debug("actor received channel")
      channel = c
    }

    case s: T => {
      Logger.debug("actor received: " + s)
      channel.push(s)
    }
  }
}

class SubscriptionActor[T] extends Actor {
  var channel: Channel[T] = null
  val me = "SubscriptionActor: "
  var pattern: WebTuple = null

  def receive = expectChannel orElse unexpected

  def expectChannel: Receive = {
    case c: Channel[T] => {
      Logger.debug(me + "received Channel")
      context.become(expectSubscription orElse unexpected)
    }
  }

  def expectSubscription: Receive = {
    case <subscription>{content}</subscription> => {
      sendCurrentStateStart
      sendCurrentStateMatches(content)
      sendEndCurrentState
      context.become(expectNewTuple orElse unexpected)
    }
  }

  def sendCurrentStateStart: Unit = {

  }

  def sendCurrentStateMatches(pattern: NodeSeq): Unit = {

  }

  def sendEndCurrentState: Unit = {

  }

  def expectNewTuple: Receive = {

  }

  def unexpected: Receive = {
    case x => {
      Logger.error(me + "received: " + x + " but don't know what to do with it")
      Logger.error(me + "type of message received was: " + x.getClass.toString)
    }
  }
}
