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

package com.openexchange.subscribe.json;

import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.subscribe.SimSubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;

/**
 * {@link SubscriptionJSONParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SubscriptionJSONParserTest {

    private static final String SOURCE_NAME = "com.openexchange.subscribe.test1";
    private JSONObject object;
    private SimSubscriptionSourceDiscoveryService discovery;
    private DynamicFormDescription form = null;

    @Before
    public void setUp() throws Exception {
        object = new JSONObject();
        object.put("id", 2);
        object.put("folder", 12);
        object.put("enabled", false);
        object.put("source", SOURCE_NAME);

        JSONObject config = new JSONObject();
        config.put("username", "My Username");
        config.put("password", "My Password");

        form = new DynamicFormDescription();
        form.add(FormElement.input("username", "Username")).add(FormElement.password("password", "Password"));

        object.put(SOURCE_NAME, config);

        discovery = new SimSubscriptionSourceDiscoveryService();
        SubscriptionSource source = new SubscriptionSource();
        source.setId(SOURCE_NAME);
        source.setFormDescription(form);
        discovery.addSource(source);
    }

    @Test
    public void testParsing() throws JSONException {
        Subscription subscription = new SubscriptionJSONParser(discovery).parse(object);
        assertNotNull("Subscription may not be null", subscription);
        assertEquals("Got wrong id", 2, subscription.getId());
        assertEquals("Got wrong folder", "12", subscription.getFolderId());
        assertNotNull("Got wrong subscription source", subscription.getSource());
        assertEquals("Got wrong subscription source", SOURCE_NAME, subscription.getSource().getId());
        assertEquals("Got wrong enablement", B(false), B(subscription.isEnabled()));

        Map<String, Object> configuration = subscription.getConfiguration();

        assertNotNull("Configuration should not be null", configuration);
        assertEquals("Expected username", "My Username", configuration.get("username"));
        assertEquals("Expected password", "My Password", configuration.get("password"));

    }

    @Test
    public void testShouldNotRequireId() throws JSONException {
        object.remove("id");
        Subscription subscription = new SubscriptionJSONParser(discovery).parse(object);
        assertNotNull("Subscription may not be null", subscription);

        assertEquals("Got wrong folder", "12", subscription.getFolderId());
        assertNotNull("Got wrong subscription source", subscription.getSource());
        assertEquals("Got wrong subscription source", SOURCE_NAME, subscription.getSource().getId());

        Map<String, Object> configuration = subscription.getConfiguration();

        assertNotNull("Configuration should not be null", configuration);
        assertEquals("Expected username", "My Username", configuration.get("username"));
        assertEquals("Expected password", "My Password", configuration.get("password"));

    }
}
