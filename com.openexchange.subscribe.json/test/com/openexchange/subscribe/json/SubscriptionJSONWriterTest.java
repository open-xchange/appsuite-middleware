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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import junit.framework.TestCase;


/**
 * {@link SubscriptionJSONWriterTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SubscriptionJSONWriterTest extends TestCase {
    
    private Subscription subscription;

    public void setUp() {
        subscription = new Subscription();
        subscription.setFolderId(12);
        subscription.setId(2);
        
        SubscriptionSource source = new SubscriptionSource();
        source.setId("com.openexchange.subscribe.test1");
        subscription.setSource(source);
        
        Map<String, String> config = new HashMap<String, String>();
        config.put("username", "My Username");
        config.put("password", "My Password");
        subscription.setConfiguration(config);
        
    }
    
    public void testWriteAsObject() throws JSONException {
        
        JSONObject object = new SubscriptionJSONWriter().write(subscription);
    
        JSONAssertion assertion = new JSONAssertion()
            .isObject()
            .hasKey("id").withValue(2)
            .hasKey("folder").withValue(12)
            .hasKey("source").withValue("com.openexchange.subscribe.test1")
            .hasKey("com.openexchange.subscribe.test1").withValueObject()
                .hasKey("username").withValue("My Username")
                .hasKey("password").withValue("My Password")
                .hasNoMoreKeys()
            .hasNoMoreKeys();
    
        assertValidates(assertion, object);
    }
    
    public void testWriteArray() {
        Map<String, String[]> specialCols = new HashMap<String, String[]>();
        String[] basicCols = new String[]{"id", "source"};
        specialCols.put("com.openexchange.subscribe.test1", new String[]{"username"});
        
        JSONArray array = new SubscriptionJSONWriter().writeArray(subscription, basicCols, specialCols, Arrays.asList("com.openexchange.subscribe.test1"));
    
        JSONAssertion assertion = new JSONAssertion()
            .isArray().withValues(2, "com.openexchange.subscribe.test1", "My Username");
    
        assertValidates(assertion, array);
    }
    
    public void testWriteArrayWithUnusedSource() {
        Map<String, String[]> specialCols = new HashMap<String, String[]>();
        String[] basicCols = new String[]{"id", "source"};
        specialCols.put("com.openexchange.subscribe.test2", new String[]{"username", "field1", "field2"});
        
        JSONArray array = new SubscriptionJSONWriter().writeArray(subscription, basicCols, specialCols, Arrays.asList("com.openexchange.subscribe.test2"));
    
        JSONAssertion assertion = new JSONAssertion()
            .isArray().withValues(2, "com.openexchange.subscribe.test1", JSONObject.NULL, JSONObject.NULL, JSONObject.NULL);
    
        assertValidates(assertion, array);
        
    }
    
    public static final void assertValidates(JSONAssertion assertion, Object o) {
        assertNotNull("Object was null", o);
        if(!assertion.validate(o)) {
            System.out.println(o);
            fail(assertion.getComplaint());
        }
    }
}
