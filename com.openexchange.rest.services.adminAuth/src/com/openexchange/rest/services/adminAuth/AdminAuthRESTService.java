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
        } catch (OXException e) {
            return new JSONObject(2).putSafe("result", Boolean.FALSE);
        }
    }

    Credentials createCredentials(String login, String password) {
        return new Credentials(login, password);
    }
}
