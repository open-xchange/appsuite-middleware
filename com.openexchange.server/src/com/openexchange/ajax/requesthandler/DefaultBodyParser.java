/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
                    } catch (final JSONException e) {
                        retval.setData(AJAXServlet.readFrom(reader));
                    }
                } else {
                    retval.setData(AJAXServlet.readFrom(reader));
                }
            }
        } catch (final IOException x) {
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
            } catch (final JSONException e) {
                requestData.setData(data);
            }
        }
    }

}
