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

package com.openexchange.ajax;

import java.io.IOException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.configjump.ICookie;
import com.openexchange.configjump.Replacements;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.java.Strings;
import com.openexchange.sessiond.ExpirationReason;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * This class implements the servlet for authenticating the user at the
 * user admin interface an returns the URL for jumping to the user admin
 * interface to the GUI.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ConfigJump extends SessionServlet {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigJump.class);

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -6938253595032363499L;

    /**
     * Default constructor.
     */
    public ConfigJump() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final ServerSession sessionObj = getSessionObject(req);
        final Response response = new Response(sessionObj);
        try {
            if (null == sessionObj) {
                String sessionId = req.getParameter(PARAMETER_SESSION);
                if (Strings.isEmpty(sessionId)) {
                    response.setException(SessionExceptionCodes.SESSION_PARAMETER_MISSING.create());
                } else {
                    OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
                    oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, ExpirationReason.NO_SUCH_SESSION.getIdentifier());
                    response.setException(oxe);
                }
            } else {
                Context ctx = ContextStorage.getInstance().getContext(sessionObj.getContextId());
                String protocol = Tools.getProtocol(req);
                URL url = com.openexchange.configjump.client.ConfigJump.getLink(new Replacements() {

                    @Override
                    public int getContextId() {
                        return sessionObj.getContextId();
                    }

                    @Override
                    public String getPassword() {
                        return sessionObj.getPassword();
                    }

                    @Override
                    public String getUsername() {
                        return sessionObj.getUserlogin();
                    }

                    @Override
                    public String getProtocol() {
                        return protocol;
                    }

                    @Override
                    public String getServerName() {
                        return req.getServerName();
                    }

                    @Override
                    public int getServerPort() {
                        return req.getServerPort();
                    }

                    @Override
                    public ICookie[] getCookies() {
                        final Cookie[] cookies = req.getCookies();
                        final ICookie[] retval = new ICookie[cookies.length];
                        for (int i = 0; i < cookies.length; i++) {
                            retval[i] = new CookieImpl(cookies[i].getName(), cookies[i].getValue());
                        }
                        return retval;
                    }

                    @Override
                    public String[] getContextInfos() {
                        return ctx.getLoginInfo();
                    }
                });
                response.setData(url);
            }
        } catch (OXException e) {
            LOG.error("", e);
            response.setException(e);
        }
        setDefaultContentType(resp);
        try {
            ResponseWriter.write(response, resp.getWriter(), localeFrom(sessionObj));
        } catch (JSONException e) {
            log(RESPONSE_ERROR, e);
            sendError(resp);
        }
    }

    private static class CookieImpl implements ICookie {

        private final String name;
        private final String value;

        public CookieImpl(final String name, final String value) {
            super();
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }
    }
}
