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

package com.openexchange.ajax.xing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.xing.actions.FindByMailRequest;
import com.openexchange.ajax.xing.actions.FindByMailResponse;
import com.openexchange.ajax.xing.actions.FindByMailsRequest;
import com.openexchange.ajax.xing.actions.FindByMailsResponse;
import com.openexchange.exception.OXException;

/**
 * {@link FindByMailTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class FindByMailTest extends AbstractAJAXSession {

    /**
     * Initializes a new {@link FindByMailTest}.
     * 
     * @param name
     */
    public FindByMailTest(String name) {
        super(name);
    }

    /**
     * Test find_by_mails action
     * 
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public void testFindByMails() throws OXException, IOException, JSONException {
    	List<String> emails = new ArrayList<String>();
    	emails.add("ewaldbartkowiak@googlemail.com");
    	emails.add("annamariaoberhuber@googlemail.com");
        final FindByMailsRequest findByMailsRequest = new FindByMailsRequest(emails, true);
        final FindByMailsResponse findByMailsResponse = client.execute(findByMailsRequest);
        JSONObject jsonResponse = (JSONObject) findByMailsResponse.getData();
        assertNotNull(findByMailsResponse);
        assertNotNull(jsonResponse.getJSONObject("results"));
        int returnedResults = jsonResponse.getJSONObject("results").getInt("total");
        assertEquals("Returned unexpected total of users", returnedResults, 2);
        JSONObject results = jsonResponse.getJSONObject("results");
        assertNotNull(results.getJSONArray("items"));
        JSONArray items = results.getJSONArray("items");
        JSONObject user1 = items.getJSONObject(0);
        assertNotNull(user1.get("user"));
        assertNotNull(user1.get("email"));
        JSONObject user2 = items.getJSONObject(1);
        assertNotNull(user2.get("user"));
        assertNotNull(user2.get("email"));
    }

    /**
     * Test find_by_mail action
     * 
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public void testFindByMail() throws OXException, IOException, JSONException {
        final FindByMailRequest findByMailRequest = new FindByMailRequest("ewaldbartkowiak@googlemail.com", true);
        final FindByMailResponse findByMailResponse = client.execute(findByMailRequest);
        JSONObject jsonResponse = (JSONObject) findByMailResponse.getData();
        assertNotNull(findByMailResponse);
        assertNotNull(jsonResponse.get("id"));
    }
}
