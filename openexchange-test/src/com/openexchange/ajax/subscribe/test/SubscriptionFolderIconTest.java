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

package com.openexchange.ajax.subscribe.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
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

/**
 * {@link SubscriptionFolderIconTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class SubscriptionFolderIconTest extends AbstractSubscriptionTest {

    private static final int FLAG_SUBSCRIBED = 3020;

    private static final String KEY_SUBSCRIBED = "com.openexchange.subscribe.subscriptionFlag";

    private FolderObject folder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // create contact folder
        folder = ftm.generatePublicFolder("publishedContacts", FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId());
        ftm.insertFolderOnServer(folder);

        // fill contact folder
        Contact contact = generateContact("Herbert", "Meier");
        contact.setParentFolderID(folder.getObjectID());
        cotm.newAction(contact);
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

    @Test
    public void testShouldSetTheIconViaGet() throws Exception {
        // check negative
        ftm.getFolderFromServer(folder.getObjectID(), false, new int[] { FLAG_SUBSCRIBED });
        GetResponse response = (GetResponse) ftm.getLastResponse();
        JSONObject data = (JSONObject) response.getData();
        assertTrue("Should contain the key '" + KEY_SUBSCRIBED + "' even before publication", data.has(KEY_SUBSCRIBED));
        assertFalse("Key '" + KEY_SUBSCRIBED + "' should have 'false' value before publication", data.getBoolean(KEY_SUBSCRIBED));

        // subscribe
        subscribe();

        // check positive
        ftm.getFolderFromServer(folder.getObjectID(), false, new int[] { FLAG_SUBSCRIBED });
        response = (GetResponse) ftm.getLastResponse();
        data = (JSONObject) response.getData();
        assertTrue("Should contain the key '" + KEY_SUBSCRIBED + "'", data.has(KEY_SUBSCRIBED));
        assertTrue("Key '" + KEY_SUBSCRIBED + "' should have 'true' value after publication", data.getBoolean(KEY_SUBSCRIBED));
    }

    @Test
    public void testShouldSetTheIconViaList() throws Exception {
        // check negative
        ftm.listFoldersOnServer(folder.getParentFolderID(), new int[] { FLAG_SUBSCRIBED });
        AbstractColumnsResponse response = (AbstractColumnsResponse) ftm.getLastResponse();
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
        ftm.listFoldersOnServer(folder.getParentFolderID(), new int[] { FLAG_SUBSCRIBED });
        response = (AbstractColumnsResponse) ftm.getLastResponse();
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

    @Test
    public void testShouldSetTheIconViaUpdates() throws Exception {
        // check negative
        Date lastModified = new Date(ftm.getLastResponse().getTimestamp().getTime() - 1);
        ftm.getUpdatedFoldersOnServer(lastModified, new int[] { FLAG_SUBSCRIBED });
        FolderUpdatesResponse response = (FolderUpdatesResponse) ftm.getLastResponse();
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
        lastModified = new Date(ftm.getLastResponse().getTimestamp().getTime() - 1);
        ftm.getUpdatedFoldersOnServer(lastModified, new int[] { FLAG_SUBSCRIBED });
        response = (FolderUpdatesResponse) ftm.getLastResponse();
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
