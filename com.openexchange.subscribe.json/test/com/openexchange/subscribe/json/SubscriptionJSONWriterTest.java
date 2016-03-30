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

package com.openexchange.subscribe.json;

import static com.openexchange.json.JSONAssertion.assertValidates;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
public class SubscriptionJSONWriterTest extends TestCase {

    private Subscription subscription;
    private DynamicFormDescription form;

    @Override
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

    public void testWriteAsObject() throws JSONException, OXException {

        JSONObject object = new SubscriptionJSONWriter().write(subscription, form, null, TimeZone.getTimeZone("utc"));

        JSONAssertion assertion =
            new JSONAssertion()
                .isObject()
                .hasKey("id").withValue(2)
                .hasKey("folder").withValue("12")
                .hasKey("enabled").withValue(false)
                .hasKey("displayName").withValue("mySubscription")
                .hasKey("source").withValue("com.openexchange.subscribe.test1")
                .hasKey("com.openexchange.subscribe.test1").withValueObject()
                .hasNoMoreKeys()
                .hasNoMoreKeys();

        assertValidates(assertion, object);
    }

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
                .isArray().withValues(2, "com.openexchange.subscribe.test1", "mySubscription", false, "My Username");

        assertValidates(assertion, array);
    }

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
            2,
            "com.openexchange.subscribe.test1",
            JSONObject.NULL,
            JSONObject.NULL,
            JSONObject.NULL);

        assertValidates(assertion, array);

    }

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
