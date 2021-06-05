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

package com.openexchange.mail.mime;

import java.io.IOException;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MessageUtility;


/**
 * {@link ClonedMimeMultipart} - Creates a clone from a given <code>javax.mail.Multipart</code> instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class ClonedMimeMultipart extends MimeMultipart {

    private static final String MULTIPART = "multipart/";

    /**
     * Initializes a new {@link ClonedMimeMultipart}.
     *
     * @param multipart The multipart to clone from
     * @throws OXException If initialization fails
     * @throws IllegalArgumentException If given multipart is <code>null</code>
     */
    public ClonedMimeMultipart(Multipart multipart) throws OXException {
        super();

        if (null == multipart) {
            throw new IllegalArgumentException("multipart must not be null");
        }

        try {
            // First get preamble to ensure Multipart is parsed
            this.preamble = (multipart instanceof MimeMultipart) ? ((MimeMultipart) multipart).getPreamble() : null;
            this.contentType = multipart.getContentType();

            int count = multipart.getCount();
            for (int i = 0; i < count; i++) {
                BodyPart part = multipart.getBodyPart(i);
                if (!(part instanceof MimeBodyPart)) {
                    throw new MessagingException("Unknown part");
                }

                // Determine body part's Content-Type
                ContentType contentType;
                {
                    String[] ct = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
                    if (ct != null && ct.length > 0) {
                        contentType = new ContentType(ct[0]);
                    } else {
                        contentType = new ContentType(MimeTypes.MIME_DEFAULT);
                    }
                }

                // Clone dependent on signaled content
                if (contentType.startsWith(MULTIPART)) {
                    Multipart ex = MimeMessageUtility.getMultipartContentFrom(part, contentType.toString());
                    MimeBodyPart mimeBodyPart = new MimeBodyPart();
                    if (ex instanceof EmptyStringMimeMultipart) {
                        MessageUtility.setContent(ex, mimeBodyPart);
                    } else {
                        MessageUtility.setContent(new ClonedMimeMultipart(ex), mimeBodyPart);
                    }
                    super.addBodyPart(mimeBodyPart);
                } else {
                    super.addBodyPart(MimeMessageUtility.clonePart(part));
                }
            }

            initializeProperties();
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (com.sun.mail.util.MessageRemovedIOException e) {
            throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
        } catch (IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

}
