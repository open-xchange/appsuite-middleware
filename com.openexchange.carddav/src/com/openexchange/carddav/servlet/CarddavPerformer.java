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

import java.util.EnumMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.carddav.CarddavProtocol;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.carddav.action.CardDAVMaxUploadSizeAction;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.webdav.action.AbstractAction;
import com.openexchange.webdav.action.OXWebdavPutAction;
import com.openexchange.webdav.action.ServletWebdavRequest;
import com.openexchange.webdav.action.ServletWebdavResponse;
import com.openexchange.webdav.action.WebdavAction;
import com.openexchange.webdav.action.WebdavCopyAction;
import com.openexchange.webdav.action.WebdavDefaultHeaderAction;
import com.openexchange.webdav.action.WebdavDeleteAction;
import com.openexchange.webdav.action.WebdavExistsAction;
import com.openexchange.webdav.action.WebdavGetAction;
import com.openexchange.webdav.action.WebdavHeadAction;
import com.openexchange.webdav.action.WebdavIfAction;
import com.openexchange.webdav.action.WebdavIfMatchAction;
import com.openexchange.webdav.action.WebdavLockAction;
import com.openexchange.webdav.action.WebdavLogAction;
import com.openexchange.webdav.action.WebdavMkcolAction;
import com.openexchange.webdav.action.WebdavMoveAction;
import com.openexchange.webdav.action.WebdavOptionsAction;
import com.openexchange.webdav.action.WebdavPropfindAction;
import com.openexchange.webdav.action.WebdavProppatchAction;
import com.openexchange.webdav.action.WebdavReportAction;
import com.openexchange.webdav.action.WebdavRequestCycleAction;
import com.openexchange.webdav.action.WebdavTraceAction;
import com.openexchange.webdav.action.WebdavUnlockAction;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.PropertyMixin;

