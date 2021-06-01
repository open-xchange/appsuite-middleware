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

package com.openexchange.ajax.infostore.test;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.UUIDs;

/**
 * {@link Bug37211Test}
 *
 * Moving drive folders from Trash to Public Folder creates undeletable folders
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug37211Test extends AbstractInfostoreTest {

    @Test
    public void testMoveFolderFromTrash() throws Exception {
        /*
         * create folder below trash
         */
        int trashFolderID = getClient().getValues().getInfostoreTrashFolder();
        String name = UUIDs.getUnformattedStringFromRandom();
        FolderObject folder = ftm.generatePrivateFolder(name, FolderObject.INFOSTORE, trashFolderID, getClient().getValues().getUserId());
        folder = ftm.insertFolderOnServer(folder);
        FolderObject reloadedFolder = ftm.getFolderFromServer(folder.getObjectID());
        assertEquals("folder type wrong", FolderObject.TRASH, reloadedFolder.getType());
        /*
         * move to public folders
         */
        FolderObject toUpdate = new FolderObject();
        toUpdate.setLastModified(folder.getLastModified());
        toUpdate.setObjectID(folder.getObjectID());
        toUpdate.setParentFolderID(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);
        ftm.updateFolderOnServer(toUpdate);
        /*
         * check folder type
         */
        reloadedFolder = ftm.getFolderFromServer(folder.getObjectID());
        assertEquals("folder type wrong", FolderObject.PUBLIC, reloadedFolder.getType());
        /*
         * verify deletion
         */
        ftm.deleteFolderOnServer(folder, Boolean.TRUE);
    }

}
