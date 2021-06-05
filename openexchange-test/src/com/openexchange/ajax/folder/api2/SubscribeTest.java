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

package com.openexchange.ajax.folder.api2;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Iterator;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.API;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.SubscribeRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.common.test.PermissionTools;

/**
 * {@link SubscribeTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class SubscribeTest extends AbstractAJAXSession {

    private FolderObject testFolder;
    private int appointmentFolder;

    /**
     * Initializes a new {@link SubscribeTest}.
     *
     * @param name The name
     */
    public SubscribeTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testFolder = new FolderObject();
        testFolder.setModule(FolderObject.CALENDAR);
        appointmentFolder = getClient().getValues().getPrivateAppointmentFolder();
        testFolder.setParentFolderID(appointmentFolder);
        testFolder.setPermissions(PermissionTools.P(I(getClient().getValues().getUserId()), "a/a"));
        testFolder.setFolderName("SubscribeTest-" + System.currentTimeMillis());
        final InsertRequest iReq = new InsertRequest(EnumAPI.OX_NEW, testFolder);
        final InsertResponse iResp = getClient().execute(iReq);
        iResp.fillObject(testFolder);
    }

    @Test
    public void testSubscribe() throws Throwable {
        final API api = EnumAPI.EAS_FOLDERS;
        final int fuid = testFolder.getObjectID();
        final SubscribeRequest subscribeRequest = new SubscribeRequest(api, FolderStorage.ROOT_ID, true);
        subscribeRequest.addFolderId(Integer.toString(fuid), true);
        /* final SubscribeResponse subscribeResponse = */getClient().execute(subscribeRequest);

        final int[] columns = { FolderObject.OBJECT_ID, FolderObject.FOLDER_ID, FolderObject.FOLDER_NAME, FolderObject.MODULE, FolderObject.SUBFOLDERS, FolderObject.STANDARD_FOLDER, FolderObject.CREATED_BY, 3040 };
        final ListRequest listRequest = new ListRequest(api, FolderStorage.ROOT_ID, columns, true);
        final ListResponse listResponse = getClient().execute(listRequest);

        {
            boolean found = false;
            for (final Iterator<FolderObject> iterator = listResponse.getFolder(); iterator.hasNext();) {
                final FolderObject folderObject = iterator.next();
                if (fuid == folderObject.getObjectID()) {
                    found = true;
                    break;
                }
            }
            assertTrue("Subscribed test folder could not be found in tree " + api.getTreeId(), found);
        }

        for (final Iterator<FolderObject> iterator = listResponse.getFolder(); iterator.hasNext();) {
            final FolderObject folderObject = iterator.next();
            assertFalse("Folder has subfolders, but shouldn't.", folderObject.hasSubfolders());
        }

        final JSONArray retArray = (JSONArray) listResponse.getData();
        final int length = retArray.length();
        for (int i = 0; i < length; i++) {
            final JSONArray folderArray = retArray.getJSONArray(i);
            if (folderArray.getBoolean(5)) {
                assertTrue("Should be preDefined, but isn't.", folderArray.getBoolean(7));
            } else {
                assertFalse("Shouldn't be preDefined, but is.", folderArray.getBoolean(7));
            }
        }
    }

}
