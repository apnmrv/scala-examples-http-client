package httptlsclient

case class TokenRequestContext(url: String,
                               username: String,
                               password: String,
                               authHeaderValue: String)