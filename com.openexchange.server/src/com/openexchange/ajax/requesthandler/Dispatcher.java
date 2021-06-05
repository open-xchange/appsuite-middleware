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

package com.openexchange.ajax.requesthandler;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.tools.session.ServerSession;

/**
 * A {@link Dispatcher} is marked as a top level dispatcher for the entire framework.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@SingletonService
public interface Dispatcher {

    /**
     * The prefix reference for dispatcher; e.g. <tt>"/ajax/"</tt> (default).
     * <p>
     * All requests starting with this prefix are directed to dispatcher framework.
     *
     * @deprecated Use {@link DispatcherPrefixService} instead! Classes of the AJAX framework (i.e.
     *             non-module-specific classes below the com.openexchange.ajax package might also
     *             use {@link Dispatchers#getPrefix()} after the framework is guaranteed to be initialized.
     *
     */
    @Deprecated
    public static final AtomicReference<String> PREFIX = new AtomicReference<String>(DispatcherPrefixService.DEFAULT_PREFIX);

    /**
     * Performs given request.
     *
     * @param requestData The request data to perform
     * @param state The state
     * @param session The session providing needed user data
     * @return The result yielded from given request
     * @throws OXException If an error occurs
     * @see AJAXRequestDataTools#parseRequest(javax.servlet.http.HttpServletRequest, boolean, boolean, ServerSession, String)
     */
    AJAXRequestResult perform(AJAXRequestData requestData, AJAXState state, ServerSession session) throws OXException;

    /**
     * Looks-up denoted factory
     *
     * @param module The module to look-up by
     * @return The factory or <code>null</code>
     */
    AJAXActionServiceFactory lookupFactory(String module);

    /**
     * Begins a dispatcher cycle.
     * <pre>
     *  dispatcher.begin();
     *  try {
     *   ...
     *  } finally {
     *   dispatcher.end();
     *  }
     * </pre>
     *
     * @return The state
     * @throws OXException If start-up fails
     * @see #end(AJAXState)
     */
    AJAXState begin() throws OXException;

    /**
     * Ends a dispatcher cycle.
     * <pre>
     *  dispatcher.begin();
     *  try {
     *   ...
     *  } finally {
     *   dispatcher.end();
     *  }
     * </pre>
     *
     * @param state The state
     * @see #begin()
     */
    void end(AJAXState state);

    /**
     * Checks whether the dispatcher knows about the given module.
     *
     * @param module The module identifier to check
     * @return <code>true</code> if it can handle the module request, <code>false</code> otherwise
     * @see AJAXRequestDataTools#getModule(String, javax.servlet.http.HttpServletRequest)
     */
    boolean handles(String module);

    /**
     * Indicates whether the fall-back session may be used for this action.
     *
     * @param module The module identifier
     * @param action The action identifier
     * @return <code>true</code> if the fall-back session may be used for this action; otherwise <code>false</code>
     * @throws OXException If check fails for any reason
     * @see AJAXRequestDataTools#getModule(String, javax.servlet.http.HttpServletRequest)
     * @see AJAXRequestDataTools#getAction(javax.servlet.http.HttpServletRequest)
     */
    boolean mayUseFallbackSession(String module, String action) throws OXException;

    /**
     * Indicates whether authentication via public session identifier is permitted
     *
     * @param module The module identifier
     * @param action The action identifier
     * @return <code>true</code> if permitted; otherwise <code>false</code>
     * @throws OXException If check fails for any reason
     * @see AJAXRequestDataTools#getModule(String, javax.servlet.http.HttpServletRequest)
     * @see AJAXRequestDataTools#getAction(javax.servlet.http.HttpServletRequest)
     */
    boolean mayPerformPublicSessionAuth(String module, String action) throws OXException;

    /**
     * Indicates that given action can be used without a session.
     *
     * @param module The module identifier
     * @param action The action identifier
     * @return <code>true</code> if given action can be used without a session; otherwise <code>false</code>
     * @throws OXException If check fails for any reason
     * @see AJAXRequestDataTools#getModule(String, javax.servlet.http.HttpServletRequest)
     * @see AJAXRequestDataTools#getAction(javax.servlet.http.HttpServletRequest)
     */
    boolean mayOmitSession(String module, String action) throws OXException;

    /**
     * Indicates that given action can be used without a secret.
     *
     * @param module The module identifier
     * @param action The action identifier
     * @return <code>true</code> if given action can be used without a secret; otherwise <code>false</code>
     * @throws OXException If check fails for any reason
     * @see AJAXRequestDataTools#getModule(String, javax.servlet.http.HttpServletRequest)
     * @see AJAXRequestDataTools#getAction(javax.servlet.http.HttpServletRequest)
     */
    boolean noSecretCallback(String module, String action) throws OXException;

}
