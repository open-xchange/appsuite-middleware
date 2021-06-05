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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.PathRequest;
import com.openexchange.ajax.folder.actions.PathResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;

/**
 * Verifies that primary mail account's root folder throws an error when invoking action=path.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug16284Test extends AbstractAJAXSession {

    private static final int[] COLUMNS = { FolderObject.OBJECT_ID, FolderObject.FOLDER_ID, FolderObject.CREATED_BY, FolderObject.MODIFIED_BY, FolderObject.FOLDER_NAME, FolderObject.MODULE, FolderObject.TYPE, FolderObject.SUBFOLDERS, FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS, FolderObject.SUMMARY, FolderObject.STANDARD_FOLDER, FolderObject.TOTAL, FolderObject.NEW, FolderObject.UNREAD, FolderObject.DELETED, FolderObject.CAPABILITIES, FolderObject.SUBSCRIBED, FolderObject.SUBSCR_SUBFLDS, 316 };

    private AJAXClient client;

    public Bug16284Test() {
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
        final PathRequest request = new PathRequest(EnumAPI.OUTLOOK, MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, MailFolder.ROOT_FOLDER_ID), COLUMNS, false);
        final PathResponse response = client.execute(request);

        if (!response.hasError()) {
            fail("Expected an error that primary account's root folder does not exist in Outlook folder tree.");
            return;
        }
        final OXException exception = response.getException();
        assertTrue("Unexpected error number.", FolderExceptionErrorMessage.prefix().equals(exception.getPrefix()) && FolderExceptionErrorMessage.NOT_FOUND.getNumber() == exception.getCode());
    }
}
