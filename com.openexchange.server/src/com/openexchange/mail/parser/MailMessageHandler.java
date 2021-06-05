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

package com.openexchange.mail.parser;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.uuencode.UUEncodedPart;

/**
 * {@link MailMessageHandler} - This interface declares the <code>handleXXX</code> methods which are invoked by the
 * {@link MailMessageParser} instance on certain parts of a message.
 * <p>
 * Each methods returns a boolean value which indicates whether the underlying {@link MailMessageParser} instance should proceed or quit
 * message parsing after method invocation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MailMessageHandler {

    /**
     * Handle the 'From' message header
     */
    public boolean handleFrom(InternetAddress[] fromAddrs) throws OXException;

    /**
     * Handle the 'To' recipient message header
     */
    public boolean handleToRecipient(InternetAddress[] recipientAddrs) throws OXException;

    /**
     * Handle the 'Cc' recipient message header
     */
    public boolean handleCcRecipient(InternetAddress[] recipientAddrs) throws OXException;

    /**
     * Handle the 'Bcc' recipient message header
     */
    public boolean handleBccRecipient(InternetAddress[] recipientAddrs) throws OXException;

    /**
     * Handle message's subject
     */
    public boolean handleSubject(String subject) throws OXException;

    /**
     * Handle message's sent date
     */
    public boolean handleSentDate(Date sentDate) throws OXException;

    /**
     * Handle message's received date
     */
    public boolean handleReceivedDate(Date receivedDate) throws OXException;

    /**
     * Handle those message headers which cannot be handled through a <code>handleXXX</code> method
     *
     * @param size The iterator's size or <code>-1</code> to use {@link Iterator#hasNext()} instead
     * @param iter The header iterator
     * @return <code>true</code> to continue parsing; otherwise <code>false</code>
     * @throws OXException
     */
    public boolean handleHeaders(int size, Iterator<Map.Entry<String, String>> iter) throws OXException;

    /**
     * Handle message's priority
     */
    public boolean handlePriority(int priority) throws OXException;

    /**
     * Handle referenced mail
     */
    public boolean handleMsgRef(String msgRef) throws OXException;

    /**
     * Handle message's disposition notification
     *
     * @param dispositionNotificationTo The E-Mail address to which disposition notification is to be sent
     * @param acknowledged Whether disposition notification has already been sent (acknowledged)
     * @throws If handling disposition notification fails
     */
    public boolean handleDispositionNotification(InternetAddress dispositionNotificationTo, boolean acknowledged) throws OXException;

    /**
     * Handle content id
     */
    public boolean handleContentId(String contentId) throws OXException;

    /**
     * Handle message's system flags (//SEEN, //ANSWERED, ...)
     */
    public boolean handleSystemFlags(int flags) throws OXException;

    /**
     * Handle message's user flags
     */
    public boolean handleUserFlags(String[] userFlags) throws OXException;

    /**
     * Handle message's color label
     */
    public boolean handleColorLabel(int colorLabel) throws OXException;

    /**
     * Handle a plain text inline part (either <code>text/plain</code> or <code>text/enriched</code>)
     */
    public boolean handleInlinePlainText(String plainTextContent, ContentType contentType, long size, String fileName, String id) throws OXException;

    /**
     * Handle a UUEncoded plain text inline part
     */
    public boolean handleInlineUUEncodedPlainText(String decodedTextContent, ContentType contentType, int size, String fileName, String id) throws OXException;

    /**
     * Handle a UUEncoded file attachment inline part
     */
    public boolean handleInlineUUEncodedAttachment(UUEncodedPart part, String id) throws OXException;

    /**
     * Handle a html inline part (<code>text/html</code>)
     */
    public boolean handleInlineHtml(ContentProvider htmlContent, ContentType contentType, long size, String fileName, String id) throws OXException;

    /**
     * Handle an attachment part (any non-inline parts and file attachments)
     */
    public boolean handleAttachment(MailPart part, boolean isInline, String baseContentType, String fileName, String id) throws OXException;

    /**
     * Handle special parts. A special part is either of MIME type <code>message/delivery-status</code>,
     * <code>message/disposition-notification</code>, <code>text/rfc822-headers</code>, <code>text/x-vcard</code>, <code>text/vcard</code>,
     * <code>text/calendar</code> or <code>text/x-vCalendar</code>
     */
    public boolean handleSpecialPart(MailPart part, String baseContentType, String fileName, String id) throws OXException;

    /**
     * Handle an image part (<code>image/*</code>)
     */
    public boolean handleImagePart(MailPart part, String imageCID, String baseContentType, boolean isInline, String fileName, String id) throws OXException;

    /**
     * Handle a multipart (<code>multipart/*</code>)
     */
    public boolean handleMultipart(MailPart mp, int bodyPartCount, String id) throws OXException;

    /**
     * Handle end of a multipart (<code>multipart/*</code>)
     */
    public boolean handleMultipartEnd(MailPart mp, String id) throws OXException;

    /**
     * Handle a nested message (<code>message/rfc822</code>)
     * <p>
     * Get the message via:
     *
     * <pre>
     *
     * MailMessage nestedMail = (MailMessage) mailPart.getContent();
     * </pre>
     */
    public boolean handleNestedMessage(MailPart mailPart, String id) throws OXException;

    /**
     * Perform some optional finishing operations
     */
    public void handleMessageEnd(MailMessage mail) throws OXException;
}
