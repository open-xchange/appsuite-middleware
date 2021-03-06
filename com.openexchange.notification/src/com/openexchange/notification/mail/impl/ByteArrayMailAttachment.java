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

package com.openexchange.notification.mail.impl;

import java.io.IOException;
import java.io.InputStream;
import javax.activation.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.datasource.MessageDataSource;


/**
 * {@link ByteArrayMailAttachment}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ByteArrayMailAttachment extends AbstractMailAttachment {

    private final byte[] bytes;

    /**
     * Initializes a new {@link ByteArrayMailAttachment}.
     */
    public ByteArrayMailAttachment(byte[] bytes) {
        super();
        this.bytes = bytes;
    }

    @Override
    public DataSource asDataHandler() throws IOException, OXException {
        return new MessageDataSource(getStream(), new ContentType(contentType).getBaseType());
    }

    @Override
    public InputStream getStream() {
        return Streams.newByteArrayInputStream(bytes);
    }

    @Override
    public long getLength() {
        return bytes.length;
    }

}
