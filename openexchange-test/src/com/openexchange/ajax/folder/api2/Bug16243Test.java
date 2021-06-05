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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.AllowedModules;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.groupware.container.FolderObject;

/**
 * Verifies that InfoStore folder can be excluded from list requests.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug16243Test extends AbstractAJAXSession {

    private static final int[] COLUMNS = { FolderObject.OBJECT_ID, FolderObject.FOLDER_ID, FolderObject.CREATED_BY, FolderObject.MODIFIED_BY, FolderObject.FOLDER_NAME, FolderObject.MODULE, FolderObject.TYPE, FolderObject.SUBFOLDERS, FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS, FolderObject.SUMMARY, FolderObject.STANDARD_FOLDER, FolderObject.TOTAL, FolderObject.NEW, FolderObject.UNREAD, FolderObject.DELETED, FolderObject.CAPABILITIES, FolderObject.SUBSCRIBED, FolderObject.SUBSCR_SUBFLDS, 316 };

    private AJAXClient client;

    public Bug16243Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    @Test
    public void testListWithoutInfoStore() throws Throwable {
        ListRequest request = new ListRequest(EnumAPI.OUTLOOK, FolderStorage.PRIVATE_ID, COLUMNS, false);
        request.setAllowedModules(AllowedModules.CALENDAR, AllowedModules.MAIL, AllowedModules.CONTACTS, AllowedModules.TASKS);
        ListResponse response = client.execute(request);
        Iterator<FolderObject> iter = response.getFolder();
        assertTrue("List request does not return any folders.", iter.hasNext());
        while (iter.hasNext()) {
            FolderObject folder = iter.next();
            assertNotSame("Found some infostore folder.", I(InfostoreContentType.getInstance().getModule()), I(folder.getModule()));
        }
    }
}
