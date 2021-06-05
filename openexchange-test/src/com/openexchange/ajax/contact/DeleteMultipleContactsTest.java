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

import static org.junit.Assert.assertFalse;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.contact.action.DeleteRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.container.Contact;

/**
 * {@link DeleteMultipleContactsTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DeleteMultipleContactsTest extends AbstractContactTest {

    private Contact c1, c2, c3;

    /**
     * Initializes a new {@link DeleteMultipleContactsTest}.
     * 
     * @param name
     */
    public DeleteMultipleContactsTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        c1 = new Contact();
        c1.setGivenName("Test 1");
        c1.setSurName("User");
        c1.setDisplayName("Test 1 User");
        c1.setEmail1("testuser1@example.org");
        c1.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        c1.setCreationDate(new Date());

        c2 = new Contact();
        c2.setGivenName("Test 2");
        c2.setSurName("User");
        c2.setDisplayName("Test 2 User");
        c2.setEmail1("testuser2@example.org");
        c2.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        c2.setCreationDate(new Date());

        c3 = new Contact();
        c3.setGivenName("Test 3");
        c3.setSurName("User");
        c3.setDisplayName("Test 3 User");
        c3.setEmail1("testuser3@example.org");
        c3.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        c3.setCreationDate(new Date());

        InsertRequest in1 = new InsertRequest(c1);
        InsertResponse res1 = getClient().execute(in1);
        res1.fillObject(c1);

        InsertRequest in2 = new InsertRequest(c2);
        InsertResponse res2 = getClient().execute(in2);
        res2.fillObject(c2);

        InsertRequest in3 = new InsertRequest(c3);
        InsertResponse res3 = getClient().execute(in3);
        res3.fillObject(c3);
    }

    @Test
    public void testDeleteMultipleContacts() throws Exception {
        int[] cids = new int[] { c1.getObjectID(), c2.getObjectID(), c3.getObjectID() };
        DeleteRequest delReq = new DeleteRequest(getClient().getValues().getPrivateContactFolder(), cids, new Date());
        CommonDeleteResponse delRes = getClient().execute(delReq);
        assertFalse("Delete of multiple contacts failed: " + delRes.getErrorMessage(), delRes.hasError());
    }

}
