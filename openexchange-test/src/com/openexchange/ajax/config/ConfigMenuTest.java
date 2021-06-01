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

package com.openexchange.ajax.config;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Random;
import java.util.TimeZone;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.tools.arrays.Arrays;

/**
 * This test case tests the AJAX interface of the config system for the AJAX
 * GUI.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ConfigMenuTest extends AbstractAJAXSession {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigMenuTest.class);
    private static final Random RAND = new Random(System.currentTimeMillis());

    /**
     * Default constructor.
     *
     * @param name Name of this test.
     */
    public ConfigMenuTest() {
        super();
    }

    /**
     * Tests if the settings can be read from the server.
     */
    @Test
    public void testReadSettings() throws Throwable {
        final GetRequest request = new GetRequest(Tree.ALL);
        final GetResponse response = getClient().execute(request);
        final JSONObject settings = response.getJSON();
        LOG.trace("Settings: {}", settings.toString());
        assertTrue("Got no value from server.", settings.length() > 0);
    }

    /**
     * Tests if the spam-button setting can be read from the server.
     */
    @Test
    public void testHasSpam() throws Throwable {
        final GetRequest request = new GetRequest(Tree.SpamButton);
        final GetResponse response = getClient().execute(request);
        final boolean spamButtonEnabled = response.getBoolean();
        LOG.trace("Spam Button enabled: {}", B(spamButtonEnabled));
        assertTrue("Got no spam-button-enabled flag from server.", response.hasValue());
    }

    /**
     * Tests if the time zone of a user can be changed.
     */
    @Test
    public void testTimeZone() throws Throwable {
        final GetRequest getRequest = new GetRequest(Tree.TimeZone);
        GetResponse getResponse = getClient().execute(getRequest);
        final String origTimeZone = getResponse.getString();
        String[] zones = Arrays.remove(TimeZone.getAvailableIDs(), origTimeZone);
        final String testTimeZone = zones[RAND.nextInt(zones.length)];
        SetRequest setRequest = new SetRequest(Tree.TimeZone, testTimeZone);
        try {
            getClient().execute(setRequest);
            getResponse = getClient().execute(getRequest);
            assertEquals("Written timezone isn't returned from server. Used session id: " + getClient().getSession().getId(), testTimeZone, getResponse.getString());
        } finally {
            setRequest = new SetRequest(Tree.TimeZone, origTimeZone);
            getClient().execute(setRequest);
        }
    }

    /**
     * Tests if the beta feature support of a user can be changed.
     */
    @Test
    public void testBeta() throws Throwable {
        final GetRequest getRequest = new GetRequest(Tree.Beta);
        GetResponse getResponse = getClient().execute(getRequest);
        final boolean beta = getResponse.getBoolean();
        final Boolean testBeta = Boolean.FALSE;
        SetRequest setRequest = new SetRequest(Tree.Beta, testBeta);
        try {
            getClient().execute(setRequest);
            getResponse = getClient().execute(getRequest);
            assertTrue("Written beta attribute isn't returned from server.", testBeta.booleanValue() == getResponse.getBoolean());
        } finally {
            setRequest = new SetRequest(Tree.Beta, B(beta));
            getClient().execute(setRequest);
        }
    }

    /**
     * Tests if the unique identifier of the user can be loaded.
     */
    @Test
    public void testIdentifier() throws Throwable {
        final GetRequest request = new GetRequest(Tree.Identifier);
        final GetResponse response = getClient().execute(request);
        final int userId = response.getInteger();
        LOG.trace("UserId: {}", I(userId));
        assertTrue("No valid user identifier", userId > 0);
    }
}
