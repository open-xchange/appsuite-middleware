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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.share.servlet.internal;

import static com.openexchange.tools.servlet.http.Tools.copyHeaders;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.passwordchange.BasicPasswordChangeService;
import com.openexchange.passwordchange.PasswordChangeEvent;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareService;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link InitGuestPasswordServlet}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class InitGuestPasswordServlet extends HttpServlet {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitGuestPasswordServlet.class);

    public InitGuestPasswordServlet() {
        super();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Create a new HttpSession if it is missing
            request.getSession(true);

            if (!"PUT".equals(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }

            // Read share token
            String token = request.getParameter("share");
            if (Strings.isEmpty(token)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            //Read new password
            JSONObject json = null;
            try {
                BufferedReader reader = request.getReader();
                json = JSONObject.parse(reader).toObject();
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String password = json.getString("password");
            String password2 = json.getString("password2");
            if (Strings.isEmpty(password) || Strings.isEmpty(password2) || !password.equals(password2)) {
                if (Strings.isEmpty(token)) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }

            ShareService shareService = ShareServiceLookup.getService(ShareService.class, true);
            GuestInfo guestInfo = shareService.resolveGuest(token);
            if (guestInfo.isPasswordSet()) {
                response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
                return;
            }

            BasicPasswordChangeService passwordChangeService = ShareServiceLookup.getService(BasicPasswordChangeService.class);
            ContextService contextService = ShareServiceLookup.getService(ContextService.class);

            Map<String, List<String>> headers = copyHeaders(request);
            com.openexchange.authentication.Cookie[] cookies = Tools.getCookieFromHeader(request);

            passwordChangeService.perform(new PasswordChangeEvent(ServerSessionAdapter.valueOf(guestInfo.getGuestID(), guestInfo.getContextID()), contextService.getContext(guestInfo.getContextID()), password, " ", headers, cookies));

        } catch (OXException e) {
            LOG.error("", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (JSONException e) {
            LOG.error("", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
