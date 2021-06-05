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

package com.openexchange.ajax.session.actions;

import com.openexchange.ajax.AJAXServlet;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RedirectRequest extends AbstractRequest<RedirectResponse> {

    private final String jvmRoute;

    public RedirectRequest(String jvmRoute, String random, String client) {
        super(new Parameter[] { new FieldParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_REDIRECT), new FieldParameter("random", random), new FieldParameter("client", client)
        });
        this.jvmRoute = jvmRoute;
    }

    @Override
    public RedirectResponseParser getParser() {
        return new RedirectResponseParser();
    }

    @Override
    public String getServletPath() {
        return super.getServletPath() + ";jsessionid=abc." + jvmRoute;
    }
}
