package mobi.eyeline.utils.restclient.web;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

class Payload extends Content {
  private final String mime;
  private final byte[] content;

  Payload(String mime, byte[] content) {
    this.mime = mime;
    this.content = content;
  }

  @Override
  protected void addContent(HttpURLConnection conn) throws IOException {
    conn.setDoOutput(true);
    conn.addRequestProperty("Content-Type", mime);
    conn.addRequestProperty("Content-Length", String.valueOf(content.length));

    try (OutputStream os = conn.getOutputStream()) {
      writeContent(os);
    }
  }

  @Override
  public void writeContent(OutputStream os) throws IOException {
    os.write(content);
  }

  @Override
  public void writeHeader(OutputStream os) throws IOException {
    os.write(("Content-Type: " + mime + "\r\n").getBytes());
    os.write(("Content-Length: " + String.valueOf(content.length) + "\r\n").getBytes());
  }

}
