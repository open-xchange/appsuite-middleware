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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajax.mobilenotifier.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import com.openexchange.ajax.mobilenotifier.actions.ConfiggetMobileNotifierRequest;
import com.openexchange.ajax.mobilenotifier.actions.ConfiggetMobileNotifierResponse;
import com.openexchange.exception.OXException;

/**
 * {@link ConfiggetTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class ConfiggetTest extends AbstractMobileNotifierTest {

    /**
     * Initializes a new {@link ConfiggetTest}.
     * 
     * @param name
     */
    public ConfiggetTest(String name) {
        super(name);
    }

    public void testMobileNotifierJSONResponse() throws OXException, IOException, JSONException {
        List<String> providerValue = new ArrayList<String>();
        providerValue.add("mail");
        providerValue.add("appointment");

        ConfiggetMobileNotifierRequest req = new ConfiggetMobileNotifierRequest(providerValue);
        ConfiggetMobileNotifierResponse res = getClient().execute(req);

        assertNotNull(res.getData());
        JSONObject notifyTemplateJSON = (JSONObject) res.getData();
        
        assertNotNull("could not find element \"provider\" in json structure", notifyTemplateJSON.get("provider"));
        JSONArray providersArray = (JSONArray) notifyTemplateJSON.get("provider");

        assertEquals(
            "size of provider parameter values not identical to size of json provider structure",
            providerValue.size(),
            providersArray.length());

        for (int i = 0; i < providersArray.length(); i++) {
            assertNotNull(providersArray.get(i));
            JSONObject providerObject = (JSONObject) providersArray.get(i);
            
            JSONObject attributesObject = (JSONObject) providerObject.get(providerValue.get(i));
            assertNotNull("could not find attribute template", attributesObject.get("template"));
            Assert.assertTrue("value of attribute template is empty ", attributesObject.getString("template").length() > 0);
            assertNotNull("could not find attribute frontendApp",attributesObject.get("frontendApp"));
            Assert.assertTrue("value of attribute template is empty ", attributesObject.getString("frontendApp").length() > 0);
            assertNotNull("could not find attribute slow", attributesObject.get("slow"));
            Assert.assertTrue("value of attribute template is empty ", attributesObject.getString("slow").length() > 0);
        }
    }

    public void testMobileNotifierJSONResponseWithoutProvider() throws OXException, IOException, JSONException {
        ConfiggetMobileNotifierRequest req = new ConfiggetMobileNotifierRequest();
        ConfiggetMobileNotifierResponse res = getClient().execute(req);

        assertNotNull(res.getData());
        JSONObject notifyTemplateJSON = (JSONObject) res.getData();

        assertNotNull("could not find element \"provider\" in json structure", notifyTemplateJSON.get("provider"));
        JSONArray providersArray = (JSONArray) notifyTemplateJSON.get("provider");

        for (int i = 0; i < providersArray.length(); i++) {
            assertNotNull(providersArray.get(i));
            JSONObject providerObject = (JSONObject) providersArray.get(i);
            Iterator<?> keys = providerObject.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                JSONObject attributesObject = (JSONObject) providerObject.get(key);
                assertNotNull("could not find attribute template", attributesObject.get("template"));
                Assert.assertTrue("value of attribute template is empty ", attributesObject.getString("template").length() > 0);
                assertNotNull("could not find attribute frontendApp",attributesObject.get("frontendApp"));
                Assert.assertTrue("value of attribute template is empty ", attributesObject.getString("frontendApp").length() > 0);
                assertNotNull("could not find attribute slow", attributesObject.get("slow"));
                Assert.assertTrue("value of attribute template is empty ", attributesObject.getString("slow").length() > 0);
            }
        }
    }

    public void testShouldThrowExceptionIfUnknownProvider() throws OXException, IOException, JSONException {
        List<String> providerValue = new ArrayList<String>();
        providerValue.add("mehl");

        ConfiggetMobileNotifierRequest req = new ConfiggetMobileNotifierRequest(providerValue);
        ConfiggetMobileNotifierResponse res = getClient().execute(req);

        assertNotNull(res.getException());
    }
}
