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

package com.openexchange.ajax.rtc;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.rtc.actions.GetTokenRequest;
import com.openexchange.ajax.rtc.actions.GetTokenResponse;
import com.openexchange.exception.OXException;

/**
 * {@link GetTokenTest}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetTokenTest extends AbstractAJAXSession {

    /**
     * Initializes a new {@link GetTokenTest}.
     * 
     * @param name
     */
    public GetTokenTest(String name) {
        super(name);
    }

    /**
     * Get token test
     * 
     * @throws JSONException
     * @throws IOException
     * @throws OXException
     */
    public void testGetToken() throws OXException, IOException, JSONException {
        GetTokenRequest request = new GetTokenRequest("messenger", true);
        GetTokenResponse response = client.execute(request);
        assertNotNull(response);

        Object data = response.getData();
        assertNotNull(data);
        if (data instanceof JSONObject) {
            JSONObject o = (JSONObject) data;
            UserValues values = client.getValues();
            assertEquals("UserId differs", values.getUserId(), o.optInt("userId"));
            assertEquals("ContextId differs", values.getContextId(), o.optInt("contextId"));
            assertEquals("SessiondId differs", client.getSession().getId(), o.optString("sessionId"));
            assertNotNull("Token is not contained in the response payload", o.hasAndNotNull("token"));
        } else {
            fail("Bad response object");
        }
    }
}
