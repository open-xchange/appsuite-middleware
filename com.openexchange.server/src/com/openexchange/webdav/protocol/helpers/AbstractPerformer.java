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

package com.openexchange.webdav.protocol.helpers;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.session.SessionHolder;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.webdav.action.AbstractAction;
import com.openexchange.webdav.action.ServletWebdavRequest;
import com.openexchange.webdav.action.ServletWebdavResponse;
import com.openexchange.webdav.action.WebdavAction;
import com.openexchange.webdav.action.WebdavDefaultHeaderAction;
import com.openexchange.webdav.action.WebdavExistsAction;
import com.openexchange.webdav.action.WebdavIfMatchAction;
import com.openexchange.webdav.action.WebdavLogAction;
import com.openexchange.webdav.action.WebdavRequestCycleAction;
import com.openexchange.webdav.loader.BulkLoader;
import com.openexchange.webdav.protocol.WebdavMethod;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link AbstractPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public abstract class AbstractPerformer implements SessionHolder {

    private final ThreadLocal<ServerSession> sessionHolder;

    /**
     * Initializes a new {@link AbstractPerformer}.
     */
    protected AbstractPerformer() {
        super();
        this.sessionHolder = new ThreadLocal<ServerSession>();
    }

    /**
     * Gets the URL prefix to apply for each incoming WebDAV request.
     *
     * @return The URL prefix
     */
    protected abstract String getURLPrefix();

    /**
     * Gets the underlying WebDAV factory.
     *
     * @return The WebDAV factory
     */
    protected abstract AbstractWebdavFactory getFactory();

    /**
     * Gets the WebDAV action that handles a specific WebDAV method.
     *
     * @param method The WebDAV method to get the action for
     * @return The action
     */
    protected abstract WebdavAction getAction(WebdavMethod method);

    /**
     * Prepares a new {@link WebdavRequestCycleAction} for a concrete WebDAV action, injecting appropriate actions for logging, default
     * response headers and if-header matching implicitly. Further actions are added as needed, as well as finally the action itself.
     *
     * @param action The action to prepare
     * @param logBody <code>true</code> to log the request body, <code>false</code>, otherwise
     * @param logResponse <code>true</code> to log the response, <code>false</code>, otherwise
     * @param additionals Additional actions to include
     * @return The prepared WebDAV action
     */
    protected WebdavAction prepare(AbstractAction action, boolean logBody, boolean logResponse, AbstractAction... additionals) {
        return prepare(action, logBody, logResponse, null, additionals);
    }

    /**
     * Prepares a new {@link WebdavRequestCycleAction} for a concrete WebDAV action, injecting appropriate actions for logging, default
     * response headers and if-header matching implicitly. Further actions are added as needed, as well as finally the action itself.
     *
     * @param action The action to prepare
     * @param logBody <code>true</code> to log the request body, <code>false</code>, otherwise
     * @param logResponse <code>true</code> to log the response, <code>false</code>, otherwise
     * @param bulkLoader The bulk loader to apply for each action, or <code>null</code> if not needed
     * @param additionals Additional actions to include
     * @return The prepared WebDAV action
     */
    protected WebdavAction prepare(AbstractAction action, boolean logBody, boolean logResponse, BulkLoader bulkLoader, AbstractAction... additionals) {
        return prepare(action, logBody, logResponse, true, bulkLoader, additionals);
    }

    /**
     * Prepares a new {@link WebdavRequestCycleAction} for a concrete WebDAV action, injecting appropriate actions for logging, default
     * response headers and if-header matching implicitly. Further actions are added as needed, as well as finally the action itself.
     *
     * @param action The action to prepare
     * @param logBody <code>true</code> to log the request body, <code>false</code>, otherwise
     * @param logResponse <code>true</code> to log the response, <code>false</code>, otherwise
     * @param ifMatch <code>true</code> to do <code>"If-Match"</code> / <code>If-None-Match</code> header validation, <code>false</code>, otherwise
     * @param bulkLoader The bulk loader to apply for each action, or <code>null</code> if not needed
     * @param additionals Additional actions to include
     * @return The prepared WebDAV action
     */
    protected WebdavAction prepare(AbstractAction action, boolean logBody, boolean logResponse, boolean ifMatch, BulkLoader bulkLoader, AbstractAction... additionals) {
        /*
         * initialize surrounding request lifecycle
         */
        WebdavRequestCycleAction lifeCycle = new WebdavRequestCycleAction();
        lifeCycle.setBulkLoader(bulkLoader);
        /*
         * add log action
         */
        WebdavLogAction logAction = new WebdavLogAction(logBody, logResponse);
        lifeCycle.setNext(logAction);
        /*
         * add default header action
         */
        WebdavDefaultHeaderAction defaultHeader = new WebdavDefaultHeaderAction();
        defaultHeader.setBulkLoader(bulkLoader);
        logAction.setNext(defaultHeader);
        AbstractAction previousAction = defaultHeader;
        /*
         * add if-match action
         */
        if (ifMatch) {
            WebdavIfMatchAction ifMatchAction = new WebdavIfMatchAction();
            ifMatchAction.setBulkLoader(bulkLoader);
            previousAction.setNext(ifMatchAction);
            previousAction = ifMatchAction;
        }
        /*
         * add additional actions
         */
        for (AbstractAction nextAction : additionals) {
            nextAction.setBulkLoader(bulkLoader);
            previousAction.setNext(nextAction);
            previousAction = nextAction;
        }
        /*
         * add action itself
         */
        action.setBulkLoader(bulkLoader);
        previousAction.setNext(action);
        return lifeCycle;
    }

    public void setGlobalMixins(PropertyMixin... mixins) {
        getFactory().setGlobalMixins(mixins);
    }

    public void doIt(HttpServletRequest request, HttpServletResponse response, WebdavMethod method, ServerSession session) {
        ServletWebdavRequest webdavRequest = new ServletWebdavRequest(getFactory(), request);
        webdavRequest.setUrlPrefix(getURLPrefix());
        session.setParameter("user-agent", request.getHeader("user-agent"));
        sessionHolder.set(session);
        try {
            ServletWebdavResponse webdavResponse = new ServletWebdavResponse(response);
            getAction(method).perform(webdavRequest, webdavResponse);
        } catch (WebdavProtocolException e) {
            if (null != response && false == response.isCommitted()) {
                response.setStatus(e.getStatus());
            }
        } finally {
            sessionHolder.set(null);
        }
    }

    @Override
    public Session getSessionObject() {
        return sessionHolder.get();
    }

    @Override
    public Context getContext() {
        return sessionHolder.get().getContext();
    }

    @Override
    public User getUser() {
        return sessionHolder.get().getUser();
    }

    /**
     * Marks specific actions as supported by so-called "lock-null" resources, i.e. locked placeholder resources to reserve a name.
     * <p/>
     * @see <a href=http://www.webdav.org/specs/rfc2518.html#rfc.section.7.4>http://www.webdav.org/specs/rfc2518.html#rfc.section.7.4</a>
     *
     * @param actions The actions to mark if applicable
     */
    protected static void makeLockNullTolerant(Map<WebdavMethod, WebdavAction> actions) {
        WebdavMethod[] nullTolerantActions = { WebdavMethod.OPTIONS, WebdavMethod.LOCK, WebdavMethod.MKCOL, WebdavMethod.PUT };
        for (WebdavMethod action : nullTolerantActions) {
            WebdavAction webdavAction = actions.get(action);
            while (null != webdavAction) {
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

}
