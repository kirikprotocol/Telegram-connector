package mobi.eyeline.utils.restclient.web;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private Object json;

    JSONResource(Option... options) {
      super(options);
    }

    @SuppressWarnings("unused")
    public JSONArray array() throws IOException {
      unmarshal();
      return (JSONArray) json;
    }

    @SuppressWarnings("unused")
    public JSONObject object() throws IOException {
      unmarshal();
      return (JSONObject) json;
    }

    private void unmarshal() throws IOException {
      if (json == null) {
        json = new JSONTokener(new InputStreamReader(inputStream, "UTF-8")).nextValue();
        inputStream.close();
      }
    }

    @Override
    public String getAcceptedTypes() {
      return "application/json";
    }

  }
}
