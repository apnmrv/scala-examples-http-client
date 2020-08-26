package httptlsclient

import java.net.Socket
import java.security.KeyStore
import java.util

import javax.net.ssl.SSLContext
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.ssl.{PrivateKeyDetails, PrivateKeyStrategy, SSLContexts}

import scala.util.Try

class SslConnectionSocketFactoryBuilder(ctx: TlsContext) {
  val idStore: JksKeyStore = ctx.identityStore
  val trustStore: JksKeyStore = ctx.trustedStore

  def build(): Try[SSLConnectionSocketFactory] = Try {
    val CERT_ALIAS: String = "client-key-certs"
    System.setProperty("com.sun.security.enableAIAcaIssuers", "true")

    val identityKeyStore: KeyStore = KeyStore.getInstance("jks")
    identityKeyStore.load(idStore.byteArrayInputStream, idStore.password.toCharArray)

    val trustKeyStore: KeyStore = KeyStore.getInstance("jks")
    trustKeyStore.load(trustStore.byteArrayInputStream, trustStore.password.toCharArray)

    val sslContext: SSLContext = SSLContexts.custom
      .loadKeyMaterial(identityKeyStore, // load identity keystore
        idStore.password.toCharArray,
        new PrivateKeyStrategy() {
          override def chooseAlias(aliases: util.Map[String, PrivateKeyDetails], socket: Socket): String = CERT_ALIAS
        })
      .loadTrustMaterial(trustKeyStore, null) // load trust keystore
      .build
    new SSLConnectionSocketFactory(sslContext, Array[String]("TLSv1.1"), null,
      SSLConnectionSocketFactory.getDefaultHostnameVerifier)
  }
}

object SslConnectionSocketFactoryBuilder {
  def apply(ctx: TlsContext): SslConnectionSocketFactoryBuilder = new SslConnectionSocketFactoryBuilder(ctx)
}
