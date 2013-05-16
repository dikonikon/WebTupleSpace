package com.dikonikon.tuplespace

/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 13/05/13
 * Time: 08:52
 */
class SubscriptionList(subscriptions: List[Subscription]){
  private def genSubId: String = ""
  def +(pattern: WebTuple): String = {
    subscriptions.find(x => x.forPattern(pattern)) match {
      case Option[Subscription](s) => { val id = genSubId; }
    }
  }
}
