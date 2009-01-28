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
import com.openexchange.groupware.contexts.impl.ContextImpl;
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
        
        final TestQuotaFileStorage quotaStorage = (TestQuotaFileStorage) FileStorage.getInstance(3,256,new URI("file://"+tempFile.getAbsolutePath()),new ContextImpl(1),new DummyDBProvider());
        quotaStorage.setQuota(10000);
        // And again, some lines from the original test
        final String fileContent = RandomString.generateLetter(100);
        final ByteArrayInputStream bais = new ByteArrayInputStream(fileContent
            .getBytes("UTF-8"));
       
        final String id = quotaStorage.saveNewFile(bais);
        
        assertEquals(fileContent.getBytes("UTF-8").length, quotaStorage.getUsage());
        assertEquals(fileContent.getBytes("UTF-8").length, quotaStorage.getFileSize(id));
        
        
        quotaStorage.deleteFile(id);
        
        assertEquals(0,quotaStorage.getUsage());
        rmdir(tempFile);
    }
	
	public void testFull() throws Exception{
//		 Taken from FileStorageTest
		final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.deleteOnExit();
        
        tempFile.delete();
        
        final TestQuotaFileStorage quotaStorage = (TestQuotaFileStorage) FileStorage.getInstance(3,256,new URI("file://"+tempFile.getAbsolutePath()),new ContextImpl(1),new DummyDBProvider());
        quotaStorage.setQuota(10000);
        
        final String fileContent = RandomString.generateLetter(100);
        
        quotaStorage.setQuota(fileContent.getBytes("UTF-8").length-2);
        
        try {
	        final ByteArrayInputStream bais = new ByteArrayInputStream(fileContent
	                .getBytes("UTF-8"));
	           
	        final String id = quotaStorage.saveNewFile(bais);
	        fail("Managed to exceed quota");
        } catch (final FileStorageException x) {
        	assertTrue(true);
        }
        rmdir(tempFile);
	}
	
	public void testExclusiveLock() throws Exception{
		
//		 Taken from FileStorageTest
		final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.deleteOnExit();
        
        tempFile.delete();
        
        final TestQuotaFileStorage quotaStorage = (TestQuotaFileStorage) FileStorage.getInstance(3,256,new URI("file://"+tempFile.getAbsolutePath()),new ContextImpl(1),new DummyDBProvider());
        quotaStorage.setQuota(10000);
        quotaStorage.storeUsage(5000);
        
        final Thread[] threads = new Thread[100];
        for(int i = 0; i < threads.length; i++) {
        	threads[i] = new AddAndRemoveThread(50,quotaStorage);
        }
        
        for(final Thread thread : threads) { thread.start(); }
        for(final Thread thread : threads) { thread.join(); }
        
        assertEquals(5000, quotaStorage.getUsage());
        rmdir(tempFile);
	}
	
	public void testConcurrentLock() throws Exception  {
		final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.deleteOnExit();
        
        tempFile.delete();
        
        final TestQuotaFileStorage quotaStorage = (TestQuotaFileStorage) FileStorage.getInstance(3,256,new URI("file://"+tempFile.getAbsolutePath()),new ContextImpl(1),new DummyDBProvider());
        quotaStorage.setQuota(10000);
        
        final int size = 1000;
        final int tests = 2;
        final long delay = 6000;
        
        quotaStorage.setQuota(size*tests);
        quotaStorage.storeUsage(0);
        
        
        
        final SaveFileThread[] saveFiles = new SaveFileThread[tests];
        
        for(int i = 0; i < saveFiles.length; i++) {
        	final DelayedInputStream is = new DelayedInputStream(new ByteArrayInputStream(new byte[size]), delay);
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
        rmdir(tempFile);
	}
	
	
	public static final class TestQuotaFileStorage extends QuotaFileStorage{

		public TestQuotaFileStorage(final Object...initData) throws FileStorageException {
			super(initData);
		}

		private long usage;
		private long quota;
		
		public void setQuota(final long quota){
			this.quota = quota;
		}

		@Override
		public long getQuota() {
			return quota;
		}

		@Override
		protected long getUsage(final boolean write) throws QuotaFileStorageException {
			return usage;
		}

		@Override
		protected void storeUsage(final long usage) throws QuotaFileStorageException {
			this.usage = usage;
		}

		
	}
	
	private static final class AddAndRemoveThread extends Thread{
		private final int counter;
		private final FileStorage fs;
		private final byte[] bytes = new byte[10];
		private final Random r = new Random();
		
		public AddAndRemoveThread(final int counter, final FileStorage fs) {
			super();
			this.counter = counter;
			this.fs = fs;
		}

		@Override
		public void run() {
			for(int i = 0; i < counter; i++) {
				try {
					final int w = r.nextInt(200);
					final String id = fs.saveNewFile(new ByteArrayInputStream(bytes));
					Thread.sleep(w);
					fs.deleteFile(id);
					//System.out.println(w);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static final class SaveFileThread extends Thread {
		private final InputStream data;
		private final FileStorage fs;
		private Exception exception;
		
		public SaveFileThread(final InputStream data, final FileStorage fs){
			this.data = data;
			this.fs = fs;
		}
		
		public Exception getException(){ return exception; }
		
		@Override
		public void run() {
			try {
				fs.saveNewFile(data);
			} catch (final FileStorageException e) {
				exception = e;
			}
		}
	}
	
	private static final class SendDataThread extends Thread {
		
		private long size;
		private final long delay;
		private final OutputStream output;
		private Exception e;
		
		public SendDataThread(final OutputStream output, final long size, final long delay) {
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
				} catch (final IOException e) {
					this.e = e;
				} catch (final InterruptedException e) {
					this.e = e;
				} finally {
					try {
						output.close();
					} catch (final IOException e) {
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

		public Connection getReadConnection(final Context ctx) throws TransactionException {
			// TODO Auto-generated method stub
			return null;
		}

		public Connection getWriteConnection(final Context ctx) throws TransactionException {
			// TODO Auto-generated method stub
			return null;
		}

		public void releaseReadConnection(final Context ctx, final Connection con) {
			// TODO Auto-generated method stub
			
		}

		public void releaseWriteConnection(final Context ctx, final Connection con) {
			// TODO Auto-generated method stub
			
		}
		
	}

    private static void rmdir(final File tempFile) {
        if (tempFile.isDirectory()) {
            for (final File f : tempFile.listFiles()) {
                rmdir(f);
            }
        }
        tempFile.delete();
    }
}
