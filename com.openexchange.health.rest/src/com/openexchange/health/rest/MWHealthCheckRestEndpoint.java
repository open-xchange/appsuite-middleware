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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.json.ImmutableJSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.health.MWHealthCheckProperty;
import com.openexchange.health.MWHealthCheckResponse;
import com.openexchange.health.MWHealthCheckResult;
import com.openexchange.health.MWHealthCheckService;
import com.openexchange.health.MWHealthState;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.rest.services.EndpointAuthenticator;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.version.Version;

/**
 * {@link MWHealthCheckRestEndpoint}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
@Path("/health")
@RoleAllowed(Role.INDIVIDUAL_BASIC_AUTHENTICATED)
public class MWHealthCheckRestEndpoint implements EndpointAuthenticator {

    private static final JSONObject SIMPLE_UP_RESPONSE;
    static {
        JSONObject simpleUpResponse = new JSONObject(3);
        simpleUpResponse.putSafe("status", "UP");
        simpleUpResponse.putSafe("checks", JSONArray.EMPTY_ARRAY);
        SIMPLE_UP_RESPONSE = ImmutableJSONObject.immutableFor(simpleUpResponse);
    }

    private static final JSONObject SIMPLE_DOWN_RESPONSE;
    static {
        JSONObject simpleUpResponse = new JSONObject(3);
        simpleUpResponse.putSafe("status", "DOWN");
        simpleUpResponse.putSafe("checks", JSONArray.EMPTY_ARRAY);
        SIMPLE_DOWN_RESPONSE = ImmutableJSONObject.immutableFor(simpleUpResponse);
    }

    private final ServiceLookup services;

    public MWHealthCheckRestEndpoint(ServiceLookup services) {
        super();
        this.services = services;
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHealth() {
        MWHealthCheckService service = services.getService(MWHealthCheckService.class);
        MWHealthCheckResult result = null;
        if (null == service) {
            MWHealthCheckService.LOG.error("Health Status: DOWN (MWHealthCheckService is unavailable)");
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }

        try {
            result = service.check();
        } catch (RuntimeException e) {
            MWHealthCheckService.LOG.error("Health Status: DOWN ({})", e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        if (null == result) {
            ResponseBuilder builder = Response.ok(SIMPLE_UP_RESPONSE, MediaType.APPLICATION_JSON);
            return builder.build();
        }

        JSONObject jsonResponse = new JSONObject(4);
        List<MWHealthCheckResponse> checkResponses = result.getChecks();
        JSONArray responseArray = new JSONArray(checkResponses.size());
        try {
            jsonResponse.put("status", MWHealthState.UP.equals(result.getStatus()) ? "UP" : "DOWN");
            for (MWHealthCheckResponse response : checkResponses) {
                JSONObject health = new JSONObject(3);
                boolean status = MWHealthState.UP.equals(response.getState());
                health.put("name", response.getName());
                health.put("status", status ? "UP" : "DOWN");
                Map<String, Object> data = response.getData();
                if (null != data) {
                    JSONObject obj = new JSONObject(data.keySet().size());
                    for (Entry<String, Object> entry : data.entrySet()) {
                        obj.put(entry.getKey(), entry.getValue());
                    }
                    health.put("data", obj);
                }
                responseArray.put(health);
            }
            jsonResponse.put("checks", responseArray);
            jsonResponse.put("service", getServerInfo());
        } catch (JSONException e) {
            // will not happen
        } catch (RuntimeException e) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(SIMPLE_DOWN_RESPONSE).type(MediaType.APPLICATION_JSON).build();
        }
        if (MWHealthState.UP.equals(result.getStatus())) {
            return Response.ok(jsonResponse, MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Status.SERVICE_UNAVAILABLE).entity(jsonResponse).type(MediaType.APPLICATION_JSON).build();
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

    @Override
    public String getRealmName() {
        return "OX HEALTH";
    }

    @Override
    public boolean permitAll(Method invokedMethod) {
        Pair<String, String> credentials;
        try {
            credentials = getCredentials();
            return (null == credentials || (Strings.isEmpty(credentials.getFirst()) && Strings.isEmpty(credentials.getSecond())));
        } catch (OXException e) {
            return false;
        }
    }

    @Override
    public boolean authenticate(String login, String password, Method invokedMethod) {
        Pair<String, String> credentials;
        try {
            credentials = getCredentials();
            return (null != credentials && credentials.getFirst().equals(login) && credentials.getSecond().equals(password));
        } catch (OXException e) {
            return false;
        }
    }

    private Pair<String, String> getCredentials() throws OXException {
        LeanConfigurationService configurationService = services.getService(LeanConfigurationService.class);
        if (null == configurationService) {
            throw ServiceExceptionCode.absentService(LeanConfigurationService.class);
        }
        String username = configurationService.getProperty(MWHealthCheckProperty.username);
        String password = configurationService.getProperty(MWHealthCheckProperty.password);
        return new Pair<String, String>(username, password);
    }

}
