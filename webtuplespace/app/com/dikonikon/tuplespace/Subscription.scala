package com.dikonikon.tuplespace

/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 13/05/13
 * Time: 08:40
 */
class Subscription(pattern: WebTuple, var subscribers: List[String]) {
  def forPattern(pattern: WebTuple):Boolean = this.pattern == pattern
  def +(): String = {
    val id = genSubId
    subscribers = id :: subscribers
    id
  }
}