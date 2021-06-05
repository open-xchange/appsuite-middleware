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

package com.openexchange.ajax.requesthandler.responseRenderers;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResponseRenderer;

/**
 * {@link StringResponseRenderer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class StringResponseRenderer implements ResponseRenderer {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(StringResponseRenderer.class);

    /**
     * Initializes a new {@link StringResponseRenderer}.
     */
    public StringResponseRenderer() {
        super();
    }

    @Override
    public int getRanking() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean handles(final AJAXRequestData request, final AJAXRequestResult result) {
        return true;
    }

    @Override
    public void write(final AJAXRequestData request, final AJAXRequestResult result, final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        /*
         * Write headers
         */
        final Map<String, String> headers = result.getHeaders();
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            resp.setHeader(entry.getKey(), entry.getValue());
        }
        /*
         * Write output to OutputStream
         */
        try {
            final Object resultObject = result.getResultObject();
            resp.getWriter().write(resultObject == null ? "" : resultObject.toString());
        } catch (RuntimeException e) {
            LOG.error("", e);
        }
    }

}
