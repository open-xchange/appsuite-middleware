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
