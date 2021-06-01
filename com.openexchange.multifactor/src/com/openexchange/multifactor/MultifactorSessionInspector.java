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

package com.openexchange.multifactor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.inspector.Reason;
import com.openexchange.session.inspector.SessionInspectorService;

/**
 * {@link MultifactorSessionInspector}
 * Inspects Session for multifactor flags
 * Throws errors if non-authenticated and multifactor required
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class MultifactorSessionInspector implements SessionInspectorService {

    // Modules not requiring multifactor authentication
    private static final String[] WHITELIST = new String[] { "multifactor/provider", "multifactor/device", "login", "token", "system", "ajax/share" };

    /**
     * Check if path in whitelist
     *
     * @param path The servlet path
     * @return true if it is whitelisted, false otherwise
     */
    private boolean inWhitelist (String path) {
        if (path == null) {
            return false;
        }
        for (String wl : WHITELIST) {
            if (path.endsWith(wl)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Reply onSessionHit(Session session, HttpServletRequest request, HttpServletResponse response) throws OXException {
        if (Boolean.TRUE.equals(session.getParameter(Session.MULTIFACTOR_PARAMETER)) &&
            !(Boolean.TRUE.equals(session.getParameter(Session.MULTIFACTOR_AUTHENTICATED)))) {
            // Multifactor required and not authenticated
            // Check if in whitelist
            if (inWhitelist(request.getServletPath()) || session.containsParameter(Session.PARAM_RESTRICTED)) {
                return Reply.NEUTRAL;
            }
            // Not authorized, throw error
            throw MultifactorExceptionCodes.ACTION_REQUIRES_AUTHENTICATION.create();
        }
        return Reply.NEUTRAL;
    }

    @Override
    public Reply onSessionMiss(String sessionId, HttpServletRequest request, HttpServletResponse response) {
        return Reply.NEUTRAL;
    }

    @Override
    public Reply onAutoLoginFailed(Reason reason, HttpServletRequest request, HttpServletResponse response) {
        return Reply.NEUTRAL;
    }

}
