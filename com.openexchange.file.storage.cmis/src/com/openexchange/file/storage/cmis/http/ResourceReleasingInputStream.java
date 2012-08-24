package com.openexchange.file.storage.cmis.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import com.openexchange.java.Streams;

public final class ResourceReleasingInputStream extends BufferedInputStream {

    private static final int defaultBufferSize = 8192;

    private final DefaultHttpClient httpClient;

    private final HttpRequestBase httpRequest;

    public ResourceReleasingInputStream(InputStream in, HttpRequestBase httpRequest, DefaultHttpClient httpClient) {
        this(in, defaultBufferSize, httpRequest, httpClient);
    }

    public ResourceReleasingInputStream(InputStream in, final int size, HttpRequestBase httpRequest, DefaultHttpClient httpClient) {
        super(in, size);
        this.httpRequest = httpRequest;
        this.httpClient = httpClient;
    }

    private static BufferedInputStream toBufferedInputStream(InputStream in, final int size) {
        if (in instanceof BufferedInputStream) {
            return (BufferedInputStream) in;
        }
        return new BufferedInputStream(in, size);
    }

    @Override
    public int read() throws IOException {
        try {
            return in.read();
        } catch (final IOException ex) {
            // In case of an IOException the connection will be released
            // back to the connection manager automatically

            // Closing the input stream will trigger connection release
            close();
            throw ex;
        } catch (final RuntimeException ex) {
            // In case of an unexpected exception you may want to abort
            // the HTTP request in order to shut down the underlying
            // connection immediately.
            httpRequest.abort();
            // Closing the input stream will trigger connection release
            close();
            throw ex;
        }
    }

    @Override
    public int read(byte b[]) throws IOException {
        try {
            return read(b, 0, b.length);
        } catch (final IOException ex) {
            // In case of an IOException the connection will be released
            // back to the connection manager automatically

            // Closing the input stream will trigger connection release
            close();
            throw ex;
        } catch (final RuntimeException ex) {
            // In case of an unexpected exception you may want to abort
            // the HTTP request in order to shut down the underlying
            // connection immediately.
            httpRequest.abort();
            // Closing the input stream will trigger connection release
            close();
            throw ex;
        }
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        try {
            return in.read(b, off, len);
        } catch (final IOException ex) {
            // In case of an IOException the connection will be released
            // back to the connection manager automatically

            // Closing the input stream will trigger connection release
            close();
            throw ex;
        } catch (final RuntimeException ex) {
            // In case of an unexpected exception you may want to abort
            // the HTTP request in order to shut down the underlying
            // connection immediately.
            httpRequest.abort();
            // Closing the input stream will trigger connection release
            close();
            throw ex;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        try {
            return in.skip(n);
        } catch (final IOException ex) {
            // In case of an IOException the connection will be released
            // back to the connection manager automatically

            // Closing the input stream will trigger connection release
            close();
            throw ex;
        } catch (final RuntimeException ex) {
            // In case of an unexpected exception you may want to abort
            // the HTTP request in order to shut down the underlying
            // connection immediately.
            httpRequest.abort();
            // Closing the input stream will trigger connection release
            close();
            throw ex;
        }
    }

    @Override
    public int available() throws IOException {
        try {
            return in.available();
        } catch (final IOException ex) {
            // In case of an IOException the connection will be released
            // back to the connection manager automatically

            // Closing the input stream will trigger connection release
            close();
            throw ex;
        } catch (final RuntimeException ex) {
            // In case of an unexpected exception you may want to abort
            // the HTTP request in order to shut down the underlying
            // connection immediately.
            httpRequest.abort();
            // Closing the input stream will trigger connection release
            close();
            throw ex;
        }
    }

    @Override
    public void close() {
        // Safely close stream and...
        Streams.close(in);
        // ... release HTTP resources immediately
        if (null != httpRequest) {
            httpRequest.reset();
        }
        if (null != httpClient) {
            httpClient.getConnectionManager().shutdown();
        }
    }

    @Override
    public void mark(int readlimit) {
        in.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        try {
            in.reset();
        } catch (final IOException ex) {
            // In case of an IOException the connection will be released
            // back to the connection manager automatically

            // Closing the input stream will trigger connection release
            close();
            throw ex;
        } catch (final RuntimeException ex) {
            // In case of an unexpected exception you may want to abort
            // the HTTP request in order to shut down the underlying
            // connection immediately.
            httpRequest.abort();
            // Closing the input stream will trigger connection release
            close();
            throw ex;
        }
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }
}