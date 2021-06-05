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

package com.openexchange.notification.mail;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;

/**
 * {@link NotificationMailFactory}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface NotificationMailFactory {

    /**
     * Composes a mail messages from the given mail data. The resulting MIME structure is:
     * <pre>
     *  MIME message (1.0)
     *    - multipart/alternative
     *      - text/plain
     *      - [html-part]
     * </pre>
     *
     * The structure of the HTML part depends on the existence of a footer image (configurable
     * via <code>as-config.yml</code>. If no footer image exists, the part is a simple <code>
     * text/html</code> body part. Otherwise it is a <code>multipart/related</code> part, with
     * the primary part being the <code>text/html</code> part and a second part of type <code>
     * image/[sub-type]</code>, containing the footer image.
     *
     * Composed mails will contain the header <code>Auto-Submitted: auto-generated</code>.
     *
     * @param mailData The mail data
     * @return The composed message
     * @throws OXException
     */
    ComposedMailMessage createMail(MailData mailData) throws OXException;

    /**
     * Composes a mail message with attachments from the given mail data. The resulting MIME structure is:
     * <pre>
     * MIME message (1.0)
     * - multipart/alternative
     * - text/plain
     * - [html-part]
     * </pre>
     *
     * The structure of the HTML part depends on the existence of a footer image (configurable
     * via <code>as-config.yml</code>. If no footer image exists, the part is a simple <code>
     * text/html</code> body part. Otherwise it is a <code>multipart/related</code> part, with
     * the primary part being the <code>text/html</code> part and a second part of type <code>
     * image/[sub-type]</code>, containing the footer image.
     *
     * Composed mails will contain the header <code>Auto-Submitted: auto-generated</code>.
     *
     * @param mailData The mail data
     * @param attachments The attachments
     * @return The composed message
     * @throws OXException
     */
    ComposedMailMessage createMail(MailData mailData, Collection<MailAttachment> attachments) throws OXException;

}
