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

package com.openexchange.messaging.generic.internet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.messaging.BinaryContent;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MimeBinaryContent} - A MIME {@link BinaryContent binary content}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class MimeBinaryContent implements BinaryContent {

    /**
     * The MIME part.
     */
    protected final Part part;

    /**
     * The cached content.
     */
    protected final byte[] cachedContent;

    /**
     * Initializes a new {@link MimeBinaryContent}.
     *
     * @param inputStream The binary content as an input stream; gets closed
     * @throws OXException If an I/O error occurs
     */
    public MimeBinaryContent(final InputStream inputStream) throws OXException {
        super();
        part = null;
        try (ByteArrayOutputStream buffer = new UnsynchronizedByteArrayOutputStream(8192 << 1)) {
            final byte[] buf = new byte[8192];
            int read;
            while ((read = inputStream.read(buf, 0, buf.length)) > 0) {
                buffer.write(buf, 0, read);
            }
            cachedContent = buffer.toByteArray();
        } catch (IOException e) {
            throw MessagingExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(inputStream);
        }
    }


    /**
     * Initializes a new {@link MimeBinaryContent}.
     *
     * @param content The binary content
     */
    public MimeBinaryContent(final byte[] content) {
        super();
        part = null;
        cachedContent = new byte[content.length];
        System.arraycopy(content, 0, cachedContent, 0, content.length);
    }

    /**
     * Initializes a new {@link MimeBinaryContent}.
     *
     * @param part The MIME part
     */
    protected MimeBinaryContent(final Part part) {
        super();
        this.part = part;
        cachedContent = null;
    }

    @Override
    public InputStream getData() throws OXException {
        return getBodyPartInputStream();
    }

    /**
     * Gets MIME part's input stream in a safe manner: If {@link Part#getInputStream()} fails, the {@link MimeBodyPart#getRawInputStream()}
     * is invoked.
     *
     * @return The either decoded or raw input stream
     * @throws OXException If neither input stream nor raw input stream can be returned
     */
    private InputStream getBodyPartInputStream() throws OXException {
        if (cachedContent != null) {
            return new UnsynchronizedByteArrayInputStream(cachedContent);
        }
        if (null == part) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create("No content");
        }
        try {
            return part.getInputStream();
        } catch (IOException e) {
            try {
                if (part instanceof MimeBodyPart) {
                    return ((MimeBodyPart) part).getRawInputStream();
                }
                throw e;
            } catch (javax.mail.MessagingException me) {
                me.setNextException(e);
                throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
            } catch (IOException ioe) {
                throw MessagingExceptionCodes.IO_ERROR.create(ioe, ioe.getMessage());
            }
        } catch (javax.mail.MessagingException e) {
            try {
                if (part instanceof MimeMessage) {
                    return ((MimeMessage) part).getRawInputStream();
                }
                throw e;
            } catch (javax.mail.MessagingException me) {
                me.setNextException(e);
                throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
            }
        }
    }

}
