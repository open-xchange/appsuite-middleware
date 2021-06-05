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

package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.infostore.test.AbstractInfostoreTest;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug44891Test}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.2
 */
public class Bug44891Test extends AbstractInfostoreTest {

    private FolderObject folder;

    /**
     * Initializes a new {@link Bug44891Test}.
     *
     * @param name
     */
    public Bug44891Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folder = generateInfostoreFolder("TestBug44891");
        InsertRequest req = new InsertRequest(EnumAPI.OX_NEW, folder);
        InsertResponse resp = getClient().execute(req);
        resp.fillObject(folder);
    }

    @Test
    public void testBug44891() throws Exception {
        folder.setFolderName("shouldFail<>");
        UpdateRequest req = new UpdateRequest(EnumAPI.OX_NEW, folder, false);
        InsertResponse resp = getClient().execute(req);
        assertTrue(resp.hasError());
        assertEquals(FileStorageExceptionCodes.ILLEGAL_CHARACTERS.getNumber(), resp.getException().getCode());
    }

}
