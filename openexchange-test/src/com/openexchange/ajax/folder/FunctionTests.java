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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.folder.actions.API;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.Abstrac2UserAJAXSession;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link FunctionTests}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class FunctionTests extends Abstrac2UserAJAXSession {

    @Test
    public void testUnknownAction() throws IOException, JSONException, OXException {
        GetResponse response = client1.execute(new UnknownActionRequest(EnumAPI.OX_OLD, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, false));
        assertTrue("JSON response should contain an error message.", response.hasError());
        OXException exception = response.getException();
        String error = exception.getErrorCode(); //was: getOrigMessage, maybe it should be .getCause().getMessage()?
        assertTrue("Error is not the expected one: \"" + error + "\"", error.equals("SVL-0001"));
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

    @Test
    public void testInsertUpdateFolder() throws OXException, IOException, JSONException {
        FolderObject toDelete = null;
        int userId1 = client1.getValues().getUserId();
        int userId2 = client2.getValues().getUserId();
        try {
            FolderObject folder = Create.createPrivateFolder("ChangeMyPermissions" + UUID.randomUUID().toString(), FolderObject.CALENDAR, userId1);
            folder.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
            InsertResponse insertR = client1.execute(new InsertRequest(EnumAPI.OX_OLD, folder));
            GetResponse getR = client1.execute(new GetRequest(EnumAPI.OX_OLD, insertR.getId()));
            toDelete = getR.getFolder();
            toDelete.setLastModified(getR.getTimestamp());
            FolderObject inserted = getR.getFolder();
            FolderObject update = new FolderObject();
            update.setParentFolderID(inserted.getParentFolderID());
            update.setObjectID(inserted.getObjectID());
            update.setLastModified(insertR.getTimestamp());
            update.addPermission(Create.ocl(userId1, false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION));
            update.addPermission(Create.ocl(userId2, false, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS));
            client1.execute(new UpdateRequest(EnumAPI.OX_OLD, update));
            getR = client1.execute(new GetRequest(EnumAPI.OX_OLD, insertR.getId()));
            toDelete = getR.getFolder();
            toDelete.setLastModified(getR.getTimestamp());
        } finally {
            client1.execute(new DeleteRequest(EnumAPI.OX_OLD, toDelete));
        }
    }

    @Test
    public void testFailDeleteFolder() throws OXException, IOException, JSONException {
        int userId = client1.getValues().getUserId();
        int secId = client2.getValues().getUserId();
        FolderObject parent = null;
        FolderObject child01 = null;
        FolderObject child02 = null;
        FolderObject subChild01 = null;
        try {
            OCLPermission[] perms = new OCLPermission[] { Create.ocl(userId, false, true, OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION), Create.ocl(secId, false, false, OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION)
            };
            {
                FolderObject folder = Create.folder(FolderObject.SYSTEM_PUBLIC_FOLDER_ID, "DeleteMeImmediately", FolderObject.CALENDAR, FolderObject.PUBLIC, perms);
                InsertResponse response = client1.execute(new InsertRequest(EnumAPI.OX_NEW, folder));
                GetResponse response2 = client1.execute(new GetRequest(EnumAPI.OX_NEW, response.getId()));
                parent = response2.getFolder();
                parent.setLastModified(response2.getTimestamp());
            }
            {
                FolderObject folder = Create.folder(parent.getObjectID(), "DeleteMeImmediatelyChild01", FolderObject.CALENDAR, FolderObject.PUBLIC, perms);
                InsertResponse response = client1.execute(new InsertRequest(EnumAPI.OX_NEW, folder));
                GetResponse response2 = client1.execute(new GetRequest(EnumAPI.OX_NEW, response.getId()));
                child01 = response2.getFolder();
                child01.setLastModified(response2.getTimestamp());
            }
            {
                FolderObject folder = Create.folder(parent.getObjectID(), "DeleteMeImmediatelyChild02", FolderObject.CALENDAR, FolderObject.PUBLIC, perms);
                InsertResponse response = client1.execute(new InsertRequest(EnumAPI.OX_NEW, folder));
                GetResponse response2 = client1.execute(new GetRequest(EnumAPI.OX_NEW, response.getId()));
                child02 = response2.getFolder();
                child02.setLastModified(response2.getTimestamp());
            }
            perms = new OCLPermission[] { Create.ocl(userId, false, false, OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION), Create.ocl(secId, false, true, OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION)
            };
            {
                FolderObject folder = Create.folder(child01.getObjectID(), "NonDeleteableSubChild01", FolderObject.CALENDAR, FolderObject.PUBLIC, perms);
                folder.setCreator(secId);
                folder.setCreatedBy(secId);
                InsertResponse response = client2.execute(new InsertRequest(EnumAPI.OX_NEW, folder));
                GetResponse response2 = client1.execute(new GetRequest(EnumAPI.OX_NEW, response.getId()));
                subChild01 = response2.getFolder();
                subChild01.setLastModified(response2.getTimestamp());
            }
            // And finally the test
            CommonDeleteResponse response = client1.execute(new DeleteRequest(EnumAPI.OX_NEW, false, parent));
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
                    client1.execute(new DeleteRequest(EnumAPI.OX_NEW, child02));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != child01) {
                try {
                    client1.execute(new DeleteRequest(EnumAPI.OX_NEW, child01));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != parent) {
                try {
                    client1.execute(new DeleteRequest(EnumAPI.OX_NEW, parent));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
