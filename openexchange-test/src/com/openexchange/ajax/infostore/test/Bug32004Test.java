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
import static org.junit.Assert.assertNotNull;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug32004Test}
 *
 * Wrong type for new folders below trash
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug32004Test extends AbstractInfostoreTest {

    /**
     * Initializes a new {@link Bug32004Test}.
     *
     * @param name The test name
     */
    public Bug32004Test() {
        super();
    }

    @Test
    public void testCreateFolderBelowTrash() throws Exception {
        /*
         * create folder below trash
         */
        int trashFolderID = getClient().getValues().getInfostoreTrashFolder();
        String name = UUID.randomUUID().toString();
        FolderObject folder = ftm.generatePrivateFolder(name, FolderObject.INFOSTORE, trashFolderID, getClient().getValues().getUserId());
        folder = ftm.insertFolderOnServer(folder);
        /*
         * reload folder in different trees and check name
         */
        for (EnumAPI api : new EnumAPI[] { EnumAPI.OUTLOOK, EnumAPI.OX_NEW, EnumAPI.OX_OLD }) {
            folder = loadFolder(api, folder.getObjectID());
            assertNotNull("folder not found", folder);
            assertEquals("folder name wrong", name, folder.getFolderName());
            assertEquals("folder type wrong", FolderObject.TRASH, folder.getType());
        }
    }

    private FolderObject loadFolder(EnumAPI api, int folderID) throws Exception {
        GetRequest request = new GetRequest(api, String.valueOf(folderID), FolderObject.ALL_COLUMNS);
        return getClient().execute(request).getFolder();
    }

}
