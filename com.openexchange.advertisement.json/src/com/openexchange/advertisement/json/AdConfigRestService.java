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

package com.openexchange.advertisement.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.advertisement.AdvertisementConfigService;
import com.openexchange.advertisement.AdvertisementPackageService;
import com.openexchange.advertisement.ConfigResult;
import com.openexchange.advertisement.json.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AdConfigRestService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
@RoleAllowed(Role.BASIC_AUTHENTICATED)
@Path("/advertisement/v1")
public class AdConfigRestService {

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/config/user")
    public Response putConfig(@QueryParam("contextId") int ctxId, @QueryParam("userId") int userId, JSONValue body) throws OXException {
        AdvertisementPackageService packageService = Services.getService(AdvertisementPackageService.class);
        AdvertisementConfigService configService = packageService.getScheme(ctxId);
        if (configService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(AdvertisementConfigService.class.getSimpleName());
        }
        ConfigResult result = configService.setConfig(userId, ctxId, body.toString());
        return createResponse(result);
    }

    private Response createResponse(ConfigResult result) throws OXException {
        switch (result.getConfigResultType()) {
            case CREATED:
                return Response.status(201).build();
            case ERROR:
                throw result.getError();
            case DELETED:
                return Response.status(204).build();
            case IGNORED:
            case UPDATED:
            default:
                return Response.status(200).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/config/package")
    public Response putConfig(@QueryParam("reseller") String reseller, @QueryParam("package") String pack, JSONValue body) throws OXException {
        AdvertisementPackageService packageService = Services.getService(AdvertisementPackageService.class);
        AdvertisementConfigService configService = packageService.getDefaultScheme();
        if (configService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(AdvertisementConfigService.class.getSimpleName());
        }
        ConfigResult result = configService.setConfig(reseller, pack, body.toString());
        return createResponse(result);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/config/reseller")
    @Produces(MediaType.APPLICATION_JSON)
    public Response putConfig(@QueryParam("reseller") String reseller, JSONValue body) throws OXException {
        AdvertisementPackageService packageService = Services.getService(AdvertisementPackageService.class);
        AdvertisementConfigService configService = packageService.getDefaultScheme();
        if (configService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(AdvertisementConfigService.class.getSimpleName());
        }

        Map<String, String> data;
        //Parse configs String
        try {
        	JSONArray array = body.toArray();
        	if (array == null) {
        		return Response.status(Status.BAD_REQUEST).build();
        	}
        	
            if (array.isEmpty()) {
                ResponseBuilder builder = Response.status(200);
                builder.entity(new JSONArray(0));
                return builder.build();
            }
        	
            data = new LinkedHashMap<>(array.length());
        	for (int i = 0; i < array.length(); i++) {
        		Object elem = array.get(i);
        		if (elem instanceof JSONObject) {
        			JSONObject json = (JSONObject) elem;
                    String pack = json.getString("package");
                    Object config = json.get("config");
                    config = config == JSONObject.NULL ? null : config.toString();                    
                    data.put(pack, (String) config);
        		} else {
        			return Response.status(Status.BAD_REQUEST).build();
        		}
        	}
        } catch (JSONException e) {
        	return Response.status(Status.BAD_REQUEST).build();
        }

        List<ConfigResult> results = configService.setConfig(reseller, data);
        // convert results to json
        JSONArray resultJSON = new JSONArray(results.size());
        for (ConfigResult result : results) {
            JSONObject singleResult = new JSONObject();
            try {
                singleResult.put("status", result.getMessage());
                if (result.hasError()) {
                    singleResult.put("error", result.getError().getMessage());
                }
            } catch (JSONException e) {
                // should never be thrown
                throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
            }
            resultJSON.put(singleResult);
        }

        ResponseBuilder builder = Response.status(200);
        builder.entity(resultJSON);
        return builder.build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/config/name")
    public Response putConfigByName(@QueryParam("name") String name, @QueryParam("contextId") int ctxId, JSONValue body) throws OXException {    	
        AdvertisementPackageService packageService = Services.getService(AdvertisementPackageService.class);
        AdvertisementConfigService configService = packageService.getDefaultScheme();
        if (configService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(AdvertisementConfigService.class.getSimpleName());
        }
        ConfigResult result = configService.setConfigByName(name, ctxId, body.toString());
        return createResponse(result);
    }

    @DELETE
    @Path("/config/user")
    public Response removeConfig(@QueryParam("contextId") int ctxId, @QueryParam("userId") int userId) throws OXException {
        AdvertisementPackageService packageService = Services.getService(AdvertisementPackageService.class);
        AdvertisementConfigService configService = packageService.getScheme(ctxId);
        if (configService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(AdvertisementConfigService.class.getSimpleName());
        }
        ConfigResult result = configService.setConfig(userId, ctxId, null);
        return createResponse(result);
    }

    @DELETE
    @Path("/config/package")
    public Response removeConfig(@QueryParam("reseller") String reseller, @QueryParam("package") String pack) throws OXException {
        AdvertisementPackageService packageService = Services.getService(AdvertisementPackageService.class);
        AdvertisementConfigService configService = packageService.getDefaultScheme();
        if (configService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(AdvertisementConfigService.class.getSimpleName());
        }
        ConfigResult result = configService.setConfig(reseller, pack, null);
        return createResponse(result);
    }

    @DELETE
    @Path("/config/name")
    public Response removeConfigByName(@QueryParam("name") String name, @QueryParam("contextId") int ctxId) throws OXException {
        AdvertisementPackageService packageService = Services.getService(AdvertisementPackageService.class);
        AdvertisementConfigService configService = packageService.getDefaultScheme();
        if (configService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(AdvertisementConfigService.class.getSimpleName());
        }
        ConfigResult result = configService.setConfigByName(name, ctxId, null);
        return createResponse(result);
    }


}
