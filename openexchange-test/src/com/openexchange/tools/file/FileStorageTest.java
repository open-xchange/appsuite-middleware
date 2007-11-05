/*
 * OPEN-XCHANGE - "the communication and information enviroment"
 *
 * All intellectual property rights in the Software are protected by
 * international copyright laws.
 *
 * OPEN-XCHANGE is a trademark of Netline Internet Service GmbH and all other
 * brand and product names are or may be trademarks of, and are used to identify
 * products or services of, their respective owners.
 *
 * Please make sure that third-party modules and libraries are used according to
 * their respective licenses.
 *
 * Any modifications to this package must retain all copyright notices of the
 * original copyright holder(s) for the original code used.
 *
 * After any such modifications, the original code will still remain copyrighted
 * by the copyright holder(s) or original author(s).
 *
 * Copyright (C) 1998 - 2005 Netline Internet Service GmbH
 * mail:                    info@netline-is.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.openexchange.tools.file;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.tools.RandomString;

import junit.framework.TestCase;

/**
 * Test for the file storage.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class FileStorageTest extends TestCase {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(FileStorageTest.class);

    private Class< ? extends FileStorage> origImpl;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        origImpl = FileStorage.getImpl();
        FileStorage.setImpl(LocalFileStorage.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        FileStorage.setImpl(origImpl);
        super.tearDown();
    }

    /**
     * Test method for
     * 'com.openexchange.tools.file.FileStorage.getInstance(Object...)'.
     * @throws Throwable if an error occurs.
     */
    public final void testGetInstance() throws Throwable {
        final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.delete();
        LOG.trace(tempFile.getAbsolutePath());
        final FileStorage storage = FileStorage.getInstance(tempFile);
        tempFile.delete();
        assertNotNull("Can't create file storage.", storage);
    }

    /**
     * Test method for
     * 'com.openexchange.tools.file.FileStorage.saveNewFile(InputStream)'.
     * @throws Throwable if an error occurs.
     */
    public final void testSaveNewFile() throws Throwable {
        final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.delete();
        final String fileContent = RandomString.generateLetter(100);
        final ByteArrayInputStream baos = new ByteArrayInputStream(fileContent
            .getBytes("UTF-8"));
        final FileStorage storage = FileStorage.getInstance(tempFile);
        final String identifier = storage.saveNewFile(baos);
        tempFile.delete();
        assertNotNull("Can't create new file in file storage.", identifier);
    }
    
    // Bug 3978
    public final void testExceptionOnUnavailableFilestore() throws Throwable {
    	final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.delete();
        final String fileContent = RandomString.generateLetter(100);
        final ByteArrayInputStream baos = new ByteArrayInputStream(fileContent
            .getBytes("UTF-8"));
        final FileStorage storage = FileStorage.getInstance(tempFile);
        final String identifier = storage.saveNewFile(baos);
        rmdir(tempFile);
        assertFalse(tempFile.exists());
        try {
        	storage.getFile(identifier);
        	fail("Expected IOException");
        } catch (FileStorageException x) {
        	// Everything fine. Error is discovered.
        }

        try {
        	storage.saveNewFile(baos);
            fail("Expected IOException");
        } catch (FileStorageException x) {
        	// Everything fine. Error is discovered.
        }
    }
    
//  Bug 3978
    public final void testExceptionOnUnknown() throws Throwable {
    	final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.delete();
        final FileStorage storage = FileStorage.getInstance(tempFile);
        try {
        	storage.getFile("00/00/01");
        	fail("Expected IOException");
        } catch (FileStorageException x) {
        	// Everything fine. Error is discovered.
        }
    }

	private void rmdir(File tempFile) {
		if(tempFile.isDirectory()) {
			for(File f : tempFile.listFiles()) {
				rmdir(f);
			}
		} 
		tempFile.delete();
	}
}
