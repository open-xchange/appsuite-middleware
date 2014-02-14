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
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import com.openexchange.ajax.mobilenotifier.actions.GetMobileNotifierRequest;
import com.openexchange.ajax.mobilenotifier.actions.GetMobileNotifierResponse;
import com.openexchange.exception.OXException;

/**
 * {@link GetTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class GetTest extends AbstractMobileNotifierTest {
    /**
     * Initializes a new {@link GetTest}.
     * 
     * @param name
     */
    public GetTest(String name) {
        super(name);
    }

    // TODO
    private JSONObject getProvider(List<String> providerValue) throws OXException, IOException, JSONException {
        GetMobileNotifierRequest req = new GetMobileNotifierRequest(providerValue);
        GetMobileNotifierResponse res = getClient().execute(req);

        assertFalse("received following error: " + res.getErrorMessage(), res.hasError());
        assertNotNull("no data in response", res.getData());
        JSONObject notifyItemJson = (JSONObject) res.getData();

        assertNotNull("could not find element \"provider\" in json structure", notifyItemJson.get("provider"));
        JSONObject providersObject = (JSONObject) notifyItemJson.get("provider");

        assertEquals(
            "size of provider parameter values not identical to size of json provider structure",
            providerValue.size(),
            providersObject.length());

        for (int i = 0; i < providersObject.length(); i++) {
            assertNotNull("could not find provider", providersObject.get(providerValue.get(i)));
            JSONObject providerJSON = (JSONObject) providersObject.get(providerValue.get(i));
            assertNotNull("could not find element \"items\"", providerJSON.get("items"));
        }

        return (JSONObject) providersObject.get(providerValue.get(0));
    }

    public void testMailMobileNotifierJSONResponse() throws OXException, IOException, JSONException {
        List<String> providerValue = new ArrayList<String>();
        providerValue.add("io.ox/mail");
        JSONObject providerJSON = getProvider(providerValue);

        List<String> mandatoryItems = new ArrayList<String>();
        mandatoryItems.add("from");
        mandatoryItems.add("received_date");
        mandatoryItems.add("subject");
        mandatoryItems.add("flags");
        mandatoryItems.add("attachements");
        mandatoryItems.add("id");
        mandatoryItems.add("folder");

        JSONArray itemsArray = (JSONArray) providerJSON.get("items");

        // TODO
    }

    public void testShouldGetExceptionIfUnknownProvider() throws OXException, IOException, JSONException {
        List<String> providerValue = new ArrayList<String>();
        providerValue.add("mehl");
        GetMobileNotifierRequest req = new GetMobileNotifierRequest(providerValue);
        GetMobileNotifierResponse res = getClient().execute(req);
        assertNotNull("exception should have thrown " + res.getException());
    }
}