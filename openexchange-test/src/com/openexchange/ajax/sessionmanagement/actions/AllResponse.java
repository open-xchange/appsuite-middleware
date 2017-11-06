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

package com.openexchange.ajax.sessionmanagement.actions;

import java.util.ArrayList;
import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;
import com.openexchange.session.management.ManagedSession;
import com.openexchange.session.management.impl.DefaultManagedSession;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * {@link AllResponse}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class AllResponse extends AbstractAJAXResponse {

    protected AllResponse(Response response) {
        super(response);
    }

    public Collection<ManagedSession> getSessions() throws OXException {
        try {
            JSONArray array = (JSONArray) getData();
            ArrayList<ManagedSession> sessions = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                sessions.add(parseSession(array.getJSONObject(i)));
            }
            return Collections.unmodifiableList(sessions);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private ManagedSession parseSession(JSONObject obj) throws JSONException {
        String sessionId = obj.getString("sessionId");
        String ipAddress = obj.getString("ipAddress");
        String client = obj.getString("client");
        String userAgent = obj.getString("userAgent");
        long loginTime = obj.optLong("loginTime");
        long lastActive = obj.optLong("lastActive");
        String location = obj.optString("location");
        JSONObject deviceInfo = obj.optJSONObject("device");
        DefaultManagedSession session = DefaultManagedSession.builder().setClient(client).setIpAddress(ipAddress).setLocation("").setLoginTime(loginTime).setLastActive(lastActive).setSessionId(sessionId).setUserAgent(userAgent).build();
        return session;
    }

}
