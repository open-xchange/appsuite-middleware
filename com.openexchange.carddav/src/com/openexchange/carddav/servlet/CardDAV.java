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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import static org.slf4j.LoggerFactory.getLogger;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.carddav.osgi.CarddavActivator;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.login.Interface;
import com.openexchange.server.ServiceLookup;
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
    private static final LoginCustomizer ALLOW_ASTERISK = new AllowAsteriskAsSeparatorCustomizer();

	private final ServiceLookup services;
	private final CarddavPerformer performer;

    /**
     * Initializes a new {@link CardDAV}.
     *
     * @param services A service lookup reference
     * @param performer The CardDAV performer
     */
    public CardDAV(ServiceLookup services, CarddavPerformer performer) {
        super();
        this.services = services;
        this.performer = performer;
    }

    @Override
    protected boolean useCookies() {
        return false;
    }

    @Override
    protected LoginCustomizer getLoginCustomizer() {
        return ALLOW_ASTERISK;
    }

    @Override
    protected Interface getInterface() {
        return Interface.CARDDAV;
    }

    @Override
    protected void doCopy(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.COPY);
    }

    @Override
    protected void doLock(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.LOCK);
    }

    @Override
    protected void doMkCol(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.MKCOL);
    }

    @Override
    protected void doMove(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.MOVE);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.OPTIONS);
    }

    @Override
    protected void doPropFind(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.PROPFIND);
    }

    @Override
    protected void doPropPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.PROPPATCH);
    }

    @Override
    protected void doUnLock(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.UNLOCK);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.DELETE);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.GET);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.HEAD);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.PUT);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.TRACE);
    }

    @Override
    protected void doReport(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doIt(req, resp, Action.REPORT);
    }

    private void doIt(HttpServletRequest request, HttpServletResponse response, Action action) throws ServletException, IOException {
        /*
         * get server session from request & check permissions
         */
        ServerSession session = null;;
        try {
            session = ServerSessionAdapter.valueOf(getSession(request));
        } catch (OXException e) {
            getLogger(CarddavActivator.class).error("Error getting server session from request", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        if (null == session || false == checkPermission(session)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        /*
         * perform the action
         */
        performer.doIt(request, response, action, session);
    }

    /**
     * Gets a value indicating whether CardDAV is enabled for the supplied session.
     *
     * @param session The session to check permissions for
     * @return <code>true</code> if CardDAV is enabled, <code>false</code>, otherwise
     */
    private boolean checkPermission(ServerSession session) {
        if (false == session.getUserPermissionBits().hasContact()) {
            return false;
        }
        ConfigViewFactory configViewFactory = services.getService(ConfigViewFactory.class);
        if (null == configViewFactory) {
            getLogger(CardDAV.class).warn("Unable to access confic cascade, unable to check servlet permissions.");
            return false;
        }
        try {
            ConfigView configView = configViewFactory.getView(session.getUserId(), session.getContextId());
            ComposedConfigProperty<Boolean> property = configView.property("com.openexchange.carddav.enabled", boolean.class);
            return property.isDefined() && property.get();
        } catch (OXException e) {
            getLogger(CardDAV.class).error("Error checking if CardDAV is enabled for user {} in context {}: {}",
                session.getUserId(), session.getContextId(), e.getMessage(), e);
            return false;
        }
    }

}
