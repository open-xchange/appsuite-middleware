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
 * {@link AJAXExceptionHandler} - Offers the possibility to handle a thrown {@link OXException} instance.
 *
 * @since v7.8.0
 */
public interface AJAXExceptionHandler {

    /**
     * Invoked when an {@link OXException} instance was thrown
     *
     * @param requestData The associated AJAX request data
     * @param e The actually thrown {@link OXException} instance
     * @param session The associated session
     */
    void exceptionOccurred(AJAXRequestData requestData, OXException e, ServerSession session);

}
