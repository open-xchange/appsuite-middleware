/*    OPEN-XCHANGE legal information
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

package com.openexchange.ajax.kata.folders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.FolderUpdatesResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.UpdatesRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.kata.NeedExistingStep;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.test.FolderTestManager;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class FolderVerificationStep extends NeedExistingStep<FolderObject> {

    private final FolderObject entry;

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

    @Override
    public void perform(AJAXClient myClient) throws Exception {
        this.client = myClient;
        this.manager = new FolderTestManager(myClient);
        assumeIdentity(entry);
        checkWithReadMethods(entry);
    }

    private void checkWithReadMethods(FolderObject folder) throws  JSONException, OXException, IOException, SAXException {
        checkViaGet(folder);
        checkViaList(folder);
        checkViaUpdates(folder);
    }

    private void checkViaGet(FolderObject folder) {
        FolderObject loaded = manager.getFolderFromServer(folder);
        compare(folder, loaded);
    }


    private void checkViaList(FolderObject folder) throws OXException, IOException, SAXException, JSONException {
        int[] requestedFields = FolderObject.ALL_COLUMNS;//new int[]{FolderObject.OBJECT_ID, FolderObject.FOLDER_ID};
        ListRequest listRequest = new ListRequest(EnumAPI.OX_OLD, Integer.toString(folder.getParentFolderID()), requestedFields, true );
        ListResponse response = client.execute(listRequest);

        Object[][] rows = response.getArray();

        checkInList(folder, rows, requestedFields, "list-");
    }

    private void checkViaUpdates(FolderObject folder) throws OXException, IOException, SAXException, JSONException {
        UpdatesRequest updates = new UpdatesRequest(EnumAPI.OX_OLD,
            FolderObject.ALL_COLUMNS,
            -1,
            Order.ASCENDING,
            new Date(0));
        FolderUpdatesResponse response = client.execute(updates);
        List<FolderObject> folders = response.getFolders();
        checkInList(folder, folders);
    }

    private void checkInList(FolderObject folder, Object[][] rows, int[] columns, String typeOfAction) {
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


    private void compare(FolderObject folder, FolderObject loaded) {
        int[] columns = FolderObject.ALL_COLUMNS;
        for (int i = 0; i < columns.length; i++) {
            int col = columns[i];
            if (col == DataObject.LAST_MODIFIED_UTC || col== DataObject.LAST_MODIFIED ) {
                continue;
            }
            if( isIgnoredColumn(col)){
                continue;
            }
            if (folder.contains(col)) {
                assertEquals(name + ": Column " + col + " differs!", folder.get(col), loaded.get(col));
            }
        }
    }

    private boolean isIgnoredColumn(int col) {
        return col == FolderObject.OWN_RIGHTS
        || col == FolderObject.PERMISSIONS_BITS
        || col == FolderObject.SUMMARY
        || col == FolderObject.STANDARD_FOLDER
        || col == FolderObject.TOTAL
        || col == FolderObject.NEW
        || col == FolderObject.UNREAD
        || col == FolderObject.DELETED
        || col == FolderObject.CAPABILITIES
        || col == FolderObject.SUBSCRIBED
        || col == FolderObject.SUBSCR_SUBFLDS
        ;
    }

    private void compare(FolderObject folder, Object[] row, int[] columns) {
        assertEquals("Result should contain same number of elements as the request", Integer.valueOf(row.length), Integer.valueOf(columns.length));
        for (int i = 0; i < columns.length; i++) {
            int column = columns[i];
            if (column == DataObject.LAST_MODIFIED_UTC || column == DataObject.LAST_MODIFIED) {
                continue;
            }
            if( isIgnoredColumn(column)){
                continue;
            }
            if (folder.contains(column)) {
                Object expected = folder.get(column);
                Object actual = row[i];
                actual = transform(actual);
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

    private void checkInList(FolderObject folder, List<FolderObject> folders) {
        for (FolderObject folderFromList : folders) {
            if (folderFromList.getObjectID() == folder.getObjectID()) {
                compare(folder, folderFromList);
                return;
            }
        }
        fail("Object not found in response: (" + folder.getObjectID() + ") " + name);
    }

    private Object transform(Object actual)  {
        return actual;
    }

    @Override
    public void cleanUp() throws Exception {
        // Nothing to clean up
    }
}
