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
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.annotation.Nullable;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;


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
