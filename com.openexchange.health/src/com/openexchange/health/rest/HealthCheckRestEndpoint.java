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

package com.openexchange.health.rest;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.eclipse.microprofile.health.HealthCheckResponse.State;
import org.json.ImmutableJSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.health.DefaultHealthCheckResponse;
import com.openexchange.health.internal.HealthCheckService;
import com.openexchange.rest.services.EndpointAuthenticator;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.version.Version;

/**
 * {@link HealthCheckRestEndpoint}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
@Path("/health")
@RoleAllowed(Role.INDIVIDUAL_BASIC_AUTHENTICATED)
public class HealthCheckRestEndpoint implements EndpointAuthenticator {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckRestEndpoint.class);

    private static final JSONObject SIMPLE_UP_RESPONSE;
    static {
        JSONObject simpleUpResponse = new JSONObject(3);
        simpleUpResponse.putSafe("status", "UP");
        simpleUpResponse.putSafe("checks", JSONArray.EMPTY_ARRAY);
        SIMPLE_UP_RESPONSE = ImmutableJSONObject.immutableFor(simpleUpResponse);
    }

    private final HealthCheckService service;

    public HealthCheckRestEndpoint(HealthCheckService service) {
        super();
        this.service = service;
    }

    @Override
    public boolean permitAll(Method invokedMethod) {
        return true;
    }

    @Override
    public boolean authenticate(String login, String password, Method invokedMethod) {
        // TODO Check against configured login/password
        return true;
    }

    @Override
    public String getRealmName() {
        return "OX HEALTH";
    }

    @GET
    @Path("/")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHealth() {
        Map<String, DefaultHealthCheckResponse> result = null;
        try {
            result = service.check();
        } catch (OXException e) {
            LOG.error(e.getLogMessage());
            Response.serverError();
        }

        if (null == result || result.size() == 0) {
            ResponseBuilder builder = Response.ok(SIMPLE_UP_RESPONSE, MediaType.APPLICATION_JSON);
            return builder.build();
        }

        boolean overallStatus = true;
        JSONObject jsonResponse = new JSONObject(2);
        JSONArray responseArray = new JSONArray(result.size());
        try {
            for (String healthCheckName : result.keySet()) {
                DefaultHealthCheckResponse response = result.get(healthCheckName);

                JSONObject health = new JSONObject(3);
                boolean status = State.UP.equals(response.getState());
                health.put("name", healthCheckName);
                health.put("status", status ? "UP" : "DOWN");
                Optional<Map<String, Object>> data = response.getData();
                if (null != data && data.isPresent()) {
                    Map<String, Object> map = data.get();
                    JSONObject obj = new JSONObject(map.keySet().size());
                    for (String key : map.keySet()) {
                        obj.put(key, map.get(key));
                    }
                    health.put("data", obj);
                }

                if (!service.getIgnoreList().contains(healthCheckName)) {
                    overallStatus &= status;
                }
                responseArray.put(health);
            }
            jsonResponse.put("status", overallStatus ? "UP" : "DOWN");
            jsonResponse.put("checks", responseArray);
            jsonResponse.put("service", getServerInfo());
        } catch (JSONException e) {
            // will not happen
        }
        return Response.ok(jsonResponse, MediaType.APPLICATION_JSON).build();
    }

    private JSONObject getServerInfo() throws JSONException {
        JSONObject serverInfo = new JSONObject(7);
        serverInfo.put("name", "appsuite-middleware");
        Version version = Version.getInstance();
        serverInfo.put("version", version.getVersionString());
        serverInfo.put("buildDate", version.getBuildDate());
        serverInfo.put("date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSSZ").format(new Date()));
        serverInfo.put("timeZone", TimeZone.getDefault().getID());
        serverInfo.put("locale", Locale.getDefault());
        serverInfo.put("charset", Charset.defaultCharset().name());
        return serverInfo;
    }
}
