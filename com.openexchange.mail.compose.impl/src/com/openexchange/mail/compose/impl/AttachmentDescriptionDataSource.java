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

package com.openexchange.mail.compose.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.AttachmentDescription;


/**
 * {@link AttachmentDescriptionDataSource} - The data source (for the JavaBeans Activation Framework) backed by an attachment.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class AttachmentDescriptionDataSource implements DataSource {

    private final AttachmentDescription attachmentDescription;
    private final InputStream input;
    private final ThresholdFileHolder fileHolder;
    private final String contentType;

    /**
     * Initializes a new {@link AttachmentDescriptionDataSource}.
     *
     * @param attachmentDescription The attachment description
     * @param input The attachment data
     */
    public AttachmentDescriptionDataSource(AttachmentDescription attachmentDescription, InputStream input) {
        this(attachmentDescription, input, attachmentDescription.getMimeType());
    }

    /**
     * Initializes a new {@link AttachmentDescriptionDataSource} with an explicit MIME type.
     *
     * @param attachment The attachment description
     * @param input The attachment data
     * @param contentType The MIME type
     */
    public AttachmentDescriptionDataSource(AttachmentDescription attachmentDescription, InputStream input, String contentType) {
        super();
        this.attachmentDescription = attachmentDescription;
        this.input = input;
        this.fileHolder = null;
        this.contentType = contentType;
    }

    /**
     * Initializes a new {@link AttachmentDescriptionDataSource} with an explicit MIME type.
     *
     * @param attachment The attachment description
     * @param fileHolder The attachment data
     * @param contentType The MIME type
     */
    public AttachmentDescriptionDataSource(AttachmentDescription attachmentDescription, ThresholdFileHolder fileHolder) {
        super();
        this.attachmentDescription = attachmentDescription;
        this.fileHolder = fileHolder;
        this.input = null;
        if (fileHolder.getContentType() == null) {
            this.contentType = attachmentDescription.getMimeType();
        } else {
            this.contentType = fileHolder.getContentType();
        }
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return fileHolder == null ? input : fileHolder.getStream();
        } catch (OXException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }

            throw new IOException(e.getMessage(), e);
        }
    }


    @Override
    public String getName() {
        return attachmentDescription.getName();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException(this.getClass().getName() + ".getOutputStream() isn't implemented");
    }

}
