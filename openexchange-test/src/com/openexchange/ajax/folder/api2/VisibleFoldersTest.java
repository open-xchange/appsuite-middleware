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

package com.openexchange.ajax.folder.api2;

import static com.openexchange.java.Autoboxing.I;
import java.util.Iterator;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.VisibleFoldersRequest;
import com.openexchange.ajax.folder.actions.VisibleFoldersResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.PermissionTools;

/**
 * {@link VisibleFoldersTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class VisibleFoldersTest extends AbstractAJAXSession {

    private AJAXClient clientA;

    private AJAXClient clientB;

    private FolderObject createdPrivateFolder;

    private FolderObject createdPublicFolder;

    private FolderObject createdSharedFolder;

    public VisibleFoldersTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clientA = getClient();
        clientB = new AJAXClient(User.User2);

        {
            createdPrivateFolder = new FolderObject();
            createdPrivateFolder.setModule(FolderObject.CALENDAR);
            createdPrivateFolder.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
            createdPrivateFolder.setType(FolderObject.PRIVATE);
            createdPrivateFolder.setPermissions(PermissionTools.P(I(clientA.getValues().getUserId()), PermissionTools.ADMIN));
            createdPrivateFolder.setFolderName("testPrivateCalendarFolder" + System.currentTimeMillis());
            final InsertRequest iReq = new InsertRequest(EnumAPI.OUTLOOK, createdPrivateFolder);
            final InsertResponse iResp = client.execute(iReq);
            iResp.fillObject(createdPrivateFolder);
        }

        {
            createdPublicFolder = new FolderObject();
            createdPublicFolder.setModule(FolderObject.CALENDAR);
            createdPublicFolder.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
            createdPublicFolder.setType(FolderObject.PUBLIC);
            createdPublicFolder.setPermissions(PermissionTools.P(I(clientA.getValues().getUserId()), PermissionTools.ADMIN));
            createdPublicFolder.setFolderName("testPublicCalendarFolder" + System.currentTimeMillis());
            final InsertRequest iReq = new InsertRequest(EnumAPI.OUTLOOK, createdPublicFolder);
            final InsertResponse iResp = client.execute(iReq);
            iResp.fillObject(createdPublicFolder);
        }

        {
            createdSharedFolder = new FolderObject();
            createdSharedFolder.setModule(FolderObject.CALENDAR);
            createdSharedFolder.setParentFolderID(clientB.getValues().getPrivateAppointmentFolder());
            createdSharedFolder.setType(FolderObject.PRIVATE);
            createdSharedFolder.setPermissions(PermissionTools.P(
                I(clientB.getValues().getUserId()),
                PermissionTools.ADMIN,
                I(clientA.getValues().getUserId()),
                "arawada"));
            createdSharedFolder.setFolderName("testSharedCalendarFolder" + System.currentTimeMillis());
            final InsertRequest iReq = new InsertRequest(EnumAPI.OUTLOOK, createdSharedFolder);
            final InsertResponse iResp = clientB.execute(iReq);
            iResp.fillObject(createdSharedFolder);
        }

    }

    @Override
    protected void tearDown() throws Exception {
        clientA.execute(new DeleteRequest(EnumAPI.OUTLOOK, createdPrivateFolder));
        clientA.execute(new DeleteRequest(EnumAPI.OUTLOOK, createdPublicFolder));
        clientB.execute(new DeleteRequest(EnumAPI.OUTLOOK, createdSharedFolder));
        super.tearDown();
    }

    public void testForVisibleFolders() throws Throwable {
        final VisibleFoldersRequest req = new VisibleFoldersRequest(EnumAPI.OUTLOOK, "calendar");
        final VisibleFoldersResponse resp = client.execute(req);
        /*
         * Iterate private folder and look-up previously created private folder
         */
        {
            final Iterator<FolderObject> privIter = resp.getPrivateFolders();
            boolean found = false;
            while (privIter.hasNext()) {
                final FolderObject privateFolder = privIter.next();
                if (privateFolder.getObjectID() == createdPrivateFolder.getObjectID()) {
                    found = true;
                    break;
                }
            }
            assertTrue("Previously created private folder not found in private folders.", found);
        }
        /*
         * Iterate public folder and look-up previously created public folder
         */
        {
            final Iterator<FolderObject> pubIter = resp.getPublicFolders();
            boolean found = false;
            while (pubIter.hasNext()) {
                final FolderObject publicFolder = pubIter.next();
                if (publicFolder.getObjectID() == createdPublicFolder.getObjectID()) {
                    found = true;
                    break;
                }
            }
            assertTrue("Previously created public folder not found in public folders.", found);
        }
        /*
         * Iterate shared folder and look-up previously created shared folder
         */
        {
            final Iterator<FolderObject> sharedIter = resp.getSharedFolders();
            boolean found = false;
            while (sharedIter.hasNext()) {
                final FolderObject sharedFolder = sharedIter.next();
                if (sharedFolder.getObjectID() == createdSharedFolder.getObjectID()) {
                    found = true;
                    break;
                }
            }
            assertTrue("Previously created shared folder not found in shared folders.", found);
        }
    }

    public void testFindingGlobalAddressbook() throws Exception {
        final VisibleFoldersRequest req = new VisibleFoldersRequest(EnumAPI.OUTLOOK, "contacts");
        final VisibleFoldersResponse resp = client.execute(req);
        final Iterator<FolderObject> publicFolders = resp.getPublicFolders();

        boolean globalAddressBookFound = false;
        while (publicFolders.hasNext()) {
            if (publicFolders.next().getObjectID() == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
                globalAddressBookFound = true;
            }
        }

        assertTrue("Could not find the global address book", globalAddressBookFound);
    }

}
