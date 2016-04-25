package mobi.eyeline.utils.restclient.web;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

abstract class Resource extends RestClient {

  @SuppressWarnings("WeakerAccess") protected HttpURLConnection conn;
  @SuppressWarnings("WeakerAccess") protected InputStream inputStream;

  private Resource(Option... options) { super(options); }

  protected abstract String getAcceptedTypes();

  void fill(HttpURLConnection conn) throws IOException {
    this.conn = conn;
    inputStream = "gzip".equals(conn.getContentEncoding()) ?
        new GZIPInputStream(conn.getInputStream()) : conn.getInputStream();
  }

  @SuppressWarnings("unused")
  public String printResponseHeaders() {
    final StringBuilder buf = new StringBuilder();

    if (conn != null) {
      for (String key : conn.getHeaderFields().keySet()) {
        for (String val : conn.getHeaderFields().get(key)) {
          buf.append(key).append(": ").append(val).append("\n");
        }
      }
    }
    return buf.toString();
  }


  //
  //
  //

  public static class JSONResource extends Resource {
    private String content;
    private Object json;

    JSONResource(Option... options) {
      super(options);
    }

    @SuppressWarnings("unused")
    public JSONArray array() throws IOException {
      ensureParsed();
      return (JSONArray) json;
    }

    @SuppressWarnings("unused")
    public JSONObject object() throws IOException {
      ensureParsed();
      return (JSONObject) json;
    }

    @SuppressWarnings("unused")
    public String text() throws IOException {
      ensureRead();
      return content;
    }

    private void ensureRead() throws IOException {
      content = toString(inputStream, "UTF-8");
      inputStream.close();
    }

    private void ensureParsed() throws IOException {
      if (content == null)  ensureRead();
      if (json == null)     json = new JSONTokener(content).nextValue();
    }

    @Override
    public String getAcceptedTypes() {
      return "application/json";
    }

  }

  static String toString(InputStream is, String charset) throws IOException {
    final ByteArrayOutputStream rc = new ByteArrayOutputStream();
    final byte[] buf = new byte[1024];

    int length;
    while ((length = is.read(buf)) != -1) {
      rc.write(buf, 0, length);
    }

    return rc.toString(charset);
  }
}
