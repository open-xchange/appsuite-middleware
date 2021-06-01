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

package com.openexchange.ajax.folder;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.FolderUpdatesResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.UpdatesRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.AbstractUpdatesRequest.Ignore;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug17027Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug17027Test extends AbstractAJAXSession {

    private static final int[] COLUMNS = { FolderObject.OBJECT_ID, FolderObject.CREATED_BY, FolderObject.MODIFIED_BY, FolderObject.CREATION_DATE, FolderObject.LAST_MODIFIED, FolderObject.LAST_MODIFIED_UTC, FolderObject.FOLDER_ID, FolderObject.FOLDER_NAME, FolderObject.MODULE, FolderObject.TYPE, FolderObject.SUBFOLDERS, FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS, FolderObject.SUMMARY, FolderObject.STANDARD_FOLDER, FolderObject.TOTAL, FolderObject.NEW, FolderObject.UNREAD, FolderObject.DELETED, FolderObject.CAPABILITIES, FolderObject.SUBSCRIBED, FolderObject.SUBSCR_SUBFLDS, 3010, 3020 };

    private AJAXClient client;
    private FolderObject createdFolder;
    private Date before;

    public Bug17027Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        createdFolder = Create.createPrivateFolder("Test for bug 17027", FolderObject.CALENDAR, client.getValues().getUserId());
        createdFolder.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        InsertResponse response = client.execute(new InsertRequest(EnumAPI.OX_NEW, createdFolder));
        response.fillObject(createdFolder);
        before = new Date(createdFolder.getLastModified().getTime() - 1);
    }

    @Test
    public void testUpdates() throws Throwable {
        FolderUpdatesResponse response = client.execute(new UpdatesRequest(EnumAPI.OX_NEW, COLUMNS, -1, null, before, Ignore.NONE));
        boolean found = false;
        for (FolderObject folder : response.getFolders()) {
            if (createdFolder.getObjectID() == folder.getObjectID()) {
                found = true;
            }
        }
        assertTrue("Newly created folder not found.", found);
        assertFalse("Newly created folder should not be contained in deleted list.", response.getDeletedIds().contains(I(createdFolder.getObjectID())));
        client.execute(new DeleteRequest(EnumAPI.OX_NEW, createdFolder));
        response = client.execute(new UpdatesRequest(EnumAPI.OX_NEW, COLUMNS, -1, null, before, Ignore.NONE));
        for (FolderObject folder : response.getFolders()) {
            assertFalse("By other user newly created private folder is returned in updates response.", createdFolder.getObjectID() == folder.getObjectID());
        }
        assertTrue("Deleted list should contain deleted folder identifier.", response.getDeletedIds().contains(I(createdFolder.getObjectID())));
    }
}
