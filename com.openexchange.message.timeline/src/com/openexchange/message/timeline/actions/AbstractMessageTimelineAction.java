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

package com.openexchange.message.timeline.actions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.message.timeline.MessageTimelineExceptionCodes;
import com.openexchange.message.timeline.MessageTimelineRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractMessageTimelineAction} - Abstract message timeline action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractMessageTimelineAction implements AJAXActionService {

    /**
     * The service look-up
     */
    protected final ServiceLookup services;

    /**
     * Registered actions.
     */
    protected final Map<String, AbstractMessageTimelineAction> actions;

    /**
     * Initializes a new {@link AbstractMessageTimelineAction}.
     *
     * @param services The service look-up
     */
    protected AbstractMessageTimelineAction(final ServiceLookup services, final Map<String, AbstractMessageTimelineAction> actions) {
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
                return performREST(new MessageTimelineRequest(requestData, session), method);
            }
            return perform(new MessageTimelineRequest(requestData, session));
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Performs given message timeline request.
     *
     * @param msgTimelineRequest The message timeline request
     * @return The AJAX result
     * @throws OXException If performing request fails
     */
    protected abstract AJAXRequestResult perform(MessageTimelineRequest msgTimelineRequest) throws OXException, JSONException;

    /**
     * Performs given message timeline request in REST style.
     *
     * @param msgTimelineRequest The message timeline request
     * @param method The REST method to perform
     * @return The AJAX result
     * @throws OXException If performing request fails for any reason
     * @throws JSONException If a JSON error occurs
     */
    @SuppressWarnings("unused")
    protected AJAXRequestResult performREST(final MessageTimelineRequest msgTimelineRequest, final Method method) throws OXException, JSONException {
        throw AjaxExceptionCodes.BAD_REQUEST.create();
    }

    /**
     * Gets the action identifier for this message timeline action.
     *
     * @return The action identifier; e.g. <code>"get"</code>
     */
    public abstract String getAction();

    /**
     * Gets the REST method identifiers for this message timeline action.
     *
     * @return The REST method identifiers or <code>null</code> (e.g. <code>"GET"</code>)
     */
    public List<Method> getRESTMethods() {
        return Collections.emptyList();
    }

    /**
     * Checks for client identifier.
     *
     * @param msgTimelineRequest The associated request
     * @return The client identifier
     * @throws OXException If client identifier is missing
     */
    protected String checkClient(final MessageTimelineRequest msgTimelineRequest) throws OXException {
        String client = msgTimelineRequest.getRequestData().getParameter("client");
        if (Strings.isEmpty(client)) {
            client = msgTimelineRequest.getSession().getClient();
            if (Strings.isEmpty(client)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("client");
            }
        }
        return client;
    }

    /**
     * Checks for client identifier.
     *
     * @param session The associated session
     * @return The client identifier
     * @throws OXException If client identifier is missing
     */
    protected String checkClient(final Session session) throws OXException {
        final String client = session.getClient();
        if (Strings.isEmpty(client)) {
            throw MessageTimelineExceptionCodes.NO_CLIENT.create(session.getSessionID());
        }
        return client;
    }

}
