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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.rest.services.JAXRSService;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceLookup;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link SessiondRESTService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
@RoleAllowed(Role.BASIC_AUTHENTICATED)
@Path("/admin/v1/sessions")
public class SessiondRESTService extends JAXRSService {

    /**
     * Initialises a new {@link SessiondRESTService}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    protected SessiondRESTService(ServiceLookup services) {
        super(services);
    }

    /**
     * Closes the sessions specified by the ids in the JSON payload
     * 
     * @param global whether to perform a cluster-wide or local clean-up. Defaults to <code>true</code>
     * @param
     * @return
     *         <ul>
     *         <li><b>200</b>: if the sessions were closed successfully</li>
     *         <li><b>400</b>: if the client issued a bad request</li>
     *         <li><b>401</b>: if the client was not authenticated</li>
     *         <li><b>403</b>: if the client was not authorised</li>
     *         </ul>
     */
    @POST
    @Path("/close/by-id")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeSessionsById(@QueryParam("global") Boolean global, JSONObject payload) {
        if (payload == null) {
            return Response.status(400).entity("The request payload was 'null'").build();
        }
        if (payload.isEmpty()) {
            return Response.ok().build();
        }

        if (global == null) {
            global = Boolean.valueOf(true);
        }
        JSONArray sessionsArray = payload.optJSONArray("sessions");
        if (sessionsArray == null || sessionsArray.isEmpty()) {
            return Response.ok().build();
        }

        JSONObject response = new JSONObject();
        SessiondService sessionService = getService(SessiondService.class);
        for (int index = 0; index < sessionsArray.length(); index++) {
            // TODO: invoke
        }
        return null;
    }

    /**
     * 
     * @param global
     * @param payload
     * @return
     */
    @POST
    @Path("/close/by-context")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeSessionsByContextId(@QueryParam("global") boolean global, JSONObject payload) {
        return null;
    }

    /**
     * 
     * @param global
     * @param payload
     * @return
     */
    @POST
    @Path("/close/by-user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response closeSessionsByUserId(@QueryParam("global") boolean global, JSONObject payload) {
        return null;
    }
}
