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

package com.openexchange.jslob.json.action;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.jslob.JSlobService;
import com.openexchange.jslob.json.JSlobRequest;
import com.openexchange.jslob.registry.JSlobServiceRegistry;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link JSlobAction} - Abstract JSlob action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class JSlobAction implements AJAXActionService {

    /**
     * The default service identifier.
     */
    protected static final String DEFAULT_SERVICE_ID = "com.openexchange.jslob.config";

    /**
     * Splits a char sequence by comma-separated (<code>','</code>) values.
     */
    protected static final Pattern SPLIT_CSV = Pattern.compile(" *, *");

    /**
     * Splits a char sequence by slash-separated (<code>'/'</code>) values.
     */
    protected static final Pattern SPLIT_PATH = Pattern.compile(Pattern.quote("/"));

    /**
     * The service look-up
     */
    protected final ServiceLookup services;

    /**
     * Registered actions.
     */
    protected final Map<String, JSlobAction> actions;

    /**
     * Initializes a new {@link JSlobAction}.
     *
     * @param services The service look-up
     */
    protected JSlobAction(final ServiceLookup services, final Map<String, JSlobAction> actions) {
        super();
        this.services = services;
        this.actions = actions;
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            final String action = requestData.getParameter("action");
            if (null == action) {
                final Method method = Method.methodFor(requestData.getAction());
                if (null == method) {
                    throw AjaxExceptionCodes.BAD_REQUEST.create();
                }
                return performREST(new JSlobRequest(requestData, session), method);
            }
            return perform(new JSlobRequest(requestData, session));
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the JSlob service associated with specified JSlob service identifier.
     *
     * @param jslobServiceId The JSlob service identifier
     * @return The JSlob service
     * @throws OXException If JSlob service cannot be returned
     */
    protected JSlobService getJSlobService(final String jslobServiceId) throws OXException {
        final JSlobServiceRegistry registry = services.getService(JSlobServiceRegistry.class);
        if (null == registry) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(JSlobServiceRegistry.class.getName());
        }
        return registry.getJSlobService(jslobServiceId);
    }

    /**
     * Performs given JSlob request.
     *
     * @param jslobRequest The JSlob request
     * @return The AJAX result
     * @throws OXException If performing request fails
     */
    protected abstract AJAXRequestResult perform(JSlobRequest jslobRequest) throws OXException, JSONException;

    /**
     * Performs given JSlob request in REST style.
     *
     * @param jslobRequest The JSlob request
     * @param method The REST method to perform
     * @return The AJAX result
     * @throws OXException If performing request fails for any reason
     * @throws JSONException If a JSON error occurs
     */
    @SuppressWarnings("unused")
    protected AJAXRequestResult performREST(final JSlobRequest jslobRequest, final Method method) throws OXException, JSONException {
        throw AjaxExceptionCodes.BAD_REQUEST.create();
    }

    /**
     * Gets the action identifier for this JSlob action.
     *
     * @return The action identifier; e.g. <code>"get"</code>
     */
    public abstract String getAction();

    /**
     * Gets the REST method identifiers for this JSlob action.
     *
     * @return The REST method identifiers or <code>null</code> (e.g. <code>"GET"</code>)
     */
    public List<Method> getRESTMethods() {
        return Collections.emptyList();
    }

    /** Checks for an empty String */
    protected static boolean isEmpty(final String string) {
        return Strings.isEmpty(string);
    }

}
