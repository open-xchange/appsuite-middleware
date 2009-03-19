/*    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.kata.contacts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.contact.action.ContactUpdatesResponse;
import com.openexchange.ajax.contact.action.ListRequest;
import com.openexchange.ajax.contact.action.SearchRequest;
import com.openexchange.ajax.contact.action.SearchResponse;
import com.openexchange.ajax.contact.action.UpdatesRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.kata.NeedExistingStep;
import com.openexchange.ajax.kata.tasks.TaskVerificationStep;
import com.openexchange.api.OXConflictException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.test.ContactTestManager;
import com.openexchange.tools.servlet.AjaxException;

/**
 * {@link ContactVerificationStep}
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ContactVerificationStep extends NeedExistingStep<ContactObject> {

    private ContactObject entry;

    private ContactTestManager manager;

    private int expectedFolderId;

    /**
     * Initializes a new {@link TaskVerificationStep}.
     * 
     * @param name
     * @param expectedError
     */
    public ContactVerificationStep(ContactObject entry, String name) {
        super(name, null);
        this.entry = entry;
    }

    @Override
    protected void assumeIdentity(ContactObject thing) {
        expectedFolderId = entry.getParentFolderID();
        boolean containsFolderId = entry.containsParentFolderID();
        super.assumeIdentity(entry);
        if( ! containsFolderId ){
            expectedFolderId = entry.getParentFolderID();
        }
    }
    
    public void perform(AJAXClient client) throws Exception {
        this.client = client;
        this.manager = new ContactTestManager(client);
        assumeIdentity(entry); 
        checkWithReadMethods(entry);
    }

    private void checkWithReadMethods(ContactObject contact) throws OXException, JSONException, AjaxException, IOException, SAXException {
        checkViaGet(contact);
        checkViaAll(contact);
        checkViaList(contact);
        checkViaUpdates(contact);
        checkViaSearch(contact);
    }

    private void checkViaGet(ContactObject contact) throws OXException, JSONException {
        ContactObject loaded = manager.getContactFromServer(expectedFolderId, contact.getObjectID());
        compare(contact, loaded);
    }

    private void checkViaAll(ContactObject contact) throws AjaxException, IOException, SAXException, JSONException {
        Object[][] rows = getViaAll(contact);

        checkInList(contact, rows, ContactObject.ALL_COLUMNS, "all-");
    }

    private void checkViaList(ContactObject contact) throws AjaxException, IOException, SAXException, JSONException {
        ListRequest listRequest = new ListRequest(
            ListIDs.l(new int[] { expectedFolderId, contact.getObjectID() }),
            ContactObject.ALL_COLUMNS,
            false);
        CommonListResponse response = client.execute(listRequest);

        Object[][] rows = response.getArray();

        checkInList(contact, rows, ContactObject.ALL_COLUMNS, "list-");
    }

    private void checkViaUpdates(ContactObject contact) throws AjaxException, IOException, SAXException, JSONException, OXConflictException {
        UpdatesRequest updates = new UpdatesRequest(
            expectedFolderId,
            ContactObject.ALL_COLUMNS,
            ContactObject.OBJECT_ID,
            Order.ASCENDING,
            new Date(0));
        ContactUpdatesResponse response = client.execute(updates);

        List<ContactObject> contacts = response.getContacts();
        checkInList(contact, contacts, "updates-");
    }

    private void checkViaSearch(ContactObject contact) throws AjaxException, IOException, SAXException, JSONException {
        Object[][] rows = getViaSearch(contact);
        checkInList(contact, rows, ContactObject.ALL_COLUMNS, "search-");
    }
    

    private void checkInList(ContactObject contact, Object[][] rows, int[] columns, String typeOfAction) throws AjaxException, IOException, SAXException, JSONException {
        int idPos = findIDIndex(columns);

        for (int i = 0; i < rows.length; i++) {
            Object[] row = rows[i];
            int id = ((Integer) row[idPos]).intValue();
            if (id == contact.getObjectID()) {
                compare(contact, row, columns);
                return;
            }
        }
        fail("Object not found in " +typeOfAction+ "response. " + name);
    }

    private Object[][] getViaAll(ContactObject contact) throws AjaxException, IOException, SAXException, JSONException {
        AllRequest all = new AllRequest(expectedFolderId, ContactObject.ALL_COLUMNS);
        CommonAllResponse response = client.execute(all);
        return response.getArray();
    }

    private Object[][] getViaSearch(ContactObject contact) throws AjaxException, IOException, SAXException, JSONException {
        ContactSearchObject contactSearch = new ContactSearchObject();
        contactSearch.setPattern("*");
        contactSearch.addFolder(expectedFolderId);
        SearchRequest searchRequest = new SearchRequest(contactSearch, ContactObject.ALL_COLUMNS, false);
        SearchResponse searchResponse = client.execute(searchRequest);
        JSONArray data = (JSONArray) searchResponse.getResponse().getData();
        Object[][] results = new Object[data.length()][];
        for (int i = 0; i < results.length; i++) {
            JSONArray tempArray = data.getJSONArray(i);
            results[i] = new Object[tempArray.length()];
            for (int j = 0; j < tempArray.length(); j++) {
                results[i][j] = tempArray.get(j);
            }
        }
        return results;
    }

    private void compare(ContactObject contact, ContactObject loaded) {
        int[] columns = ContactObject.ALL_COLUMNS;
        for (int i = 0; i < columns.length; i++) {
            int col = columns[i];
            if (col == DataObject.LAST_MODIFIED_UTC || col== DataObject.LAST_MODIFIED) {
                continue;
            }
            if (contact.contains(col)) {
                assertEquals(name + ": Column " + col + " differs!", contact.get(col), loaded.get(col));
            }
        }
    }
    
    private void compare(ContactObject contact, Object[] row, int[] columns) throws AjaxException, IOException, SAXException, JSONException {
        assertEquals("Result should contain same number of elements as the request", Integer.valueOf(row.length), Integer.valueOf(columns.length));
        for (int i = 0; i < columns.length; i++) {
            int column = columns[i];
            if (column == DataObject.LAST_MODIFIED_UTC || column == DataObject.LAST_MODIFIED) {
                continue;
            }
            if (contact.contains(column)) {
                Object expected = contact.get(column);
                Object actual = row[i];
                actual = transform(column, actual);
                assertEquals(name + " Column: " + column, expected, actual);
            }
        }
    }

    private int findIDIndex(int[] columns) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] == ContactObject.OBJECT_ID) {
                return i;
            }
        }
        fail("No ID column requested. This won't work. " + name);
        return -1;
    }

    private void checkInList(ContactObject contact, List<ContactObject> contacts, String nameOfAction) {
        for (ContactObject contactFromList : contacts) {
            if (contactFromList.getObjectID() == contact.getObjectID()) {
                compare(contact, contactFromList);
                return;
            }
        }
        fail("Object not found in response. " + name);
    }
    
    private Object transform(int column, Object actual) throws AjaxException, IOException, SAXException, JSONException {

        return actual;
    }

    public void cleanUp() throws Exception {
    }
}
