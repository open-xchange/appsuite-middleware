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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.xing;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.xing.actions.NewsFeedRequest;
import com.openexchange.ajax.xing.actions.NewsFeedResponse;
import com.openexchange.exception.OXException;
import com.openexchange.xing.UserField;

/**
 * {@link NewsFeedTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class NewsFeedTest extends AbstractAJAXSession {

    /**
     * Initializes a new {@link NewsFeedTest}.
     */
    public NewsFeedTest(String name) {
        super(name);
    }
    
    /**
     * Simple test to verify that the action fetches the network_activities
     * 
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public void r() throws OXException, IOException, JSONException {
        NewsFeedRequest request = new NewsFeedRequest(false, -1, -1, new int[0], true);
        NewsFeedResponse response = client.execute(request);
        assertNotNull(response);
        JSONObject json = (JSONObject) response.getData();
        assertNotNull(json.getJSONArray("network_activities"));
    }
    
    /**
     * Test to verify userfields
     * 
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public void testNewsFeedWithUserFields() throws OXException, IOException, JSONException {
        int[] uf = {UserField.FIRST_NAME.ordinal(), UserField.LAST_NAME.ordinal()};
        NewsFeedRequest request = new NewsFeedRequest(false, -1, -1, uf, true);
        NewsFeedResponse response = client.execute(request);
        assertNotNull(response);
        
        JSONObject json = (JSONObject) response.getData();
        JSONArray network_activities = json.getJSONArray("network_activities");
        assertNotNull(network_activities);
        assertTrue(network_activities.length() > 0);
        
        JSONObject na1 = network_activities.getJSONObject(0);
        JSONArray objects = na1.getJSONArray("objects");
        assertTrue(objects.length() > 0);
        
        JSONObject obj1 = objects.getJSONObject(0);
        assertTrue(obj1.hasAndNotNull("creator"));
        
        JSONObject creator = obj1.getJSONObject("creator");
        
        assertTrue(creator.hasAndNotNull("id"));
        assertTrue(creator.hasAndNotNull("last_name"));
        assertTrue(creator.hasAndNotNull("first_name"));
        assertFalse(creator.hasAndNotNull("active_email"));
        assertFalse(creator.hasAndNotNull("wants"));
    }
    
    /**
     * Test error code for mutually exclusive URL parameters 'since' and 'until'
     * 
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public void testNewsFeedWithSinceAndUntil() throws OXException, IOException, JSONException {
        NewsFeedRequest request = new NewsFeedRequest(true, 123, 123, new int[0], false);
        NewsFeedResponse response = client.execute(request);
        assertNotNull(response);
        assertEquals("XING-0021", response.getException().getErrorCode());
    }
}
