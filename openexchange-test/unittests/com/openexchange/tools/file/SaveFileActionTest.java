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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.impl.LocalFileStorageFactory;
import com.openexchange.groupware.tx.AbstractActionTest;
import com.openexchange.java.Streams;
import com.openexchange.tx.UndoableAction;

public class SaveFileActionTest extends AbstractActionTest {

	private static final String content = "I am the test content";

	private File tempFile;

	private SaveFileAction saveFile = null;
	private com.openexchange.filestore.FileStorage storage = null;

	@Override
    protected void setUp() throws Exception {
        super.setUp();
        tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.delete();
    }

    @Override
    protected void tearDown() throws Exception {
        rmdir(tempFile);
        super.tearDown();
    }

    private static void rmdir(final File tempFile) {
        if (tempFile.isDirectory()) {
            for (final File f : tempFile.listFiles()) {
                rmdir(f);
            }
        }
        tempFile.delete();
    }

	@Override
	protected UndoableAction getAction() throws Exception {
		storage = new LocalFileStorageFactory().getFileStorage(tempFile.toURI());
		saveFile = new SaveFileAction(storage, new ByteArrayInputStream(content.getBytes(com.openexchange.java.Charsets.UTF_8)), 0);
		return saveFile;
	}

	@Override
	protected void verifyPerformed() throws Exception {
		assertTrue(null != saveFile.getFileStorageID());
		InputStream in = null;
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			in = new BufferedInputStream(storage.getFile(saveFile.getFileStorageID()), 65536);
			int b = 0;
			while((b = in.read()) != -1) {
				out.write(b);
			}
			out.flush();
		} finally {
		    Streams.close(in, out);
		}
		final String got = new String(out.toByteArray(), com.openexchange.java.Charsets.UTF_8);
		assertEquals(content, got);
	}

	@Override
	protected void verifyUndone() throws Exception {
		try {
			storage.getFile(saveFile.getFileStorageID());
			fail("Expected Exception");
		} catch (final OXException x) {
			assertTrue(true);
		}
	}
}