/**
 * The {@link CarddavPerformer} contains all the wiring for Carddav actions. This is the central entry point for Carddav requests.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CarddavPerformer implements SessionHolder {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CarddavPerformer.class);
    private static final Protocol PROTOCOL = new CarddavProtocol();

    private final ThreadLocal<ServerSession> sessionHolder;
    private final GroupwareCarddavFactory factory;
    private final Map<Action, WebdavAction> actions;

    /**
     * Initializes a new {@link CarddavPerformer}.
     *
     * @param services A service lookup reference
     */
    public CarddavPerformer(ServiceLookup services) {
        super();
        this.sessionHolder = new ThreadLocal<ServerSession>();
        this.factory = new GroupwareCarddavFactory(services, this);
        this.actions = initActions();
    }

    /**
     * Sets the global property mix-ins to use.
     *
     * @param mixins The gloabl property mix-ins
     */
    public void setGlobalMixins(PropertyMixin... mixins) {
        factory.setGlobalMixins(mixins);
    }

    /**
     * Performs a CardDAV request.
     *
     * @param request The HTTP servlet request
     * @param response The HTTP servlet response
     * @param action The action to execute
     * @param session The associated session
     */
    public void doIt(HttpServletRequest request, HttpServletResponse response, Action action, ServerSession session) {
        try {
            ServletWebdavRequest webdavRequest = new ServletWebdavRequest(factory, request);
            webdavRequest.setUrlPrefix("/carddav/");
            ServletWebdavResponse webdavResponse = new ServletWebdavResponse(response);
            session.setParameter("user-agent", request.getHeader("user-agent"));
            sessionHolder.set(session);
            LOG.debug("Executing {}", action);
            actions.get(action).perform(webdavRequest, webdavResponse);
        } catch (WebdavProtocolException x) {
            response.setStatus(x.getStatus());
        } catch (NullPointerException x) {
            LOG.error("Null reference detected.", x);
        } finally {
            sessionHolder.set(null);
        }
    }

    @Override
    public ServerSession getSessionObject() {
        ServerSession session = sessionHolder.get();
        if (null == session) {
            IllegalStateException e = new IllegalStateException();
            LOG.error("No session found in Session holder", e.fillInStackTrace());
        }
        return session;
    }

    @Override
    public Context getContext() {
        return sessionHolder.get().getContext();
    }

    @Override
    public User getUser() {
        return sessionHolder.get().getUser();
    }

    private EnumMap<Action, WebdavAction> initActions() {
        EnumMap<Action, WebdavAction> actions = new EnumMap<Action, WebdavAction>(Action.class);
        actions.put(Action.UNLOCK, prepare(new WebdavUnlockAction(), true, true, new WebdavIfAction(0, false, false)));
        actions.put(Action.PROPPATCH, prepare(new WebdavProppatchAction(PROTOCOL), true, true, new WebdavExistsAction(), new WebdavIfAction(0, true, false)));
        actions.put(Action.PROPFIND, prepare(new WebdavPropfindAction(PROTOCOL), true, true, new WebdavExistsAction(), new WebdavIfAction(0, false, false)));
        actions.put(Action.REPORT, prepare(new WebdavReportAction(PROTOCOL), true, true, new WebdavExistsAction(), new WebdavIfAction(0, false, false)));
        actions.put(Action.OPTIONS, prepare(new WebdavOptionsAction(), true, true, new WebdavIfAction(0, false, false)));
        actions.put(Action.MOVE, prepare(new WebdavMoveAction(factory), true, true, new WebdavExistsAction(), new WebdavIfAction(0, true, true)));
        actions.put(Action.MKCOL, prepare(new WebdavMkcolAction(), true, true, new WebdavIfAction(0, true, false)));
        actions.put(Action.LOCK, prepare(new WebdavLockAction(), true, true, new WebdavIfAction(0, true, false)));
        actions.put(Action.COPY, prepare(new WebdavCopyAction(factory), true, true, new WebdavExistsAction(), new WebdavIfAction(0, false, true)));
        actions.put(Action.DELETE, prepare(new WebdavDeleteAction(), true, true, new WebdavExistsAction(), new WebdavIfMatchAction(), new WebdavIfAction(0, true, false)));
        actions.put(Action.GET, prepare(new WebdavGetAction(), true, true, new WebdavExistsAction(), new WebdavIfAction(0, false, false)));
        actions.put(Action.HEAD, prepare(new WebdavHeadAction(), true, true, new WebdavExistsAction(), new WebdavIfAction(0, false, false)));
        actions.put(Action.TRACE, prepare(new WebdavTraceAction(), true, true, new WebdavIfAction(0, false, false)));
        OXWebdavPutAction oxWebdavPut = new OXWebdavPutAction();
        oxWebdavPut.setSessionHolder(this);
        CardDAVMaxUploadSizeAction maxUploadSizeAction = new CardDAVMaxUploadSizeAction(factory, this);
        actions.put(Action.PUT, prepare(oxWebdavPut, true, true, new WebdavIfMatchAction(), maxUploadSizeAction));
        makeLockNullTolerant(actions);
        return actions;
    }

    private static void makeLockNullTolerant(Map<Action, WebdavAction> actions) {
        Action[] nullTolerantActions = { Action.OPTIONS, Action.LOCK, Action.MKCOL, Action.PUT };
        for (Action action : nullTolerantActions) {
            WebdavAction webdavAction = actions.get(action);
            while (webdavAction != null) {
                if (webdavAction instanceof WebdavExistsAction) {
                    ((WebdavExistsAction) webdavAction).setTolerateLockNull(true);
                    webdavAction = null;
                } else if (webdavAction instanceof AbstractAction) {
                    webdavAction = ((AbstractAction) webdavAction).getNext();
                } else {
                    webdavAction = null;
                }
            }

        }
    }

    private WebdavAction prepare(AbstractAction action, boolean logBody, boolean logResponse, AbstractAction... additionals) {
        AbstractAction lifeCycle = new WebdavRequestCycleAction();
        WebdavLogAction logAction = new WebdavLogAction(logBody, logResponse);
        lifeCycle.setNext(logAction);
        AbstractAction defaultHeader = new WebdavDefaultHeaderAction();
        logAction.setNext(defaultHeader);
        AbstractAction ifMatch = new WebdavIfMatchAction();
        defaultHeader.setNext(ifMatch);
        AbstractAction previousAction = ifMatch;
        for (AbstractAction nextAction : additionals) {
            previousAction.setNext(nextAction);
            previousAction = nextAction;
        }
        previousAction.setNext(action);
        return lifeCycle;
    }

}
