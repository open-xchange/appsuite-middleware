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

package com.openexchange.ajax.login;

import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.guest.GuestService;
import com.openexchange.java.Strings;
import com.openexchange.login.LoginRampUpService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.user.UserService;

/**
 * {@link GuestLogin}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GuestLogin extends AbstractShareBasedLoginRequestHandler {

    /**
     * Initializes a new {@link GuestLogin}.
     *
     * @param login
     */
    public GuestLogin(ShareLoginConfiguration conf, Set<LoginRampUpService> rampUp) {
        super(conf, rampUp);
    }

    @Override
    protected boolean checkAuthenticationMode(AuthenticationMode authenticationMode) throws OXException {
        return (AuthenticationMode.GUEST_PASSWORD == authenticationMode || AuthenticationMode.GUEST == authenticationMode);
    }

    @Override
    protected LoginInfo getLoginInfoFrom(HttpServletRequest httpRequest) throws OXException {
        try {
            final String login;
            final String pass;
            ConfigurationService configService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
            if (null == configService) {
                throw ServiceExceptionCode.absentService(ConfigurationService.class);
            }

            String body = AJAXServlet.getBody(httpRequest);
            if (Strings.isEmpty(body)) {
                // By parameters
                login = httpRequest.getParameter(LoginFields.NAME_PARAM);
                if (Strings.isEmpty(login)) {
                    throw AjaxExceptionCodes.MISSING_PARAMETER.create(LoginFields.NAME_PARAM);
                }

                pass = httpRequest.getParameter(LoginFields.PASSWORD_PARAM);
            } else {
                // By request body
                JSONObject jBody = new JSONObject(body);
                pass = jBody.optString("password", null);
                login = jBody.getString("login");
            }

            final Map<String, Object> loginProperties = new HashMap<String, Object>(1);
            return new LoginInfo() {
                @Override
                public String getPassword() {
                    return pass;
                }
                @Override
                public String getUsername() {
                    return login;
                }
                @Override
                public Map<String, Object> getProperties() {
                    return loginProperties;
                }
            };
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    protected User resolveUser(GuestInfo guest, Context context) throws OXException {
        // Resolve the user
        UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class, true);
        return userService.getUser(guest.getGuestID(), context);
    }

    @Override
    protected void authenticateUser(LoginInfo loginInfo, User user, Context context) throws OXException {
        // Check credentials
        UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class, true);
        if (Strings.isEmpty(user.getUserPassword()) && Strings.isEmpty(loginInfo.getPassword())) {
            // Nothing to do
            return;
        }

        // Authenticate the user
        if (!userService.authenticate(user, loginInfo.getPassword())) {
            GuestService guestService = ServerServiceRegistry.getInstance().getService(GuestService.class);
            if (guestService != null) {
                boolean authenticate = guestService.authenticate(user, context.getContextId(), loginInfo.getPassword());
                if (authenticate == false) {
                    throw LoginExceptionCodes.INVALID_GUEST_PASSWORD.create();
                }
            } else {
                throw LoginExceptionCodes.INVALID_GUEST_PASSWORD.create();
            }
        }
        if (!loginInfo.getUsername().equals(user.getMail())) {
            throw INVALID_CREDENTIALS.create();
        }
    }

}
