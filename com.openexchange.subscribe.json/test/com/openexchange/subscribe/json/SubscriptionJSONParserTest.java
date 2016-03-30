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

import java.util.Map;
import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;
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
public class SubscriptionJSONParserTest extends TestCase {
    private static final String SOURCE_NAME = "com.openexchange.subscribe.test1";
    private JSONObject object;
    private SimSubscriptionSourceDiscoveryService discovery;
    private DynamicFormDescription form = null;

    @Override
    public void setUp() throws Exception{
        object = new JSONObject();
        object.put("id", 2);
        object.put("folder" , 12);
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

    public void testParsing() throws JSONException {
        Subscription subscription = new SubscriptionJSONParser(discovery).parse(object);
        assertNotNull("Subscription may not be null", subscription);
        assertEquals("Got wrong id", 2, subscription.getId());
        assertEquals("Got wrong folder", "12", subscription.getFolderId());
        assertNotNull("Got wrong subscription source", subscription.getSource());
        assertEquals("Got wrong subscription source", SOURCE_NAME, subscription.getSource().getId());
        assertEquals("Got wrong enablement", false, subscription.isEnabled());

        Map<String, Object> configuration = subscription.getConfiguration();

        assertNotNull("Configuration should not be null", configuration);
        assertEquals("Expected username", "My Username", configuration.get("username"));
        assertEquals("Expected password", "My Password", configuration.get("password"));

    }

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
