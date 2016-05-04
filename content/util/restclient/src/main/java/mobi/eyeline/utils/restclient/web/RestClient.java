package mobi.eyeline.utils.restclient.web;

import mobi.eyeline.utils.restclient.web.Content.DELETE;
import mobi.eyeline.utils.restclient.web.Content.POST;
import mobi.eyeline.utils.restclient.web.Content.PUT;
import mobi.eyeline.utils.restclient.web.Resource.JSONResource;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple and lightweight REST API client.
 *
 * <p>
 *   Supports:
 *   <ul>
 *     <li>GET, POST, PUT and DELETE queries,
 *     <li>Request and response handling using either built-in JSON object model or plain text,
 *     <li>GZIP and uncompressed response types,
 *     <li>Custom headers, timeout adjustment, basic SSL settings.
 *   </ul>
 *
 * </p>
 *
 * <p>
 *   <h1>Examples</h1>
 *
 *   <h2>Example 1: GET</h2>
 *   <pre>
 *     // Execute GET and receive response as JSON object right away, ...
 *     JSONObject rc = new RestClient().json("example.com").object();
 *
 *     // ... then query its' fields.
 *     String rcAttr = rc.getString("value");
 *
 *     // Or get just the raw response.
 *     String plainTextResponse = new RestClient().json("example.com").text();
 *   </pre>
 *
 *   <h2>Example 2: POST</h2>
 *
 *   <pre>
 *     // Construct JSON payload, ...
 *     JSONArray postData = new JSONArray();
 *     postData.put("foo");
 *     postData.put("bar");
 *
 *     // ... and POST it to a target URL.
 *     JSONResource rc = new RestClient().json("example.com", RestClient.post(RestClient.content(postData)));
 *
 *     // Check the response if needed.
 *     JSONObject obj = rc.object();
 *     JSONArray arrat = rc.array();
 *     String plainText = rc.text();
 *   </pre>
 *
 * </p>
 */
public class RestClient {

  private Map<String, String> headers;
  private final Option[] options;

  @SuppressWarnings("WeakerAccess")
  public RestClient(Option... options) {
    this.options = (options == null) ? new Option[0] : options;
    for (Option o : this.options) o.init(this);
  }


  //
  //  Request types.
  //

  @SuppressWarnings({"unused", "WeakerAccess"})
  public JSONResource json(String string) throws IOException {
    return json(URI.create(string));
  }

  @SuppressWarnings({"unused", "WeakerAccess"})
  public JSONResource json(URI uri) throws IOException {
    final JSONResource resource = new JSONResource(options);
    return readResponse(openConnection(uri, resource), resource);
  }

  @SuppressWarnings({"unused", "WeakerAccess"})
  public JSONResource json(URI uri, Content content) throws IOException {
    final JSONResource resource = new JSONResource(options);
    final HttpURLConnection conn = openConnection(uri, resource);
    content.addContent(conn);
    return readResponse(conn, resource);
  }

  @SuppressWarnings({"unused", "WeakerAccess"})
  public JSONResource json(String uri, Content content) throws IOException {
    return json(URI.create(uri), content);
  }


  //
  //  Payload.
  //

  @SuppressWarnings("unused")
  public static Payload content(JSONObject json) {
    return content(json.toString());
  }

  @SuppressWarnings("unused")
  public static Payload content(JSONArray json) {
    return content(json.toString());
  }

  @SuppressWarnings({"unused", "WeakerAccess"})
  public static Payload content(String json) {
    try {
      return new Payload("application/json; charset=UTF-8", json.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unused")
  public static String enc(String rawString) {
    try {
      return URLEncoder.encode(rawString, "UTF-8");

    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }


  //
  //  Methods.
  //

  @SuppressWarnings("unused") public static Content put(Payload _)  { return new PUT(_);   }
  @SuppressWarnings("unused") public static Content post(Payload _) { return new POST(_);  }
  @SuppressWarnings("unused") public static Content delete()        { return new DELETE(); }


  //
  //  Connection.
  //

  @SuppressWarnings("WeakerAccess")
  protected <T extends Resource> HttpURLConnection openConnection(URI uri,
                                                                  T resource) throws IOException {
    final HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();

    conn.setRequestProperty("Accept", resource.getAcceptedTypes());
    for (Map.Entry<String, String> header : getHeaders().entrySet()) {
      conn.addRequestProperty(header.getKey(), header.getValue());
    }

    for (Option o : options) o.apply(conn);

    return conn;
  }

  private <T extends Resource> T readResponse(HttpURLConnection con,
                                              T resource) throws IOException {
    resource.fill(con);
    resource.getHeaders().putAll(getHeaders());
    return resource;
  }

  @SuppressWarnings("WeakerAccess")
  protected Map<String, String> getHeaders() {
    return (headers =
        (headers != null ? headers : new HashMap<String, String>())
    );
  }

  @SuppressWarnings("unused")
  public static void ignoreAllCerts() {
    try {

      // Disable certificate check.
      {
        class TrustAllX509Certificates implements X509TrustManager {
          @Override public void checkClientTrusted(X509Certificate[] certs, String authType) {}
          @Override public void checkServerTrusted(X509Certificate[] certs, String authType) {}
          @Override public X509Certificate[] getAcceptedIssuers() { return null; }
        }

        final SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{new TrustAllX509Certificates()}, null);

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      }

      // Disable hostname verification during handshake.
      HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
        @Override public boolean verify(String s, SSLSession sslSession) { return true; }
      });

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  //
  //  Options.
  //

  @SuppressWarnings("unused")
  public RestClient withHeader(String name, String value) {
    getHeaders().put(name, value);
    return this;
  }

  @SuppressWarnings("WeakerAccess")
  abstract public static class Option {
    public void apply(HttpURLConnection conn) {}
    public void init(@SuppressWarnings("UnusedParameters") RestClient client) {}

    @SuppressWarnings("unused")
    public static Timeout timeout(int millis) { return new Timeout(millis); }
  }

  private static class Timeout extends Option {
    private final int timeout;
    Timeout(int t) { timeout = t; }

    @Override
    public void apply(HttpURLConnection conn) {
      conn.setConnectTimeout(timeout);
    }

  }

}
