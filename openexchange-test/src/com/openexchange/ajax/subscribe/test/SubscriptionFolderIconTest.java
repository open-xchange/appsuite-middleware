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

package com.openexchange.ajax.subscribe.test;

import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.folder.actions.FolderUpdatesResponse;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.subscribe.actions.NewSubscriptionRequest;
import com.openexchange.ajax.subscribe.actions.NewSubscriptionResponse;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.Subscription;
import com.openexchange.test.FolderTestManager;

/**
 * {@link SubscriptionFolderIconTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class SubscriptionFolderIconTest extends AbstractSubscriptionTest {

    private static final int FLAG_SUBSCRIBED = 3020;

    private static final String KEY_SUBSCRIBED = "com.openexchange.subscribe.subscriptionFlag";

    private FolderTestManager fMgr;

    public SubscriptionFolderIconTest(String name) {
        super(name);
    }

    private FolderObject folder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fMgr = getFolderManager();

        // create contact folder
        folder = fMgr.generatePublicFolder(
            "publishedContacts",
            FolderObject.CONTACT,
            getClient().getValues().getPrivateContactFolder(),
            getClient().getValues().getUserId());
        fMgr.insertFolderOnServer(folder);

        // fill contact folder
        Contact contact = generateContact("Herbert", "Meier");
        contact.setParentFolderID(folder.getObjectID());
        getContactManager().newAction(contact);
    }

    protected void subscribe() throws Exception {
        // form
        DynamicFormDescription form = generateFormDescription();
        Subscription expected = generateOXMFSubscription(form);
        expected.setFolderId(String.valueOf(folder.getObjectID()));

        // new request
        NewSubscriptionRequest newReq = new NewSubscriptionRequest(expected, form);
        NewSubscriptionResponse newResp = getClient().execute(newReq);

        assertFalse("Should succeed creating the subscription: " + newResp.getException(), newResp.hasError());
        expected.setId(newResp.getId());
    }

    public void testShouldSetTheIconViaGet() throws Exception {
        // check negative
        fMgr.getFolderFromServer(folder.getObjectID(), false, new int[] { FLAG_SUBSCRIBED });
        GetResponse response = (GetResponse) fMgr.getLastResponse();
        JSONObject data = (JSONObject) response.getData();
        assertTrue(
            "Should contain the key '"+KEY_SUBSCRIBED+"' even before publication",
            data.has(KEY_SUBSCRIBED));
        assertFalse(
            "Key '"+KEY_SUBSCRIBED+"' should have 'false' value before publication",
            data.getBoolean(KEY_SUBSCRIBED));

        // subscribe
        subscribe();

        // check positive
        fMgr.getFolderFromServer(folder.getObjectID(), false, new int[] { FLAG_SUBSCRIBED });
        response = (GetResponse) fMgr.getLastResponse();
        data = (JSONObject) response.getData();
        assertTrue(
            "Should contain the key '"+KEY_SUBSCRIBED+"'",
            data.has(KEY_SUBSCRIBED));
        assertTrue(
            "Key '"+KEY_SUBSCRIBED+"' should have 'true' value after publication",
            data.getBoolean(KEY_SUBSCRIBED));
    }

    public void testShouldSetTheIconViaList() throws Exception {
        // check negative
        fMgr.listFoldersOnServer(folder.getParentFolderID(), new int[] { FLAG_SUBSCRIBED });
        AbstractColumnsResponse response = (AbstractColumnsResponse) fMgr.getLastResponse();
        JSONArray folders = (JSONArray) response.getData();
        assertTrue("Should return at least one folder", folders.length() > 0);
        boolean found = false;
        for (int i = 0; i < folders.length(); i++) {
            JSONArray subfolder = folders.getJSONArray(i);
            int id = subfolder.getInt(response.getColumnPos(FolderObject.OBJECT_ID)); // note: this work only as long as we have this stupid "oh, I'll add the folder id anyways"
                                          // feature in that request
            boolean published = subfolder.getBoolean(response.getColumnPos(FLAG_SUBSCRIBED));
            if (id == folder.getObjectID()) {
                found = true;
                assertFalse("Folder " + id + " should not be published already", published);
            }
        }
        assertTrue("Should find folder " + folder.getObjectID() + " as a subfolder of " + folder.getParentFolderID() + ".", found);

        // subscribe
        subscribe();

        // check positive
        fMgr.listFoldersOnServer(folder.getParentFolderID(), new int[] { FLAG_SUBSCRIBED });
        response = (AbstractColumnsResponse) fMgr.getLastResponse();
        folders = (JSONArray) response.getData();
        assertTrue("Should return at least one folder", folders.length() > 0);
        found = false;
        for (int i = 0; i < folders.length(); i++) {
            JSONArray subfolder = folders.getJSONArray(i);
            int id = subfolder.getInt(response.getColumnPos(FolderObject.OBJECT_ID)); // note: this work only as long as we have this stupid "oh, I'll add the folder id anyways"
                                          // feature in that request
            boolean published = subfolder.getBoolean(response.getColumnPos(FLAG_SUBSCRIBED));
            if (id == folder.getObjectID()) {
                found = true;
                assertTrue("Folder " + id + " should not published", published);
            }
        }
        assertTrue("Should find folder " + folder.getObjectID() + " as a subfolder of " + folder.getParentFolderID() + ".", found);

    }

    public void testShouldSetTheIconViaUpdates() throws Exception {
        // check negative
        Date lastModified = new Date(fMgr.getLastResponse().getTimestamp().getTime() - 1);
        fMgr.getUpdatedFoldersOnServer(lastModified, new int[] { FLAG_SUBSCRIBED });
        FolderUpdatesResponse response = (FolderUpdatesResponse) fMgr.getLastResponse();
        int idPos = findPositionOfColumn(response.getColumns(), CalendarObject.OBJECT_ID);
        int flagPos = findPositionOfColumn(response.getColumns(), FLAG_SUBSCRIBED);
        JSONArray arr = (JSONArray) response.getData();
        assertTrue("Should return at least one update", arr.length() > 0);
        int folderPos = findPosition(arr, folder.getObjectID(), idPos);
        JSONArray data = arr.getJSONArray(folderPos);
        assertFalse("Should be false if not published", data.getBoolean(flagPos));

        // subscribe
        subscribe();

        // check positive
        lastModified = new Date(fMgr.getLastResponse().getTimestamp().getTime() - 1);
        fMgr.getUpdatedFoldersOnServer(lastModified, new int[] { FLAG_SUBSCRIBED });
        response = (FolderUpdatesResponse) fMgr.getLastResponse();
        arr = (JSONArray) response.getData();
        assertTrue("Should return at least one update", arr.length() > 0);
        folderPos = findPosition(arr, folder.getObjectID(), idPos);
        data = arr.getJSONArray(folderPos);
        assertTrue("Should be true if published", data.getBoolean(flagPos));
    }

    private int findPositionOfColumn(int[] haystack, int needle) {
        for (int i = 0; i < haystack.length; i++) {
            if (haystack[i] == needle) {
                return i;
            }
        }
        return -1;
    }

    private int findPosition(JSONArray arr, int objectID, int idPos) throws JSONException {
        for (int i = 0; i < arr.length(); i++) {
            if (arr.getJSONArray(i).getInt(idPos) == objectID) {
                return i;
            }
        }
        return -1;
    }
}
