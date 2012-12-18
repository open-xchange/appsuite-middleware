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

package com.openexchange.ajax.requesthandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.RequestConstants;
import com.openexchange.exception.OXException;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MultipleAdapter} maps the {@link MultipleHandler} to several {@link AJAXActionService}s. This class is not thread safe because it
 * has to remember the {@link AJAXRequestResult} between calling {@link #performRequest(String, JSONObject, ServerSession, boolean)} and
 * {@link #getTimestamp()} methods.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MultipleAdapter implements MultipleHandler {

    private final AJAXActionServiceFactory factory;

    private final AtomicReference<AJAXRequestResult> result;

    /**
     * Initializes a new {@link MultipleAdapter}.
     *
     * @param factory The factory used to create {@link AJAXActionService} instances
     */
    public MultipleAdapter(final AJAXActionServiceFactory factory) {
        super();
        this.factory = factory;
        result = new AtomicReference<AJAXRequestResult>();
    }

    /**
     * The pattern to split by commas.
     */
    private static final Pattern SPLIT_CSV = Pattern.compile("\\s*,\\s*");

    public static AJAXRequestData parse(String module, String path, String action, final JSONObject jsonObject, final ServerSession session, final boolean secure) throws JSONException {
        final AJAXRequestData request = new AJAXRequestData();
        request.setSecure(secure);

        /*
         * Check for decorators
         */
        if (jsonObject.hasAndNotNull("decorators")) {
            final String parameter = jsonObject.getString("decorators");
            if (null != parameter) {
                for (final String id : SPLIT_CSV.split(parameter, 0)) {
                    request.addDecoratorId(id.trim());
                }
            }
        }

        request.setHostname(jsonObject.getString(HOSTNAME));
        request.setRoute(jsonObject.getString(ROUTE));
        request.setRemoteAddress(jsonObject.getString(REMOTE_ADDRESS));

        for (final Entry<String, Object> entry : jsonObject.entrySet()) {
            final String name = entry.getKey();
            if (RequestConstants.DATA.equals(name)) {
                request.setData(entry.getValue());
            } else {
            	final Object value = entry.getValue();
                if (value != null && value != JSONObject.NULL) {
                    request.putParameter(name, entry.getValue().toString());
            	}
            }
        }
        if (path.startsWith("/")) {
        	path = path.substring(1);
        }
        request.setModule(module);
        request.setFormat("json");
        request.setAction(action);
        request.setServletRequestURI(path);
        request.setPathInfo(path);

        return request;
    }

    @Override
    public Object performRequest(final String action, final JSONObject jsonObject, final ServerSession session, final boolean secure) throws JSONException, OXException {
        final AJAXActionService actionService = factory.createActionService(action);
        if (null == actionService) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create( action);
        }
        final AJAXRequestData request = parse("", "", action, jsonObject, session, secure);
        final AJAXRequestResult requestResult;
        try {
            requestResult = actionService.perform(request, session);
        } catch (final IllegalStateException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        result.set(requestResult);
        return requestResult.getResultObject();
    }

    @Override
    public Date getTimestamp() {
        final AJAXRequestResult result = this.result.get();
        if (null == result) {
            return null;
        }
        final Date timestamp = result.getTimestamp();
        return null == timestamp ? null : new Date(timestamp.getTime());
    }

    @Override
    public void close() {
        result.set(null);
    }

    @Override
    public Collection<OXException> getWarnings() {
        if (null == result.get()) {
            return Collections.<OXException> emptySet();
        }
        return result.get().getWarnings();
    }

}
