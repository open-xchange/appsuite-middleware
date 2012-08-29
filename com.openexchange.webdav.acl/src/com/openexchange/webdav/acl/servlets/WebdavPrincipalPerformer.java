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

package com.openexchange.webdav.acl.servlets;

import java.util.EnumMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.user.UserService;
import com.openexchange.webdav.InfostorePerformer;
import com.openexchange.webdav.acl.PrincipalWebdavFactory;
import com.openexchange.webdav.action.AbstractAction;
import com.openexchange.webdav.action.OXWebdavMaxUploadSizeAction;
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
import com.openexchange.webdav.action.WebdavRequestCycleAction;
import com.openexchange.webdav.action.WebdavTraceAction;
import com.openexchange.webdav.action.WebdavUnlockAction;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.PropertyMixin;

/**
 * {@link WebdavPrincipalPerformer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class WebdavPrincipalPerformer implements SessionHolder{
    private static final Log LOG = com.openexchange.log.Log.loggerFor(WebdavPrincipalPerformer.class);

    private static WebdavPrincipalPerformer INSTANCE = null;

    private static ServiceLookup services;

    public static void setServices(final ServiceLookup lookup){
        services = lookup;
    }

    /**
     * Gets the instance of {@link InfostorePerformer}.
     *
     * @return The instance of {@link InfostorePerformer}.
     */
    public static WebdavPrincipalPerformer getInstance() {
        if (INSTANCE == null) {
            return INSTANCE = new WebdavPrincipalPerformer();
        }
        return INSTANCE;
    }

    public static enum Action {
        UNLOCK, PROPPATCH, PROPFIND, OPTIONS, MOVE, MKCOL, LOCK, COPY, DELETE, GET, HEAD, PUT, TRACE
    }

    private final PrincipalWebdavFactory factory;

    private final Protocol protocol = new Protocol();

    private final Map<Action, WebdavAction> actions = new EnumMap<Action, WebdavAction>(Action.class);

    private final ThreadLocal<ServerSession> session = new ThreadLocal<ServerSession>();

    private WebdavPrincipalPerformer() {

        WebdavAction unlock;
        WebdavAction propPatch;
        WebdavAction propFind;
        WebdavAction options;
        WebdavAction move;
        WebdavAction mkcol;
        WebdavAction lock;
        WebdavAction copy;
        WebdavAction delete;
        WebdavAction get;
        WebdavAction head;
        WebdavAction put;
        WebdavAction trace;


        this.factory = new PrincipalWebdavFactory(services.getService(UserService.class),this);

        unlock = prepare(new WebdavUnlockAction(), true, true, new WebdavIfAction(0, false, false));
        propPatch = prepare(new WebdavProppatchAction(protocol), true, true, new WebdavExistsAction(), new WebdavIfAction(0, true, false));
        propFind = prepare(new WebdavPropfindAction(protocol), true, true, new WebdavExistsAction(), new WebdavIfAction(0, false, false));
        options = prepare(new WebdavOptionsAction(), true, true, new WebdavIfAction(0, false, false));
        move = prepare(new WebdavMoveAction(factory), true, true, new WebdavExistsAction(), new WebdavIfAction(0, true, true));
        mkcol = prepare(new WebdavMkcolAction(), true, true, new WebdavIfAction(0, true, false));
        lock = prepare(new WebdavLockAction(), true, true, new WebdavIfAction(0, true, false));
        copy = prepare(new WebdavCopyAction(factory), true, true, new WebdavExistsAction(), new WebdavIfAction(0, false, true));
        delete = prepare(new WebdavDeleteAction(), true, true, new WebdavExistsAction(), new WebdavIfAction(0, true, false));
        get = prepare(new WebdavGetAction(), true, false, new WebdavExistsAction(), new WebdavIfAction(0, false, false));
        head = prepare(new WebdavHeadAction(), true, true, new WebdavExistsAction(), new WebdavIfAction(0, false, false));

        final OXWebdavPutAction oxWebdavPut = new OXWebdavPutAction();
        oxWebdavPut.setSessionHolder(this);

        final OXWebdavMaxUploadSizeAction oxWebdavMaxUploadSize = new OXWebdavMaxUploadSizeAction();
        oxWebdavMaxUploadSize.setSessionHolder(this);

        put = prepare(oxWebdavPut, false, true, new WebdavIfAction(0, false, false), oxWebdavMaxUploadSize);
        trace = prepare(new WebdavTraceAction(), true, true, new WebdavIfAction(0, false, false));

        actions.put(Action.UNLOCK, unlock);
        actions.put(Action.PROPPATCH, propPatch);
        actions.put(Action.PROPFIND, propFind);
        actions.put(Action.OPTIONS, options);
        actions.put(Action.MOVE, move);
        actions.put(Action.MKCOL, mkcol);
        actions.put(Action.LOCK, lock);
        actions.put(Action.COPY, copy);
        actions.put(Action.DELETE, delete);
        actions.put(Action.GET, get);
        actions.put(Action.HEAD, head);
        actions.put(Action.PUT, put);
        actions.put(Action.TRACE, trace);

        makeLockNullTolerant();

    }

    private static volatile Action[] NULL_TOLERANT_ACTIONS;

    private void makeLockNullTolerant() {
        // Single-check-idiom to initialize constant
        Action[] tmp = NULL_TOLERANT_ACTIONS;
        if (null == tmp) {
            NULL_TOLERANT_ACTIONS = tmp = new Action[] { Action.OPTIONS, Action.LOCK, Action.MKCOL, Action.PUT };
        }
        for (final Action action : tmp) {
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


    private WebdavAction prepare(final AbstractAction action, final boolean logBody, final boolean logResponse, final AbstractAction... additionals) {
        final WebdavLogAction logAction = new WebdavLogAction();
        logAction.setLogRequestBody(logBody);
        logAction.setLogResponseBody(logResponse);

        final AbstractAction lifeCycle = new WebdavRequestCycleAction();
        final AbstractAction defaultHeader = new WebdavDefaultHeaderAction();
        final AbstractAction ifMatch = new WebdavIfMatchAction();

        if (logAction.isEnabled()) {
            lifeCycle.setNext(logAction);
            logAction.setNext(defaultHeader);
        } else {
            lifeCycle.setNext(defaultHeader);
        }
        defaultHeader.setNext(ifMatch);

        AbstractAction a = ifMatch;

        for (final AbstractAction a2 : additionals) {
            a.setNext(a2);
            a = a2;
        }

        a.setNext(action);

        return lifeCycle;
    }

    @Override
    public ServerSession getSessionObject() {
        sessionNotNull();
        return session.get();
    }

    private void sessionNotNull() {
        if (session.get() == null) {
            final IllegalStateException exc = new IllegalStateException();
            LOG.error("No session found in Session holder", exc.fillInStackTrace());
        }
    }

    @Override
    public Context getContext() {
        return session.get().getContext();
    }

    @Override
    public User getUser() {
        return session.get().getUser();
    }

    public void doIt(final HttpServletRequest req, final HttpServletResponse resp, final Action action, final ServerSession sess) {
        try {
            final ServletWebdavRequest webdavRequest = new ServletWebdavRequest(factory, req);
            webdavRequest.setUrlPrefix("/principals/users/");
            final ServletWebdavResponse webdavResponse = new ServletWebdavResponse(resp);

            session.set(sess);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing " + action);
            }
            actions.get(action).perform(webdavRequest, webdavResponse);
        } catch (final WebdavProtocolException x) {
            resp.setStatus(x.getStatus());
        } catch (final OXException x) {
            resp.setStatus(500);
        } catch (final NullPointerException x) {
            LOG.error("Null reference detected.", x);
        } finally {
            session.set(null);
        }
    }

    public PrincipalWebdavFactory getFactory() {
        return factory;
    }

    public void setGlobalMixins(final PropertyMixin...mixins) {
        factory.setGlobalMixins(mixins);
    }
}
