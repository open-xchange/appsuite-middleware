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

package com.openexchange.ajax.redirect.actions;

import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.framework.Header.SimpleHeader;

/**
 * {@link RedirectRequest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class RedirectRequest implements AJAXRequest<RedirectResponse> {

    private final String referer;
    private final String location;

    public RedirectRequest(String referer, String location) {
        super();
        this.referer = referer;
        this.location = location;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public String getServletPath() {
        return "/ajax/redirect";
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { new URLParameter("location", location)
        };
    }

    @Override
    public RedirectParser getParser() {
        return new RedirectParser();
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return new Header[] { new SimpleHeader("Referer", referer)
        };
    }
}
