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

package com.openexchange.ajax.folder;

import static com.openexchange.java.Autoboxing.I;
import java.util.Date;
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

    private static final int[] COLUMNS = {
        FolderObject.OBJECT_ID, FolderObject.CREATED_BY, FolderObject.MODIFIED_BY, FolderObject.CREATION_DATE, FolderObject.LAST_MODIFIED,
        FolderObject.LAST_MODIFIED_UTC, FolderObject.FOLDER_ID, FolderObject.FOLDER_NAME, FolderObject.MODULE, FolderObject.TYPE,
        FolderObject.SUBFOLDERS, FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS, FolderObject.SUMMARY,
        FolderObject.STANDARD_FOLDER, FolderObject.TOTAL, FolderObject.NEW, FolderObject.UNREAD, FolderObject.DELETED,
        FolderObject.CAPABILITIES, FolderObject.SUBSCRIBED, FolderObject.SUBSCR_SUBFLDS, 3010, 3020 };

    private AJAXClient client;
    private FolderObject createdFolder;
    private Date before;
    private boolean folderDeleted = false;

    public Bug17027Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        createdFolder = Create.createPrivateFolder("Test for bug 17027", FolderObject.CALENDAR, client.getValues().getUserId());
        createdFolder.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        InsertResponse response = client.execute(new InsertRequest(EnumAPI.OX_NEW, createdFolder));
        response.fillObject(createdFolder);
        before = new Date(createdFolder.getLastModified().getTime() - 1);
    }

    @Override
    protected void tearDown() throws Exception {
        if (!folderDeleted) {
            client.execute(new DeleteRequest(EnumAPI.OX_NEW, createdFolder));
        }
        super.tearDown();
    }

    public void testUpdates() throws Throwable {
        FolderUpdatesResponse response = client.execute(new UpdatesRequest(EnumAPI.OX_NEW, COLUMNS, -1, null, before, Ignore.NONE));
        boolean found = false;
        for (FolderObject folder : response.getFolders()) {
            if (createdFolder.getObjectID() == folder.getObjectID()) {
                found = true;
            }
        }
        assertTrue("Newly created folder not found.", found);
        assertFalse(
            "Newly created folder should not be contained in deleted list.",
            response.getDeletedIds().contains(I(createdFolder.getObjectID())));
        client.execute(new DeleteRequest(EnumAPI.OX_NEW, createdFolder));
        folderDeleted = true;
        response = client.execute(new UpdatesRequest(EnumAPI.OX_NEW, COLUMNS, -1, null, before, Ignore.NONE));
        for (FolderObject folder : response.getFolders()) {
            assertFalse("By other user newly created private folder is returned in updates response.", createdFolder.getObjectID() == folder.getObjectID());
        }
        assertTrue("Deleted list should contain deleted folder identifier.", response.getDeletedIds().contains(I(createdFolder.getObjectID())));
    }
}
