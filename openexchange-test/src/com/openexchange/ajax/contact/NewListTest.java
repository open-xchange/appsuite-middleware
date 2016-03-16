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

package com.openexchange.ajax.contact;

import java.util.Iterator;
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
    public NewListTest(final String name) {
        super(name);
    }

    /**
     * This method tests the new handling of not more available objects for LIST
     * requests.
     */
    public void testRemovedObjectHandling() throws Throwable {
        final AJAXClient clientA = getClient();
        final int folderA = clientA.getValues().getPrivateContactFolder();

        final InsertRequest[] inserts = new InsertRequest[NUMBER];
        for (int i = 0; i < inserts.length; i++) {

            final Contact contactObj = new Contact();
            contactObj.setSurName("NewTestList"+i);
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
            final InsertResponse insertR = mInsert
                .getResponse( (DELETES + i) );
            deletes1[i] = new DeleteRequest(folderA, insertR.getId(), allR
                .getTimestamp());
        }
        Executor.execute(clientA, MultipleRequest.create(deletes1));

        // List request of A must now not contain the deleted objects and give
        // no error.
        final CommonListResponse listR = Executor.execute(
            clientA, new ListRequest(allR.getListIDs(), columns, true));

        final Iterator<Object[]> it = listR.iterator();
        while (it.hasNext()) {
            final Object[] ar = it.next();


            final InsertResponse irr = mInsert.getResponse(DELETES);
            final InsertResponse irr2 = mInsert.getResponse(DELETES+1);

            if ( ((Integer)ar[1]).intValue() == irr.getId() || ((Integer)ar[1]).intValue() == irr2.getId()){
                assertFalse("Error: Object was found in list", true);
            }

        }

        final DeleteRequest[] deletes2 = new DeleteRequest[NUMBER - DELETES];

        int cnt = 0;
        for (int i = 0; i < NUMBER; i++) {
            if ( (i != DELETES) && (i != (DELETES +1)) ){
                final InsertResponse insertR = mInsert.getResponse(i);
                deletes2[cnt] = new DeleteRequest(folderA, insertR.getId(),listR.getTimestamp());
                cnt++;
            }
        }

        Executor.execute(getClient(), MultipleRequest.create(deletes2));
    }
}
