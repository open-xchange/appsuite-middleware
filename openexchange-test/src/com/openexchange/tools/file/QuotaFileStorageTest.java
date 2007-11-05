package com.openexchange.tools.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.sql.Connection;
import java.util.Random;

import junit.framework.TestCase;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.test.DelayedInputStream;
import com.openexchange.tools.RandomString;

public class QuotaFileStorageTest extends TestCase {
	
	
	 private Class<? extends FileStorage> origImpl;

	/**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        origImpl = FileStorage.getImpl();
        FileStorage.setImpl(TestQuotaFileStorage.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        FileStorage.setImpl(origImpl);
        super.tearDown();
    }

	
	public void testBasic() throws Exception{
		// Taken from FileStorageTest
		final File tempFile = File.createTempFile("filestorage", ".tmp");
		
        tempFile.deleteOnExit();
        
        tempFile.delete();
        
        TestQuotaFileStorage quotaStorage = (TestQuotaFileStorage) FileStorage.getInstance(3,256,new URI("file://"+tempFile.getAbsolutePath()),new ContextImpl(1),new DummyDBProvider());
        quotaStorage.setQuota(10000);
        // And again, some lines from the original test
        final String fileContent = RandomString.generateLetter(100);
        final ByteArrayInputStream bais = new ByteArrayInputStream(fileContent
            .getBytes("UTF-8"));
       
        String id = quotaStorage.saveNewFile(bais);
        
        assertEquals(fileContent.getBytes("UTF-8").length, quotaStorage.getUsage());
        assertEquals(fileContent.getBytes("UTF-8").length, quotaStorage.getFileSize(id));
        
        
        quotaStorage.deleteFile(id);
        
        assertEquals(0,quotaStorage.getUsage());
        
    }
	
	public void testFull() throws Exception{
//		 Taken from FileStorageTest
		final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.deleteOnExit();
        
        tempFile.delete();
        
        TestQuotaFileStorage quotaStorage = (TestQuotaFileStorage) FileStorage.getInstance(3,256,new URI("file://"+tempFile.getAbsolutePath()),new ContextImpl(1),new DummyDBProvider());
        quotaStorage.setQuota(10000);
        
        final String fileContent = RandomString.generateLetter(100);
        
        quotaStorage.setQuota(fileContent.getBytes("UTF-8").length-2);
        
        try {
	        final ByteArrayInputStream bais = new ByteArrayInputStream(fileContent
	                .getBytes("UTF-8"));
	           
	        String id = quotaStorage.saveNewFile(bais);
	        fail("Managed to exceed quota");
        } catch (FileStorageException x) {
        	assertTrue(true);
        }
	}
	
	public void testExclusiveLock() throws Exception{
		
//		 Taken from FileStorageTest
		final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.deleteOnExit();
        
        tempFile.delete();
        
        TestQuotaFileStorage quotaStorage = (TestQuotaFileStorage) FileStorage.getInstance(3,256,new URI("file://"+tempFile.getAbsolutePath()),new ContextImpl(1),new DummyDBProvider());
        quotaStorage.setQuota(10000);
        quotaStorage.storeUsage(5000);
        
        Thread[] threads = new Thread[100];
        for(int i = 0; i < threads.length; i++) {
        	threads[i] = new AddAndRemoveThread(50,quotaStorage);
        }
        
        for(Thread thread : threads) { thread.start(); }
        for(Thread thread : threads) { thread.join(); }
        
        assertEquals(5000, quotaStorage.getUsage());
	}
	
	public void testConcurrentLock() throws Exception  {
		final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.deleteOnExit();
        
        tempFile.delete();
        
        TestQuotaFileStorage quotaStorage = (TestQuotaFileStorage) FileStorage.getInstance(3,256,new URI("file://"+tempFile.getAbsolutePath()),new ContextImpl(1),new DummyDBProvider());
        quotaStorage.setQuota(10000);
        
        int size = 1000;
        int tests = 2;
        long delay = 6000;
        
        quotaStorage.setQuota(size*tests);
        quotaStorage.storeUsage(0);
        
        
        
        SaveFileThread[] saveFiles = new SaveFileThread[tests];
        
        for(int i = 0; i < saveFiles.length; i++) {
        	DelayedInputStream is = new DelayedInputStream(new ByteArrayInputStream(new byte[size]), delay);
        	saveFiles[i] = new SaveFileThread(is, quotaStorage);
        	saveFiles[i].start();
        }
        
        
        for(int i = 0; i < saveFiles.length; i++) {
        	saveFiles[i].join();
        	if(saveFiles[i].getException() != null) {
        		saveFiles[i].getException().printStackTrace();
        		assertTrue(false);
        	}
        }
        
        assertFalse(new File(tempFile,".lock").exists());
	}
	
	
	public static final class TestQuotaFileStorage extends QuotaFileStorage{

		public TestQuotaFileStorage(Object...initData) throws FileStorageException {
			super(initData);
		}

		private long usage;
		private long quota;
		
		public void setQuota(long quota){
			this.quota = quota;
		}

		@Override
		public long getQuota() {
			return quota;
		}

		@Override
		protected long getUsage(boolean write) throws QuotaFileStorageException {
			return usage;
		}

		@Override
		protected void storeUsage(long usage) throws QuotaFileStorageException {
			this.usage = usage;
		}

		
	}
	
	private static final class AddAndRemoveThread extends Thread{
		private int counter;
		private FileStorage fs;
		private byte[] bytes = new byte[10];
		private Random r = new Random();
		
		public AddAndRemoveThread(int counter, FileStorage fs) {
			super();
			this.counter = counter;
			this.fs = fs;
		}

		@Override
		public void run() {
			for(int i = 0; i < counter; i++) {
				try {
					int w = r.nextInt(200);
					String id = fs.saveNewFile(new ByteArrayInputStream(bytes));
					Thread.sleep(w);
					fs.deleteFile(id);
					//System.out.println(w);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static final class SaveFileThread extends Thread {
		private InputStream data;
		private FileStorage fs;
		private Exception exception;
		
		public SaveFileThread(InputStream data, FileStorage fs){
			this.data = data;
			this.fs = fs;
		}
		
		public Exception getException(){ return exception; }
		
		@Override
		public void run() {
			try {
				fs.saveNewFile(data);
			} catch (FileStorageException e) {
				exception = e;
			}
		}
	}
	
	private static final class SendDataThread extends Thread {
		
		private long size;
		private long delay;
		private OutputStream output;
		private Exception e;
		
		public SendDataThread(OutputStream output, long size, long delay) {
			this.output = output;
			this.delay = delay;
			this.size = size;
		}
		
		@Override
		public void run() {
			for(;size > 0; size--) {
				try {
					output.write((int)size);
					Thread.sleep(delay);
				} catch (IOException e) {
					this.e = e;
				} catch (InterruptedException e) {
					this.e = e;
				} finally {
					try {
						output.close();
					} catch (IOException e) {
						//Ignore
					}
				}
			}
		}
		
		public Exception getException(){
			return e;
		}
	}
	
	private static final class DummyDBProvider implements DBProvider{

		public Connection getReadConnection(Context ctx) throws TransactionException {
			// TODO Auto-generated method stub
			return null;
		}

		public Connection getWriteConnection(Context ctx) throws TransactionException {
			// TODO Auto-generated method stub
			return null;
		}

		public void releaseReadConnection(Context ctx, Connection con) {
			// TODO Auto-generated method stub
			
		}

		public void releaseWriteConnection(Context ctx, Connection con) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
