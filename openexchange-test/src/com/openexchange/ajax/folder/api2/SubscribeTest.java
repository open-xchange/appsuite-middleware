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
import org.json.JSONArray;
import com.openexchange.ajax.folder.actions.API;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.SubscribeRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.PermissionTools;

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
    public SubscribeTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testFolder = new FolderObject();
        testFolder.setModule(FolderObject.CALENDAR);
        appointmentFolder = client.getValues().getPrivateAppointmentFolder();
        testFolder.setParentFolderID(appointmentFolder);
        testFolder.setPermissions(PermissionTools.P(I(client.getValues().getUserId()), "a/a"));
        testFolder.setFolderName("SubscribeTest-" + System.currentTimeMillis());
        final InsertRequest iReq = new InsertRequest(EnumAPI.OX_NEW, testFolder);
        final InsertResponse iResp = client.execute(iReq);
        iResp.fillObject(testFolder);
    }

    @Override
    protected void tearDown() throws Exception {
        getClient().execute(new DeleteRequest(EnumAPI.EAS_FOLDERS, testFolder));
        super.tearDown();
    }

    public void testSubscribe() throws Throwable {
        final API api = EnumAPI.EAS_FOLDERS;
        final int fuid = testFolder.getObjectID();
        final SubscribeRequest subscribeRequest = new SubscribeRequest(api, FolderStorage.ROOT_ID, true);
        subscribeRequest.addFolderId(Integer.toString(fuid), true);
        /*final SubscribeResponse subscribeResponse = */getClient().execute(subscribeRequest);

        final int[] columns = {
            FolderObject.OBJECT_ID, FolderObject.FOLDER_ID, FolderObject.FOLDER_NAME, FolderObject.MODULE, FolderObject.SUBFOLDERS, FolderObject.STANDARD_FOLDER,
            FolderObject.CREATED_BY, 3040 };
        final ListRequest listRequest = new ListRequest(api, FolderStorage.ROOT_ID, columns, true);
        final ListResponse listResponse = client.execute(listRequest);

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
