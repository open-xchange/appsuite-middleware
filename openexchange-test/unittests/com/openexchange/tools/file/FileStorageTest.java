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

import java.io.ByteArrayInputStream;
import java.io.File;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.impl.LocalFileStorage;
import com.openexchange.tools.RandomString;

/**
 * Test for the file storage.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class FileStorageTest extends TestCase {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileStorageTest.class);



    @Override
	protected void setUp() throws Exception {
		// Nothing to do
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		// Nothing to do
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
        final com.openexchange.filestore.FileStorage storage = new LocalFileStorage(tempFile.toURI());
        rmdir(tempFile);
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
            .getBytes(com.openexchange.java.Charsets.UTF_8));
        final com.openexchange.filestore.FileStorage storage = new LocalFileStorage(tempFile.toURI());
        final String identifier = storage.saveNewFile(baos);
        rmdir(tempFile);
        assertNotNull("Can't create new file in file storage.", identifier);
    }


    /**
     * Test for bug 3978.
     */
    public final void testExceptionOnUnavailableFilestore() throws Throwable {
        final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.delete();
        final String fileContent = RandomString.generateLetter(100);
        final ByteArrayInputStream baos = new ByteArrayInputStream(fileContent.getBytes(com.openexchange.java.Charsets.UTF_8));
        final com.openexchange.filestore.FileStorage storage = new LocalFileStorage(tempFile.toURI());
        final String identifier = storage.saveNewFile(baos);
        rmdir(tempFile);
        assertFalse(tempFile.exists());
        try {
            storage.getFile(identifier);
            fail("Expected IOException");
        } catch (OXException e) {
            // Everything fine. Error is discovered.
        }
        try {
            storage.saveNewFile(baos);
            fail("Expected IOException");
        } catch (OXException e) {
            // Everything fine. Error is discovered.
        }
    }

    /**
     * Test for bug 3978.
     */
    public final void testExceptionOnUnknown() throws Throwable {
        File tempFile =  File.createTempFile("filestorage", ".tmp");
        tempFile.delete();
        final com.openexchange.filestore.FileStorage storage = new LocalFileStorage(tempFile.toURI());
        try {
            storage.getFile("00/00/01");
            fail("Expected IOException");
        } catch (OXException e) {
            // Everything fine. Error is discovered.
        }
        rmdir(tempFile);
    }

    /**
     * Test for changes related to bug 19600, which was caused by creating directories
     * when checking for their existence (using the root user, not the open-xchange user).
     */
    public final void testDeleteFile() throws Throwable {
        final ByteArrayInputStream baos = new ByteArrayInputStream(
        		RandomString.generateLetter(100).getBytes(com.openexchange.java.Charsets.UTF_8));

        final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.delete();
        final com.openexchange.filestore.FileStorage storage = new LocalFileStorage(tempFile.toURI());
        final String identifier = storage.saveNewFile(baos);

        assertTrue( storage.deleteFile(identifier));
        assertFalse(storage.deleteFile(identifier));
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
