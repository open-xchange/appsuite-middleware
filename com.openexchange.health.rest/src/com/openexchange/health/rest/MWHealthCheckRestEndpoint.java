/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.health.rest;

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
import com.openexchange.exception.OXException;
import com.openexchange.health.MWHealthCheckResponse;
import com.openexchange.health.MWHealthCheckResult;
import com.openexchange.health.MWHealthCheckService;
import com.openexchange.health.MWHealthState;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceLookup;
import com.openexchange.version.VersionService;

/**
 * {@link MWHealthCheckRestEndpoint}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
@Path("/health")
@RoleAllowed(Role.INDIVIDUAL_BASIC_AUTHENTICATED)
public class MWHealthCheckRestEndpoint extends MWHealthAbstractEndpoint {

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

    public MWHealthCheckRestEndpoint(ServiceLookup services) {
        super(services);
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHealth() {
        MWHealthCheckService service = services.getService(MWHealthCheckService.class);
        MWHealthCheckResult result = null;
        if (null == service) {
            MWHealthCheckService.LOG.error("Health Status: DOWN (MWHealthCheckService is unavailable)");
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
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
        } catch (OXException | RuntimeException e) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(SIMPLE_DOWN_RESPONSE).type(MediaType.APPLICATION_JSON).build();
        }
        if (MWHealthState.UP.equals(result.getStatus())) {
            return Response.ok(jsonResponse, MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Status.SERVICE_UNAVAILABLE).entity(jsonResponse).type(MediaType.APPLICATION_JSON).build();
    }

    private JSONObject getServerInfo() throws JSONException, OXException {
        JSONObject serverInfo = new JSONObject(7);
        serverInfo.put("name", "appsuite-middleware");
        VersionService version = services.getServiceSafe(VersionService.class);
        serverInfo.put("version", version.getVersionString());
        serverInfo.put("buildDate", version.getBuildDate());
        serverInfo.put("date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSSZ").format(new Date()));
        serverInfo.put("timeZone", TimeZone.getDefault().getID());
        serverInfo.put("locale", Locale.getDefault());
        serverInfo.put("charset", Charset.defaultCharset().name());
        return serverInfo;
    }

}
