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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import com.openexchange.ajax.container.Response;
import com.openexchange.configjump.ConfigJumpException;
import com.openexchange.configjump.ICookie;
import com.openexchange.configjump.Replacements;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.session.Session;

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
    private static final Log LOG = LogFactory.getLog(ConfigJump.class);

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
        final Session sessionObj = getSessionObject(req);
        final Response response = new Response();
        try {
            final Context ctx = ContextStorage.getInstance().getContext(
                sessionObj.getContextId());
            final String protocol;
            if (req.isSecure()) {
                protocol = "https";
            } else {
                protocol = "http";
            }
            final URL url = com.openexchange.configjump.client.ConfigJump.getLink(new Replacements() {
                public int getContextId() {
                    return sessionObj.getContextId();
                }
                public String getPassword() {
                    return sessionObj.getPassword();
                }
                public String getUsername() {
                    return sessionObj.getUserlogin();
                }
                public String getProtocol() {
                    return protocol;
                }
                public String getServerName() {
                    return req.getServerName();
                }
                public int getServerPort() {
                    return req.getServerPort();
                }
                public ICookie[] getCookies() {
                    final Cookie[] cookies = req.getCookies();
                    final ICookie[] retval = new ICookie[cookies.length];
                    for (int i = 0; i < cookies.length; i++) {
                        retval[i] = new CookieImpl(cookies[i].getName(),
                            cookies[i].getValue());
                    }
                    return retval;
                }
                public String[] getContextInfos() {
                    return ctx.getLoginInfo();
                }
            });
            response.setData(url);
        } catch (ConfigJumpException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (ContextException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        }
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        try {
            Response.write(response, resp.getWriter());
        } catch (JSONException e) {
            log(RESPONSE_ERROR, e);
            sendError(resp);
        }
    }

    private class CookieImpl implements ICookie {
        private final String name;
        private final String value;
        public CookieImpl(final String name, final String value) {
            super();
            this.name = name;
            this.value = value;
        }
        public String getName() {
            return name;
        }
        public String getValue() {
            return value;
        }
    }
}
