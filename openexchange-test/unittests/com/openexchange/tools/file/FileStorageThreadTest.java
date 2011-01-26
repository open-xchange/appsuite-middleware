package com.openexchange.tools.file;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestCase;
import java.io.*;

import com.openexchange.tools.file.internal.LocalFileStorage;
import com.openexchange.tools.file.external.FileStorage;

import java.util.*;

/**
 * Test for the file storage.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:steffen.templin@open-xchange.org">Steffen Templin</a>
 */
public class FileStorageThreadTest extends TestCase {
	private final FMRunner[] fmr = new FMRunner[5];
	private final Thread[] thread = new Thread[fmr.length];
	private static File tempFile;
    
    

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.delete();
        new LocalFileStorage(tempFile.toURI()); // initialize file storage    
        
        for (int i = 0; i < fmr.length; i++) {
            fmr[i] = new FMRunner();
            thread[i] = new Thread(fmr[i]);
        }
        for (int i = 0; i < thread.length; i++) {
            thread[i].start();
        }
    }
    
    @Override
    public void tearDown() throws Exception {
        for (int i = 0; i < fmr.length; i++) {
            fmr[i].stop();
        }
        for (int i = 0; i < thread.length; i++) {
            thread[i].join();
        }
        for (int i = 0; i < fmr.length; i++) {
            if (fmr[i].getThrowable() != null) {
                System.out.println("================= FEHLER: " + fmr[i].getThrowable().getMessage());
            }
            assertNull(fmr[i].getThrowable());            
        }

        super.tearDown();
        
        final FileStorage fm = new LocalFileStorage(tempFile.toURI());        
        assertTrue(fm.stateFileIsCorrect()); 
    	rmdir(new File("file:" + tempFile.toString()));
    }
    
    private static void rmdir(final File dir) {
        if (dir.isDirectory()) {
            for (final File f : dir.listFiles()) {
                rmdir(f);
            }
        }
        dir.delete();
    }
    
    private static final class FMRunner implements Runnable {
        private boolean run = true;
        private Throwable t;
        
        FMRunner() 
        {
            super();
        }
        void stop() 
        {
            run = false;
        }
        
        Throwable getThrowable() {
            return t;
        }

        public void run()
        {
        	try {        	    
                final File testfile = File.createTempFile("filestorage", ".test");
                final FileStorage fm = new LocalFileStorage(tempFile.toURI());

            	SortedSet<String> set = new TreeSet<String>();
            	while (run) {
            		for (int i = 0; i < 10; i++) {
            			InputStream is = new FileInputStream(testfile); 		
            			String str = fm.saveNewFile(is);            	    	  
            	    	is.close();
            	    	set.add(str); 
            	    	System.out.println("Thread: " + Thread.currentThread().getName() + ", speichern: " + str);
            		}
            		for (int i = 0; i < 10; i = i + 3) {
            			boolean del = fm.deleteFile(set.first());
            			set.remove(set.first());
            			System.out.println("Thread: " + Thread.currentThread().getName() + ", l\u00f6schen: " + del);
            		}
            	}
            	testfile.delete();
        	} catch (Throwable t2) {
                t = t2;
        	}
        	
        }
    }
    
    public void testStateFileIsCorrect() throws Throwable
    {
        final File testfile = File.createTempFile("filestorage", ".test");
        final FileStorage fm = new LocalFileStorage(tempFile.toURI());
    	SortedSet<String> set = new TreeSet<String>();
    	for (int j = 0; j < 10; j++) {
    		for (int i = 0; i < 10; i++) {
    			InputStream is = new FileInputStream(testfile);
    			String str = fm.saveNewFile(is);
    	    	set.add(str);   
    	    	System.out.println("Thread: " + Thread.currentThread().getName() + ", speichern: " + str);
    	    	is.close();
    		}
    		for (int k = 0; k < 10; k = k + 3) {
    			System.out.println("Thread: " + Thread.currentThread().getName() + ", speichern: " + fm.deleteFile(set.first()));
    			set.remove(set.first());
    		}
    	}
    	testfile.delete();

    }


}
