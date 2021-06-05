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

package com.openexchange.ajax.response;

import com.openexchange.exception.OXException;


/**
 * {@link IncludeStackTraceService} - Signals for a given user and context tuple if stack trace information shall be included in HTTP-API JSON responses.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IncludeStackTraceService {

    /**
     * Signals for given user and context tuple if stack trace information shall be included in HTTP-API JSON responses
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> to included stack trace information; otherwise <code>false</code>
     * @throws OXException If check fails for any reason
     */
    boolean includeStackTraceOnError(int userId, int contextId) throws OXException;

    /**
     * Checks if including stack trace information is currently enabled for any user.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    boolean isEnabled();

}
