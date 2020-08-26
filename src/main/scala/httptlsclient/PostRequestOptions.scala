package httptlsclient

import org.apache.http.client.methods.CloseableHttpResponse

case class PostRequestOptions(
                               urlString: String,
                               headers: Option[Map[String, String]] = None,
                               params: Option[Map[String, String]] = None,
                               payload: Option[String] = None,
                               responseDataExtractor: Option[Function[CloseableHttpResponse, Any]] = None)