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

import javax.servlet.http.HttpServletRequest;
import com.openexchange.exception.OXException;

/**
 * {@link BodyParser} - Sets the appropriate body object in a given <code>requestData</code> using specified HTTP request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public interface BodyParser {

    /**
     * Sets the appropriate body object in given <code>requestData</code> using passed HTTP request.
     *
     * @param requestData The AJAX request data
     * @param req The HTTP request
     * @throws OXException If setting the body fails
     */
    void setBody(AJAXRequestData requestData, HttpServletRequest req) throws OXException;

    /**
     * Gets the ranking for this body parser.
     *
     * @return The ranking
     */
    int getRanking();

    /**
     * Signals whether this body parser handles specified request data.
     *
     * @param requestData The AJAX request data
     * @return <code>true</code> if accepted; otherwise <code>false</code>
     */
    boolean accepts(AJAXRequestData requestData);
}
