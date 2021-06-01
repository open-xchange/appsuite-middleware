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

import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

public class MoveTest extends AbstractContactTest {

    private FolderObject folder;
    private int targetFolder = -1;
    private int objectId = -1;

    @Test
    public void testMove2PrivateFolder() throws Exception {
        final Contact contactObj = new Contact();
        contactObj.setSurName("testMove2PrivateFolder");
        contactObj.setParentFolderID(contactFolderId);
        objectId = insertContact(contactObj);

        folder = Create.createPrivateFolder("testCopy" + UUID.randomUUID().toString(), FolderObject.CONTACT, userId);
        folder.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        final InsertResponse folderCreateResponse = getClient().execute(new InsertRequest(EnumAPI.OUTLOOK, folder));
        folderCreateResponse.fillObject(folder);

        targetFolder = folder.getObjectID();

        contactObj.setParentFolderID(targetFolder);
        updateContact(contactObj, contactFolderId);
        final Contact loadContact = loadContact(objectId, targetFolder);
        contactObj.setObjectID(objectId);
        compareObject(contactObj, loadContact);
    }

    @Test
    public void testMove2PublicFolder() throws Exception {
        final Contact contactObj = new Contact();
        contactObj.setSurName("testMove2PublicFolder");
        contactObj.setParentFolderID(contactFolderId);
        objectId = insertContact(contactObj);

        folder = Create.createPrivateFolder("testCopy" + UUID.randomUUID().toString(), FolderObject.CONTACT, userId);
        folder.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        final InsertResponse folderCreateResponse = getClient().execute(new InsertRequest(EnumAPI.OUTLOOK, folder));
        folderCreateResponse.fillObject(folder);

        targetFolder = folder.getObjectID();

        contactObj.setParentFolderID(targetFolder);
        updateContact(contactObj, contactFolderId);
        final Contact loadContact = loadContact(objectId, targetFolder);
        contactObj.setObjectID(objectId);
        compareObject(contactObj, loadContact);
    }

}
