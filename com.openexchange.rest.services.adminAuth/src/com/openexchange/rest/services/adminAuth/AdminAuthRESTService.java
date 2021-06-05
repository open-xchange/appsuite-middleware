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

package com.openexchange.rest.services.adminAuth;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.auth.Authenticator;
import com.openexchange.auth.Credentials;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AdminAuthRESTService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.8.0
 */
@Path("/preliminary/adminproc/v1")
@RoleAllowed(Role.BASIC_AUTHENTICATED)
public class AdminAuthRESTService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AdminAuthRESTService}.
     *
     * @param services
     */
    public AdminAuthRESTService(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * <pre>
     * PUT /rest/adminproc/v1/adminAuth
     * { "login" : String,
     * "password": String,
     * "contenxt: int (optional)
     * }
     * </pre>
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/adminAuth")
    public JSONObject doAdminAuth(JSONObject body) throws OXException {
        if (body == null || body.isEmpty()) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }

        if (false == body.hasAndNotNull("login")) {
            throw AjaxExceptionCodes.MISSING_FIELD.create("login");
        }

        if (false == body.hasAndNotNull("password")) {
            throw AjaxExceptionCodes.MISSING_FIELD.create("password");
        }
        Authenticator authenticator = services.getService(Authenticator.class);
        try {
            int contextId = body.optInt("context", 0);
            if (contextId <= 0) {
                authenticator.doAuthentication(createCredentials(body.getString("login"), body.getString("password")));
            } else {
                authenticator.doAuthentication(createCredentials(body.getString("login"), body.getString("password")), contextId);
            }
            return new JSONObject(2).put("result", true);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (@SuppressWarnings("unused") OXException e) {
            return new JSONObject(2).putSafe("result", Boolean.FALSE);
        }
    }

    Credentials createCredentials(String login, String password) {
        return new Credentials(login, password);
    }
}
