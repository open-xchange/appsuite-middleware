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
import java.io.Reader;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.UnsynchronizedPushbackReader;
import com.openexchange.java.UnsynchronizedStringReader;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link DefaultBodyParser} - The default body parser.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public class DefaultBodyParser implements BodyParser {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultBodyParser.class);

    private static final DefaultBodyParser INSTANCE = new DefaultBodyParser();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static DefaultBodyParser getInstance() {
        return INSTANCE;
    }

    // ---------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link DefaultBodyParser}.
     */
    protected DefaultBodyParser() {
        super();
    }

    @Override
    public int getRanking() {
        return 0;
    }

    @Override
    public boolean accepts(final AJAXRequestData requestData) {
        return true;
    }

    @Override
    public void setBody(final AJAXRequestData retval, final HttpServletRequest req) throws OXException {
        UnsynchronizedPushbackReader reader = null;
        try {
            reader = new UnsynchronizedPushbackReader(hookGetReaderFor(req));
            int read = reader.read();
            if (read < 0) {
                hookTrySetDataByParameter(req, retval);
            } else {
                // Skip whitespaces
                while (Strings.isWhitespace((char) read)) {
                    read = reader.read();
                    if (read < 0) {
                        hookTrySetDataByParameter(req, retval);
                        Streams.close(reader);
                        reader = null;
                        return;
                    }
                }
                // Check first non-whitespace character
                final char c = (char) read;
                reader.unread(c);
                if ('[' == c || '{' == c) {
                    try {
                        retval.setData(JSONObject.parse(reader));
                    } catch (JSONException e) {
                        throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create(e, new Object[0]);
                    }
                } else {
                    retval.setData(AJAXServlet.readFrom(reader));
                }
            }
        } catch (IOException x) {
            hookHandleIOException(x);
        } finally {
            Streams.close(reader);
        }
    }

    /**
     * Handles given I/O error.
     *
     * @param ioe The I/O error
     * @throws OXException
     */
    protected void hookHandleIOException(final IOException ioe) throws OXException {
        LOG.debug("", ioe);
    }

    /**
     * Gets the reader for HTTP request's input stream.
     *
     * @param req The HTTP request
     * @return The reader
     * @throws IOException If an I/O error occurs
     */
    protected Reader hookGetReaderFor(final HttpServletRequest req) throws IOException {
        return AJAXServlet.getReaderFor(req);
    }

    /**
     * Attempts to set body by URL parameter.
     *
     * @param req The HTTP request
     * @param requestData The AJAX request data
     */
    protected void hookTrySetDataByParameter(final HttpServletRequest req, final AJAXRequestData requestData) {
        requestData.setData(null);
        final String data = req.getParameter("data");
        if (data != null && data.length() > 0) {
            try {
                final char c = data.charAt(0);
                if ('[' == c || '{' == c) {
                    requestData.setData(JSONObject.parse(new UnsynchronizedStringReader(data)));
                } else {
                    requestData.setData(data);
                }
            } catch (JSONException e) {
                requestData.setData(data);
            }
        }
    }

}
