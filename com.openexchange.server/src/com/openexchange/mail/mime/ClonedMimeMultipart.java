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
