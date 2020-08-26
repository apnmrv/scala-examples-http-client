package httptlsclient

import org.apache.http.conn.ssl.SSLConnectionSocketFactory

import scala.util.{Failure, Success}

case class HttpTlsConnectionContext(tokenRequestParams: TokenRequestContext,
                                    sslConnectionSocketFactory: SSLConnectionSocketFactory)

object HttpTlsConnectionContext {
  def apply(tokenRequestParams: TokenRequestContext,
            tlsContext: TlsContext): HttpTlsConnectionContext =
    SslConnectionSocketFactoryBuilder(tlsContext).build() match {
      case Success(sslFactory) => HttpTlsConnectionContext(
        tokenRequestParams,
        sslFactory
      )
      case Failure(e) => throw e
    }
}
