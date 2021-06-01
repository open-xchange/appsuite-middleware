/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax;

import static org.junit.Assert.assertFalse;
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
import com.openexchange.test.common.test.TestInit;

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
        assertFalse("Unexpected error: " + itm.getLastResponse().getErrorMessage(), itm.getLastResponse().hasError());

        File file2 = InfostoreTestManager.createFile(folderId, "test url", "text/plain");
        file2.setURL("http://www.open-xchange.com");
        file2.setDescription("test url description");
        itm.newAction(file2, upload);
        assertFalse("Unexpected error: " + itm.getLastResponse().getErrorMessage(), itm.getLastResponse().hasError());
    }

    private int createFolderForTest() throws JSONException, OXException, IOException {
        final int parent = getClient().getValues().getPrivateInfostoreFolder();
        FolderObject folder = FolderTestManager.createNewFolderObject("NewInfostoreFolder" + UUID.randomUUID().toString(), Module.INFOSTORE.getFolderConstant(), FolderObject.PUBLIC, getClient().getValues().getUserId(), parent);
        return ftm.insertFolderOnServer(folder).getObjectID();
    }

    public File createFile(int folderId, String fileName) {
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(folderId));
        file.setTitle(fileName);
        file.setFileName(file.getTitle());
        file.setDescription(file.getTitle());
        return file;
    }
}
