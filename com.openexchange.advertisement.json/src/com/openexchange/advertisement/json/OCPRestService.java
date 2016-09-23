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

package com.openexchange.advertisement.json;

import java.util.HashMap;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.advertisement.AdvertisementConfigService;
import com.openexchange.advertisement.AdvertisementExceptionCodes;
import com.openexchange.advertisement.AdvertisementPackageService;
import com.openexchange.advertisement.ConfigResult;
import com.openexchange.advertisement.json.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link OCPRestService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
@RoleAllowed(Role.BASIC_AUTHENTICATED)
@Path("/advertisement/v1")
public class OCPRestService {

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/config/user")
    public Response putConfig(@QueryParam("ctxId") int ctxId, @QueryParam("userId") int userId, JSONObject body) throws OXException {
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
    public Response putConfig(@QueryParam("reseller") String reseller, @QueryParam("package") String pack, JSONObject body) throws OXException {
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
    public Response putConfig(@QueryParam("reseller") String reseller, JSONArray array) throws OXException {
        AdvertisementPackageService packageService = Services.getService(AdvertisementPackageService.class);
        AdvertisementConfigService configService = packageService.getDefaultScheme();
        if (configService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(AdvertisementConfigService.class.getSimpleName());
        }

        Map<String, String> data;
        //Parse configs String
        try {
            if (array.isEmpty()) {
                ResponseBuilder builder = Response.status(200);
                builder.entity(new JSONArray(0));
                return builder.build();
            }
            data = new LinkedHashMap<>(array.length());

            for (Object value : array.asList()) {
                if (!(value instanceof HashMap)) {
                    throw new JSONException("Child is not a JSONObject.");
                }
                @SuppressWarnings("unchecked") HashMap<String, String> obj = (HashMap<String, String>) value;
                String pack = obj.get("package");
                String config = obj.get("config");
                config = config == JSONObject.NULL ? null : config;
                data.put(pack, config);
            }
        } catch (JSONException e) {
            throw AdvertisementExceptionCodes.PARSING_ERROR.create(e.getMessage());
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
    public Response putConfigByName(@QueryParam("name") String name, @QueryParam("ctxId") int ctxId, JSONObject body) throws OXException {
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
    public Response removeConfig(@QueryParam("ctxId") int ctxId, @QueryParam("userId") int userId) throws OXException {
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
    public Response removeConfigByName(@QueryParam("name") String name, @QueryParam("ctxId") int ctxId) throws OXException {
        AdvertisementPackageService packageService = Services.getService(AdvertisementPackageService.class);
        AdvertisementConfigService configService = packageService.getDefaultScheme();
        if (configService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(AdvertisementConfigService.class.getSimpleName());
        }
        ConfigResult result = configService.setConfigByName(name, ctxId, null);
        return createResponse(result);
    }


}
