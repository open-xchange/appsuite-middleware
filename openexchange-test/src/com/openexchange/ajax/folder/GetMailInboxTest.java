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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Iterator;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.MailFolderUtility;

/**
 * {@link GetMailInboxTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class GetMailInboxTest extends AbstractAJAXSession {

    /**
     * Initializes a new {@link GetMailInboxTest}.
     *
     * @param name name of the test.
     */
    public GetMailInboxTest() {
        super();
    }

    @Test
    public void testGetMailInbox() throws Throwable {
        ListRequest request = new ListRequest(EnumAPI.OX_OLD, String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID));
        ListResponse response = getClient().execute(request);
        Iterator<FolderObject> iter = response.getFolder();
        FolderObject defaultIMAPFolder = null;
        String primaryMailFolder = MailFolderUtility.prepareFullname(0, MailFolder.ROOT_FOLDER_ID);
        while (iter.hasNext()) {
            final FolderObject fo = iter.next();
            if (fo.containsFullName() && primaryMailFolder.equals(fo.getFullName())) {
                defaultIMAPFolder = fo;
                break;
            }
        }
        assertNotNull("Default email folder not found.", defaultIMAPFolder);
        assertTrue("Default email folder has no subfolders.", defaultIMAPFolder.hasSubfolders());
        request = new ListRequest(EnumAPI.OX_OLD, defaultIMAPFolder.getFullName());
        response = getClient().execute(request);
        iter = response.getFolder();
        FolderObject inboxFolder = null;
        while (iter.hasNext()) {
            final FolderObject fo = iter.next();
            if (fo.getFullName().endsWith("INBOX")) {
                inboxFolder = fo;
                break;
            }
        }
        assertNotNull("Inbox folder for default mail account not found.", inboxFolder);
        GetRequest request2 = new GetRequest(EnumAPI.OX_OLD, inboxFolder.getFullName(), new int[] { FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME, FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS });
        GetResponse response2 = getClient().execute(request2);
        assertFalse("Get failed.", response2.hasError());
    }
}
