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

package com.openexchange.multiple;

import java.util.Collection;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MultipleHandler} - Handles a multiple request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @deprecated Consider using {@link AJAXActionServiceFactory} framework instead
 */
@Deprecated
public interface MultipleHandler {

    /**
     * The constant for the key to get the data element from passed {@link JSONObject JSON object} instance.
     */
    public static final String DATA = "data";

    /**
     * The constant for the key to get a subpath after the original module definition
     */
    public static final String PATH = "__path";

    /**
     * The constant for the key to get the hostname used in the access.
     */
    public static final String HOSTNAME = "__hostname";

    /**
     * The constant for the key to get the route.
     */
    public static final String ROUTE = "__route";

    /**
     * The constant for the key to get the remote address.
     */
    public static final String REMOTE_ADDRESS = "__remoteAddress";

    /**
     * Performs the multiple request identified by specified action string.
     *
     * @param action The action string denoting the request to perform
     * @param jsonObject The JSON object providing request parameters and/or body
     * @param session The session providing needed user data
     * @param secure <code>true</code> for a secure connection such as HTTPS; otherwise <code>false</code>
     * @return A {@link JSONValue} as a result of the performed request
     * @throws JSONException If a JSON error occurs
     * @throws OXException If performing the request fails
     */
    public Object performRequest(String action, JSONObject jsonObject, ServerSession session, boolean secure) throws JSONException, OXException;

    /**
     * Gets the time stamp when {@link #performRequest()} has been called.
     *
     * @return The time stamp associated with performed request or <code>null</code> if none available
     */
    public Date getTimestamp();

    /**
     * Gets the warnings.
     *
     * @return The warnings
     */
    public Collection<OXException> getWarnings();

    /**
     * Closes/frees all associated resources.
     * <p>
     * Must <b>not</b> throw any (runtime) exception!
     */
    public void close();

}
