package library

import scala.concurrent.Future

import controllers.Conference
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class TLSFilter extends Filter {

  def apply(nextFilter: RequestHeader => Future[SimpleResult])
           (requestHeader: RequestHeader): Future[SimpleResult] = {
    if(secure(requestHeader) || !Conference.useHTTPS)
      nextFilter(requestHeader).map(_.withHeaders("Strict-Transport-Security" -> "max-age=31536000; includeSubDomains"))
    else
      Future.successful(Results.MovedPermanently("https://" + requestHeader.host + requestHeader.uri))
  }

  private def secure(request: RequestHeader) = {
    val protoO = request.headers.get("X-Forwarded-Proto") orElse request.headers.get("X-Scheme")
    protoO.map(_ == "https").getOrElse(false)
  }
}