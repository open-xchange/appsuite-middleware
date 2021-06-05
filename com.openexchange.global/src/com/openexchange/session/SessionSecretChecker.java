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
import com.openexchange.exception.OXException;

/**
 * {@link SessionSecretChecker} - Callback interface to check a session's secret.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SessionSecretChecker {

    /**
     * Checks if secret matches/aligns to given session.
     *
     * @param session The session looked-up for the incoming request
     * @param req The incoming HTTP request
     * @param source The configured cookie hash source
     * @throws OXException If the secret differs
     */
    void checkSecret(Session session, HttpServletRequest req, String cookieHashSource) throws OXException;

}
