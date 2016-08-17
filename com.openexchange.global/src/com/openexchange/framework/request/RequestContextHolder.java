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

package com.openexchange.framework.request;

import com.openexchange.groupware.notify.hostname.HostData;

/**
 * A global reference to acquire the current {@link RequestContext}. Request contexts
 * are managed per-thread and their life time is bound to their according request. If
 * the static methods of this class are accessed by threads not bound to a currently
 * processing HTTP request, the according results are undefined.
 *
 * HTTP-based interfaces providing a request context must always ensure to orderly clean
 * up the request threads state after processing. Usually such code looks like this:
 *
 * <pre>
 * <code>
 * RequestContext context = buildRequestContext();
 * RequestContextHolder.set(context);
 * try {
 *     processRequest();
 * } finally {
 *     RequestContextHolder.reset();
 * }
 * </code>
 * </pre>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class RequestContextHolder {

    private static final ThreadLocal<RequestContext> REQUEST_CONTEXT = new ThreadLocal<>();

    /**
     * Sets the request context for the current thread. This method must only
     * be called during initialization of a received HTTP request that is to be
     * processed. After processing is done {@link #reset()} must be called.
     *
     * @param context The request context; never <code>null</code>
     * @throws IllegalArgumentException If the passed request context is considered
     *         invalid (probably because of missing data).
     */
    public static void set(RequestContext context) throws IllegalArgumentException {
        validate(context);
        REQUEST_CONTEXT.set(context);
    }

    /**
     * Gets the context of the HTTP request that is currently processed. Calling
     * this method from a thread not bound to a currently processing HTTP request
     * produces an undefined result.
     *
     * @return The request context. Can be <code>null</code> if
     *         <ul>
     *          <li>the current thread is not bound to request</li>
     *          <li>the current request is not a HTTP request</li>
     *          <li>the requests interface is not capable of maintaining request contexts</li>
     *         </ul>
     */
    public static RequestContext get() {
        return REQUEST_CONTEXT.get();
    }

    /**
     * Gets the context of the HTTP request that is currently processed. Calling
     * this method from a thread not bound to a currently processing HTTP request
     * produces an undefined result.
     *
     * @return The request context.
     * @throws IllegalStateException In cases {@link #get()} would return <code>null</code>.
     */
    public static RequestContext require() throws IllegalStateException {
        RequestContext requestContext = REQUEST_CONTEXT.get();
        if (requestContext == null) {
            throw new IllegalStateException("No request context exists for thread '" + Thread.currentThread().getName() + "'!");
        }
        return requestContext;
    }

    /**
     * Resets the request context for the current thread.
     */
    public static void reset() {
        REQUEST_CONTEXT.remove();
    }

    private static void validate(RequestContext context) throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("Request context was null!");
        }

        validateHostData(context.getHostData());
    }

    private static void validateHostData(HostData hostData) throws IllegalArgumentException {
        if (hostData == null) {
            throw new IllegalArgumentException("Host data was null!");
        }

        if (hostData.getHost() == null) {
            throw new IllegalArgumentException("Host name was null!");
        }

        if (hostData.getDispatcherPrefix() == null) {
            throw new IllegalArgumentException("Dispatcher servlet prefix was null!");
        }

        if (hostData.getRoute() == null) {
            throw new IllegalArgumentException("Route was null!");
        }
    }

}
