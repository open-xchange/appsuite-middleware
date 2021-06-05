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

import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

/**
 * {@link AJAXRequestHandler} - Handles an AJAX request.
 * @deprecated use {@link AJAXActionService} instead.
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Deprecated
public interface AJAXRequestHandler {

    /**
     * Performs the action indicated through given parameter <code>action</code>.
     *
     * @param action The action to perform
     * @param jsonObject The JSON data object (containing "data", "timestamp", etc.)
     * @param session The session providing needed user data
     * @param ctx The context
     * @return An appropriate result corresponding to request
     * @throws OXException If action cannot be performed
     * @throws JSONException If a JSON error occurs
     */
    public AJAXRequestResult performAction(String action, JSONObject jsonObject, Session session, Context ctx) throws OXException, JSONException;

    /**
     * Gets this request handler's module.
     *
     * @return The module
     */
    public String getModule();

    /**
     * Gets this request handler's supported actions.
     *
     * @return The supported actions
     */
    public Set<String> getSupportedActions();

}
