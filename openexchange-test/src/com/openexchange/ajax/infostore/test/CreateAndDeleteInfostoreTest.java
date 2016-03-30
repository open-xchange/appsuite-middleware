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

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.TestInit;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CreateAndDeleteInfostoreTest extends AbstractInfostoreTest {

    public CreateAndDeleteInfostoreTest(String name) {
        super(name);
    }

    public void testCreatingOneItem() throws OXException, IOException, SAXException, JSONException, OXException {
        FolderObject folder = generateInfostoreFolder("InfostoreCreateDeleteTest Folder");
        fMgr.insertFolderOnServer(folder);

        File expected = new DefaultFile();
        expected.setCreated(new Date());
        expected.setFolderId(String.valueOf(folder.getObjectID()));
        expected.setTitle("InfostoreCreateDeleteTest Item");
        expected.setLastModified(new Date());
        final Map<String, Object> meta = new LinkedHashMap<String, Object>(2);
        meta.put("customField0012", "value0012");
        expected.setMeta(meta);

        infoMgr.newAction(expected);
        assertFalse("Creating an entry should work", infoMgr.getLastResponse().hasError());

        File actual = infoMgr.getAction(expected.getId());
        assertEquals("Name should be the same", expected.getTitle(), actual.getTitle());

        infoMgr.deleteAction(expected);
        assertFalse("Deleting an entry should work", infoMgr.getLastResponse().hasError());
    }

    public void testCreatingOneItemWithFile() throws Exception {

        FolderObject folder = generateInfostoreFolder("InfostoreCreateDeleteTest Folder");
        fMgr.insertFolderOnServer(folder);

        File expected = new DefaultFile();
        expected.setCreated(new Date());
        expected.setFolderId(String.valueOf(folder.getObjectID()));
        expected.setTitle("InfostoreCreateDeleteTest File");
        expected.setLastModified(new Date());
        new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));

        infoMgr.newAction(expected);//TODO, upload);
        assertFalse("Creating an entry should work", infoMgr.getLastResponse().hasError());

        File actual = infoMgr.getAction(expected.getId());
        assertEquals("Name should be the same", expected.getTitle(), actual.getTitle());

        infoMgr.deleteAction(expected);
        assertFalse("Deleting an entry should work", infoMgr.getLastResponse().hasError());
    }

    public void testCreatingTwoItemWithFiles() throws Exception {

        FolderObject folder = generateInfostoreFolder("InfostoreCreateDeleteTest Folder");
        fMgr.insertFolderOnServer(folder);

        {
            File expected = new DefaultFile();
            expected.setCreated(new Date());
            expected.setFolderId(String.valueOf(folder.getObjectID()));
            expected.setTitle("InfostoreCreateDeleteTest File1");
            expected.setLastModified(new Date());
            java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));

            infoMgr.newAction(expected, file);
            assertFalse("Creating an entry should work", infoMgr.getLastResponse().hasError());

            File actual = infoMgr.getAction(expected.getId());
            assertEquals("Name should be the same", expected.getTitle(), actual.getTitle());
        }

        {
            File expected = new DefaultFile();
            expected.setCreated(new Date());
            expected.setFolderId(String.valueOf(folder.getObjectID()));
            expected.setTitle("InfostoreCreateDeleteTest File2");
            expected.setLastModified(new Date());
            java.io.File file = new java.io.File(TestInit.getTestProperty("webdavPropertiesFile"));

            infoMgr.newAction(expected, file);
            assertFalse("Creating an entry should work", infoMgr.getLastResponse().hasError());

            File actual = infoMgr.getAction(expected.getId());
            assertEquals("Name should be the same", expected.getTitle(), actual.getTitle());
        }
    }
}
