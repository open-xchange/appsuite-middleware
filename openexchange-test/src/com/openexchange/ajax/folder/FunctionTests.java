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

import java.io.IOException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.folder.actions.API;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link FunctionTests}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class FunctionTests extends AbstractAJAXSession {

    private AJAXClient client;
    private AJAXClient client2;

    public FunctionTests(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        client2 = new AJAXClient(User.User2);
    }

    @Override
    protected void tearDown() throws Exception {
        client2.logout();
        super.tearDown();
    }

    public void testUnknownAction() throws IOException, JSONException, OXException {
        GetResponse response = client.execute(new UnknownActionRequest(EnumAPI.OX_OLD, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, false));
        assertTrue("JSON response should contain an error message.", response.hasError());
        OXException exception = response.getException();
        String error = exception.getErrorCode(); //was: getOrigMessage, maybe it should be .getCause().getMessage()?
        assertTrue(
            "Error is not the expected one: \"" + error + "\"", error.equals("SVL-0001"));
    }

    private class UnknownActionRequest extends GetRequest {

        UnknownActionRequest(API api, int folderId, boolean failOnError) {
            super(api, folderId, failOnError);
        }

        @Override
        protected void addParameters(List<Parameter> params) {
            params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "unknown"));
            params.add(new Parameter(AJAXServlet.PARAMETER_ID, getFolderIdentifier()));
            params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, getColumns()));
        }
    }

    public void testInsertUpdateFolder() throws OXException, IOException, JSONException, OXException, OXException {
        FolderObject toDelete = null;
        int userId1 = client.getValues().getUserId();
        int userId2 = client2.getValues().getUserId();
        try {
            FolderObject folder = Create.createPrivateFolder("ChangeMyPermissions" + System.currentTimeMillis(), FolderObject.CALENDAR, userId1);
            folder.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
            InsertResponse insertR = client.execute(new InsertRequest(EnumAPI.OX_OLD, folder));
            GetResponse getR = client.execute(new GetRequest(EnumAPI.OX_OLD, insertR.getId()));
            toDelete = getR.getFolder();
            toDelete.setLastModified(getR.getTimestamp());
            FolderObject inserted = getR.getFolder();
            FolderObject update = new FolderObject();
            update.setParentFolderID(inserted.getParentFolderID());
            update.setObjectID(inserted.getObjectID());
            update.setLastModified(insertR.getTimestamp());
            update.addPermission(Create.ocl(userId1, false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION));
            update.addPermission(Create.ocl(userId2, false, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS));
            client.execute(new UpdateRequest(EnumAPI.OX_OLD, update));
            getR = client.execute(new GetRequest(EnumAPI.OX_OLD, insertR.getId()));
            toDelete = getR.getFolder();
            toDelete.setLastModified(getR.getTimestamp());
        } finally {
            client.execute(new DeleteRequest(EnumAPI.OX_OLD, toDelete));
        }
    }

    public void testFailDeleteFolder() throws OXException, IOException, JSONException, OXException, OXException {
        int userId = client.getValues().getUserId();
        int secId = client2.getValues().getUserId();
        FolderObject parent = null;
        FolderObject child01 = null;
        FolderObject child02 = null;
        FolderObject subChild01 = null;
        try {
            OCLPermission[] perms = new OCLPermission[] {
                Create.ocl(userId, false, true, OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
                Create.ocl(secId, false, false, OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION)
            };
            {
                FolderObject folder = Create.folder(FolderObject.SYSTEM_PUBLIC_FOLDER_ID, "DeleteMeImmediately", FolderObject.CALENDAR, FolderObject.PUBLIC, perms);
                InsertResponse response = client.execute(new InsertRequest(EnumAPI.OX_NEW, folder));
                GetResponse response2 = client.execute(new GetRequest(EnumAPI.OX_NEW, response.getId()));
                parent = response2.getFolder();
                parent.setLastModified(response2.getTimestamp());
            }
            {
                FolderObject folder = Create.folder(parent.getObjectID(), "DeleteMeImmediatelyChild01", FolderObject.CALENDAR, FolderObject.PUBLIC, perms);
                InsertResponse response = client.execute(new InsertRequest(EnumAPI.OX_NEW, folder));
                GetResponse response2 = client.execute(new GetRequest(EnumAPI.OX_NEW, response.getId()));
                child01 = response2.getFolder();
                child01.setLastModified(response2.getTimestamp());
            }
            {
                FolderObject folder = Create.folder(parent.getObjectID(), "DeleteMeImmediatelyChild02", FolderObject.CALENDAR, FolderObject.PUBLIC, perms);
                InsertResponse response = client.execute(new InsertRequest(EnumAPI.OX_NEW, folder));
                GetResponse response2 = client.execute(new GetRequest(EnumAPI.OX_NEW, response.getId()));
                child02 = response2.getFolder();
                child02.setLastModified(response2.getTimestamp());
            }
            perms = new OCLPermission[] {
                Create.ocl(userId, false, false, OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
                Create.ocl(secId, false, true, OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION)
            };
            {
                FolderObject folder = Create.folder(child01.getObjectID(), "NonDeleteableSubChild01", FolderObject.CALENDAR, FolderObject.PUBLIC, perms);
                folder.setCreator(secId);
                folder.setCreatedBy(secId);
                InsertResponse response = client2.execute(new InsertRequest(EnumAPI.OX_NEW, folder));
                GetResponse response2 = client.execute(new GetRequest(EnumAPI.OX_NEW, response.getId()));
                subChild01 = response2.getFolder();
                subChild01.setLastModified(response2.getTimestamp());
            }
            // And finally the test
            CommonDeleteResponse response = client.execute(new DeleteRequest(EnumAPI.OX_NEW, false, parent));
            JSONArray notDeleted = (JSONArray) response.getData();
            assertEquals("Expected identifier of not deletable folder.", 1, notDeleted.length());
            assertEquals("Wrong folder identifier", parent.getObjectID(), notDeleted.getInt(0));
        } finally {
            if (null != subChild01) {
                try {
                    client2.execute(new DeleteRequest(EnumAPI.OX_NEW, subChild01));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != child02) {
                try {
                    client.execute(new DeleteRequest(EnumAPI.OX_NEW, child02));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != child01) {
                try {
                    client.execute(new DeleteRequest(EnumAPI.OX_NEW, child01));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != parent) {
                try {
                    client.execute(new DeleteRequest(EnumAPI.OX_NEW, parent));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
