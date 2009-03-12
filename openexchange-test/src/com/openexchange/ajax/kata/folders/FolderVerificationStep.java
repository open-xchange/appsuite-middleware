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

package com.openexchange.ajax.kata.folders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.openexchange.ajax.folder.actions.FolderUpdatesResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.UpdatesRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllRequest;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.kata.NeedExistingStep;
import com.openexchange.ajax.kata.tasks.TaskVerificationStep;
import com.openexchange.api.OXConflictException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.test.FolderTestManager;
import com.openexchange.tools.servlet.AjaxException;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class FolderVerificationStep extends NeedExistingStep<FolderObject> {

    private FolderObject entry;

    private FolderTestManager manager;

    /**
     * Initializes a new {@link FolderVerificationStep}.
     * 
     * @param name
     * @param expectedError
     */
    public FolderVerificationStep(FolderObject entry, String name) {
        super(name, null);
        this.entry = entry;
    }

    public void perform(AJAXClient client) throws Exception {
        this.client = client;
        this.manager = new FolderTestManager(client);
        assumeIdentity(entry);
        checkWithReadMethods(entry);
    }

    private void checkWithReadMethods(FolderObject folder) throws OXException, JSONException, AjaxException, IOException, SAXException {
        checkViaGet(folder);
        checkViaAll(folder);
        checkViaList(folder);
        checkViaUpdates(folder);
    }

    private void checkViaGet(FolderObject folder) throws OXException, JSONException {
        FolderObject loaded = manager.getFolderFromServer(folder);
        compare(folder, loaded);
    }

    private void checkViaAll(FolderObject folder) throws AjaxException, IOException, SAXException, JSONException {
        Object[][] rows = getViaAll(folder);

        checkInList(folder, rows, FolderObject.ALL_COLUMNS, "all-");
    }

    private void checkViaList(FolderObject folder) throws AjaxException, IOException, SAXException, JSONException {
        ListRequest listRequest = new ListRequest(Integer.toString(folder.getParentFolderID()));
        CommonListResponse response = client.execute(listRequest);

        Object[][] rows = response.getArray();

        checkInList(folder, rows, FolderObject.ALL_COLUMNS, "list-");
    }

    private void checkViaUpdates(FolderObject folder) throws AjaxException, IOException, SAXException, JSONException, OXConflictException {
        UpdatesRequest updates = new UpdatesRequest(
            folder.getParentFolderID(),
            FolderObject.ALL_COLUMNS,
            FolderObject.OBJECT_ID,
            Order.ASCENDING,
            new Date(0));
        FolderUpdatesResponse response = (FolderUpdatesResponse) client.execute(updates);
        // TODO: write FolderUpdatesParser
        //List<FolderObject> folders = response.getFolders();
        //checkInList(folder, folders, "updates-");
    }
    
    private void checkInList(FolderObject folder, Object[][] rows, int[] columns, String typeOfAction) throws AjaxException, IOException, SAXException, JSONException {
        int idPos = findIDIndex(columns);

        for (int i = 0; i < rows.length; i++) {
            Object[] row = rows[i];
            int id = ((Integer) row[idPos]).intValue();
            if (id == folder.getObjectID()) {
                compare(folder, row, columns);
                return;
            }
        }
        fail("Object not found in " +typeOfAction+ "response. " + name);
    }

    private Object[][] getViaAll(FolderObject folder) throws AjaxException, IOException, SAXException, JSONException {
        CommonAllRequest all = new CommonAllRequest("/ajax/folders", folder.getParentFolderID(), FolderObject.ALL_COLUMNS, 0, null, true);
        CommonAllResponse response = client.execute(all);
        return response.getArray();
    }

    private void compare(FolderObject folder, FolderObject loaded) {
        int[] columns = FolderObject.ALL_COLUMNS;
        for (int i = 0; i < columns.length; i++) {
            int col = columns[i];
            if (col == DataObject.LAST_MODIFIED_UTC || col== DataObject.LAST_MODIFIED) {
                continue;
            }
            if (folder.contains(col)) {
                assertEquals(name + ": Column " + col + " differs!", folder.get(col), loaded.get(col));
            }
        }
    }
    
    private void compare(FolderObject folder, Object[] row, int[] columns) throws AjaxException, IOException, SAXException, JSONException {
        assertEquals("Result should contain same number of elements as the request", Integer.valueOf(row.length), Integer.valueOf(columns.length));
        for (int i = 0; i < columns.length; i++) {
            int column = columns[i];
            if (column == DataObject.LAST_MODIFIED_UTC || column == DataObject.LAST_MODIFIED) {
                continue;
            }
            if (folder.contains(column)) {
                Object expected = folder.get(column);
                Object actual = row[i];
                actual = transform(column, actual);
                assertEquals(name + " Column: " + column, expected, actual);
            }
        }
    }

    private int findIDIndex(int[] columns) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] == FolderObject.OBJECT_ID) {
                return i;
            }
        }
        fail("No ID column requested. This won't work. " + name);
        return -1;
    }

    private void checkInList(FolderObject folder, List<FolderObject> folders, String nameOfAction) {
        for (FolderObject folderFromList : folders) {
            if (folderFromList.getObjectID() == folder.getObjectID()) {
                compare(folder, folderFromList);
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
