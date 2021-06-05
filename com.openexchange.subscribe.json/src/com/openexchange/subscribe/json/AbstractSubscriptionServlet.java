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

package com.openexchange.subscribe.json;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AbstractSubscriptionServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public abstract class AbstractSubscriptionServlet extends PermissionServlet {

    private static final long serialVersionUID = -5028531694735004787L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractSubscriptionServlet.class);

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return session.getUserPermissionBits().isSubscription();
    }

    protected void writeOXException(final OXException x, final HttpServletResponse resp, final Session session) {
        getLog().error("", x);
        final Response response = new Response();
        response.setException(x);
        writeResponseSafely(response, resp, session);
    }

    protected void writeData(final Object data, final HttpServletResponse resp, final Session session) {
        final Response response = new Response();
        response.setData(data);
        writeResponseSafely(response, resp, session);
    }

    protected OXException wrapThrowable(final Throwable t) {
        LOG.error("", t);
        return SubscriptionJSONErrorMessages.THROWABLE.create(t, t.getMessage());
    }

    protected void writeResponseSafely(final Response response, final HttpServletResponse resp, final Session session) {
        try {
            writeResponse(response, resp, session);
        } catch (IOException e) {
            getLog().error("", e);
        }
    }

    protected abstract Logger getLog();
}
