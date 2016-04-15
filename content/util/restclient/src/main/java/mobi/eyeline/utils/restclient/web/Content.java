package mobi.eyeline.utils.restclient.web;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

abstract class Content {

  public void writeHeader(OutputStream os) throws IOException {}
  public void writeContent(OutputStream os) throws IOException {}
  protected abstract void addContent(HttpURLConnection con) throws IOException;


  //
  //
  //

  static class DELETE extends Content {
    @Override
    protected void addContent(HttpURLConnection con) throws IOException {
      con.setDoOutput(true);
      con.setRequestMethod("DELETE");
    }
  }


  //
  //
  //

  static class POST extends Content {
    private Payload payload;

    POST(Payload payload) { this.payload = payload; }

    @Override
    protected void addContent(HttpURLConnection con) throws IOException {
      con.setDoOutput(true);
      con.setRequestMethod("POST");
      payload.addContent(con);
    }
  }


  //
  //
  //

  static class PUT extends Content {
    private final Payload payload;

    PUT(Payload payload) { this.payload = payload; }

    @Override
    protected void addContent(HttpURLConnection con) throws IOException {
      con.setDoOutput(true);
      con.setRequestMethod("PUT");
      payload.addContent(con);
    }

  }
}
