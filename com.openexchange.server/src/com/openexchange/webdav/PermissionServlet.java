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

package com.openexchange.webdav;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.webdav.OXServlet;

public abstract class PermissionServlet extends OXServlet {

    /**
	 *
	 */
    private static final long serialVersionUID = 2572228529208334966L;

    /**
     * Initializes a new {@link PermissionServlet}.
     */
    protected PermissionServlet() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean useHttpAuth() {
        return false;
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // create a new HttpSession if it's missing
        req.getSession(true);

        if (!super.doAuth(req, resp, getInterface())) {
            return;
        }
        final Session session = getSession(req);
        if (null == session) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "No session found");
            return;
        }
        try {
            final Context ct = ContextStorage.getStorageContext(session.getContextId());
            // No redundant null check.
            if (!hasModulePermission(session, ct)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "No Permission");
                return;
            }
        } catch (OXException ce) {
            return;
        }
        super.service(req, resp);
    }

    protected abstract boolean hasModulePermission(Session session, Context ctx);
}
