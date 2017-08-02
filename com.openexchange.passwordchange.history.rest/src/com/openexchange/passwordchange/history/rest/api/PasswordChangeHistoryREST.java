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

package com.openexchange.passwordchange.history.rest.api;

import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.auth.Authenticator;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.passwordchange.history.groupware.PasswordChangeClients;
import com.openexchange.passwordchange.history.groupware.PasswordChangeHistoryProperties;
import com.openexchange.passwordchange.history.handler.PasswordChangeInfo;
import com.openexchange.passwordchange.history.handler.PasswordHistoryHandler;
import com.openexchange.passwordchange.history.handler.SortType;
import com.openexchange.passwordchange.history.rest.exception.HistoryRestException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.servlet.http.Authorization.Credentials;

/**
 * {@link PasswordChangeHistoryREST}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
@Path("admin/v1/contexts/{context-id}/users/{user-id}/passwd-changes")
@PermitAll
public class PasswordChangeHistoryREST {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeHistoryREST.class);
    private final ServiceLookup           service;

    /**
     * Initializes a new {@link PasswordChangeHistoryREST}.
     * 
     * @throws Exception
     */
    public PasswordChangeHistoryREST(ServiceLookup service) throws OXException {
        super();
        this.service = service;
    }

    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.APPLICATION_JSON)
    public Object listHistory(@HeaderParam(HttpHeaders.AUTHORIZATION) String auth, @PathParam("context-id") int contextID, @PathParam("user-id") int userID, @QueryParam("limit") int limit, @QueryParam("order") String order) {
        try {
            Object retval = checkAccess(contextID, auth);
            if (null != retval) {
                return retval;
            }

            // Get services
            PasswordHistoryHandler handler = getService(PasswordHistoryHandler.class);
            ConfigViewFactory config = getService(ConfigViewFactory.class);
            ConfigView view = config.getView(userID, contextID);

            /*
             * Check configuration
             * Note: PasswordChangeHistory does not need to be enabled to return the data
             * So we just check if there is a service configured that can provide the data
             */
            String symbolicName = view.get(PasswordChangeHistoryProperties.handler.getFQPropertyName(), String.class);
            if (null == symbolicName || symbolicName.isEmpty() || false == symbolicName.equals(handler.getSymbolicName())) {
                return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            List<PasswordChangeInfo> history = handler.listPasswordChanges(userID, contextID, getType(order));

            // Check data
            if (history.size() == 0) {
                return new JSONObject();
            }

            // Build response
            JSONArray entries = new JSONArray();
            int i = 0;
            for (PasswordChangeInfo info : history) {
                if (limit > 0 && i >= limit) {
                    break;
                }
                JSONObject data = new JSONObject();
                data.put("date", info.getCreated());
                data.put("client_id", info.getClient());
                putOptionalReadable(data, info.getClient());
                data.put("client_address", info.getIP());
                entries.add(i++, data);
            }
            return entries;
        } catch (Exception e) {
            LOG.error("Error while listing password change history for user {} in context {}. Reason: {}", userID, contextID, e);
            return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a specific service and throws {@link HistoryRestException#MISSING_SERVICE} if it can't be loaded
     * 
     * @param clazz The service to load
     * @return The service instance
     */
    private <T> T getService(Class<? extends T> clazz) throws OXException {
        T retval = service.getService(clazz);
        if (null == retval) {
            throw HistoryRestException.MISSING_SERVICE.create(clazz.getSimpleName());
        }
        return retval;
    }

    /**
     * Check if access should be granted
     * 
     * @param contextID The context ID to authenticated against
     * @param auth The Base64 decoded authentication string
     * @return <code>null</code> if everything is fine or a {@link Response} with error code.
     * @throws OXException In case of missing service
     */
    private Object checkAccess(int contextID, String auth) throws OXException {
        // Decode auth and validate
        if (Authorization.checkForBasicAuthorization(auth)) {
            // Valid header
            Credentials creds = Authorization.decode(auth);
            if (Authorization.checkLogin(creds.getPassword())) {
                // Authenticate the context administrator
                Authenticator authenticator = getService(Authenticator.class);
                try {
                    authenticator.doAuthentication(new com.openexchange.auth.Credentials(creds.getLogin(), creds.getPassword()), contextID, true);
                    return null;
                } catch (OXException e) {
                    // Fall through
                }
            }
        } // No valid header
        return Response.serverError().status(Response.Status.FORBIDDEN).build();
    }

    /**
     * Check if client is "well known" and convert into readable if so
     * 
     * @param data The {@link JSONObject} to put the data in
     * @param convertee The identifier that might be known
     * @throws JSONException
     */
    private void putOptionalReadable(JSONObject data, String convertee) throws JSONException {
        for (PasswordChangeClients client : PasswordChangeClients.values()) {
            if (client.matches(convertee)) {
                data.put("client_name", client.getDisplayName());
                return;
            }
        }
    }

    /**
     * Get the type fitting to the given string. Default(Defined in MW-809) is {@link #NEWEST}
     * 
     * @param type The type as String
     * @return A {@link SortType}
     */
    private SortType getType(String type) {
        if (null == type) {
            return SortType.NEWEST;
        }
        for (SortType sType : SortType.values()) {
            String name = sType.getTypeName();
            if (name.equals(type)) {
                return sType;
            }
        }
        return SortType.NEWEST;
    }
}
