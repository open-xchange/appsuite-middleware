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

package com.openexchange.ajax.folder.api2;

import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GenJSONRequest;
import com.openexchange.ajax.folder.actions.GenJSONResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;

/**
 * {@link MoveTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MoveTest extends AbstractAJAXSession {

    private AJAXClient client;

    /**
     * Initializes a new {@link MoveTest}.
     *
     * @param name The name of the test.
     */
    public MoveTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    public void testMoveCalBelowMail() throws Throwable {
        // Get root folder
        String newCalId = null;
        String newMailId = null;
        try {
            final int userId = client.getValues().getUserId();
            {
                JSONObject newFolder =
                    new JSONObject(
                        "{\"title\":\"newCalFolder" + System.currentTimeMillis() + "\",\"module\":\"calendar\",\"permissions\":[{\"group\":false,\"bits\":403710016,\"entity\":" + userId + "}],\"subscribed\":1}");
                GenJSONRequest request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                request.setJSONValue(newFolder);
                request.setParameter("action", "new");
                request.setParameter("folder_id", "1");
                GenJSONResponse response = client.execute(request);
                newCalId = (String) response.getData();
                assertNotNull("New ID must not be null!", newCalId);

                newFolder =
                    new JSONObject(
                        "{\"title\":\"newMailFolder" + System.currentTimeMillis() + "\",\"module\":\"mail\",\"permissions\":[{\"group\":false,\"bits\":403710016,\"entity\":" + userId + "}],\"subscribed\":1}");
                request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                request.setJSONValue(newFolder);
                request.setParameter("action", "new");
                request.setParameter("folder_id", "1");
                response = client.execute(request);
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
                final GenJSONResponse response = client.execute(request);
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
                    client.execute(request);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != newMailId) {
                // Delete folder
                try {
                    final GenJSONRequest request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                    request.setJSONValue(new JSONArray("[\"" + newMailId + "\"]"));
                    request.setParameter("action", "delete");
                    client.execute(request);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void testMoveMailBelowCal() throws Throwable {
        // Get root folder
        String newCalId = null;
        String newMailId = null;
        try {
            final int userId = client.getValues().getUserId();
            {
                JSONObject newFolder =
                    new JSONObject(
                        "{\"title\":\"newCalFolder" + System.currentTimeMillis() + "\",\"module\":\"calendar\",\"permissions\":[{\"group\":false,\"bits\":403710016,\"entity\":" + userId + "}],\"subscribed\":1}");
                GenJSONRequest request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                request.setJSONValue(newFolder);
                request.setParameter("action", "new");
                request.setParameter("folder_id", "1");
                GenJSONResponse response = client.execute(request);
                newCalId = (String) response.getData();
                assertNotNull("New ID must not be null!", newCalId);

                newFolder =
                    new JSONObject(
                        "{\"title\":\"newMailFolder" + System.currentTimeMillis() + "\",\"module\":\"mail\",\"permissions\":[{\"group\":false,\"bits\":403710016,\"entity\":" + userId + "}],\"subscribed\":1}");
                request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                request.setJSONValue(newFolder);
                request.setParameter("action", "new");
                request.setParameter("folder_id", "1");
                response = client.execute(request);
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
                final GenJSONResponse response = client.execute(request);
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
                    client.execute(request);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != newMailId) {
                // Delete folder
                try {
                    final GenJSONRequest request = new GenJSONRequest(EnumAPI.OUTLOOK, true);
                    request.setJSONValue(new JSONArray("[\"" + newMailId + "\"]"));
                    request.setParameter("action", "delete");
                    client.execute(request);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
