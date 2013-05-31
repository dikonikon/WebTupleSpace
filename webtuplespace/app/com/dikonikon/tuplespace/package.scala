package com.dikonikon

import java.security.MessageDigest


/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 13/05/13
 * Time: 09:08
 */
package object tuplespace {
  def genSubId: String = ""
  def toHash(x: Array[Byte]): Array[Byte] = {
    val m = MessageDigest.getInstance("SHA-256")
    m.update(x)
    m.digest()

  }

}
