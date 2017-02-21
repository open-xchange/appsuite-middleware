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

package com.openexchange.userfeedback.rest.services;

import java.util.Date;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.rest.services.JAXRSService;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceLookup;
import com.openexchange.uadetector.UserAgentParser;
import net.sf.uadetector.ReadableUserAgent;

/**
 * {@link CollectUserFeedbackService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.4
 */
@RoleAllowed(Role.BASIC_AUTHENTICATED)
@Path("/userfeedback/star-rating-v1")
public class CollectUserFeedbackService extends JAXRSService {

    public CollectUserFeedbackService(ServiceLookup services) {
        super(services);
    }

    @GET
    @Path("/test")
    public Object test() {
        AJAXRequestData requestData = getAJAXRequestData();
        UserAgentParser parser = getService(UserAgentParser.class);
        String userAgent = requestData.getUserAgent();
        ReadableUserAgent agent = parser.parse(userAgent);
        String os = agent.getOperatingSystem().getFamilyName();
        String osVersion = agent.getOperatingSystem().getVersionNumber().toVersionString();
        String browser = agent.getName();
        String browserVersion = agent.getVersionNumber().toVersionString();

        return os + osVersion + browser + browserVersion;
    }

    @PUT
    @Path("/collect")
    public Object collect(@QueryParam("ctxId") int ctxId, @QueryParam("userId") int userId, @QueryParam("cxtGroupId") int ctxGroupId, @QueryParam("loginName") String loginName) {
        AJAXRequestData requestData = getAJAXRequestData();
        UserAgentParser parser = getService(UserAgentParser.class);
        String userAgent = requestData.getUserAgent();
        Object data = requestData.getData();
        Date now = new Date();
        if (null == data || !JSONObject.class.isInstance(data)) {
            throw new BadRequestException();
        }
        JSONObject json = (JSONObject) data;
        try {
            int score = json.getInt("score");
            String app = json.getString("app");
            String entryPoint = json.getString("entryPoint");
            String comment = json.getString("comment");
            String screenResolution = json.getString("screenResolution");
            String language = json.getString("language");

            ReadableUserAgent agent = parser.parse(userAgent);
            String os = agent.getOperatingSystem().getFamilyName();
            String osVersion = agent.getOperatingSystem().getVersionNumber().toVersionString();
            String browser = agent.getName();
            String browserVersion = agent.getVersionNumber().toVersionString();
        } catch (JSONException e) {
            //
        }

        return null;
    }

}
