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

package com.openexchange.ajax;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

public abstract class PermissionServlet extends SessionServlet {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PermissionServlet.class);

    private static final long serialVersionUID = -1496492688713194989L;

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        Tools.disableCaching(resp);
        try {
            initializeSession(req, resp);
            ServerSession session = getSessionObject(req);
            if (null != session && !hasModulePermission(session)) {
                LOG.info("Status code 403 (FORBIDDEN): No permission to access module.");
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "No Permission");
                return;
            }
            super.service(req, resp);
        } catch (OXException e) {
            handleOXException(e, req, resp);
        }
    }

    /**
     * Indicates if incoming request is allowed to being performed due to permission settings.
     *
     * @param session The session providing needed user data
     * @return <code>true</code> if request is allowed to being performed due to permission settings; <code>false</code> otherwise.
     */
    protected abstract boolean hasModulePermission(final ServerSession session);
}
