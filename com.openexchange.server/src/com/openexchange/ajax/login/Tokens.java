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

package com.openexchange.ajax.login;

import static com.openexchange.ajax.fields.LoginFields.CLIENT_PARAM;
import static com.openexchange.ajax.fields.LoginFields.CLIENT_TOKEN;
import static com.openexchange.ajax.fields.LoginFields.SERVER_TOKEN;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.writer.LoginWriter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.DefaultSessionAttributes;
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
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp, LoginRequestContext requestContext) throws IOException {
        try {
            doTokens(req, resp, requestContext);
            if(requestContext.getMetricProvider().isStateUnknown()) {
               requestContext.getMetricProvider().recordSuccess();
            }
        } catch (OXException e) {
            LoginServlet.logAndSendException(resp, e);
            requestContext.getMetricProvider().recordException(e);
        }
    }

    private void doTokens(HttpServletRequest req, HttpServletResponse resp, LoginRequestContext requestContext) throws OXException, IOException {
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
        {
            String newIP = req.getRemoteAddr();
            SessionUtility.checkIP(session, newIP);

            // IP check passed: update IP address if necessary
            LoginTools.updateIPAddress(conf, newIP, session);
        }

        // Update client, which is necessary for hash calculation. OXNotifier must not know which client will be used - maybe
        // com.openexchange.ox.gui.dhtml or open-xchange-appsuite.
        DefaultSessionAttributes.Builder sessionAttrs = DefaultSessionAttributes.builder();
        sessionAttrs.withClient(client);

        // Update hash if the property com.openexchange.cookie.hash is configured to remember.
        String hash = HashCalculator.getInstance().getHash(req, userAgent, client);
        sessionAttrs.withHash(hash);

        // Update User-Agent
        if (Strings.isNotEmpty(userAgent)) {
            sessionAttrs.withUserAgent(userAgent);
        }

        SessiondService service = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (service != null) {
            service.setSessionAttributes(session.getSessionID(), sessionAttrs.build());
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
            requestContext.getMetricProvider().recordErrorCode(OXJSONExceptionCodes.JSON_WRITE_ERROR);
        }

        Tools.disableCaching(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        AJAXServlet.setDefaultContentType(resp);
        LoginServlet.writeSecretCookie(req, resp, session, hash, req.isSecure(), req.getServerName(), conf);
        LoginServlet.writeSessionCookie(resp, session, hash, req.isSecure(), req.getServerName());
        try {
            ResponseWriter.write(response, resp.getWriter(), locale);
        } catch (JSONException e) {
            LOG.error("", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            requestContext.getMetricProvider().recordErrorCode(OXJSONExceptionCodes.JSON_WRITE_ERROR);
        }
    }
}
