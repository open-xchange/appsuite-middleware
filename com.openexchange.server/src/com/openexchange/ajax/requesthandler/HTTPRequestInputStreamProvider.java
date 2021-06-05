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

package com.openexchange.ajax.requesthandler;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.requesthandler.AJAXRequestData.InputStreamProvider;

/**
 * {@link HTTPRequestInputStreamProvider} - The <tt>InputStreamProvider</tt> backed by a <tt>HttpServletRequest</tt> instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HTTPRequestInputStreamProvider implements InputStreamProvider {

    private final HttpServletRequest req;

    /**
     * Initializes a new {@link HTTPRequestInputStreamProvider}.
     *
     * @param req The Servlet's HTTP request
     */
    public HTTPRequestInputStreamProvider(final HttpServletRequest req) {
        super();
        this.req = req;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return req.getInputStream();
    }
}
