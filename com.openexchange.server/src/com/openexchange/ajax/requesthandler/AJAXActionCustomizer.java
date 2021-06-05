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

import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AJAXActionCustomizer} - Customizes a specified request data and result pair.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface AJAXActionCustomizer {

    /**
     * Invoked prior to handling the request data with {@link AJAXActionService#perform(AJAXRequestData, ServerSession)}.
     *
     * @param requestData The request data
     * @param session The associated session
     * @return The possibly modified request data which is then passed to {@link AJAXActionService#perform(AJAXRequestData, ServerSession)}
     * @throws OXException If modifying the request data fails
     */
    AJAXRequestData incoming(AJAXRequestData requestData, ServerSession session) throws OXException;

    /**
     * Invoked after {@link AJAXActionService#perform(AJAXRequestData, ServerSession)} is performed.
     *
     * @param requestData The request data
     * @param result The result of {@link AJAXActionService#perform(AJAXRequestData, ServerSession)} invocation
     * @param session The associated session
     * @return The possibly modified request result
     * @throws OXException If modifying the result fails
     */
    AJAXRequestResult outgoing(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session) throws OXException;
}
