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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.service.indexing.hazelcast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.attachments.Attachment;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.parser.MailMessageHandler;
import com.openexchange.mail.uuencode.UUEncodedPart;


/**
 * {@link IndexMailHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IndexMailHandler implements MailMessageHandler {
    
    private final String account;
    
    private final String folder;
    
    private final String mailId;
    
    private final List<IndexDocument<Attachment>> attachments;
    

    public IndexMailHandler(String account, String folder, String mailId) {
        super();
        this.account = account;
        this.folder = folder;
        this.mailId = mailId;
        attachments = new ArrayList<IndexDocument<Attachment>>();
    }

    @Override
    public boolean handleFrom(InternetAddress[] fromAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleToRecipient(InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleCcRecipient(InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleBccRecipient(InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleSubject(String subject) throws OXException {
        return true;
    }

    @Override
    public boolean handleSentDate(Date sentDate) throws OXException {
        return true;
    }

    @Override
    public boolean handleReceivedDate(Date receivedDate) throws OXException {
        return true;
    }

    @Override
    public boolean handleHeaders(int size, Iterator<Entry<String, String>> iter) throws OXException {
        return true;
    }

    @Override
    public boolean handlePriority(int priority) throws OXException {
        return true;
    }

    @Override
    public boolean handleMsgRef(String msgRef) throws OXException {
        return true;
    }

    @Override
    public boolean handleDispositionNotification(InternetAddress dispositionNotificationTo, boolean seen) throws OXException {
        return true;
    }

    @Override
    public boolean handleContentId(String contentId) throws OXException {
        return true;
    }

    @Override
    public boolean handleSystemFlags(int flags) throws OXException {
        return true;
    }

    @Override
    public boolean handleUserFlags(String[] userFlags) throws OXException {
        return true;
    }

    @Override
    public boolean handleColorLabel(int colorLabel) throws OXException {
        return true;
    }

    @Override
    public boolean handleInlinePlainText(String plainTextContent, ContentType contentType, long size, String fileName, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleInlineUUEncodedPlainText(String decodedTextContent, ContentType contentType, int size, String fileName, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleInlineUUEncodedAttachment(UUEncodedPart part, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleInlineHtml(String htmlContent, ContentType contentType, long size, String fileName, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleAttachment(MailPart part, boolean isInline, String baseContentType, String fileName, String id) throws OXException {
        if (Attachment.allowedMimeTypes().contains(baseContentType)) {
            Attachment attachment = new Attachment();
            attachment.setModule(Types.EMAIL);
            attachment.setAccount(account);
            attachment.setFolder(folder);
            attachment.setObjectId(mailId);
            attachment.setAttachmentId(id);
            attachment.setFileName(fileName);
            attachment.setMimeType(baseContentType);
            attachment.setContent(part.getInputStream());            
            attachments.add(new StandardIndexDocument<Attachment>(attachment));
        }
        
        return true;
    }

    @Override
    public boolean handleSpecialPart(MailPart part, String baseContentType, String fileName, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleImagePart(MailPart part, String imageCID, String baseContentType, boolean isInline, String fileName, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleMultipart(MailPart mp, int bodyPartCount, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleMultipartEnd(MailPart mp, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleNestedMessage(MailPart mailPart, String id) throws OXException {
        return true;
    }

    @Override
    public void handleMessageEnd(MailMessage mail) throws OXException {
        
    }
    
    public List<IndexDocument<Attachment>> getAttachments() {
        return attachments;
    }

}
