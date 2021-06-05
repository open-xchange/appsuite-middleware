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
import java.util.Iterator;
import org.junit.Test;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.contact.action.DeleteRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.contact.action.ListRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.groupware.container.Contact;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:ben.pahne@open-xchange.org">Ben Pahne</a>
 */
public class NewListTest extends AbstractAJAXSession {

    private static final int NUMBER = 10;

    private static final int DELETES = 2;

    /**
     * Default constructor.
     */
    public NewListTest() {
        super();
    }

    /**
     * This method tests the new handling of not more available objects for LIST
     * requests.
     */
    @Test
    public void testRemovedObjectHandling() throws Throwable {
        final AJAXClient clientA = getClient();
        final int folderA = clientA.getValues().getPrivateContactFolder();

        final InsertRequest[] inserts = new InsertRequest[NUMBER];
        for (int i = 0; i < inserts.length; i++) {

            final Contact contactObj = new Contact();
            contactObj.setSurName("NewTestList" + i);
            contactObj.setParentFolderID(folderA);

            inserts[i] = new InsertRequest(contactObj, true);
        }

        final MultipleRequest<InsertResponse> mRequest = MultipleRequest.create(inserts);
        final MultipleResponse<InsertResponse> mInsert = Executor.execute(getClient(), mRequest);

        // A now gets all of the folder.
        final int[] columns = new int[] { Contact.SUR_NAME, Contact.OBJECT_ID, Contact.FOLDER_ID };

        final CommonAllResponse allR = Executor.execute(clientA, new AllRequest(folderA, columns));

        // Now B deletes some of them.
        final DeleteRequest[] deletes1 = new DeleteRequest[DELETES];
        for (int i = 0; i < deletes1.length; i++) {
            final InsertResponse insertR = mInsert.getResponse((DELETES + i));
            deletes1[i] = new DeleteRequest(folderA, insertR.getId(), allR.getTimestamp());
        }
        Executor.execute(clientA, MultipleRequest.create(deletes1));

        // List request of A must now not contain the deleted objects and give
        // no error.
        final CommonListResponse listR = Executor.execute(clientA, new ListRequest(allR.getListIDs(), columns, true));

        final Iterator<Object[]> it = listR.iterator();
        while (it.hasNext()) {
            final Object[] ar = it.next();

            final InsertResponse irr = mInsert.getResponse(DELETES);
            final InsertResponse irr2 = mInsert.getResponse(DELETES + 1);

            if ((Integer.parseInt(String.class.cast(ar[1])) == irr.getId()) || (Integer.parseInt(String.class.cast(ar[1])) == irr2.getId())) {
                assertFalse("Error: Object was found in list", true);
            }
        }

        final DeleteRequest[] deletes2 = new DeleteRequest[NUMBER - DELETES];

        int cnt = 0;
        for (int i = 0; i < NUMBER; i++) {
            if ((i != DELETES) && (i != (DELETES + 1))) {
                final InsertResponse insertR = mInsert.getResponse(i);
                deletes2[cnt] = new DeleteRequest(folderA, insertR.getId(), listR.getTimestamp());
                cnt++;
            }
        }

        Executor.execute(getClient(), MultipleRequest.create(deletes2));
    }
}
