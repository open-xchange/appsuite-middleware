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

import static com.openexchange.ajax.AJAXServlet.CONTENTTYPE_JAVASCRIPT;
import static com.openexchange.ajax.fields.LoginFields.CLIENT_PARAM;
import static com.openexchange.ajax.fields.LoginFields.CLIENT_TOKEN;
import static com.openexchange.ajax.fields.LoginFields.SERVER_TOKEN;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.writer.LoginWriter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.user.UserService;

/**
 * Implements the tokens login request taking the client and the server token to activate a previously created session.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Tokens implements LoginRequestHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Tokens.class);

    private final LoginConfiguration conf;

    public Tokens(LoginConfiguration conf) {
        super();
        this.conf = conf;
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            doTokens(req, resp);
        } catch (OXException e) {
            LoginServlet.logAndSendException(resp, e);
        }
    }

    private void doTokens(HttpServletRequest req, HttpServletResponse resp) throws OXException, IOException {
        String clientToken = LoginTools.parseParameter(req, CLIENT_TOKEN);
        String serverToken = LoginTools.parseParameter(req, SERVER_TOKEN);
        String client = LoginTools.parseParameter(req, CLIENT_PARAM);
        String userAgent = req.getHeader(Header.USER_AGENT);

        // TODO Register this login action dynamically if LoginPerformer and UserService gets available.
        ContextService contextService = ServerServiceRegistry.getInstance().getService(ContextService.class, false);
        UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class, false);
        Session session = LoginPerformer.getInstance().lookupSessionWithTokens(clientToken, serverToken);

        // IP check if enabled; otherwise update session's IP address if different to request's IP address. Insecure check is done in
        // updateIPAddress method.
        if (!conf.isIpCheck()) {
            // Update IP address if necessary
            LoginTools.updateIPAddress(conf, req.getRemoteAddr(), session);
        } else {
            final String newIP = req.getRemoteAddr();
            SessionUtility.checkIP(true, conf.getRanges(), session, newIP, conf.getIpCheckWhitelist());
            // IP check passed: update IP address if necessary
            LoginTools.updateIPAddress(conf, newIP, session);
        }
        SessiondService service = ServerServiceRegistry.getInstance().getService(SessiondService.class);

        // Update client, which is necessary for hash calculation. OXNotifier must not know which client will be used - maybe
        // com.openexchange.ox.gui.dhtml or open-xchange-appsuite.
        if (null != service) {
            service.setClient(session.getSessionID(), client);
        }

        // Update hash if the property com.openexchange.cookie.hash is configured to remember.
        String hash = HashCalculator.getInstance().getHash(req, userAgent, client);
        if (null != service) {
            service.setHash(session.getSessionID(), hash);
        }

        Locale locale;
        if (null != contextService && null != userService) {
            Context context = contextService.getContext(session.getContextId());
            locale = userService.getUser(session.getUserId(), context).getLocale();
        } else {
            locale = Locale.US;
        }

        Response response = new Response();
        try {
            JSONObject json = new JSONObject();
            LoginWriter.write(session, json, locale);
            response.setData(json);
        } catch (JSONException e) {
            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error("", oje);
            response.setException(oje);
        }

        Tools.disableCaching(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        LoginServlet.writeSecretCookie(req, resp, session, hash, req.isSecure(), req.getServerName(), conf);
        try {
            ResponseWriter.write(response, resp.getWriter(), locale);
        } catch (JSONException e) {
            LOG.error("", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
