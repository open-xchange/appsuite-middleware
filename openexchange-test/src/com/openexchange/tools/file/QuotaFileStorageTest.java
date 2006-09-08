package com.openexchange.tools.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import com.openexchange.tools.RandomString;

import junit.framework.TestCase;

public class QuotaFileStorageTest extends TestCase {
	
	public void testBasic() throws Exception{
		// Taken from FileStorageTest
		final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.deleteOnExit();
        
        tempFile.delete();
        final FileStorage storage = FileStorage.getInstance(tempFile);
        
        TestQuotaFileStorage quotaStorage = new TestQuotaFileStorage(storage);
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
        
        final FileStorage storage = FileStorage.getInstance(tempFile);
        
        final String fileContent = RandomString.generateLetter(100);
        
        TestQuotaFileStorage quotaStorage = new TestQuotaFileStorage(storage);
        quotaStorage.setQuota(fileContent.getBytes("UTF-8").length-2);
        
        try {
	        final ByteArrayInputStream bais = new ByteArrayInputStream(fileContent
	                .getBytes("UTF-8"));
	           
	        String id = quotaStorage.saveNewFile(bais);
	        fail("Managed to exceed quota");
        } catch (IOException x) {
        	assertTrue(true);
        }
	}
	
	public void notestExclusiveLock() throws Exception{
		//FIXME
//		 Taken from FileStorageTest
		final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.deleteOnExit();
        
        tempFile.delete();
        final FileStorage storage = FileStorage.getInstance(tempFile);
        
        TestQuotaFileStorage quotaStorage = new TestQuotaFileStorage(storage);
        quotaStorage.setQuota(10000);
        quotaStorage.storeUsage(5000);
        
        Thread[] threads = new Thread[30];
        for(int i = 0; i < threads.length; i++) {
        	threads[i] = new AddAndRemoveThread(50,quotaStorage);
        }
        
        for(Thread thread : threads) { thread.start(); }
        for(Thread thread : threads) { thread.join(); }
        
        assertEquals(5000, quotaStorage.getUsage());
	}
	
	
	private static final class TestQuotaFileStorage extends QuotaFileStorage{

		private long usage;
		private long quota;

		public TestQuotaFileStorage(FileStorage delegate) throws IOException {
			this(DEFAULT_DEPTH, DEFAULT_FILES, delegate);
		}
		
		public TestQuotaFileStorage(int depth, int files, FileStorage delegate) throws IOException {
			super(depth, files, delegate, null, null);
		}
		
		public void setQuota(long quota){
			this.quota = quota;
		}

		@Override
		public long getQuota() {
			return quota;
		}

		@Override
		public long getUsage() throws IOException {
			return usage;
		}

		@Override
		protected void storeUsage(long usage) throws IOException {
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
					String id = fs.saveNewFile(new ByteArrayInputStream(bytes));
					Thread.sleep(r.nextInt(200));
					fs.deleteFile(id);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}
