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

package com.openexchange.rest.services.session;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.crypto.CryptographicServiceAuthenticationFactory;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;

/**
 *
 * The {@link SessionRESTService} allows clients to retrieve session information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.1
 */
@Path("/preliminary/session/v1/")
@RoleAllowed(Role.BASIC_AUTHENTICATED)
public class SessionRESTService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link SessionRESTService}.
     */
    public SessionRESTService(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Checks if the given session is a guest session.
     *
     * @param session The session
     * @return <code>true</code> if the session is a guest session, <code>false</code> otherwise
     * @throws OXExceptionIf checking for a guest session fails
     */
    private boolean isGuest(Session session) throws OXException {
        if (session == null) {
            return false;
        }

        if (Boolean.TRUE.equals(session.getParameter(Session.PARAM_GUEST))) {
            return true;
        }

        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        if (serverSession != null) {
            User user = serverSession.getUser();
            return user != null && user.isGuest();
        }
        return false;
    }

    /**
     * <pre>
     * GET /preliminary/session/v1/get/{session}
     * </pre>
     */
    @GET
    @Path("/get/{session}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject all(@PathParam("session") String session) throws OXException {
        try {
            SessiondService sessiondService = services.getOptionalService(SessiondService.class);
            if (null == sessiondService) {
                throw ServiceExceptionCode.absentService(SessiondService.class);
            }

            Session ses = sessiondService.peekSession(session);
            if (ses == null) {
                // No such session...
                return new JSONObject(0);
            }

            // Basic user information
            JSONObject jResponse = new JSONObject(6).put("context", ses.getContextId()).put("user", ses.getUserId());

            // Add "guest" flag
            boolean isGuest = isGuest(ses);
            jResponse.put("guest", isGuest);

            // Add crypto session identifier
            CryptographicServiceAuthenticationFactory cryptoAuthenticationFactory = services.getOptionalService(CryptographicServiceAuthenticationFactory.class);
            if (cryptoAuthenticationFactory != null) {
                String cryptoSessionId = cryptoAuthenticationFactory.getSessionValueFrom(ses);
                jResponse.put("cryptoSessionId", cryptoSessionId);
            }

            return jResponse;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
