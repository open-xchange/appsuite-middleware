package org.quickcached;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Akshath
 */
public class DataCarrier implements java.io.Serializable {
	private final ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
	public final Lock readLock = rwlock.readLock();
    public final Lock writeLock = rwlock.writeLock();

	private byte data[];
	private String flags;
	private int cas;
	
	public int getSize() {
		readLock.lock();
		try {
			if(data==null) return 0;
			return data.length;
		} finally {
			readLock.unlock();
		}
	}
	
	private void incCas() {
		cas = cas + 1;//no need of explicit locking.. private method
	}
	
	public void append(byte chunk[]) {
		writeLock.lock();
		try {
			int newlen = data.length + chunk.length;
			byte data_new[] = new byte[newlen];

			System.arraycopy(data, 0, data_new, 0, data.length);
			System.arraycopy(chunk, 0, data_new, data.length, chunk.length);

			data = data_new;
			data_new = null;

			incCas();
		} finally {
			writeLock.unlock();
		}
	}	

	public void prepend(byte chunk[]) {
		writeLock.lock();
		try {
			int newlen = data.length + chunk.length;
			byte data_new[] = new byte[newlen];

			System.arraycopy(chunk, 0, data_new, 0, chunk.length);
			System.arraycopy(data, 0, data_new, chunk.length, data.length);

			data = data_new;
			data_new = null;

			incCas();
		} finally {
			writeLock.unlock();
		}
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public DataCarrier(byte data[]) {
		setData(data);
	}

	public byte[] getData() {
		readLock.lock();
		try {
			return data;
		} finally {
			readLock.unlock();
		}
	}

	public void setData(byte[] data) {
		writeLock.lock();
		try {
			this.data = data;
			incCas();
		} finally {
			writeLock.unlock();
		}
	}

	public int getCas() {
		return cas;
	}

	public void setCas(int cas) {
		this.cas = cas;
	}

	public boolean checkCas(String newcas) {
		if(newcas==null || "0000000000000000".equals(newcas)) return true;
		
		StringBuilder sb = new StringBuilder();
		sb.append(cas);
		//0000 0000 0000 0001
		while(sb.length()<16) {
			sb.insert(0, "0");
		}

		return sb.toString().equals(newcas);
	}
}
