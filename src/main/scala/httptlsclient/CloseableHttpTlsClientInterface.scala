package httptlsclient

import scala.concurrent.Future
import scala.util.Try

trait CloseableHttpTlsClientInterface {
  def post(url: String, payload: String): Future[Int]
  def close: Try[Unit]
}
