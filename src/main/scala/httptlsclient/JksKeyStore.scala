package httptlsclient

import java.io.ByteArrayInputStream

case class JksKeyStore(byteArrayInputStream: ByteArrayInputStream, password: String)
