
package com.openexchange.test;

import java.io.IOException;
import java.io.InputStream;

public class DelayedInputStream extends InputStream {

    private long delay = 0;
    private final InputStream delegate;

    public DelayedInputStream(final InputStream delegate, final long delay) {
        this.delegate = delegate;
        this.delay = delay;
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public boolean equals(final Object arg0) {
        return delegate.equals(arg0);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public void mark(final int arg0) {
        delegate.mark(arg0);
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    @Override
    public int read() throws IOException {
        sleep();
        return delegate.read();
    }

    @Override
    public int read(final byte[] arg0, final int arg1, final int arg2) throws IOException {
        sleep();
        return delegate.read(arg0, arg1, arg2);
    }

    @Override
    public int read(final byte[] arg0) throws IOException {
        sleep();
        return delegate.read(arg0);
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public long skip(final long arg0) throws IOException {
        sleep();
        return delegate.skip(arg0);
    }

    private void sleep() {
        try {
            Thread.sleep(delay);
        } catch (final InterruptedException e) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
