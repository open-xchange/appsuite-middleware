package com.openexchange.groupware.importexport;

import java.io.IOException;
import java.io.InputStream;

/**
 * Defines a wrapper for an InputStream that also contains the size of this
 * InputStream. This is necessary to be able to set the correct size when
 * returning a HTTP-response - else the whole connection might be cancelled
 * either too early (resulting in corrupt data) or to late (resulting in
 * a lot of waiting).
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class SizedInputStream extends InputStream{
	
	private InputStream in;
	private long size;
	
	public SizedInputStream(InputStream in, long size){
		this.size = size;
		this.in = in;
	}

	public long getSize() {
		return size;
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		in.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return in.markSupported();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return in.read(b);
	}

	@Override
	public synchronized void reset() throws IOException {
		in.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return in.skip(n);
	}
	
}
