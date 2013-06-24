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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

import static com.openexchange.ajax.AJAXServlet.CONTENTTYPE_HTML;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Login;
import com.openexchange.ajax.writer.LoginWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.log.LogFactory;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tokenlogin.TokenLoginSecret;
import com.openexchange.tokenlogin.TokenLoginService;
import com.openexchange.tools.servlet.http.Tools;


/**
 * {@link RedeemToken}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class RedeemToken implements LoginRequestHandler {
    
    private final LoginConfiguration conf;
    private final static Log LOG = LogFactory.getLog(RedeemToken.class);

    /**
     * Initializes a new {@link RedeemToken}.
     */
    public RedeemToken(LoginConfiguration conf) {
        super();
        this.conf = conf;
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            doRedeemToken(req, resp);
        } catch (OXException e) {
            String errorPage = conf.getErrorPageTemplate().replace("ERROR_MESSAGE", e.getMessage());
            resp.setContentType(CONTENTTYPE_HTML);
            resp.getWriter().write(errorPage);
        }
    }
    
    private void doRedeemToken(HttpServletRequest req, HttpServletResponse resp) throws OXException, IOException {
        String authId = LoginTools.parseAuthId(req, true);
        String client = LoginTools.parseClient(req, true, "");
        String userAgent = LoginTools.parseUserAgent(req);
        String hash = HashCalculator.getInstance().getHash(req, userAgent, client);
        String token = LoginTools.parseToken(req);
        String appSecret = LoginTools.parseAppSecret(req);
        if (null == token || null == appSecret) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Tools.disableCaching(resp);
        resp.setContentType(AJAXServlet.CONTENTTYPE_JAVASCRIPT);
        TokenLoginService service = ServerServiceRegistry.getInstance().getService(TokenLoginService.class);
        Session session = null;
        try {
            session = service.redeemToken(token, appSecret, client, authId, hash);
        } catch (OXException e) {
            Login.logAndSendException(resp, e);
            return;
        }
        TokenLoginSecret tokenLoginSecret = service.getTokenLoginSecret(appSecret);
        Boolean writePassword = (Boolean) tokenLoginSecret.getParameters().get("accessPassword");
        if (writePassword == null) {
            writePassword = false;
        }
        try {
            final Context context = ContextStorage.getInstance().getContext(session.getContextId());
            final User user = UserStorage.getInstance().getUser(session.getUserId(), context);
            if (!context.isEnabled() || !user.isMailEnabled()) {
                LOG.info("Either context " + context.getContextId() + " or user " + user.getId() + " not enabled");
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } catch (final UndeclaredThrowableException e) {
            LOG.info("Unexpected error occurred during login: " + e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } catch (final OXException e) {
            LOG.info("Couldn't resolve context/user by identifier: " + session.getContextId() + '/' + session.getUserId(), e);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Login.writeSecretCookie(resp, session, hash, req.isSecure(), req.getServerName(), conf);

        try {
            final JSONObject json = new JSONObject();
            LoginWriter.write(session, json);
            if (writePassword) {
                json.put("password", session.getPassword());
            }
            json.write(resp.getWriter());
        } catch (final JSONException e) {
            LOG.info(e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
