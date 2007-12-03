package com.openexchange.test;

import java.io.IOException;
import java.io.InputStream;

public class DelayedInputStream extends InputStream {
	private long delay = 0;
	private InputStream delegate;
	
	public DelayedInputStream(InputStream delegate, long delay) {
		this.delegate = delegate;
		this.delay = delay;
	}

	public int available() throws IOException {
		return delegate.available();
	}

	public void close() throws IOException {
		delegate.close();
	}

	public boolean equals(Object arg0) {
		return delegate.equals(arg0);
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public void mark(int arg0) {
		delegate.mark(arg0);
	}

	public boolean markSupported() {
		return delegate.markSupported();
	}

	public int read() throws IOException {
		sleep();
		return delegate.read();
	}

	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		sleep();
		return delegate.read(arg0, arg1, arg2);
	}

	public int read(byte[] arg0) throws IOException {
		sleep();
		return delegate.read(arg0);
	}

	public void reset() throws IOException {
		delegate.reset();
	}

	public long skip(long arg0) throws IOException {
		sleep();
		return delegate.skip(arg0);
	}

	private void sleep() {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
		}
	}

	public String toString() {
		return delegate.toString();
	}
}
