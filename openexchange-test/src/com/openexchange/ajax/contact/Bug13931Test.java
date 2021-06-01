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

package com.openexchange.ajax.contact;

import static com.openexchange.ajax.folder.Create.ocl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Date;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13931Test extends AbstractAJAXSession {

    private int userId;

    private int privateFolderId, folderId;

    private FolderObject folder;

    private Contact AAA, aaa, BBB, bbb;

    private final int[] columns = new int[] { Contact.OBJECT_ID, Contact.SUR_NAME };

    public Bug13931Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        userId = getClient().getValues().getUserId();
        privateFolderId = getClient().getValues().getPrivateContactFolder();
        final OCLPermission ocl = ocl(userId, false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        folder = Create.folder(privateFolderId, "Folder to test bug 13931 (" + new Date().getTime() + ")", FolderObject.CONTACT, FolderObject.PRIVATE, ocl);
        final CommonInsertResponse response = getClient().execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
        response.fillObject(folder);
        folderId = folder.getObjectID();

        AAA = new Contact();
        AAA.setParentFolderID(folderId);
        AAA.setSurName("AAA");
        getClient().execute(new InsertRequest(AAA)).fillObject(AAA);

        BBB = new Contact();
        BBB.setParentFolderID(folderId);
        BBB.setSurName("BBB");
        getClient().execute(new InsertRequest(BBB)).fillObject(BBB);

        aaa = new Contact();
        aaa.setParentFolderID(folderId);
        aaa.setSurName("aaa");
        getClient().execute(new InsertRequest(aaa)).fillObject(aaa);

        bbb = new Contact();
        bbb.setParentFolderID(folderId);
        bbb.setSurName("bbb");
        getClient().execute(new InsertRequest(bbb)).fillObject(bbb);
    }

    @Test
    public void testBug13931() throws Exception {
        final AllRequest allRequest = new AllRequest(folderId, columns);
        final CommonAllResponse allResponse = getClient().execute(allRequest);
        final JSONArray jsonArray = (JSONArray) allResponse.getResponse().getData();
        assertNotNull("No result", jsonArray);
        assertEquals("Wrong amount of results", 4, jsonArray.length());
        assertEquals("Wrong order", AAA.getObjectID(), jsonArray.getJSONArray(0).getInt(0));
        assertEquals("Wrong order", aaa.getObjectID(), jsonArray.getJSONArray(1).getInt(0));
        assertEquals("Wrong order", BBB.getObjectID(), jsonArray.getJSONArray(2).getInt(0));
        assertEquals("Wrong order", bbb.getObjectID(), jsonArray.getJSONArray(3).getInt(0));
    }

}
