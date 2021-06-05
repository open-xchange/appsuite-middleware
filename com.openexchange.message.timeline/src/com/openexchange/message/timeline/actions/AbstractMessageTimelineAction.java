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
        } catch (JSONException e) {
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
