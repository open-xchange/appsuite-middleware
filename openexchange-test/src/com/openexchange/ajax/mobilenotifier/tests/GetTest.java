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

    // TODO: update to correct mandatory items
    public void testMailProviderJSONResponse() throws OXException, IOException, JSONException {
        singleProvider("io.ox/mail", new MandatoryFields("from", "received_date", "subject", "flags", "attachments", "id", "folder"));
    }

    // TODO: update to correct mandatory items
    public void testCalendarProviderJSONResponse() throws OXException, IOException, JSONException {
        singleProvider("io.ox/calendar", new MandatoryFields(
            "id",
            "folder",
            "title",
            "location",
            "start_date",
            "organizer",
            "status",
            "comments",
            "recurrence_"));
    }

    public void testShouldGetExceptionIfUnknownProvider() throws OXException, IOException, JSONException {
        GetMobileNotifierRequest req = new GetMobileNotifierRequest("io.ox/mehl");
        GetMobileNotifierResponse res = getClient().execute(req);
        assertNotNull("exception should have thrown ", res.getException());
    }

    private void singleProvider(String providerName, MandatoryFields mandatoryItems) throws OXException, IOException, JSONException {
        GetMobileNotifierRequest req = new GetMobileNotifierRequest(providerName);
        GetMobileNotifierResponse res = getClient().execute(req);

        assertFalse("received following error: " + res.getException(), res.hasError());

        assertNotNull("no data in response", res.getData());
        JSONObject providerObject = (JSONObject) res.getData();
        assertTrue("could not find element \"provider\" in json structure", providerObject.has("provider"));
        JSONObject singleProviderObject = (JSONObject) providerObject.get("provider");

        assertTrue("could not find provider " + providerName, singleProviderObject.has(providerName));
        JSONObject providerJSON = (JSONObject) singleProviderObject.get(providerName);

        singleProviderObject = (JSONObject) singleProviderObject.get(providerName);

        assertTrue("could not find element \"items\"", providerJSON.has("items"));
        JSONArray itemsArray = (JSONArray) providerJSON.get("items");

        List<String> missingFields = new ArrayList<String>();
        String[] mandatoryArr = mandatoryItems.getMandatory();

        assertTrue("no items found ", itemsArray.length() > 0);

        // get only the first notification item
        JSONObject itemObject = itemsArray.getJSONObject(0);
        for (int i = 0; i < mandatoryArr.length; i++) {
            if (!itemObject.has(mandatoryArr[i])) {
                missingFields.add(mandatoryArr[i]);
            }
        }

        assertFalse("could not found the mandatory item: " + missingFields, missingFields.size() > 0);
    }
}