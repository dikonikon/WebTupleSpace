

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
