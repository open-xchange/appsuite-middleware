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

package com.openexchange.url.mail.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.net.ssl.HttpsURLConnection;
import com.google.common.collect.ImmutableSet;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.DataSource;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.java.InetAddresses;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.net.URITools;

/**
 * {@link URLMailAttachmentDataSource}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class URLMailAttachmentDataSource implements DataSource {

    /**
     * 10sec default time out.
     */
    private static final int DEFAULT_TIMEOUT = 10000;

    private final ServiceLookup services;

    private static final String LOCAL_HOST_NAME;
    private static final String LOCAL_HOST_ADDRESS;

    static {
        // Host name initialization
        String localHostName;
        String localHostAddress;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            localHostName = localHost.getCanonicalHostName();
            localHostAddress = localHost.getHostAddress();
        } catch (UnknownHostException e) {
            localHostName = "localhost";
            localHostAddress = "127.0.0.1";
        }
        LOCAL_HOST_NAME = localHostName;
        LOCAL_HOST_ADDRESS = localHostAddress;
    }

    /**
     * Initializes a new {@link URLMailAttachmentDataSource}.
     */
    public URLMailAttachmentDataSource(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public <D> Data<D> getData(final Class<? extends D> type, final DataArguments dataArguments, final Session session) throws OXException {
        if (!InputStream.class.equals(type)) {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(type.getName());
        }
        URLConnection urlCon = null;
        try {
            /*
             * Determine read/connect timeout
             */
            int timeoutMillis;
            {
                final String sTimeoutMillis = dataArguments.get("timeout");
                if (null == sTimeoutMillis) {
                    timeoutMillis = DEFAULT_TIMEOUT;
                } else {
                    try {
                        timeoutMillis = Integer.parseInt(sTimeoutMillis.trim());
                    } catch (NumberFormatException e) {
                        throw DataExceptionCodes.INVALID_ARGUMENT.create("timeout", sTimeoutMillis.trim());
                    }
                }
            }
            /*
             * Get URL
             */
            try {
                String sUrl = dataArguments.get("url");
                if (null == sUrl) {
                    throw DataExceptionCodes.MISSING_ARGUMENT.create("url");
                }
                Function<URLConnection, Optional<OXException>> decorator = con -> {
                    try {
                        if ("https".equalsIgnoreCase(con.getURL().getProtocol())) {
                            SSLSocketFactoryProvider factoryProvider = services.getService(SSLSocketFactoryProvider.class);
                            ((HttpsURLConnection) con).setSSLSocketFactory(factoryProvider.getDefault());
                        }
                        con.setConnectTimeout(timeoutMillis);
                        con.setReadTimeout(timeoutMillis);
                        return Optional.empty();
                    } catch (Exception e) {
                        return Optional.of(DataExceptionCodes.ERROR.create(e, e.getMessage()));
                    }
                };
                urlCon = URITools.getTerminalConnection(sUrl.trim(), Optional.of(VALIDATOR), Optional.of(decorator));
            } catch (MalformedURLException e) {
                throw DataExceptionCodes.ERROR.create(e, e.getMessage());
            }
            /*
             * After successful connect, create data properties instance
             */
            final DataProperties properties = new DataProperties();
            /*
             * Determine content type
             */
            final String sFileName = dataArguments.get("fileName");
            final ContentType contentType;
            {
                final String sCts = dataArguments.get("contentType");
                final String cts = null == sCts ? urlCon.getContentType() : sCts;
                if (null == cts) {
                    if (null == sFileName) {
                        contentType = new ContentType("application/octet-stream");
                    } else {
                        contentType = new ContentType(MimeType2ExtMap.getContentType(sFileName));
                    }
                } else {
                    contentType = new ContentType(cts);
                }
            }
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, contentType.getBaseType());
            /*
             * Determine charset
             */
            final String charset;
            {
                final String sCharset = dataArguments.get("charset");
                if (null == sCharset) {
                    final String tmp = contentType.getCharsetParameter();
                    charset = null == tmp ? MailProperties.getInstance().getDefaultMimeCharset() : tmp;
                } else {
                    charset = sCharset.trim();
                }
            }
            properties.put(DataProperties.PROPERTY_CHARSET, charset);
            /*
             * Determine size
             */
            final String size;
            {
                final String sSize = dataArguments.get("size");
                size = null == sSize ? String.valueOf(urlCon.getContentLength()) : sSize.trim();
            }
            properties.put(DataProperties.PROPERTY_SIZE, size);
            /*
             * Determine disposition & file name
             */
            final String disposition;
            final String fileName;
            {
                final String sDisp = dataArguments.get("disposition");
                final String cds = urlCon.getHeaderField("Content-Disposition");
                final ContentDisposition contentDisposition;
                if (null == cds) {
                    contentDisposition = new ContentDisposition("attachment");
                } else {
                    contentDisposition = new ContentDisposition(cds);
                }
                disposition = null == sDisp ? contentDisposition.getDisposition() : sDisp.trim();
                fileName = null == sFileName ? contentDisposition.getFilenameParameter() : sFileName.trim();
            }
            properties.put(DataProperties.PROPERTY_NAME, fileName);
            properties.put(DataProperties.PROPERTY_DISPOSITION, disposition);
            /*
             * Return data
             */
            return new SimpleData<D>((D) urlCon.getInputStream(), properties);
        } catch (OXException e) {
            /*
             * No closure of URL connection here
             */
            throw e;
        } catch (IOException e) {
            closeURLConnection(urlCon);
            throw DataExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (Exception e) {
            closeURLConnection(urlCon);
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

    private static void closeURLConnection(final URLConnection urlCon) {
        if (null != urlCon) {
            try {
                Streams.close(urlCon.getInputStream());
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private static final Set<String> ALLOWED_PROTOCOLS = ImmutableSet.of("http", "https", "ftp", "ftps");
    private static final Set<String> DENIED_HOSTS = ImmutableSet.of("localhost", "127.0.0.1", LOCAL_HOST_ADDRESS, LOCAL_HOST_NAME);

    /**
     * Validates the given URL according to white-listed protocols and blacklisted hosts.
     *
     * @param url The URL to validate
     * @return An optional OXException
     */
    private static Function<URL, Optional<OXException>> VALIDATOR = (url) -> {
        String protocol = url.getProtocol();
        if (protocol == null || !ALLOWED_PROTOCOLS.contains(Strings.asciiLowerCase(protocol))) {
            return Optional.of(DataExceptionCodes.INVALID_ARGUMENT.create("url", url.toString()));
        }

        String host = Strings.asciiLowerCase(url.getHost());
        if (host == null || DENIED_HOSTS.contains(host)) {
            return Optional.of(DataExceptionCodes.INVALID_ARGUMENT.create("url", url.toString()));
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(url.getHost());
            if (InetAddresses.isInternalAddress(inetAddress)) {
                return Optional.of(DataExceptionCodes.INVALID_ARGUMENT.create("url", url.toString()));
            }
        } catch (UnknownHostException e) {
            return Optional.of(DataExceptionCodes.INVALID_ARGUMENT.create("url", url.toString()));
        }
        return Optional.empty();
    };

    @Override
    public String[] getRequiredArguments() {
        return new String[] { "url" };
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { InputStream.class };
    }

}
