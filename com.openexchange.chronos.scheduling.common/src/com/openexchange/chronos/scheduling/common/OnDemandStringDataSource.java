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

    private final Supplier supplier;
    private final ContentType contentType;
    private byte[] data;

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
        if (null == data) {
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
            data = content.getBytes(contentType.getCharsetParameter());
        }
        return new UnsynchronizedByteArrayInputStream(data);
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
