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

package com.openexchange.mail.compose.json.rest;

import java.util.EnumMap;
import java.util.Map;
import com.openexchange.ajax.requesthandler.rest.AbstractRestServlet;
import com.openexchange.ajax.requesthandler.rest.Method;
import com.openexchange.ajax.requesthandler.rest.MethodHandler;

/**
 * {@link MailComposeRestServlet} - The REST servlet for mail compose module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailComposeRestServlet extends AbstractRestServlet {

    private static final long serialVersionUID = 531054544302924919L;

    private final transient Map<Method, MethodHandler> handlerMap;

    /**
     * Initializes a new {@link MailComposeRestServlet}.
     * @param prefix The dispatcher servlet prefix
     */
    public MailComposeRestServlet(String prefix) {
        super(prefix);
        final EnumMap<Method, MethodHandler> m = new EnumMap<Method, MethodHandler>(Method.class);
        m.put(Method.GET, new GetMethodHandler());
        m.put(Method.PATCH, new PatchMethodHandler());
        m.put(Method.PUT, new PutMethodHandler());
        m.put(Method.POST, new PostMethodHandler());
        m.put(Method.DELETE, new DeleteMethodHandler());
        handlerMap = m;
    }

    @Override
    public MethodHandler getMethodHandler(final Method method) {
        return handlerMap.get(method);
    }

}
