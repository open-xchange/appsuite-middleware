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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.ajax.infostore.test;

import java.io.IOException;
import java.util.Date;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.TestInit;

/**
 * {@link Bug40618Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class Bug40618Test extends AbstractInfostoreTest {

    public Bug40618Test(String name) {
        super(name);
    }

    private final static char[] INVALID_CHARS = { '"', '?', ':', '*', '<', '>', '|', '\\' };

    public void testRenamingFileWithInvalidWindowsChar() throws OXException, IOException, SAXException, JSONException {

        final FolderObject folder = generateInfostoreFolder("InfostoreWindowsFilenameCharValidation Folder");
        fMgr.insertFolderOnServer(folder);
        File actual;
        {
            final File expected = new DefaultFile();
            expected.setCreated(new Date());
            expected.setFolderId(String.valueOf(folder.getObjectID()));
            expected.setFileName("name.txt");
            expected.setLastModified(new Date());
            final java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));

            infoMgr.newAction(expected, file);
            assertFalse("Creating an entry should work", infoMgr.getLastResponse().hasError());

            actual = infoMgr.getAction(expected.getId());
            assertEquals("Name should be the same", expected.getFileName(), actual.getFileName());
        }

        for (char invalid : INVALID_CHARS) {

            actual.setFileName("name" + invalid + ".txt");
            infoMgr.updateAction(actual, new File.Field[] { File.Field.FILENAME }, new Date());
            assertTrue("Updateaction Should throw an error", infoMgr.getLastResponse().hasError());
            assertEquals("Should be the error code for invalid characters", infoMgr.getLastResponse().getException().getCode(), 2101);
        }
    }

    public void testCreatingFileWithInvalidWindowsChar() throws OXException, IOException, SAXException, JSONException {

        final FolderObject folder = generateInfostoreFolder("InfostoreWindowsFilenameCharValidation Folder");
        fMgr.insertFolderOnServer(folder);
        final File file = new DefaultFile();
        file.setCreated(new Date());
        file.setFolderId(String.valueOf(folder.getObjectID()));
        file.setLastModified(new Date());
        final java.io.File data = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));

        for (char invalid : INVALID_CHARS) {

            file.setFileName("name" + invalid + ".txt");
            infoMgr.newAction(file, data);
            assertTrue("Updateaction Should throw an error", infoMgr.getLastResponse().hasError());
            assertEquals("Should be the error code for invalid characters", infoMgr.getLastResponse().getException().getCode(), 2101);
        }
    }
}
