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

package com.openexchange.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;

/**
 * {@link SessionSsoProvider} - Checks if a given session has been spawned by an SSO system.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public interface SessionSsoProvider {

    /**
     * Checks whether processing of an inbound {@code /login?action=login} request shall be skipped, to
     * keep any session cookies and potentially perform some SSO mechanism specific actions during subsequent
     * HTTP requests.
     * <p>
     * If {@code false} is returned, the processing of the request continues as usual. Otherwise it is handled
     * as if an {@code AjaxExceptionCodes.DISABLED_ACTION} would have been thrown.
     *
     * @param request The inbound HTTP request
     * @param response The according HTTP response
     * @return {@code true} to skip further auto-login processing of the core login handler, {@code false} to
     *         continue as usual
     * @throws OXException If check fails; the processing is then continued as if {@code false} was returned
     */
    boolean skipAutoLoginAttempt(HttpServletRequest request, HttpServletResponse response) throws OXException;

    /**
     * Checks if given session has been spawned by an SSO system.
     *
     * @param session The session to check
     * @return <code>true</code> if spawned by an SSO system; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isSsoSession(Session session) throws OXException;

}
