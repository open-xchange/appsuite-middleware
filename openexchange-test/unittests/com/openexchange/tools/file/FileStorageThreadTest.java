/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.tools.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.SortedSet;
import java.util.TreeSet;
import junit.framework.TestCase;
import com.openexchange.filestore.impl.LocalFileStorage;

/**
 * Test for the file storage.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:steffen.templin@open-xchange.org">Steffen Templin</a>
 */
public class FileStorageThreadTest extends TestCase {

	private final FMRunner[] fmr = new FMRunner[5];
	private final Thread[] thread = new Thread[fmr.length];
	static File tempFile;

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
                fail(fmr[i].getThrowable().getMessage());
            }
        }
        final com.openexchange.filestore.FileStorage fm = new LocalFileStorage(tempFile.toURI());
        assertTrue("State file is not correct.", fm.stateFileIsCorrect());
    	rmdir(new File("file:" + tempFile.toString()));
    	super.tearDown();
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

        FMRunner() {
            super();
        }
        void stop() {
            run = false;
        }
        Throwable getThrowable() {
            return t;
        }

        @Override
        public void run() {
        	try {
                final File testfile = File.createTempFile("filestorage", ".test");
                final com.openexchange.filestore.FileStorage fm = new LocalFileStorage(tempFile.toURI());
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

    public void testStateFileIsCorrect() throws Throwable {
        final File testfile = File.createTempFile("filestorage", ".test");
        final com.openexchange.filestore.FileStorage fm = new LocalFileStorage(tempFile.toURI());
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
    	rmdir(testfile);
    }
}
