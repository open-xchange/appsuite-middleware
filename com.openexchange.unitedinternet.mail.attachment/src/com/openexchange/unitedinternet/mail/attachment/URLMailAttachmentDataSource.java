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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.unitedinternet.mail.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataException;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.DataSource;
import com.openexchange.conversion.SimpleData;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.session.Session;

/**
 * {@link URLMailAttachmentDataSource}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class URLMailAttachmentDataSource implements DataSource {

    /**
     * Initializes a new {@link URLMailAttachmentDataSource}.
     */
    public URLMailAttachmentDataSource() {
        super();
    }

    public <D> Data<D> getData(final Class<? extends D> type, final DataArguments dataArguments, final Session session) throws DataException {
        if (!InputStream.class.equals(type)) {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(type.getName());
        }
        URLConnection urlCon = null;
        try {
            URL url;
            {
                final String sUrl = dataArguments.get("url");
                if (null == sUrl) {
                    throw DataExceptionCodes.MISSING_ARGUMENT.create("url");
                }
                url = new URL(sUrl.trim());
            }
            final int timeoutMillis;
            {
                final String sTimeoutMillis = dataArguments.get("timeout");
                if (null == sTimeoutMillis) {
                    timeoutMillis = 2500;
                } else {
                    try {
                        timeoutMillis = Integer.parseInt(sTimeoutMillis.trim());
                    } catch (final NumberFormatException e) {
                        throw DataExceptionCodes.INVALID_ARGUMENT.create("timeout", sTimeoutMillis.trim());
                    }
                }
            }
            /*
             * Open URL connection from parsed URL
             */
            urlCon = url.openConnection();
            urlCon.setConnectTimeout(timeoutMillis);
            urlCon.setReadTimeout(timeoutMillis);
            urlCon.connect();
            /*
             * After successful connect, create data properties instance
             */
            final DataProperties properties = new DataProperties();
            final ContentType contentType;
            {
                final String cts = urlCon.getContentType();
                if (null == cts) {
                    contentType = new ContentType("application/octet-stream");
                } else {
                    contentType = new ContentType(cts);
                }
            }
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, urlCon.getContentType());
            final String charset = contentType.getCharsetParameter();
            if (charset == null) {
                properties.put(DataProperties.PROPERTY_CHARSET, MailProperties.getInstance().getDefaultMimeCharset());
            } else {
                properties.put(DataProperties.PROPERTY_CHARSET, charset);
            }
            properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(urlCon.getContentLength()));
            final ContentDisposition contentDisposition;
            {
                final String cds = urlCon.getHeaderField("Content-Disposition");
                if (null == cds) {
                    contentDisposition = new ContentDisposition("attachment");
                } else {
                    contentDisposition = new ContentDisposition(cds);
                }
            }
            properties.put(DataProperties.PROPERTY_NAME, contentDisposition.getFilenameParameter());
            /*
             * Return data
             */
            return new SimpleData<D>((D) urlCon.getInputStream(), properties);
        } catch (final AbstractOXException e) {
            closeURLConnection(urlCon);
            throw new DataException(e);
        } catch (final IOException e) {
            closeURLConnection(urlCon);
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            closeURLConnection(urlCon);
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

    private static void closeURLConnection(final URLConnection urlCon) {
        if (null != urlCon) {
            try {
                urlCon.getInputStream().close();
            } catch (final IOException e) {
                org.apache.commons.logging.LogFactory.getLog(URLMailAttachmentDataSource.class).error(e.getMessage(), e);
            }
        }
    }

    public String[] getRequiredArguments() {
        return new String[] { "url" };
    }

    public Class<?>[] getTypes() {
        return new Class<?>[] { InputStream.class };
    }

}
