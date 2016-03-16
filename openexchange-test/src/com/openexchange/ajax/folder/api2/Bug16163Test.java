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
import java.util.Date;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.PathRequest;
import com.openexchange.ajax.folder.actions.PathResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.PermissionTools;

/**
 * {@link Bug16163Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug16163Test extends AbstractAJAXSession {

    private static final int[] ATTRIBUTES = { FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME };
    private AJAXClient client;
    private FolderObject testFolder;
    private AJAXClient client2;
    private int appointmentFolder;

    /**
     * Initializes a new {@link Bug16163Test}.
     *
     * @param name The name
     */
    public Bug16163Test(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        client2 = new AJAXClient(User.User2);
        testFolder = new FolderObject();
        testFolder.setModule(FolderObject.CALENDAR);
        appointmentFolder = client.getValues().getPrivateAppointmentFolder();
        testFolder.setParentFolderID(appointmentFolder);
        testFolder.setPermissions(PermissionTools.P(I(client.getValues().getUserId()), "a/a", I(client2.getValues().getUserId()), "v"));
        testFolder.setFolderName("testFolder4Bug16163-" + System.currentTimeMillis());
        final InsertRequest iReq = new InsertRequest(EnumAPI.OUTLOOK, testFolder);
        final InsertResponse iResp = client.execute(iReq);
        iResp.fillObject(testFolder);
        // Unfortunately no timestamp when creating a mail folder through Outlook folder tree.
        testFolder.setLastModified(new Date());
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(EnumAPI.OUTLOOK, testFolder));
        super.tearDown();
    }

    public void testPathRequestWorks() throws Throwable {
        {
            // Fill cache with database folder.
            final GetRequest request = new GetRequest(EnumAPI.OX_NEW, testFolder.getObjectID());
            client.execute(request);
        }
        {
            // Fill cache with outlook folder.
            final GetRequest request = new GetRequest(EnumAPI.OUTLOOK, testFolder.getObjectID());
            client.execute(request);
        }
        {
            // Test with user seeing a shared folder.
            final PathRequest request = new PathRequest(EnumAPI.OUTLOOK, testFolder.getObjectID(), ATTRIBUTES, false);
            final PathResponse response = client2.execute(request);
            final Object[][] data = response.getArray();
            assertFalse("Path request should work for that folder, but failed with: " + response.getErrorMessage(), response.hasError());
            final int idPos = response.getColumnPos(FolderObject.OBJECT_ID);
            assertTrue("Response should contain folder identifier.", idPos >= 0);
            final int namePos = response.getColumnPos(FolderObject.FOLDER_NAME);
            assertTrue("Response should contain folder names.", namePos >= 0);
            assertTrue("Path on Outlook like tree should have at least 4 parts, but has " + data.length, data.length >= 4);
            assertEquals("Path should start with test folder but is folder " + data[0][namePos], Integer.toString(testFolder.getObjectID()), data[0][idPos]);
            if (4 == data.length) {
                assertEquals("Parent of created folder should be virtual shared user folder but is " + data[1][namePos], FolderObject.SHARED_PREFIX + client.getValues().getUserId(), data[1][idPos]);
                assertEquals("Parent of virtual shared user folder should be system shared root folder but is " + data[2][namePos], Integer.toString(FolderObject.SYSTEM_SHARED_FOLDER_ID), data[2][idPos]);
                assertEquals("Root folder should be IPM_ROOT but is " + data[3][namePos], FolderStorage.PRIVATE_ID, data[3][idPos]);
            } else {
                assertEquals("Parent of created folder should be user1's Calendar folder but is " + data[1][namePos], Integer.toString(appointmentFolder), data[1][idPos]);
                assertEquals("Parent of created folder should be virtual shared user folder but is " + data[2][namePos], FolderObject.SHARED_PREFIX + client.getValues().getUserId(), data[2][idPos]);
                assertEquals("Parent of virtual shared user folder should be system shared root folder but is " + data[3][namePos], Integer.toString(FolderObject.SYSTEM_SHARED_FOLDER_ID), data[3][idPos]);
                assertEquals("Root folder should be IPM_ROOT but is " + data[4][namePos], FolderStorage.PRIVATE_ID, data[4][idPos]);
            }
        }
        {
            // Check cached folder.
            final GetRequest request = new GetRequest(EnumAPI.OUTLOOK, testFolder.getObjectID());
            final GetResponse response = client.execute(request);
            assertEquals("Identifier of cached folder is broken.", testFolder.getObjectID(), response.getFolder().getObjectID());
        }
        {
            // Test with sharing user if caching breaks his path.
            final PathRequest request = new PathRequest(EnumAPI.OUTLOOK, testFolder.getObjectID(), ATTRIBUTES, false);
            final PathResponse response = client.execute(request);
            final Object[][] data = response.getArray();
            assertFalse("Path request should work for that folder.", response.hasError());
            final int idPos = response.getColumnPos(FolderObject.OBJECT_ID);
            assertTrue("Response should contain folder identifier.", idPos >= 0);
            final int namePos = response.getColumnPos(FolderObject.FOLDER_NAME);
            assertTrue("Response should contain folder names.", namePos >= 0);
            assertEquals("Path on Outlook like tree should have 3 parts.", 3, data.length);
            assertEquals("Path should start with test folder but is folder " + data[0][namePos], Integer.toString(testFolder.getObjectID()), data[0][idPos]);
            assertEquals("Parent of created folder should be users private calendar folder but is " + data[1][namePos], Integer.toString(appointmentFolder), data[1][idPos]);
            assertEquals("Root folder should be IPM_ROOT but is " + data[2][namePos], FolderStorage.PRIVATE_ID, data[2][idPos]);
        }
    }
}
