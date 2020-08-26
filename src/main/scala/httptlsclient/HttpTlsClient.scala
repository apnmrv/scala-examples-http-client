package httptlsclient

import java.util

import org.apache.http.NameValuePair
import org.apache.http.client.methods.{CloseableHttpResponse, HttpPost}
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.message.{BasicHeader, BasicNameValuePair}
import org.apache.http.util.EntityUtils
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JValue
import org.json4s.native.JsonParser
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class HttpTlsClient(ctx: HttpTlsConnectionContext) extends CloseableHttpTlsClientInterface {
  val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  private val _tokenRequestHeaders: Map[String, String] = Map(
    "Accept" -> "application/json",
    "Authorization" -> ctx.tokenRequestParams.authHeaderValue,
    "Content-type" -> "application/x-www-form-urlencoded"
  )
  private val _tokenRequestParams: Map[String, String] = Map(
    "grant_type" -> "password",
    "username" -> ctx.tokenRequestParams.username,
    "password" -> ctx.tokenRequestParams.password
  )

  private lazy val _client: CloseableHttpClient =
    HttpClients.custom.setSSLSocketFactory(ctx.sslConnectionSocketFactory).build

  private lazy val tokenReq: HttpPost = createPostRequest(PostRequestOptions(
    ctx.tokenRequestParams.url,
    Option(_tokenRequestHeaders),
    Option(_tokenRequestParams)
  ))

  override def post(url: String, payload: String): Future[Int] = {
    logger.debug("requesting token")
    requestAccessToken(_client, tokenReq)
      .flatMap { token =>
        logger.debug("token received {}", token)
        val postDataReq: HttpPost = createPostRequest(PostRequestOptions(
          url,
          Option(Map(
            "Accept" -> "application/json",
            "Authorization" -> s"Bearer $token",
          )),
          None,
          Option(payload)
        ))
        sendPostData(_client, postDataReq)
      }
  }

  private def createPostRequest(opts: PostRequestOptions): HttpPost = {
    val uriBuilder: URIBuilder = new URIBuilder(opts.urlString)

    opts.params match {
      case Some(ps) =>
        val postParameters: util.ArrayList[NameValuePair] = new util.ArrayList[NameValuePair]
        ps.map { case (k, v) => postParameters.add(new BasicNameValuePair(k, v)) }
        uriBuilder.addParameters(postParameters)
      case None =>
    }

    val request: HttpPost = new HttpPost(uriBuilder.build)

    opts.headers match {
      case Some(hs) =>
        request.setHeaders(hs.map { case (k, v) => new BasicHeader(k, v) }.toArray)
      case None =>
    }

    val entity: StringEntity =
      new StringEntity(
        opts.payload.getOrElse(""),
        ContentType.APPLICATION_JSON)
    request.setEntity(entity)
    request
  }

  private def sendPostData(httpClient: CloseableHttpClient, postRequest: HttpPost): Future[Int] = {
    Future(httpClient.execute(postRequest))
      .andThen {
        case Success(resp) =>
          logger.debug("post request headers: {}", postRequest.getAllHeaders.mkString(";"))
          logger.debug("post request content: {}", EntityUtils.toString(postRequest.getEntity))
          logger.debug("post request success; code : {}", resp.getStatusLine.getStatusCode)
          logger.debug("post request success; reason : {}", resp.getStatusLine.getReasonPhrase)
          logger.debug("post request success; content : {}", EntityUtils.toString(resp.getEntity))
        case Failure(e) =>
          logger.debug("post request failure : {}", e.getMessage)
      }.map(response => response.getStatusLine.getStatusCode)
  }

  private def requestAccessToken(httpClient: CloseableHttpClient, postRequest: HttpPost): Future[String] = {
    logger.debug("token request headers: {}", postRequest.getAllHeaders)
    Future(httpClient.execute(postRequest))
      .andThen {
        case Success(resp) =>
          logger.debug("token request success; code : {}", resp.getStatusLine.getStatusCode)
          logger.debug("token request success; reason : {}", resp.getStatusLine.getReasonPhrase)
        case Failure(e) =>
          logger.debug("token request failure : {}", e.getMessage)
      }
      .flatMap(resp => tokenRequestExtractor(resp))
      .andThen {
        case Success(t) =>
          logger.debug("token extraction success : {}", t)
        case Failure(e) =>
          logger.debug("token extraction failure : {}", e.getMessage)
      }
  }

  private def tokenRequestExtractor(response: CloseableHttpResponse): Future[String] = Future {
    implicit lazy val formats: DefaultFormats.type = DefaultFormats
    val responseString: String = EntityUtils.toString(response.getEntity)
    val responseJson: JValue = JsonParser.parse(responseString)
    (responseJson \\ "access_token").extract[String]
  }

  override def close: Try[Unit] = Try {
    _client.close()
  }
}

object HttpTlsClient {
  def apply(ctx: HttpTlsConnectionContext): HttpTlsClient = new HttpTlsClient(ctx)
}

