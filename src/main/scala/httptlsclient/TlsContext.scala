package httptlsclient

import java.io.ByteArrayInputStream

case class TlsContext (identityStore: JksKeyStore, trustedStore: JksKeyStore)

object TlsContext {
  def apply(idStoreByteArrayInputStream: ByteArrayInputStream,
            idStorePass: String,
            trustStoreByteArrayInputStream: ByteArrayInputStream,
            trustStorePass: String): TlsContext = {
    TlsContext(
      JksKeyStore(idStoreByteArrayInputStream, idStorePass),
      JksKeyStore(trustStoreByteArrayInputStream, trustStorePass))
  }
}
