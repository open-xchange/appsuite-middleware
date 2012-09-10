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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.zmal.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.zmal.ZmalException;
import com.openexchange.zmal.config.ZmalConfig;
import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.cs.zclient.GetMethodInputStream;
import com.zimbra.cs.zclient.ZMailbox;

/**
 * {@link UrlSink} - Grabs the content from a certain URI.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UrlSink {

    /**
     * Initializes a new {@link UrlSink}.
     */
    private UrlSink() {
        super();
    }

    /**
     * Grabs the content from specified URI.
     * 
     * @param url The URI to content
     * @param config The configuration
     * @param mailbox The mailbox instance
     * @return The grabbed content
     * @throws OXException If operation fails
     */
    public static byte[] getContent(final String url, final ZmalConfig config, final ZMailbox mailbox) throws OXException {
        HttpClient httpClient = null;
        GetMethod get = null;
        try {
            httpClient = mailbox.getHttpClient(new URI(url));
            get = new GetMethod(url);

            final int readTimeout = config.getZmalProperties().getZmalTimeout();
            if (readTimeout > -1) {
                get.getParams().setSoTimeout(readTimeout);
            }

            final int statusCode = HttpClientUtil.executeMethod(httpClient, get);
            if ((statusCode != 200) && (statusCode != 201) && (statusCode != 203) && (statusCode != 206)) {
                throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, statusCode + " - " + get.getStatusText());
            }
            // parse the response
            InputStream in = null;
            try {
                in = new GetMethodInputStream(get);
                final ByteArrayOutputStream out = Streams.newByteArrayOutputStream(8192);
                final byte[] buf = new byte[2048];
                for (int read = in.read(buf, 0, buf.length); read > 0; read = in.read(buf, 0, buf.length)) {
                    out.write(buf, 0, read);
                }
                out.flush();
                return out.toByteArray();
            } finally {
                Streams.close(in);
            }
        } catch (URISyntaxException e) {
            throw MailExceptionCode.URI_PARSE_FAILED.create(e, e.getMessage());
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            if (null != get) {
                get.releaseConnection();
            }
        }
    }

}
