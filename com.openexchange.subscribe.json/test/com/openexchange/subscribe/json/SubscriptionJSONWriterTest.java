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
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.json.JSONAssertion.assertValidates;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.json.JSONAssertion;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;

/**
 * {@link SubscriptionJSONWriterTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionJSONWriterTest {
    private Subscription subscription;
    private DynamicFormDescription form;

    @Before
    public void setUp() {
        subscription = new Subscription();
        subscription.setFolderId("12");
        subscription.setId(2);
        subscription.setEnabled(false);
        subscription.setDisplayName("mySubscription");
        subscription.setLastUpdate(12);

        SubscriptionSource source = new SubscriptionSource();
        source.setId("com.openexchange.subscribe.test1");
        subscription.setSource(source);

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("username", "My Username");
        config.put("password", "My Password");
        subscription.setConfiguration(config);

        form = new DynamicFormDescription();
        form.add(FormElement.input("username", "Username")).add(FormElement.password("password", "Password"));

    }

         @Test
     public void testWriteAsObject() throws JSONException, OXException {

        JSONObject object = new SubscriptionJSONWriter().write(subscription, form, null, TimeZone.getTimeZone("utc"));

        JSONAssertion assertion =
            new JSONAssertion()
                .isObject()
                .hasKey("id").withValue(I(2))
                .hasKey("folder").withValue("12")
                .hasKey("enabled").withValue(B(false))
                .hasKey("displayName").withValue("mySubscription")
                .hasKey("source").withValue("com.openexchange.subscribe.test1")
                .hasKey("com.openexchange.subscribe.test1").withValueObject()
                .hasNoMoreKeys()
                .hasNoMoreKeys();

        assertValidates(assertion, object);
    }

         @Test
     public void testWriteArray() throws OXException {
        Map<String, String[]> specialCols = new HashMap<String, String[]>();
        String[] basicCols = new String[] { "id", "source", "displayName", "enabled" };
        specialCols.put("com.openexchange.subscribe.test1", new String[] { "username" });

        JSONArray array = new SubscriptionJSONWriter().writeArray(
            subscription,
            basicCols,
            specialCols,
            Arrays.asList("com.openexchange.subscribe.test1"), form, TimeZone.getTimeZone("utc"));

        JSONAssertion assertion =
            new JSONAssertion()
                .isArray().withValues(I(2), "com.openexchange.subscribe.test1", "mySubscription", B(false), "My Username");

        assertValidates(assertion, array);
    }

         @Test
     public void testWriteArrayWithUnusedSource() throws OXException {
        Map<String, String[]> specialCols = new HashMap<String, String[]>();
        String[] basicCols = new String[] { "id", "source" };
        specialCols.put("com.openexchange.subscribe.test2", new String[] { "username", "field1", "field2" });

        JSONArray array = new SubscriptionJSONWriter().writeArray(
            subscription,
            basicCols,
            specialCols,
            Arrays.asList("com.openexchange.subscribe.test2"), form, TimeZone.getTimeZone("utc"));

        JSONAssertion assertion = new JSONAssertion().isArray().withValues(
            I(2),
            "com.openexchange.subscribe.test1",
            JSONObject.NULL,
            JSONObject.NULL,
            JSONObject.NULL);

        assertValidates(assertion, array);

    }

         @Test
     public void testUnknownColumnGeneratesException() {
        Map<String, String[]> specialCols = new HashMap<String, String[]>();
        String[] basicCols = new String[] { "id", "unknownColumn" };

        try {
            new SubscriptionJSONWriter().writeArray(
                subscription,
                basicCols,
                specialCols,
                Arrays.asList("com.openexchange.subscribe.test2"), form, TimeZone.getTimeZone("utc"));
            fail("Expected Exception");
        } catch (OXException x) {

        }
        //TODO: Then what?
    }
}
