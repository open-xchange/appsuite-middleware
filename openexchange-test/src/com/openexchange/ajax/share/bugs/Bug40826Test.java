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

package com.openexchange.ajax.share.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug40826Test}
 *
 * An appsuite user should not be able to create a guest share with WRITE permissions in the contacts or tasks module
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug40826Test extends ShareTest {

    /**
     * Initializes a new {@link Bug40826Test}.
     *
     * @param name The test name
     */
    public Bug40826Test() {
        super();
    }

    @Test
    public void testShareTasksToAuthor() throws Exception {
        testShareToAuthor(FolderObject.TASK);
    }

    @Test
    public void testShareContactsToAuthor() throws Exception {
        testShareToAuthor(FolderObject.CONTACT);
    }

    private void testShareToAuthor(int module) throws Exception {
        OCLGuestPermission guestPermission = createNamedAuthorPermission();
        /*
         * try and create folder shared to guest user
         */
        FolderObject folder = Create.createPrivateFolder(randomUID(), module, getClient().getValues().getUserId(), guestPermission);
        folder.setParentFolderID(getDefaultFolder(module));
        InsertRequest insertRequest = new InsertRequest(randomFolderAPI(), folder, getClient().getValues().getTimeZone());
        insertRequest.setFailOnError(false);
        InsertResponse insertResponse = getClient().execute(insertRequest);
        assertNotNull(insertResponse);
        assertTrue("No error in response", insertResponse.hasError());
        assertNotNull("No error in response", insertResponse.getException());
        assertEquals("Unexpected error", "FLD-1039", insertResponse.getException().getErrorCode());
    }

}
