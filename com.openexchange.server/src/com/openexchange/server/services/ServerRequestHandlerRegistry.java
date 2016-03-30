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

package com.openexchange.server.services;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXRequestHandler;

/**
 * {@link ServerRequestHandlerRegistry} - A registry for request handlers
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ServerRequestHandlerRegistry {

    private static final ServerRequestHandlerRegistry INSTANCE = new ServerRequestHandlerRegistry();

    /**
     * Gets the server's request handler registry
     *
     * @return The server's request handler registry
     */
    public static ServerRequestHandlerRegistry getInstance() {
        return INSTANCE;
    }

    private final Map<String, Map<String, AJAXRequestHandler>> requestHandlers;

    /**
     * Initializes a new {@link ServerRequestHandlerRegistry}
     */
    private ServerRequestHandlerRegistry() {
        super();
        requestHandlers = new ConcurrentHashMap<String, Map<String, AJAXRequestHandler>>();
    }

    /**
     * Clears the whole registry
     */
    public void clearRegistry() {
        requestHandlers.clear();
    }

    /**
     * Removes a request handler bound to indicated module name and its
     * supported actions from this registry
     *
     * @param requestHandler
     *            The request handler to remove
     */
    public void removeHandler(final AJAXRequestHandler requestHandler) {
        final Map<String, AJAXRequestHandler> actionHandlers = requestHandlers.get(requestHandler.getModule());
        if (actionHandlers != null) {
            final Set<String> actions = requestHandler.getSupportedActions();
            for (final String action : actions) {
                actionHandlers.remove(action);
            }
            if (actionHandlers.isEmpty()) {
                requestHandlers.remove(requestHandler.getModule());
            }
        }
    }

    /**
     * Adds a request handler bound to indicated module name and actions to this
     * registry.
     * <p>
     * Any existing boundaries are overwritten.
     *
     * @param requestHandler
     *            The request handler to add
     */
    public void addHandler(final AJAXRequestHandler requestHandler) {
        Map<String, AJAXRequestHandler> actionHandlers = requestHandlers.get(requestHandler.getModule());
        if (actionHandlers == null) {
            synchronized (this) {
                if ((actionHandlers = requestHandlers.get(requestHandler.getModule())) == null) {
                    actionHandlers = new ConcurrentHashMap<String, AJAXRequestHandler>();
                    requestHandlers.put(requestHandler.getModule(), actionHandlers);
                }
            }
        }
        final Set<String> actions = requestHandler.getSupportedActions();
        for (final String action : actions) {
            actionHandlers.put(action, requestHandler);
        }
    }

    private static final AJAXRequestHandler[] EMPTY_ARR = new AJAXRequestHandler[0];

    /**
     * Gets the request handlers by given module name
     *
     * @param moduleName
     *            The module name
     * @return The request handlers
     */
    public AJAXRequestHandler[] getModuleHandlers(final String moduleName) {
        final Map<String, AJAXRequestHandler> actionHandlers = requestHandlers.get(moduleName);
        if (actionHandlers == null) {
            return EMPTY_ARR;
        }
        final Set<AJAXRequestHandler> set = new HashSet<AJAXRequestHandler>(actionHandlers.values());
        return set.toArray(new AJAXRequestHandler[set.size()]);
    }

    /**
     * Gets the request handlers by given module and action
     *
     * @param moduleName
     *            The module name
     * @param action
     *            The action identifier
     * @return The request handler if present; otherwise <code>null</code>
     */
    public AJAXRequestHandler getHandler(final String moduleName, final String action) {
        final Map<String, AJAXRequestHandler> actionHandlers = requestHandlers.get(moduleName);
        if (actionHandlers == null) {
            return null;
        }
        return actionHandlers.get(action);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        sb.append("Server request handler registry:\n");
        if (requestHandlers.isEmpty()) {
            sb.append("<empty>");
        } else {
            final Iterator<Map.Entry<String, Map<String, AJAXRequestHandler>>> iter = requestHandlers.entrySet()
                    .iterator();
            while (true) {
                final Map.Entry<String, Map<String, AJAXRequestHandler>> e = iter.next();
                sb.append(e.getKey()).append(": ").append(e.getValue().getClass().getName());
                if (iter.hasNext()) {
                    sb.append('\n');
                } else {
                    break;
                }
            }
        }
        return sb.toString();
    }

}
