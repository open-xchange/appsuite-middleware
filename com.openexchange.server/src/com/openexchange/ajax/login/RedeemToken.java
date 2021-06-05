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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.writer.LoginWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.servlet.Constants;
import com.openexchange.session.Session;
import com.openexchange.tokenlogin.TokenLoginSecret;
import com.openexchange.tokenlogin.TokenLoginService;
import com.openexchange.tools.net.URITools;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.user.User;

/**
 * {@link RedeemToken}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class RedeemToken implements LoginRequestHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RedeemToken.class);

    private final LoginConfiguration conf;

    /**
     * Initializes a new {@link RedeemToken}.
     */
    public RedeemToken(LoginConfiguration conf) {
        super();
        this.conf = conf;
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp, LoginRequestContext requestContext) throws IOException {
        try {
            doRedeemToken(req, resp, requestContext);
            if(requestContext.getMetricProvider().isStateUnknown()) {
               requestContext.getMetricProvider().recordSuccess();
            }
        } catch (OXException e) {
            LoginTools.useErrorPageTemplateOrSendException(e, conf.getErrorPageTemplate(), req, resp);
            requestContext.getMetricProvider().recordException(e);
        }
    }

    private void doRedeemToken(HttpServletRequest req, HttpServletResponse resp, LoginRequestContext requestContext) throws OXException, IOException {
        // Parse token and app-secret
        String token = LoginTools.parseToken(req);
        String appSecret = LoginTools.parseAppSecret(req);
        if (null == token || null == appSecret) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Tools.disableCaching(resp);
        AJAXServlet.setDefaultContentType(resp);
        TokenLoginService service = ServerServiceRegistry.getInstance().getService(TokenLoginService.class);
        // Parse more request information
        String client = LoginTools.parseClient(req, true, "");
        String userAgent = LoginTools.parseUserAgent(req);
        String hash = HashCalculator.getInstance().getHash(req, userAgent, client);
        // Redeem token for a session
        Session session;
        try {
            String authId = LoginTools.parseAuthId(req, true);
            String clientIp = LoginTools.parseClientIP(req);
            session = service.redeemToken(token, appSecret, client, authId, hash, clientIp, userAgent);
        } catch (OXException e) {
            LoginServlet.logAndSendException(resp, e);
            requestContext.getMetricProvider().recordException(e);
            return;
        }
        req.getSession().setAttribute(Constants.HTTP_SESSION_ATTR_AUTHENTICATED, Boolean.TRUE);
        TokenLoginSecret tokenLoginSecret = service.getTokenLoginSecret(appSecret);
        if (tokenLoginSecret == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Boolean writePassword = (Boolean) tokenLoginSecret.getParameters().get("accessPassword");
        try {
            Context context = ContextStorage.getInstance().getContext(session.getContextId());
            User user = UserStorage.getInstance().getUser(session.getUserId(), context);
            if (!context.isEnabled() || !user.isMailEnabled()) {
                LOG.info("Either context {} or user {} not enabled", Integer.valueOf(context.getContextId()), Integer.valueOf(user.getId()));
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } catch (OXException e) {
            LOG.info("Couldn't resolve context/user by identifier: {}/{}", Integer.valueOf(session.getContextId()), Integer.valueOf(session.getUserId()), e);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            requestContext.getMetricProvider().recordException(e);
            return;
        } catch (Exception e) {
            LOG.info("Unexpected error occurred during login", e);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        // Write cookie accordingly
        LoginServlet.writeSecretCookie(req, resp, session, hash, req.isSecure(), req.getServerName(), conf);
        LoginServlet.writeSessionCookie(resp, session, session.getHash(), req.isSecure(), req.getServerName());

        String redirectUrl = LoginTools.parseRedirectUrl(req);
        if (Strings.isEmpty(redirectUrl)) {
            // Generate JSON response
            try {
                final JSONObject json = new JSONObject(12);
                LoginWriter.write(session, json);
                if (null != writePassword && writePassword.booleanValue()) {
                    final String password = session.getPassword();
                    json.put("password", null == password ? JSONObject.NULL : password);
                }
                json.write(resp.getWriter());
            } catch (JSONException e) {
                LOG.info("", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            try {
                URI uri = new URI(redirectUrl);
                if (!URITools.DEFAULT_VALIDATOR.apply(uri).booleanValue()) {
                    throw new IOException("Invalid redirect URL");
                }
                StringBuilder sb = new StringBuilder(redirectUrl).append("&session=").append(session.getSessionID());
                resp.sendRedirect(sb.toString());
            } catch (URISyntaxException | IOException e) {
                LOG.info("Illegal redirect URL", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                requestContext.getMetricProvider().recordHTTPStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

}
