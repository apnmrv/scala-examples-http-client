package httptlsclient

import java.io.ByteArrayInputStream

import com.typesafe.config.Config

case class HttpClientConfig(postUrl: String, httpTlsConnectionContext: HttpTlsConnectionContext)
object HttpClientConfig {
  def apply(conf: Config, identityStoreByteArray: Array[Byte], trustedStoreByteArray: Array[Byte]): HttpClientConfig = {
    val postUrl: String = conf.getString("post.data.url")
    val tokenRequestUrl: String = conf.getString("token.request.url")
    val tokenRequestUsername: String = conf.getString("token.request.username")
    val tokenRequestPassword: String = conf.getString("token.request.password")
    val tokenRequestHeaderAuthValue: String = conf.getString("token.request.header.auth.value")
    val identityStorePassword: String = conf.getString("ssl.identity.password")
    val trustedStorePassword: String = conf.getString("ssl.trusted.password")

    val identityStoreInputStream: ByteArrayInputStream = new ByteArrayInputStream(identityStoreByteArray)
    val trustedStoreInputStream: ByteArrayInputStream = new ByteArrayInputStream(trustedStoreByteArray)

    val httpTlsConnectionContext: HttpTlsConnectionContext = HttpTlsConnectionContext(
      TokenRequestContext(
        tokenRequestUrl,
        tokenRequestUsername,
        tokenRequestPassword,
        tokenRequestHeaderAuthValue
      ),
      TlsContext(
        identityStoreInputStream, identityStorePassword,
        trustedStoreInputStream, trustedStorePassword
      )
    )
    HttpClientConfig(postUrl, httpTlsConnectionContext)
  }
}
