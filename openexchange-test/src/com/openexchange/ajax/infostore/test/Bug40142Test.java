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

package com.openexchange.ajax.infostore.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.io.IOException;
import java.util.Date;
import org.json.JSONException;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.TestInit;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * {@link Bug40142Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class Bug40142Test extends AbstractInfostoreTest {

    public Bug40142Test() {
        super();
    }

    @Test
    public void testCreatingTwoEquallyNamedFiles() throws OXException, IOException, SAXException, JSONException {
        final FolderObject folder = generateInfostoreFolder("InfostoreCreateDeleteTest Folder");
        ftm.insertFolderOnServer(folder);

        {
            final File expected = new DefaultFile();
            expected.setCreated(new Date());
            expected.setFolderId(String.valueOf(folder.getObjectID()));
            expected.setFileName("name.name.txt.pgp");
            expected.setLastModified(new Date());
            final java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));

            itm.newAction(expected, file);
            assertFalse("Creating an entry should work", itm.getLastResponse().hasError());

            final File actual = itm.getAction(expected.getId());
            assertEquals("Name should be the same", expected.getFileName(), actual.getFileName());
        }

        {
            final File expected = new DefaultFile();
            expected.setCreated(new Date());
            expected.setFolderId(String.valueOf(folder.getObjectID()));
            expected.setFileName("name.name.txt.pgp");
            expected.setLastModified(new Date());
            final java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));

            itm.newAction(expected, file);
            assertFalse("Creating an entry should work", itm.getLastResponse().hasError());

            final File actual = itm.getAction(expected.getId());
            assertEquals("Name should be the same", "name.name (1).txt.pgp", actual.getFileName());
        }
    }

    @Test
    public void testUpdateFileWithExistingName() throws OXException, IOException, SAXException, JSONException {
        final FolderObject folder = generateInfostoreFolder("InfostoreCreateDeleteTest Folder");
        ftm.insertFolderOnServer(folder);

        {
            final File expected = new DefaultFile();
            expected.setCreated(new Date());
            expected.setFolderId(String.valueOf(folder.getObjectID()));
            expected.setFileName("name.name.txt.pgp");
            expected.setLastModified(new Date());
            final java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));

            itm.newAction(expected, file);
            assertFalse("Creating an entry should work", itm.getLastResponse().hasError());

            final File actual = itm.getAction(expected.getId());
            assertEquals("Name should be the same", expected.getFileName(), actual.getFileName());
        }

        File actual;
        {
            final File expected = new DefaultFile();
            expected.setCreated(new Date());
            expected.setFolderId(String.valueOf(folder.getObjectID()));
            expected.setFileName("name.name.txt");
            expected.setLastModified(new Date());
            final java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));

            itm.newAction(expected, file);
            assertFalse("Creating an entry should work", itm.getLastResponse().hasError());

            actual = itm.getAction(expected.getId());
            assertEquals("Name should be the same", expected.getFileName(), actual.getFileName());
        }

        actual.setFileName("name.name.txt.pgp");
        itm.updateAction(actual, new File.Field[] { File.Field.FILENAME }, new Date());
        assertFalse("Creating an entry should work", itm.getLastResponse().hasError());

        actual = itm.getAction(actual.getId());
        assertEquals("Name should be the same", "name.name (1).txt.pgp", actual.getFileName());
    }

    @Test
    public void testCopyFile() throws OXException, IOException, SAXException, JSONException {

        final FolderObject folder = generateInfostoreFolder("InfostoreCreateDeleteTest Folder");
        ftm.insertFolderOnServer(folder);
        File actual;

        {
            final File expected = new DefaultFile();
            expected.setCreated(new Date());
            expected.setFolderId(String.valueOf(folder.getObjectID()));
            expected.setFileName("name.name.txt.pgp");
            expected.setLastModified(new Date());
            final java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));

            itm.newAction(expected, file);
            assertFalse("Creating an entry should work", itm.getLastResponse().hasError());

            actual = itm.getAction(expected.getId());
            assertEquals("Name should be the same", expected.getFileName(), actual.getFileName());
        }
        itm.copyAction(actual.getId(), folder.getObjectID() + "", actual);
        final String id = actual.getId();
        

        actual = itm.getAction(id);
        assertEquals("Name should be the same", "name.name (1).txt.pgp", actual.getFileName());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteFileWithExistingNameInTrash() throws OXException, IOException, SAXException, JSONException {
        final FolderObject folder = generateInfostoreFolder("InfostoreCreateDeleteTest Folder");
        ftm.insertFolderOnServer(folder);
        File actual;

        {
            final File expected = new DefaultFile();
            expected.setCreated(new Date());
            expected.setFolderId(String.valueOf(folder.getObjectID()));
            expected.setFileName("name.name.txt.pgp");
            expected.setLastModified(new Date());
            final java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));

            itm.newAction(expected, file);
            assertFalse("Creating an entry should work", itm.getLastResponse().hasError());

            actual = itm.getAction(expected.getId());
            assertEquals("Name should be the same", expected.getFileName(), actual.getFileName());
        }
        final String oldID = actual.getId();
        itm.deleteAction(Collections.singletonList(actual.getId()), Collections.singletonList(actual.getFolderId()), new Date(), false);
        assertFalse("Deleting an entry should work", itm.getLastResponse().hasError());
        actual = null;

        {
            final File expected = new DefaultFile();
            expected.setCreated(new Date());
            expected.setFolderId(String.valueOf(folder.getObjectID()));
            expected.setFileName("name.name.txt.pgp");
            expected.setLastModified(new Date());
            final java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));

            itm.newAction(expected, file);
            assertFalse("Creating an entry should work", itm.getLastResponse().hasError());

            actual = itm.getAction(expected.getId());
            assertEquals("Name should be the same", expected.getFileName(), actual.getFileName());
        }

        itm.deleteAction(Collections.singletonList(actual.getId()), Collections.singletonList(actual.getFolderId()), new Date(), false);

        assertFalse("Deleting an entry should work", itm.getLastResponse().hasError());
        final int result = (int) itm.getConfigAction("/modules/infostore/folder/trash");
        final String id1 = result + "/" + oldID.substring(oldID.indexOf("/") + 1, oldID.length());
        final String id2 = result + "/" + actual.getId().substring(actual.getId().indexOf("/") + 1, actual.getId().length());
        actual = itm.getAction(id2);
        assertEquals("Name should be the same", "name.name (1).txt.pgp", actual.getFileName());

        itm.deleteAction(Collections.singletonList(id1), Collections.singletonList(result), new Date(), true);
        itm.deleteAction(Collections.singletonList(id2), Collections.singletonList(result), new Date(), true);
    }

}
