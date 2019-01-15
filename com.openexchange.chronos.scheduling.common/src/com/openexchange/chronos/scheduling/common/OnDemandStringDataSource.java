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

package com.openexchange.chronos.scheduling.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link OnDemandStringDataSource} - This class provides the possibility to generate strings for mails on demand by
 * implementing the {@link Supplier} interface. String then are only generated when calling {@link Supplier#get()}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class OnDemandStringDataSource implements DataSource {

    private Supplier supplier;
    private ContentType contentType;

    /**
     * Initializes a new {@link OnDemandStringDataSource}.
     * 
     * @param supplier The {@link Supplier} to get a String from. E.g. <code>() -> description.getText()</code>
     * @param contentType The {@link ContentType} of the data source
     * 
     */
    public OnDemandStringDataSource(Supplier supplier, ContentType contentType) {
        super();
        this.supplier = supplier;
        this.contentType = contentType;
    }

    @Override
    public String getContentType() {
        return contentType.toString();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (null == supplier) {
            throw new IOException("Unable to get data.");
        }
        String content;
        try {
            content = supplier.get();
        } catch (OXException e) {
            throw new IOException(e.getMessage(), e);
        }
        if (null == content) {
            throw new IOException("No content to add.");
        }
        return new UnsynchronizedByteArrayInputStream(content.getBytes(contentType.getCharsetParameter()));
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException(this.getClass().getName() + ".getOutputStream() isn't implemented");
    }

    @FunctionalInterface
    interface Supplier {

        /**
         * Generates a single String to add to a mail.
         *
         * @return A {@link String}
         * @throws OXException If getting or generating the String fails.
         */
        String get() throws OXException;
    }

}
