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

package com.openexchange.mail.compose;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import com.openexchange.exception.OXException;


/**
 * {@link AttachmentDataSource} - The data source (for the JavaBeans Activation Framework) backed by an attachment.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class AttachmentDataSource implements DataSource {

    private final Attachment attachment;
    private final String contentType;

    /**
     * Initializes a new {@link AttachmentDataSource}.
     *
     * @param attachment The attachment
     */
    public AttachmentDataSource(Attachment attachment) {
        this(attachment, attachment.getMimeType());
    }

    /**
     * Initializes a new {@link AttachmentDataSource} with an explicit MIME type.
     *
     * @param attachment The attachment
     * @param contentType The MIME type
     */
    public AttachmentDataSource(Attachment attachment, String contentType) {
        super();
        this.attachment = attachment;
        this.contentType = contentType;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return attachment.getData();
        } catch (OXException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(e.getMessage(), e);
        }
    }


    @Override
    public String getName() {
        return attachment.getName();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException(this.getClass().getName() + ".getOutputStream() isn't implemented");
    }

}
