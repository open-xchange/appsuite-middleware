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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.carddav.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import com.openexchange.carddav.servlet.CarddavPerformer.Action;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.login.Interface;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.webdav.AllowAsteriskAsSeparatorCustomizer;
import com.openexchange.tools.webdav.LoginCustomizer;
import com.openexchange.tools.webdav.OXServlet;

/**
 * The {@link CalDAV} servlet. It delegates all calls to the CaldavPerformer
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CardDAV extends OXServlet {

	private static final long serialVersionUID = -6381396333467867154L;

	private static final transient Log LOG = com.openexchange.log.Log.loggerFor(CardDAV.class);

    private static volatile ServiceLookup services;

    public static void setServiceLookup(ServiceLookup serviceLookup) {
        services = serviceLookup;
    }

    @Override
    protected Interface getInterface() {
        return Interface.CARDDAV;
    }

    @Override
    protected void doCopy(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.COPY);
    }

    @Override
    protected void doLock(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.LOCK);
    }

    @Override
    protected void doMkCol(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.MKCOL);
    }

    @Override
    protected void doMove(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.MOVE);
    }

    @Override
    protected void doOptions(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.OPTIONS);
    }

    @Override
    protected void doPropFind(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.PROPFIND);
    }

    @Override
    protected void doPropPatch(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.PROPPATCH);
    }

    @Override
    protected void doUnLock(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.UNLOCK);
    }

    @Override
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.DELETE);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.GET);
    }

    @Override
    protected void doHead(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.HEAD);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.PUT);
    }

    @Override
    protected void doTrace(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.TRACE);
    }

    @Override
    protected void doReport(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.REPORT);
    }

    private void doIt(final HttpServletRequest req, final HttpServletResponse resp, final Action action) throws ServletException, IOException {
        ServerSession session = null;
        try {
            try {
                session = ServerSessionAdapter.valueOf(getSession(req));
                if (!checkPermission(session)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            } catch (final OXException exc) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            CarddavPerformer.getInstance().doIt(req, resp, action, session);
        } finally {
            if (null != session && mustLogOut(req)) {
                logout(session, req, resp);
            }
        }
    }

    private boolean checkPermission(ServerSession session) {
        try {
            ComposedConfigProperty<Boolean> property = services.getService(ConfigViewFactory.class).getView(session.getUserId(), session.getContextId()).property("com.openexchange.carddav.enabled", boolean.class);
            return property.isDefined() && property.get() && session.getUserConfiguration().hasContact();

        } catch (OXException e) {
            return false;
        }
    }

    private void logout(final ServerSession session, final HttpServletRequest req, final HttpServletResponse resp) {
        removeCookie(req, resp);
        try {
            LoginPerformer.getInstance().doLogout(session.getSessionID());
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static final transient Tools.CookieNameMatcher COOKIE_MATCHER = new Tools.CookieNameMatcher() {

        @Override
		public boolean matches(final String cookieName) {
            return (COOKIE_SESSIONID.equals(cookieName) || Tools.JSESSIONID_COOKIE.equals(cookieName));
        }
    };

    private void removeCookie(final HttpServletRequest req, final HttpServletResponse resp) {
        Tools.deleteCookies(req, resp, COOKIE_MATCHER);
    }

    private boolean mustLogOut(final HttpServletRequest req) {
        return true; // Check this
    }

    private static final LoginCustomizer ALLOW_ASTERISK = new AllowAsteriskAsSeparatorCustomizer();

    @Override
    protected LoginCustomizer getLoginCustomizer() {
        return ALLOW_ASTERISK;
    }
}
