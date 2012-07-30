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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.messaging.facebook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.BinaryContent;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link FacebookURLConnectionContent} - A {@link BinaryContent} read from an URL.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class FacebookURLConnectionContent implements BinaryContent {

    /**
     * The URL.
     */
    private final URL url;

    /**
     * The content bytes.
     */
    private volatile byte[] bytes;

    /**
     * The MIME type
     */
    private volatile String mimeType;

    /**
     * Initializes a new {@link FacebookURLConnectionContent}.
     *
     * @param url The URL
     * @param stream <code>true</code> to stream the URL's data; otherwise <code>false</code> to hold in memory
     * @throws OXException If initialization fails
     */
    public FacebookURLConnectionContent(final String url, final boolean stream) throws OXException {
        this(toURL(url), stream);
    }

    /**
     * Initializes a new {@link FacebookURLConnectionContent}.
     *
     * @param url The URL
     * @param stream <code>true</code> to stream the URL's data; otherwise <code>false</code> to hold in memory
     * @throws OXException If initialization fails
     */
    public FacebookURLConnectionContent(final URL url, final boolean stream) throws OXException {
        super();
        this.url = url;
        if (stream) {
            init();
        }
    }

    /**
     * Gets the MIME type as indicated by opened {@link URLConnection URL connection}.
     *
     * @return The MIME type
     * @throws OXException If initialization fails
     */
    public String getMimeType() throws OXException {
        String tmp = mimeType;
        if (null == tmp) {
            synchronized (this) {
                tmp = mimeType;
                if (null == tmp) {
                    URLConnection urlCon = null;
                    try {
                        urlCon = url.openConnection();
                        urlCon.setConnectTimeout(10000);
                        urlCon.setReadTimeout(10000);
                        urlCon.connect();
                        mimeType = urlCon.getContentType();
                    } catch (final IOException e) {
                        throw MessagingExceptionCodes.IO_ERROR.create(e, e.getMessage());
                    } finally {
                        closeURLConnection(urlCon);
                    }
                }
            }
        }
        return tmp;
    }

    /**
     * Gets (a copy of) the bytes.
     *
     * @return The (copied) bytes
     * @throws OXException If initialization fails
     */
    public byte[] getBytes() throws OXException {
        byte[] tmp = bytes;
        if (null == tmp) {
            synchronized (this) {
                tmp = bytes;
                if (null == tmp) {
                    init();
                }
            }
        }
        return tmp;
    }

    private void init() throws OXException {
        try {
            final URLConnection urlCon = url.openConnection();
            urlCon.setConnectTimeout(2500);
            urlCon.setReadTimeout(2500);
            urlCon.connect();
            mimeType = urlCon.getContentType();
            final InputStream in = urlCon.getInputStream();
            try {
                final int available = in.available();
                final ByteArrayOutputStream buffer = new UnsynchronizedByteArrayOutputStream(available <= 0 ? 8192 : available);
                transfer(in, buffer);
                bytes = buffer.toByteArray();
            } finally {
                closeStream(in);
            }
        } catch (final SocketTimeoutException e) {
            throw MessagingExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw MessagingExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public InputStream getData() throws OXException {
        final byte[] tmp = bytes;
        if (null == tmp) {
            try {
                final URLConnection urlCon = url.openConnection();
                urlCon.setConnectTimeout(2500);
                urlCon.setReadTimeout(2500);
                urlCon.connect();
                return urlCon.getInputStream();
            } catch (final SocketTimeoutException e) {
                throw MessagingExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } catch (final IOException e) {
                throw MessagingExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }
        return new UnsynchronizedByteArrayInputStream(tmp);
    }

    /*-
     * ----------------------------------------- HELPER -----------------------------------------
     */

    private static void transfer(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[4096];
        int length = -1;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
    }

    private static void closeURLConnection(final URLConnection urlCon) {
        if (null != urlCon) {
            try {
                urlCon.getInputStream().close();
            } catch (final IOException e) {
                com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FacebookURLConnectionContent.class)).error(e.getMessage(), e);
            }
        }
    }

    private static void closeStream(final InputStream in) {
        if (null != in) {
            try {
                in.close();
            } catch (final IOException e) {
                com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FacebookURLConnectionContent.class)).error(e.getMessage(), e);
            }
        }
    }

    private static URL toURL(final String url) throws OXException {
        try {
            return new URL(url);
        } catch (final MalformedURLException e) {
            throw MessagingExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

}
