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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.webdav.client.jackrabbit;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.tools.stream.CountingOnlyInputStream;

/**
 * {@link HttpResponseStream}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class HttpResponseStream extends CountingOnlyInputStream {

    /**
     * Initializes a new {@link HttpResponseStream} for given HTTP response.
     *
     * @param response The HTTP response to create the stream for
     * @return The newly created response stream
     * @throws IOException If initialization fails
     */
    public static HttpResponseStream createHttpResponseStreamFor(HttpResponse response) throws IOException {
        if (response == null) {
            return null;
        }

        HttpEntity entity = response.getEntity();
        if (null == entity) {
            throw new IOException("got no response entity");
        }

        long contentLength = entity.getContentLength();
        return new HttpResponseStream(entity.getContent(), contentLength, response);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final HttpResponse response;
    private final long contentLength;

    /**
     * Initializes a new {@link HttpResponseStream}.
     *
     * @param entityStream The response entity's input stream
     * @param contentLength The length of the content, which is the number of bytes of the content, or a negative number if unknown
     * @param response The HTTP response whose entity stream shall be read from
     * @throws IOException If initialization fails
     */
    private HttpResponseStream(InputStream entityStream, long contentLength, HttpResponse response) throws IOException {
        super(entityStream);
        this.response = response;
        this.contentLength = contentLength;
    }

    @Override
    public void close() throws IOException {
        /*
         * close underlying HTTP response if entity not entirely consumed
         */
        try {
            if (0 < contentLength && contentLength > getCount()) {
                // Invoke with consumeEntity=false since stream is closed in finally block
                HttpClients.close(response, false);
            }
        } finally {
            super.close();
        }
    }

}
