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

import java.util.List;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link ShareFolderTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ShareFolderTest extends AbstractAJAXSession {

    private AJAXClient secondClient;
    private UserValues secondUserValues;
    private FolderObject testFolder;
    private int parentId = -1;

    /**
     * Initializes a new {@link ShareFolderTest}.
     * @param name name of the test.
     */
    public ShareFolderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Create 2. User
        secondClient = new AJAXClient(User.User2);
        secondUserValues = secondClient.getValues();

        // Create folder
        final OCLPermission perm1 =
            Create.ocl(
                client.getValues().getUserId(),
                false,
                true,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION);
        final OCLPermission perm2 =
            Create.ocl(
                secondUserValues.getUserId(),
                false,
                false,
                OCLPermission.CREATE_OBJECTS_IN_FOLDER,
                OCLPermission.READ_ALL_OBJECTS,
                OCLPermission.NO_PERMISSIONS,
                OCLPermission.NO_PERMISSIONS);
        parentId = client.getValues().getPrivateAppointmentFolder();
        testFolder = Create.folder(parentId, "TestShared" + System.currentTimeMillis(), FolderObject.CALENDAR, FolderObject.PRIVATE, perm1, perm2);
        InsertRequest insFolder = new InsertRequest(EnumAPI.OX_OLD, testFolder);
        InsertResponse folderInsertResponse = client.execute(insFolder);
        testFolder.setObjectID(folderInsertResponse.getId());
        testFolder.setLastModified(client.execute(new GetRequest(EnumAPI.OX_OLD, testFolder.getObjectID())).getTimestamp());
    }

    @Override
    public void tearDown() throws Exception {
        if (secondClient != null) {
            secondClient.logout();
            secondClient = null;
            secondUserValues = null;
        }

        // Delete testFolder
        if (testFolder != null) {
            client.execute(new DeleteRequest(EnumAPI.OX_OLD, testFolder));
            testFolder = null;
            parentId = -1;
        }

        super.tearDown();
    }

    public void testShareFolder() throws Throwable {
        final int folderId = testFolder.getObjectID();

        final int shareFolderId = FolderObject.SYSTEM_SHARED_FOLDER_ID;
        final List<FolderObject> l = FolderTools.getSubFolders(secondClient, Integer.toString(shareFolderId), true);
        assertTrue("No shared subfolder available for second user " + secondUserValues.getUserId(), l != null && !l.isEmpty());

        /*-
         * Expected:
         *
         * - Shared folders
         *       |
         *        - ...
         *       |
         *        - Calendar
         *              |
         *              - TestShared...
         */

        boolean found = false;
        Next: for (FolderObject virtualFO : l) {
            final List<FolderObject> subList = FolderTools.getSubFolders(secondClient, virtualFO.getFullName(), true);
            for (final FolderObject sharedFolder : subList) {
                if (sharedFolder.getObjectID() == parentId) {

                    final List<FolderObject> subsubList = FolderTools.getSubFolders(secondClient, Integer.toString(parentId), true);
                    for (final FolderObject subsharedFolder : subsubList) {
                        if (subsharedFolder.getObjectID() == folderId) {
                            found = true;
                            break Next;
                        }
                    }

                } else if (sharedFolder.getObjectID() == folderId) {
                    found = true;
                    break Next;
                }
            }
        }
        assertTrue("Folder " + folderId + " not beneath shared folder of second user " + secondUserValues.getUserId(), found);
    }

}
