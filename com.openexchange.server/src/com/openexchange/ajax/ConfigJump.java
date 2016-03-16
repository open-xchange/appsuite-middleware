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
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * This class implements the servlet for authenticating the user at the
 * user admin interface an returns the URL for jumping to the user admin
 * interface to the GUI.
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
    protected void doGet(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
        final ServerSession sessionObj = getSessionObject(req);
        final Response response = new Response(sessionObj);
        try {
            final Context ctx = ContextStorage.getInstance().getContext(
                sessionObj.getContextId());
            final String protocol = Tools.getProtocol(req);
            final URL url = com.openexchange.configjump.client.ConfigJump.getLink(new Replacements() {
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
                        retval[i] = new CookieImpl(cookies[i].getName(),
                            cookies[i].getValue());
                    }
                    return retval;
                }
                @Override
                public String[] getContextInfos() {
                    return ctx.getLoginInfo();
                }
            });
            response.setData(url);
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        }
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        try {
            ResponseWriter.write(response, resp.getWriter(), localeFrom(sessionObj));
        } catch (final JSONException e) {
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
