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

package com.openexchange.jslob.json;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link JSlobRequest} - A JSlob request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JSlobRequest {

    private final AJAXRequestData requestData;

    private final ServerSession session;

    /**
     * Initializes a new {@link JSlobRequest}.
     *
     * @param requestData The AJAX request data
     * @param session The session
     */
    public JSlobRequest(final AJAXRequestData requestData, final ServerSession session) {
        super();
        this.requestData = requestData;
        this.session = session;
    }

    public int getContextId() {
        return session.getContextId();
    }

    public int getUserId() {
        return session.getUserId();
    }

    /**
     * Checks for presence of specified parameter.
     *
     * @param name The parameter name
     * @return <code>true</code> if such a parameter exists; otherwise <code>false</code> if absent
     */
    public boolean containsParameter(final String name) {
        return requestData.containsParameter(name);
    }

    /**
     * Gets the value mapped to given parameter name.
     *
     * @param name The parameter name
     * @return The value mapped to given parameter name
     * @throws NullPointerException If name is <code>null</code>
     * @throws OXException If no such parameter exists
     */
    public String checkParameter(final String name) throws OXException {
        return requestData.checkParameter(name);
    }

    /**
     * Tries to get a parameter value as parsed as a certain type
     *
     * @param name The parameter name
     * @param coerceTo The type the parameter should be interpreted as
     * @param optional Whether the parameter is optional
     * @return The coerced value
     * @throws OXException if coercion fails
     */
    public <T> T getParameter(final String name, @NonNull Class<T> coerceTo, boolean optional) throws OXException {
        return requestData.getParameter(name, coerceTo, optional);
    }
    
    /**
     * Gets the parsed <code>int</code> value of denoted parameter.
     *
     * @param name The parameter name
     * @return The parsed <code>int</code> value
     * @throws OXException If parameter is missing or not a number.
     */
    public int getIntParameter(final String name) throws OXException {
        return requestData.getParameter(name, int.class).intValue();
    }

    /**
     * Gets the associated session.
     *
     * @return The session
     */
    public ServerSession getSession() {
        return session;
    }

    /**
     * Gets the AJAX request data
     *
     * @return The AJAX request data
     */
    public AJAXRequestData getRequestData() {
        return requestData;
    }

}
