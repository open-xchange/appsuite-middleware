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

import com.openexchange.tools.file.external.FileStorageException;
import com.openexchange.tools.file.external.QuotaFileStorageException;
import com.openexchange.tools.file.external.FileStorage;
import com.openexchange.tools.file.internal.LocalFileStorage;
import com.openexchange.tools.file.internal.DBQuotaFileStorage;

import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.test.DelayedInputStream;
import com.openexchange.tools.RandomString;

public class QuotaFileStorageTest extends TestCase {
	private FileStorage fs;
	
	 //private Class<? extends FileStorage> origImpl;

	/**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
       //FileStorage.setImpl(origImpl);
        super.tearDown();
    }

	
	public void testBasic() throws Exception{
		// Taken from FileStorageTest
		final File tempFile = File.createTempFile("filestorage", ".tmp");
		
        tempFile.deleteOnExit();
        
        tempFile.delete();
        
        fs = new LocalFileStorage(new URI("file:"+tempFile.getAbsolutePath()));
        final TestQuotaFileStorage quotaStorage = new TestQuotaFileStorage(new ContextImpl(1), fs, new DummyDatabaseService());
        
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
        rmdir(new File("file:" + tempFile.toString()));
    }
	
	public void testFull() throws Exception{
//		 Taken from FileStorageTest
		final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.deleteOnExit();
        
        tempFile.delete();
        
        fs = new LocalFileStorage(new URI("file://"+tempFile.getAbsolutePath()));
        final TestQuotaFileStorage quotaStorage = new TestQuotaFileStorage(new ContextImpl(1), fs, new DummyDatabaseService());
        quotaStorage.setQuota(10000);
        
        final String fileContent = RandomString.generateLetter(100);
        
        quotaStorage.setQuota(fileContent.getBytes("UTF-8").length-2);
        
        try {
	        final ByteArrayInputStream bais = new ByteArrayInputStream(fileContent
	                .getBytes("UTF-8"));
	           
	        final String id = quotaStorage.saveNewFile(bais);
	        fail("Managed to exceed quota");
        } catch (final QuotaFileStorageException x) {
        	assertTrue(true);
        }
        rmdir(new File("file:" + tempFile.toString()));
	}
	
	public void testExclusiveLock() throws Exception{
		
//		 Taken from FileStorageTest
		final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.deleteOnExit();
        
        tempFile.delete();
        
        fs = new LocalFileStorage(new URI("file://"+tempFile.getAbsolutePath()));
        final TestQuotaFileStorage quotaStorage = new TestQuotaFileStorage(new ContextImpl(1), fs, new DummyDatabaseService());
        quotaStorage.setQuota(10000);
        quotaStorage.setUsage(5000);
        
        final Thread[] threads = new Thread[100];
        for(int i = 0; i < threads.length; i++) {
        	threads[i] = new AddAndRemoveThread(50,quotaStorage);
        }
        
        for(final Thread thread : threads) { thread.start(); }
        for(final Thread thread : threads) { thread.join(); }
        
        assertEquals(5000, quotaStorage.getUsage());
        rmdir(new File("file:" + tempFile.toString()));
	}
	
	public void testConcurrentLock() throws Exception  {
		final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.deleteOnExit();
        
        tempFile.delete();
        
        fs = new LocalFileStorage(new URI("file://"+tempFile.getAbsolutePath()));
        final TestQuotaFileStorage quotaStorage = new TestQuotaFileStorage(new ContextImpl(1), fs, new DummyDatabaseService());
        quotaStorage.setQuota(10000);
        
        final int size = 1000;
        final int tests = 2;
        final long delay = 6000;
        
        quotaStorage.setQuota(size*tests);
        quotaStorage.setUsage(0);
        
        
        
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
        rmdir(new File("file:" + tempFile.toString()));
	}
	
	
	public static final class TestQuotaFileStorage extends DBQuotaFileStorage {

		public TestQuotaFileStorage(final Context ctx, final FileStorage fs, final DatabaseService dbs) throws QuotaFileStorageException {
			//FileStorageImpl fsi = new FileStorageImpl(uri);
		    super(ctx, fs, dbs);
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
        public long getUsage() throws QuotaFileStorageException {
			return usage;
		}
		
		@Override
		protected void setUsage(long usage) throws QuotaFileStorageException {
			this.usage = usage;
		}
		
		@Override
		protected boolean incUsage(long usage)  throws QuotaFileStorageException {
		    boolean full = false;
		    if (this.usage + usage <= this.quota) {
		        this.usage += usage;
		    } else {
		        full = true;
		    }
		    
		    return full;
		}
		
		@Override 
		protected void decUsage(long usage) {
		    this.usage -= usage;
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
	
	
	private static final class DummyDatabaseService implements DatabaseService {

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

        public void back(int poolId, Connection con) {
            // TODO Auto-generated method stub
            
        }

        public void backForUpdateTask(int contextId, Connection con) {
            // TODO Auto-generated method stub
            
        }

        public void backReadOnly(Context ctx, Connection con) {
            // TODO Auto-generated method stub
            
        }

        public void backReadOnly(int contextId, Connection con) {
            // TODO Auto-generated method stub
            
        }

        public void backWritable(Context ctx, Connection con) {
            // TODO Auto-generated method stub
            
        }

        public void backWritable(int contextId, Connection con) {
            // TODO Auto-generated method stub
            
        }

        public Connection get(int poolId, String schema) throws DBPoolingException {
            // TODO Auto-generated method stub
            return null;
        }

        public int[] getContextsInSameSchema(int contextId) throws DBPoolingException {
            // TODO Auto-generated method stub
            return null;
        }

        public Connection getForUpdateTask(int contextId) throws DBPoolingException {
            // TODO Auto-generated method stub
            return null;
        }

        public Connection getReadOnly(Context ctx) throws DBPoolingException {
            // TODO Auto-generated method stub
            return null;
        }

        public Connection getReadOnly(int contextId) throws DBPoolingException {
            // TODO Auto-generated method stub
            return null;
        }

        public String getSchemaName(int contextId) throws DBPoolingException {
            // TODO Auto-generated method stub
            return null;
        }

        public Connection getWritable(Context ctx) throws DBPoolingException {
            // TODO Auto-generated method stub
            return null;
        }

        public Connection getWritable(int contextId) throws DBPoolingException {
            // TODO Auto-generated method stub
            return null;
        }

        public int getWritablePool(int contextId) throws DBPoolingException {
            // TODO Auto-generated method stub
            return 0;
        }

        public void invalidate(int contextId) throws DBPoolingException {
            // TODO Auto-generated method stub
            
        }

        public void backReadOnly(Connection con) {
            // TODO Auto-generated method stub
            
        }

        public void backWritable(Connection con) {
            // TODO Auto-generated method stub
            
        }

        public Connection getReadOnly() throws DBPoolingException {
            // TODO Auto-generated method stub
            return null;
        }

        public Connection getWritable() throws DBPoolingException {
            // TODO Auto-generated method stub
            return null;
        }

        public int[] listContexts(int poolId) throws DBPoolingException {
            // TODO Auto-generated method stub
            return null;
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
