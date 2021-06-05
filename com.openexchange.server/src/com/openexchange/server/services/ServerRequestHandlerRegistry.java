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
