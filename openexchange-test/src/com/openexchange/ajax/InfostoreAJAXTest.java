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

package com.openexchange.ajax;

import java.io.IOException;
import java.util.UUID;
import org.json.JSONException;
import org.junit.Before;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.TestInit;

public class InfostoreAJAXTest extends AbstractAJAXSession {

    protected static final int[] virtualFolders = { FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID };

    public static final String INFOSTORE_FOLDER = "infostore.folder";

    protected int folderId;

    protected String hostName = null;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.folderId = createFolderForTest();

        java.io.File upload = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));

        File file1 = InfostoreTestManager.createFile(folderId, "test knowledge", "text/plain");
        file1.setDescription("test knowledge description");
        itm.newAction(file1, upload);

        File file2 = InfostoreTestManager.createFile(folderId, "test url", "text/plain");
        file2.setURL("http://www.open-xchange.com");
        file2.setDescription("test url description");
        itm.newAction(file2, upload);
    }

    private int createFolderForTest() throws JSONException, OXException, IOException {
        final int parent = getClient().getValues().getPrivateInfostoreFolder();
        FolderObject folder = FolderTestManager.createNewFolderObject("NewInfostoreFolder" + UUID.randomUUID().toString(), Module.INFOSTORE.getFolderConstant(), FolderObject.PUBLIC, getClient().getValues().getUserId(), parent);
        return ftm.insertFolderOnServer(folder).getObjectID();
    }

    public File createFile(int folderId, String fileName) throws Exception {
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(folderId));
        file.setTitle(fileName);
        file.setFileName(file.getTitle());
        file.setDescription(file.getTitle());
        return file;
    }
}
