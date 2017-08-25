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

package com.openexchange.admin.rest.passwordchange.history.api;

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
import com.openexchange.auth.Authenticator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.ldap.UserExceptionCode;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeHistoryREST.class);
    private final ServiceLookup           services;

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
            LOG.error("Error while listing password change history for user {} in context {}. Reason: {}", userId, contextId, e);
            return Response.serverError().build();
        } catch (BadRequestException e) {
            // SortField unknown; 400
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            //Unknown error; 500
            LOG.error("Error while listing password change history for user {} in context {}. Reason: {}", userId, contextId, e);
            return Response.serverError().build();
        }
    }

    /**
     * Matches REST field names to data field names for sorting
     *
     * @param sort The unparsed field names
     * @param fieldNames The field names to sort by
     * @return A set of data field. Can be empty
     */
    private Map<SortField, SortOrder> getFields(String sort) {
        if (Strings.isEmpty(sort)) {
            return Collections.singletonMap(SortField.DATE, SortOrder.DESC);
        }

        String[] fields = Strings.splitByComma(sort);
        Map<SortField, SortOrder> retval = new LinkedHashMap<>(fields.length);
        for (String field : fields) {
            if (false == Strings.isEmpty(field)) {
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
     * Get a specific service and throws {@link ServiceExceptionCode.SERVICE_UNAVAILABLE} if it can't be loaded
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
     * @throws OXException
     */
    private Response checkUserAndContext(int contextId, int userId) throws OXException {
        ContextService contextService = getService(ContextService.class);
        UserService userService = getService(UserService.class);
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
                LOG.warn("Granting access to password change history without basicAuth! 'CONTEXT_AUTHENTICATION_DISABLED' is set to 'true'.");
                return null;
            }
            ConfigurationService configService = services.getOptionalService(ConfigurationService.class);
            if (null != configService) {
                Boolean masertAccountOverride = configService.getBoolProperty("MASTER_ACCOUNT_OVERRIDE", false);
                if (authenticator.isMasterAuthenticationDisabled() && masertAccountOverride) {
                    LOG.warn("Granting access to password change history without basicAuth! 'MASTER_AUTHENTICATION_DISABLED' and 'MASTER_ACCOUNT_OVERRIDE' are set to 'true' ");
                    return null;
                }
            } else {
                LOG.debug("Could not get ConfigurationService. Can not check if access without basicAuth HEADER ist allowed.");
            }
        }
        return Response.status(Status.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"OX REST\", encoding=\"UTF-8\"").build();
    }

    /**
     * Check if client is "well known" and convert into readable if so
     *
     * @param data The {@link JSONObject} to put the data in
     * @param convertee The identifier that might be known
     * @throws JSONException
     */
    private void putOptionalReadable(JSONObject data, String convertee) throws JSONException {
        PasswordChangeClients client = PasswordChangeClients.match(convertee);
        if (null != client) {
            data.put("client_name", client.getDisplayName());
            return;
        }
    }
}
