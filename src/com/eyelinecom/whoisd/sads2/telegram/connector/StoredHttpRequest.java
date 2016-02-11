package com.eyelinecom.whoisd.sads2.telegram.connector;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StoredHttpRequest extends HttpServletRequestWrapper {
  private ByteArrayOutputStream body;

  public StoredHttpRequest(HttpServletRequest request) {
    super(request);
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    if (body == null) {
      body = cacheInputStream();
    }

    return new CachedServletInputStream(body.toByteArray());
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(getInputStream()));
  }

  public String getContent() throws IOException {
    return IOUtils.toString(getInputStream());
  }

  private ByteArrayOutputStream cacheInputStream() throws IOException {
    final ByteArrayOutputStream buf = new ByteArrayOutputStream();
    IOUtils.copy(super.getInputStream(), buf);
    return buf;
  }

  private class CachedServletInputStream extends ServletInputStream {
    private final InputStream in;

    public CachedServletInputStream(byte[] bytes) {
      in = new ByteArrayInputStream(bytes);
    }

    @Override
    public int read() throws IOException {
      return in.read();
    }
  }
}
