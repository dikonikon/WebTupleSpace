

/**
 * See: https://github.com/dikonikon
 * This is open source software provided under the license
 * at the root directory of the project
 * Date: 17/06/13
 * Time: 13:40
 */


import play.api._
import play.api.mvc._
import play.api.mvc.Results._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("WebTupleSpaceServer starting...")
  }

  override def onStop(app: Application) {
    Logger.info("WebTupleSpaceServer shutting down...")
  }

  // todo: this shouldn't be a web page - look into what this response should be, other than 500
  override def onError(request: RequestHeader, ex: Throwable) = {
    InternalServerError(
      views.html.errorpage(ex)
    )
  }

}
