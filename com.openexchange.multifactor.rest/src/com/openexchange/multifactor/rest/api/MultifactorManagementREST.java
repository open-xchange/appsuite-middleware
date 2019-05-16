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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.multifactor.rest.api;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import javax.annotation.security.PermitAll;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.auth.Authenticator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.MultifactorManagementService;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;
import com.openexchange.osgi.Tools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.servlet.http.Authorization.Credentials;
import com.openexchange.user.UserService;

/**
 * {@link MultifactorManagementRESTService} - The REST endpoint for administrating multifactor authentication devices.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
@Path("admin/v1/contexts/{context-id}/users/{user-id}/multifactor/devices")
@PermitAll
public class MultifactorManagementREST {

    private static final Logger LOG =  org.slf4j.LoggerFactory.getLogger(MultifactorManagementREST.class);
    private static final String JSON_ID = "id";
    private static final String JSON_NAME = "name";
    private static final String JSON_PROVIDER_NAME = "providerName";
    private static final String JSON_ENABLED = "enabled";
    private static final String JSON_BACKUP = "backup";
    private final ServiceLookup services;

    /**
     * Initializes a new {@link MultifactorManagementREST}.
     *
     * @param services The {@link ServiceLookup} to get services from
     */
    public MultifactorManagementREST(ServiceLookup services) {
        this.services = services;
    }

    private <T> T requireService(Class<? extends T> clazz) throws OXException {
        return Tools.requireService(clazz, services);
    }

    private <T> T getService(Class<? extends T> clazz) {
        return services.getOptionalService(clazz);
    }

    /**
     * Internal helper method to read MASTER_ACCOUNT_OVERRIDE property
     *
     * @return The value of MASTER_ACCOUNT_OVERRIDE, i.e. <code>true</code> if the master-admin has access to all contexts.
     */
    private boolean isMasterAccountOverride() {
       ConfigurationService configService = getService(ConfigurationService.class);
       if(configService != null) {
           return configService.getBoolProperty("MASTER_ACCOUNT_OVERRIDE", false);
       }
       LOG.debug("Could not get ConfigurationService");
       return false;
    }

    /**
     * Generates an error JSON object
     *
     * @param ex The {@link OXException} to create the error object from
     * @return The JSON error object for the given {@link OXException}
     * @throws JSONException
     */
    private JSONObject generateError(OXException ex) throws JSONException {
        JSONObject main = new JSONObject();
        try {
            ResponseWriter.addException(main, ex);
        } catch (JSONException e) {
            LOG.error("Error while generating error for client.", e);
            throw e;
        }
        return main;
    }

    /**
     * Check if context and user IDs are valid and the according entities exist.
     *
     * @param contextId The identifier of the context
     * @param userId The identifier of the user
     * @return <code>null</code> if both IDs are valid. A {@link Response} to be returned to the client
     *         in case one of the IDs is invalid.
     * @throws OXException If services can't be obtained or context or user are unknown
     */
    private Response checkUserAndContext(int contextId, int userId) throws OXException {
        ContextService contextService = requireService(ContextService.class);
        UserService userService = requireService(UserService.class);
        try {
            Context context = contextService.getContext(contextId);
            userService.getUser(userId, context);
            return null;
        } catch (OXException e) {
            if (ContextExceptionCodes.UPDATE.equals(e)) {
                // update tasks running
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            } else if (ContextExceptionCodes.NOT_FOUND.equals(e) || UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                return Response.status(Status.NOT_FOUND).build();
            }
            throw e;
        }
    }

    /**
     * Internal method to validate authentication
     *
     * @param contextId The context-id
     * @param auth The BASIC-AUTH authentication data
     * @return An error {@link Response} if the authentication failed, or <code>null</code> if the no error, i.e. authentication granted.
     * @throws OXException
     */
    private Response authenticate(int contextId, String auth) throws OXException {
        Authenticator authenticator = getService(Authenticator.class);
        if (authenticator != null) {
            if (Authorization.checkForBasicAuthorization(auth)) {
                //Check the provided credentials
                Credentials credentials = Authorization.decode(auth);
                if (Authorization.checkLogin(credentials.getPassword())) {
                    try {
                        authenticator.doAuthentication(new com.openexchange.auth.Credentials(credentials.getLogin(), credentials.getPassword()), contextId, true);
                        //access granted
                        return null;
                    } catch (@SuppressWarnings("unused") OXException e) {
                        //Fall through
                    }
                }
            } else {
                //No valid auth header provided by the client
                //Check if unauthorized access is allowed
                if (authenticator.isContextAuthenticationDisabled()) {
                    //no auth required for the context admin
                    return null;
                }

                if (authenticator.isMasterAuthenticationDisabled() && isMasterAccountOverride()) {
                    //no auth required for master admin
                    return null;
                }
            }
            return Response.status(Status.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"OX REST\", encoding=\"UTF-8\"").build();
        }
        LOG.error("Could not perform authentication due missing Authenticator service");
        return Response.status(Status.SERVICE_UNAVAILABLE).build();
    }

    /**
     * Internal method to create a JSONObject from the given {@link MultifactorDevice}
     *
     * @param device The device
     * @return The device as JSONObject
     * @throws JSONException
     */
    private JSONObject createDeviceResponse(MultifactorDevice device) throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(JSON_ID, device.getId());
        json.put(JSON_NAME,device.getName());
        json.put(JSON_PROVIDER_NAME, device.getProviderName());
        json.put(JSON_ENABLED, device.isEnabled());
        json.put(JSON_BACKUP, device.isBackup());
        return json;
    }

    /**
     * Internal method to create a JSONArray from the given collection of {@link MultifactorDevice} instances.
     *
     * @param devices The devices
     * @return The devices as JSONArray
     * @throws JSONException
     */
    private JSONArray createDevicesResponse(Collection<MultifactorDevice> devices) throws JSONException {
       JSONArray array = new JSONArray(devices.size());
       int i = 0;
       for(MultifactorDevice device : devices) {
           array.add(i++, createDeviceResponse(device));
       }
       return array;
    }

    /**
     * Gets a list of multifactor authentication devices for a given user
     *
     * @param auth BASIC-AUTH Authorization data
     * @param contextId The context-ID of the user to get the devices for
     * @param userId The ID of the user to get the devices for
     * @return A {@link Repsonse} containing an JSONArray of devices, or an error code
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevices(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, @PathParam("context-id") int contextId, @PathParam("user-id") int userId) throws JSONException {
        try {
            Response authenticationError = authenticate(contextId, auth);
            if (authenticationError != null) {
                //access denied
                return authenticationError;
            }
            Response error = checkUserAndContext(contextId, userId);
            if (null != error) {
                // Return error response
                return error;
            }
            final JSONArray result = createDevicesResponse(requireService(MultifactorManagementService.class).getMultifactorDevices(contextId, userId));
            return Response.ok().entity(result).build();
        } catch (OXException e) {
            LOG.error("Error while listing multifactor devices for user {} in context {}", I(userId), I(contextId), e);
            return Response.serverError().entity(generateError(e)).build();
        } catch (Exception e) {
            LOG.error("Error while listing multifactor devices for user {} in context {}", I(userId), I(contextId), e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    /**
     * Removes a specific multifactor authentication device for a given user
     *
     * @param auth BASIC-AUTH Authorization data
     * @param contextId The context-ID of the user to delete the device for
     * @param userId The ID of the user to delete the device for
     * @param providerName The name of the device's provider
     * @param deviceId The ID of the device
     * @return A {@link Response} containing an appropriated return code
     * @throws JSONException
     */
    @DELETE
    @Path("/{provider-name}/{device-id}")
    public Response removeDevice(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth,
        @PathParam("context-id") int contextId,
        @PathParam("user-id") int userId,
        @PathParam("provider-name") String providerName,
        @PathParam("device-id") String deviceId) throws JSONException {

        try {
            Response authenticationError = authenticate(contextId, auth);
            if (authenticationError != null) {
                //access denied
                return authenticationError;
            }
            Response error = checkUserAndContext(contextId, userId);
            if (null != error) {
                // Return error response
                return error;
            }
            requireService(MultifactorManagementService.class).removeDevice(contextId, userId, providerName, deviceId);
            LOG.info("Removed multifactor device with id {} from provider {} for user {} in context {}", deviceId, providerName, I(userId), I(contextId));
            return Response.ok().build();
        } catch (OXException e) {
            LOG.error("Error while removing multifactor device with id {} from provider {} for user {} in context {}", deviceId, providerName, I(userId), I(contextId), e);
            if (MultifactorExceptionCodes.DEVICE_REMOVAL_FAILED.equals(e) || MultifactorExceptionCodes.UNKNOWN_PROVIDER.equals(e) || MultifactorExceptionCodes.UNKNOWN_DEVICE_ID.equals(e)) {
                return Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build();
            }
            return Response.serverError().entity(generateError(e)).build();
        }
    }

    /**
     * Removes all known multifactor authentication devices for a given user
     *
     * @param auth BASIC-AUTH Authorization data
     * @param contextId The context-ID of the user to delete the devices for
     * @param userId The ID of the user to delete the devices for
     * @return A {@link Response} containing an appropriated return code
     * @throws JSONException
     */
    @DELETE
    public Response removeDevices(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, @PathParam("context-id") int contextId, @PathParam("user-id") int userId) throws JSONException {
        try {
            Response authenticationError = authenticate(contextId, auth);
            if (authenticationError != null) {
                //access denied
                return authenticationError;
            }
            Response error = checkUserAndContext(contextId, userId);
            if (null != error) {
                // Return error response
                return error;
            }
            requireService(MultifactorManagementService.class).removeAllDevices(contextId, userId);
            return Response.ok().build();
        } catch (OXException e) {
            LOG.error("Error while removing all multifactor devices for user {} in context {}", I(userId), I(contextId), e);
            return Response.serverError().entity(generateError(e)).build();
        }
    }
}
