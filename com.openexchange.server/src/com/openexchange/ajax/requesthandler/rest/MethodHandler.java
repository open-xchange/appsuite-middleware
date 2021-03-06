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

package com.openexchange.ajax.requesthandler.rest;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;

/**
 * Handles requests in a REST-like manner for a certain HTTP method; like <code>GET</code>, <code>PUT</code>, ...
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MethodHandler {

    /**
     * Modifies specified {@link AJAXRequestData} instance in a way that the appropriate Dispatcher call is executed.
     *
     * @param requestData The request data to modify
     * @param restRequest The REST request
     * @return An appropriate {@link AJAXRequestData} instance routing to the right Dispatcher call
     * @throws IOException If an I/O error occurs
     * @throws OXException If an Open-Xchange error occurs
     */
    AJAXRequestData modifyRequest(AJAXRequestData requestData, HttpServletRequest restRequest) throws IOException, OXException;

    /**
     * Gets the module identifier.
     *
     * @return The module identifier
     */
    String getModule();

    /**
     * Gets the action identifier dependent n given extra path information.
     *
     * @param restPathElements The extra path information or <code>null</code>
     * @param restRequest The REST request
     * @return The action identifier
     */
    String getAction(String[] restPathElements, HttpServletRequest restRequest);

}
