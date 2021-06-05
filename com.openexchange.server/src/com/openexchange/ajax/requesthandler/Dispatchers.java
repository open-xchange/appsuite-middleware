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
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.annotation.Nullable;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link Dispatchers} - Utility class for dispatched processing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class Dispatchers {

    private static final AtomicReference<DispatcherPrefixService> DPS_REF = new AtomicReference<DispatcherPrefixService>();

    /**
     * Initializes a new {@link Dispatchers}.
     */
    private Dispatchers() {
        super();
    }

    /**
     * Performs specified request using default dispatcher instance.
     *
     * @param requestData The request data
     * @param session The associated session
     * @param ox The dispatcher
     * @return The result object (if any)
     * @throws OXException If execution fails
     */
    public static <V> V perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        return perform(requestData, session, DispatcherServlet.getDispatcher());
    }

    /**
     * Performs specified request with given dispatcher instance.
     *
     * @param requestData The request data
     * @param session The associated session
     * @param dispatcher The dispatcher
     * @return The result object (if any)
     * @throws OXException If execution fails
     */
    public static <V> V perform(AJAXRequestData requestData, ServerSession session, Dispatcher dispatcher) throws OXException {
        AJAXRequestResult requestResult = null;
        Exception exc = null;
        try {
            requestResult = dispatcher.perform(requestData, null, session);
            return (V) requestResult.getResultObject();
        } catch (OXException x) {
            exc = x;
            throw x;
        } catch (RuntimeException x) {
            exc = x;
            throw new OXException(x);
        } finally {
            Dispatchers.signalDone(requestResult, exc);
        }
    }

    /**
     * Triggers post-processing for specified request result.
     *
     * @param requestResult The request result or <code>null</code>
     * @param e The exception that caused termination, or <code>null</code> if execution completed normally
     */
    public static void signalDone(@Nullable AJAXRequestResult requestResult, Exception e) {
        if (null != requestResult) {
            requestResult.signalDone(e);
        }
    }

    /**
     * Gets the action associated with given HTTP request
     *
     * @param req The HTTP request
     * @return The associated action string
     */
    public static String getActionFrom(HttpServletRequest req) {
        String action = req.getParameter(AJAXServlet.PARAMETER_ACTION);
        return null == action ? Strings.toUpperCase(req.getMethod()) : action;
    }

    /**
     * Check if common API response is expected for specified HTTP request
     *
     * @param req The HTTP request to check
     * @return <code>true</code> if common API response is expected; otherwise <code>false</code>
     */
    public static boolean isApiOutputExpectedFor(HttpServletRequest req) {
        String prefix = getPrefix();
        if (req.getRequestURI().startsWith(prefix)) {
            // Common dispatcher action - Try to determine if JSON is expected or not
            AJAXRequestDataTools requestDataTools = AJAXRequestDataTools.getInstance();
            String module = requestDataTools.getModule(prefix, req);
            AJAXActionServiceFactory factory = DispatcherServlet.getDispatcher().lookupFactory(module);
            if (factory != null) {
                return isApiOutputExpectedFor(optActionFor(requestDataTools.getAction(req), factory));
            }
        }
        return true;
    }

    /**
     * Gets the dispatcher's path prefix
     *
     * @return The prefix
     * @throws IllegalStateException If the AJAX framework has not been initialized yet
     */
    public static String getPrefix() {
        DispatcherPrefixService dispatcherPrefixService = DPS_REF.get();
        if (dispatcherPrefixService == null) {
            throw new IllegalStateException(Dispatchers.class.getName() + " has not been initialized. DispatcherPrefixService is not set!");
        }

        return dispatcherPrefixService.getPrefix();
    }

    private static AJAXActionService optActionFor(String sAction, final AJAXActionServiceFactory factory) {
        try {
            return factory.createActionService(sAction);
        } catch (OXException e) {
            return null;
        }
    }

    /**
     * Check if common API response is expected for specified AJAX action.
     *
     * @param action The AJAX action to check
     * @return <code>true</code> if no common API response is expected; otherwise <code>false</code>
     */
    public static boolean isApiOutputExpectedFor(AJAXActionService action) {
        if (null == action) {
            return true;
        }

        if ((action instanceof ETagAwareAJAXActionService) || (action instanceof LastModifiedAwareAJAXActionService)) {
            return false;
        }

        DispatcherNotes dispatcherNotes = action.getClass().getAnnotation(DispatcherNotes.class);
        if (null != dispatcherNotes && "file".equals(dispatcherNotes.defaultFormat())) {
            return false;
        }

        return true;
    }

    /**
     * Only to be used by the AJAX framework initializer!
     */
    public static void setDispatcherPrefixService(DispatcherPrefixService service) {
        DPS_REF.set(service);
    }

}
