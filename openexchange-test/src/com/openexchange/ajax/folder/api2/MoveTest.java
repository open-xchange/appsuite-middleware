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

package com.openexchange.ajax.folder.api2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GenJSONRequest;
import com.openexchange.ajax.folder.actions.GenJSONResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;

/**
 * {@link MoveTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MoveTest extends AbstractAJAXSession {

    /**
     * Initializes a new {@link MoveTest}.
     *
     * @param name The name of the test.
     */
    public MoveTest() {
        super();
    }

    @Test
    public void testMoveCalBelowMail() throws Throwable {
        // Get root folder
        String newCalId = null;
        String newMailId = null;
        try {
            final int userId = getClient().getValues().getUserId();
            {
                JSONObject newFolder = new JSONObject("{\"title\":\"newCalFolder" + System.currentTimeMillis() + "\",\"module\":\"calendar\",\"permissions\":[{\"group\":false,\"bits\":403710016,\"entity\":" + userId + "}],\"subscribed\":1}");
                GenJSONRequest request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                request.setJSONValue(newFolder);
                request.setParameter("action", "new");
                request.setParameter("folder_id", "1");
                GenJSONResponse response = getClient().execute(request);
                newCalId = (String) response.getData();
                assertNotNull("New ID must not be null!", newCalId);

                newFolder = new JSONObject("{\"title\":\"newMailFolder" + System.currentTimeMillis() + "\",\"module\":\"mail\",\"permissions\":[{\"group\":false,\"bits\":403710016,\"entity\":" + userId + "}],\"subscribed\":1}");
                request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                request.setJSONValue(newFolder);
                request.setParameter("action", "new");
                request.setParameter("folder_id", "default0/INBOX");
                response = getClient().execute(request);
                newMailId = (String) response.getData();
                assertNotNull("New ID must not be null!", newMailId);
            }

            {
                /*
                 * Move calendar folder below mail folder without timestamp parameter
                 */
                final GenJSONRequest request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                request.setJSONValue(new JSONObject("{\"folder_id\":\"" + newMailId + "\"}"));
                request.setParameter("action", "update");
                request.setParameter("id", newCalId);
                final GenJSONResponse response = getClient().execute(request);
                final String newCalIDMoved = (String) response.getData();
                assertEquals("ID not equal.", newCalId, newCalIDMoved);
            }
        } finally {
            if (null != newCalId) {
                // Delete folder
                try {
                    final GenJSONRequest request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                    request.setJSONValue(new JSONArray("[\"" + newCalId + "\"]"));
                    request.setParameter("action", "delete");
                    getClient().execute(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != newMailId) {
                // Delete folder
                try {
                    final GenJSONRequest request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                    request.setJSONValue(new JSONArray("[\"" + newMailId + "\"]"));
                    request.setParameter("action", "delete");
                    getClient().execute(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testMoveMailBelowCal() throws Throwable {
        // Get root folder
        String newCalId = null;
        String newMailId = null;
        try {
            final int userId = getClient().getValues().getUserId();
            {
                JSONObject newFolder = new JSONObject("{\"title\":\"newCalFolder" + System.currentTimeMillis() + "\",\"module\":\"calendar\",\"permissions\":[{\"group\":false,\"bits\":403710016,\"entity\":" + userId + "}],\"subscribed\":1}");
                GenJSONRequest request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                request.setJSONValue(newFolder);
                request.setParameter("action", "new");
                request.setParameter("folder_id", "1");
                GenJSONResponse response = getClient().execute(request);
                newCalId = (String) response.getData();
                assertNotNull("New ID must not be null!", newCalId);

                newFolder = new JSONObject("{\"title\":\"newMailFolder" + System.currentTimeMillis() + "\",\"module\":\"mail\",\"permissions\":[{\"group\":false,\"bits\":403710016,\"entity\":" + userId + "}],\"subscribed\":1}");
                request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                request.setJSONValue(newFolder);
                request.setParameter("action", "new");
                request.setParameter("folder_id", "default0/INBOX");
                response = getClient().execute(request);
                newMailId = (String) response.getData();
                assertNotNull("New ID must not be null!", newMailId);
            }

            {
                /*
                 * Move mail folder below calendar folder without timestamp parameter
                 */
                final GenJSONRequest request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                request.setJSONValue(new JSONObject("{\"folder_id\":\"" + newCalId + "\"}"));
                request.setParameter("action", "update");
                request.setParameter("id", newMailId);
                final GenJSONResponse response = getClient().execute(request);
                final String newMailIDMoved = (String) response.getData();
                assertEquals("ID must not be equal.", newMailId, newMailIDMoved);
                newMailId = newMailIDMoved;
            }
        } finally {
            if (null != newCalId) {
                // Delete folder
                try {
                    final GenJSONRequest request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                    request.setJSONValue(new JSONArray("[\"" + newCalId + "\"]"));
                    request.setParameter("action", "delete");
                    getClient().execute(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != newMailId) {
                // Delete folder
                try {
                    final GenJSONRequest request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                    request.setJSONValue(new JSONArray("[\"" + newMailId + "\"]"));
                    request.setParameter("action", "delete");
                    getClient().execute(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
