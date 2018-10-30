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

package com.openexchange.sessiond.rest;

import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.services.JAXRSService;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceLookup;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link SessiondRESTService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
@RoleAllowed(Role.BASIC_AUTHENTICATED)
@Path("/admin/v1/close-sessions")
public class SessiondRESTService extends JAXRSService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessiondRESTService.class);

    /**
     * Initialises a new {@link SessiondRESTService}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    public SessiondRESTService(ServiceLookup services) {
        super(services);
    }

    /**
     * Closes the sessions specified by the ids in the JSON payload
     * 
     * @param global whether to perform a cluster-wide or local clean-up. Defaults to <code>true</code>
     * @param payload the payload containing the session identifiers
     * @return
     *         <ul>
     *         <li><b>200</b>: if the sessions were closed successfully</li>
     *         <li><b>400</b>: if the client issued a bad request</li>
     *         <li><b>401</b>: if the client was not authenticated</li>
     *         <li><b>403</b>: if the client was not authorised</li>
     *         <li><b>500</b>: if any server side error is occurred</li>
     *         </ul>
     */
    @POST
    @Path("/by-id")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeSessionsById(@QueryParam("global") Boolean global, JSONObject payload) {
        try {
            return closeSessions(global, createFilter(getPayloadValues(payload, SessiondRESTField.SESSION_IDS), SessionFilterType.SESSION));
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    /**
     * Closes the sessions that belong to the specified contexts specified by the identifiers in the JSON payload
     * 
     * @param global whether to perform a cluster-wide or local clean-up. Defaults to <code>true</code>
     * @param payload the payload containing the context identifiers
     * @return
     *         <ul>
     *         <li><b>200</b>: if the sessions were closed successfully</li>
     *         <li><b>400</b>: if the client issued a bad request</li>
     *         <li><b>401</b>: if the client was not authenticated</li>
     *         <li><b>403</b>: if the client was not authorised</li>
     *         <li><b>500</b>: if any server side error is occurred</li>
     *         </ul>
     */
    @POST
    @Path("/by-context")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeSessionsByContextId(@QueryParam("global") Boolean global, JSONObject payload) {
        try {
            return closeSessions(global, createFilter(getPayloadValues(payload, SessiondRESTField.CONTEXT_IDS), SessionFilterType.CONTEXT));
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    /**
     * Closes the sessions that belong to the specified users specified by the contextId/userId tuple in the JSON payload
     * 
     * @param global whether to perform a cluster-wide or local clean-up. Defaults to <code>true</code>
     * @param payload the payload containing the context identifiers
     * @return
     *         <ul>
     *         <li><b>200</b>: if the sessions were closed successfully</li>
     *         <li><b>400</b>: if the client issued a bad request</li>
     *         <li><b>401</b>: if the client was not authenticated</li>
     *         <li><b>403</b>: if the client was not authorised</li>
     *         <li><b>500</b>: if any server side error is occurred</li>
     *         </ul>
     */
    @POST
    @Path("/by-user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeSessionsByUserId(@QueryParam("global") Boolean global, JSONObject payload) {
        try {
            return closeSessions(global, createFilter(getPayloadValues(payload, SessiondRESTField.USERS), SessionFilterType.USER));
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    /////////////////////////////////////////// HELPERS /////////////////////////////////////////////

    /**
     * Retrieves from the specified payload the requested values array.
     * 
     * @param payload The payload
     * @param restField The field to request
     * @return The {@link JSONArray} with the values
     * @throws IllegalArgumentException if the payload is either <code>null</code> or empty, or if the values array
     *             is either <code>null</code> or empty.
     */
    private JSONArray getPayloadValues(JSONObject payload, SessiondRESTField restField) {
        checkPayload(payload);
        JSONArray array = payload.optJSONArray(restField.getFieldName());
        if (array == null || array.isEmpty()) {
            throw new IllegalArgumentException("Missing values array: either sessionIds, contextIds, or context/user id tuples must be specified.");
        }
        return array;
    }

    /**
     * Checks whether the payload is <code>null</code> or empty.
     * 
     * @param payload The payload to check
     * @throws IllegalArgumentException if the payload is either <code>null</code> or empty.
     */
    private void checkPayload(JSONObject payload) {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("Missing request payload.");
        }
    }

    /**
     * {@link SessionFilterType} - Defines a {@link SessionFilter} type and incorporates
     * the logic to append to one.
     */
    private enum SessionFilterType {
        /**
         * Creates a {@link SessionFilter} with a {@link SessionFilter#SESSION_ID}
         */
        SESSION() {

            @Override
            void apply(StringBuilder filterBuilder, Object object) {
                if (false == (object instanceof String)) {
                    return;
                }
                String sessionId = (String) object;
                if (Strings.isEmpty(sessionId)) {
                    return;
                }
                filterBuilder.append("(").append(SessionFilter.SESSION_ID).append("=").append(object).append(")");
            }
        },

        /**
         * Creates a {@link SessionFilter} with a {@link SessionFilter#CONTEXT_ID}
         */
        CONTEXT() {

            @Override
            void apply(StringBuilder filterBuilder, Object object) {
                if (false == (object instanceof Integer)) {
                    return;
                }
                filterBuilder.append("(").append(SessionFilter.CONTEXT_ID).append("=").append(object).append(")");
            }
        },

        /**
         * Creates a {@link SessionFilter} with a {@link SessionFilter#CONTEXT_ID} and {@link SessionFilter#USER_ID}
         */
        USER() {

            @Override
            void apply(StringBuilder filterBuilder, Object object) {
                if (false == (object instanceof JSONObject)) {
                    return;
                }
                JSONObject user = (JSONObject) object;
                if (user.isEmpty()) {
                    return;
                }
                filterBuilder.append("(&(").append(SessionFilter.CONTEXT_ID).append("=").append(user.optInt(SessiondRESTField.CONTEXT_ID.getFieldName())).append(")");
                filterBuilder.append("(").append(SessionFilter.USER_ID).append("=").append(user.optInt(SessiondRESTField.USER_ID.getFieldName())).append("))");
            }
        };

        /**
         * Applies the value of the specified object to the specified filter builder
         * 
         * @param builder The filter builder
         * @param object The object
         */
        abstract void apply(StringBuilder filterBuilder, Object object);
    }

    /**
     * Creates a {@link SessionFilter} of the specified {@link SessionFilterType}
     * 
     * @param array The array containing the values for the {@link SessionFilter}
     * @param sessionFilterType The {@link SessionFilterType}
     * @return The new {@link SessionFilter}
     */
    private SessionFilter createFilter(JSONArray array, SessionFilterType sessionFilterType) {
        StringBuilder filter = new StringBuilder(64);
        filter.append("(|");
        for (int index = 0; index < array.length(); index++) {
            sessionFilterType.apply(filter, array.opt(index));
        }
        filter.append(")");
        return SessionFilter.create(filter.toString());
    }

    /**
     * Closes the sessions (either globally or locally) that meet the criteria of the specified filter.
     * 
     * @param global Whether to close sessions on the entire cluster or on the local node.
     * @param filter The {@link SessionFilter}
     * @return The {@link Response} with the outcome, 200 if the operation succeeded, 500 if it failed.
     */
    private Response closeSessions(Boolean global, SessionFilter sessionFilter) {
        try {
            if (global == null) {
                global = Boolean.valueOf(true);
            }
            SessiondService sessionService = SessiondService.SERVICE_REFERENCE.get();
            Collection<String> sessions = global ? sessionService.removeSessionsGlobally(sessionFilter) : sessionService.removeSessions(sessionFilter);
            log(sessions, sessionFilter, global);
        } catch (IllegalArgumentException | OXException e) {
            LOGGER.error("{}", e.getMessage(), e);
            return Response.status(500).build();
        }
        return Response.ok().build();
    }

    /**
     * Decides whether to log the filter used to clear the sessions as well as the cleared sessions
     * 
     * @param sessions The cleared sessions
     * @param filter The filter use to clear the sessions
     * @param global whether a global clear was invoked
     */
    private void log(Collection<String> sessions, SessionFilter filter, boolean global) {
        if (false == LOGGER.isDebugEnabled()) {
            return;
        }
        if (sessions.isEmpty()) {
            LOGGER.debug("No sessions were cleared {} via REST invocation with filter '{}'.", global ? "globally" : "locally", filter.toString());
            return;
        }
        StringBuilder b = new StringBuilder();
        for (String s : sessions) {
            b.append(s).append(",");
        }
        b.setLength(b.length() - 1);
        LOGGER.debug("Cleared sessions {} via REST invocation with filter '{}', {}", global ? "globally" : "locally", filter.toString(), sessions);
    }
}
