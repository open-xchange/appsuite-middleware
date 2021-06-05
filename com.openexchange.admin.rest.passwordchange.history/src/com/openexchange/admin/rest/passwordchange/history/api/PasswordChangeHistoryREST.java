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

package com.openexchange.admin.rest.passwordchange.history.api;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.security.PermitAll;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.auth.Authenticator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.UpdateBehavior;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.java.Strings;
import com.openexchange.osgi.Tools;
import com.openexchange.passwordchange.history.PasswordChangeClients;
import com.openexchange.passwordchange.history.PasswordChangeInfo;
import com.openexchange.passwordchange.history.PasswordChangeRecorder;
import com.openexchange.passwordchange.history.PasswordChangeRecorderException;
import com.openexchange.passwordchange.history.PasswordChangeRecorderRegistryService;
import com.openexchange.passwordchange.history.SortField;
import com.openexchange.passwordchange.history.SortOrder;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.servlet.http.Authorization.Credentials;
import com.openexchange.user.UserExceptionCode;
import com.openexchange.user.UserService;

/**
 * {@link PasswordChangeHistoryREST} - The REST endpoint for PasswordChangeHistory
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
@Path("admin/v1/contexts/{context-id}/users/{user-id}/passwd-changes")
@PermitAll
public class PasswordChangeHistoryREST {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeHistoryREST.class);
    }

    /** The service look-up */
    private final ServiceLookup services;

    /**
     * Initializes a new {@link PasswordChangeHistoryREST}.
     *
     * @param services The {@link ServiceLookup} to get services from
     */
    public PasswordChangeHistoryREST(ServiceLookup services) {
        super();
        this.services = services;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object listHistory(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, @PathParam("context-id") int contextId, @PathParam("user-id") int userId, @QueryParam("limit") int limit, @QueryParam("sort") String sort) {
        try {
            // Check validity of user and context IDs
            Response error = checkUserAndContext(contextId, userId);
            if (null != error) {
                // Return error response
                return error;
            }

            // Check for access
            error = checkAccess(contextId, auth);
            if (null != error) {
                // Return error response
                return error;
            }

            // Get recorder from registry
            PasswordChangeRecorderRegistryService registry = getService(PasswordChangeRecorderRegistryService.class);
            PasswordChangeRecorder recorder = registry.getRecorderForUser(userId, contextId);

            // Filter field "sort" information an get data
            Map<SortField, SortOrder> fields = getFields(sort);
            List<PasswordChangeInfo> history = recorder.listPasswordChanges(userId, contextId, fields);

            // Check data
            int size = history.size();
            if (size == 0) {
                return new JSONArray(0);
            }

            // Build response
            JSONArray entries = new JSONArray(size);
            int i = 0;
            for (PasswordChangeInfo info : history) {
                if (limit > 0 && i >= limit) {
                    break;
                }
                JSONObject data = new JSONObject(8);
                data.put("date", info.getCreated());
                data.put("client_id", info.getClient());
                putOptionalReadable(data, info.getClient());
                data.put("client_address", info.getIP());
                entries.add(i++, data);
            }
            return entries;
        } catch (OXException e) {
            if (PasswordChangeRecorderException.DENIED_FOR_GUESTS.equals(e) || PasswordChangeRecorderException.DISABLED.equals(e)) {
                // No fall back. Resource for the user not available. In other terms the user can not see the feature.
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            LoggerHolder.LOG.error("Error while listing password change history for user {} in context {}. Reason: {}", I(userId), I(contextId), e);
            return Response.serverError().build();
        } catch (BadRequestException e) {
            // SortField unknown; 400
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            //Unknown error; 500
            LoggerHolder.LOG.error("Error while listing password change history for user {} in context {}. Reason: {}", I(userId), I(contextId), e);
            return Response.serverError().build();
        }
    }

    /**
     * Matches REST field names to data field names for sorting
     *
     * @param sort The unparsed field names
     * @return A set of data field. Can be empty
     */
    private Map<SortField, SortOrder> getFields(String sort) {
        if (Strings.isEmpty(sort)) {
            return Collections.singletonMap(SortField.DATE, SortOrder.DESC);
        }

        String[] fields = Strings.splitByComma(sort);
        Map<SortField, SortOrder> retval = new LinkedHashMap<>(fields.length);
        for (String field : fields) {
            if (Strings.isNotEmpty(field)) {
                boolean desc = false;
                if (field.startsWith("-")) {
                    field = field.substring(1);
                    desc = true;
                }
                // Check field names
                if (field.equals("client_id")) {
                    retval.put(SortField.CLIENT_ID, desc ? SortOrder.DESC : SortOrder.ASC);
                } else if (field.equals("date")) {
                    retval.put(SortField.DATE, desc ? SortOrder.DESC : SortOrder.ASC);
                } else {
                    // Not supported
                    throw new BadRequestException("Can't match field " + field);
                }
            }
        }
        return retval;
    }

    /**
     * Get a specific service and throws {@link ServiceExceptionCode#SERVICE_UNAVAILABLE} if it can't be loaded
     *
     * @param clazz The service to load
     * @return The service instance
     */
    private <T> T getService(Class<? extends T> clazz) throws OXException {
        return Tools.requireService(clazz, services);
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
        try {
            ContextService contextService = getService(ContextService.class);
            UserService userService = getService(UserService.class);
            userService.getUser(userId, contextService.getContext(contextId, UpdateBehavior.DENY_UPDATE));
            return null;
        } catch (OXException e) {
            if (ContextExceptionCodes.UPDATE.equals(e) || ContextExceptionCodes.UPDATE_NEEDED.equals(e)) {
                // update tasks running or pending
                return Response.status(Status.SERVICE_UNAVAILABLE).build();
            } else if (ContextExceptionCodes.NOT_FOUND.equals(e) || UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                return Response.status(Status.NOT_FOUND).build();
            }
            throw e;
        }
    }

    /**
     * Check if access should be granted
     *
     * @param contextId The context ID to authenticated against
     * @param auth The Base64 decoded authentication string
     * @return <code>null</code> if everything is fine or a {@link Response} with error code.
     * @throws OXException In case of missing service
     */
    private Response checkAccess(int contextId, String auth) throws OXException {
        Authenticator authenticator = getService(Authenticator.class);
        // Decode auth and validate
        if (Authorization.checkForBasicAuthorization(auth)) {
            // Valid header
            Credentials creds = Authorization.decode(auth);
            if (Authorization.checkLogin(creds.getPassword())) {
                // Authenticate the context administrator
                try {
                    authenticator.doAuthentication(new com.openexchange.auth.Credentials(creds.getLogin(), creds.getPassword()), contextId, true);
                    return null;
                } catch (OXException e) {
                    // Fall through
                }
            }
        } else {
            // No valid header, check if unauthorized access is allowed
            if (authenticator.isContextAuthenticationDisabled()) {
                LoggerHolder.LOG.warn("Granting access to password change history without basicAuth! 'CONTEXT_AUTHENTICATION_DISABLED' is set to 'true'.");
                return null;
            }
            ConfigurationService configService = services.getOptionalService(ConfigurationService.class);
            if (null != configService) {
                boolean masertAccountOverride = configService.getBoolProperty("MASTER_ACCOUNT_OVERRIDE", false);
                if (authenticator.isMasterAuthenticationDisabled() && masertAccountOverride) {
                    LoggerHolder.LOG.warn("Granting access to password change history without basicAuth! 'MASTER_AUTHENTICATION_DISABLED' and 'MASTER_ACCOUNT_OVERRIDE' are set to 'true' ");
                    return null;
                }
            } else {
                LoggerHolder.LOG.debug("Could not get ConfigurationService. Cannot check if access without basicAuth HEADER ist allowed.");
            }
        }
        return Response.status(Status.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"OX REST\", encoding=\"UTF-8\"").build();
    }

    /**
     * Check if client is "well known" and convert into readable if so
     *
     * @param data The {@link JSONObject} to put the data in
     * @param convertee The identifier that might be known
     * @throws JSONException See {@link JSONObject#put(String, Object)}
     */
    private void putOptionalReadable(JSONObject data, String convertee) throws JSONException {
        PasswordChangeClients client = PasswordChangeClients.match(convertee);
        if (null != client) {
            data.put("client_name", client.getDisplayName());
        }
    }
}
