
package com.openexchange.webdav.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MockWebdavResponse implements WebdavResponse {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private final Map<String, String> headers = new HashMap<String, String>();

    private int status;

    public String getResponseBodyAsString() {
        try {
            return new String(out.toByteArray(), "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public String getHeader(final String headerName) {
        return headers.get(headerName.toUpperCase());
    }

    @Override
    public OutputStream getOutputStream() {
        return out;
    }

    @Override
    public void setHeader(final String header, final String value) {
        headers.put(header.toUpperCase(), value);
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(final int status) {
        this.status = status;
    }

    public byte[] getResponseBytes() {
        return out.toByteArray();
    }

    @Override
    public void setContentType(final String s) {
        setHeader("Content-Type", s);
    }

    @Override
    public void sendString(final String notFound) throws IOException {
        final byte[] bytes = notFound.getBytes(com.openexchange.java.Charsets.UTF_8);
        setHeader("Content-Length", String.valueOf(bytes.length));
        getOutputStream().write(bytes);
    }

}
