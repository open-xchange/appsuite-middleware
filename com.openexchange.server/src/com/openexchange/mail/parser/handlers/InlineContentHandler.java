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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.mail.parser.handlers;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.parser.ContentProvider;
import com.openexchange.mail.parser.MailMessageHandler;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.uuencode.UUEncodedPart;

/**
 * {@link InlineContentHandler} - Finds matching inline parts to given content IDs
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class InlineContentHandler implements MailMessageHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InlineContentHandler.class);

    private final int size;

    private final List<String> cids;

    private final Map<String, MailPart> inlineContents;

    /**
     * Constructor
     *
     * @param cids The content IDs of the inline parts
     */
    public InlineContentHandler(final List<String> cids) {
        super();
        size = cids.size();
        this.cids = cids;
        inlineContents = new HashMap<String, MailPart>(size);
    }

    /**
     * Private constructor for recursive calls
     *
     * @param cids The content IDs of the inline parts
     * @param inlineContents The container for matching mail parts
     */
    private InlineContentHandler(final List<String> cids, final Map<String, MailPart> inlineContents) {
        super();
        size = cids.size();
        this.cids = cids;
        this.inlineContents = inlineContents;
    }

    /**
     * Gets the found inline contents corresponding to given content IDs. Those inline content which could not be found are set to
     * <code>null</code>.
     *
     * @return The found inline contents
     */
    public Map<String, MailPart> getInlineContents() {
        return inlineContents;
    }

    @Override
    public boolean handleMultipartEnd(final MailPart mp, final String id) throws OXException {
        return true;
    }

    private static final String IMAGE = "image/";

    @Override
    public boolean handleAttachment(final MailPart part, final boolean isInline, final String baseContentType, final String fileName, final String id) throws OXException {
        if (part.getContentType().startsWith(IMAGE)) {
            String partCid = part.getContentId();
            if (partCid == null || partCid.length() == 0) {
                partCid = part.getFirstHeader(MessageHeaders.HDR_CONTENT_ID);
            }
            partCid = partCid == null ? "" : partCid;
            String realFilename = MimeMessageUtility.getRealFilename(part);
            realFilename = realFilename == null ? "" : realFilename;
            if (partCid.length() == 0 && realFilename.length() == 0) {
                return true;
            }
            for (int i = 0; i < size; i++) {
                final String cid = cids.get(i);
                if (MimeMessageUtility.equalsCID(cid, partCid)) {
                    inlineContents.put(cid, part);
                } else if (MimeMessageUtility.equalsCID(cid, realFilename)) {
                    inlineContents.put(cid, part);
                }
            }
            if (inlineContents.size() >= size) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean handleBccRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleCcRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleColorLabel(final int colorLabel) throws OXException {
        return true;
    }

    @Override
    public boolean handleContentId(final String contentId) throws OXException {
        return true;
    }

    @Override
    public boolean handleDispositionNotification(final InternetAddress dispositionNotificationTo, final boolean seen) throws OXException {
        return true;
    }

    @Override
    public boolean handleFrom(final InternetAddress[] fromAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleHeaders(final int size, final Iterator<Entry<String, String>> iter) throws OXException {
        return true;
    }

    @Override
    public boolean handleImagePart(final MailPart part, final String imageCIDArg, final String baseContentType, final boolean isInline, final String fileName, final String id) throws OXException {
        String imageCID = imageCIDArg;
        if (imageCID == null) {
            imageCID = "";
        }
        String realFilename = MimeMessageUtility.getRealFilename(part);
        realFilename = realFilename == null ? "" : realFilename;
        if (imageCID.length() == 0 && realFilename.length() == 0) {
            return true;
        }
        for (int i = 0; i < size; i++) {
            final String cid = cids.get(i);
            if (MimeMessageUtility.equalsCID(cid, imageCID)) {
                inlineContents.put(cid, part);
            } else if (MimeMessageUtility.equalsCID(cid, realFilename)) {
                inlineContents.put(cid, part);
            }
        }
        return (inlineContents.size() < size);
    }

    @Override
    public boolean handleInlineHtml(final ContentProvider htmlContent, final ContentType contentType, final long size, final String fileName, final String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleInlinePlainText(final String plainTextContent, final ContentType contentType, final long size, final String fileName, final String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleInlineUUEncodedPlainText(final String decodedTextContent, final ContentType contentType, final int size, final String fileName, final String id) throws OXException {
        return true;
    }

    @Override
    public void handleMessageEnd(final MailMessage mail) throws OXException {

    }

    @Override
    public boolean handleMsgRef(final String msgRef) throws OXException {
        return true;
    }

    @Override
    public boolean handleMultipart(final MailPart mp, final int bodyPartCount, final String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleNestedMessage(final MailPart mailPart, final String id) throws OXException {
        final Object content = mailPart.getContent();
        final MailMessage nestedMail;
        if (content instanceof MailMessage) {
            nestedMail = (MailMessage) content;
        } else if (content instanceof InputStream) {
            try {
                nestedMail = MimeMessageConverter.convertMessage(new MimeMessage(
                    MimeDefaultSession.getDefaultSession(),
                    (InputStream) content));
            } catch (final MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            }
        } else {
            LOG.error("Ignoring nested message. Cannot handle part's content which should be a RFC822 message according to its content type: {}", (null == content ? "null" : content.getClass().getSimpleName()));
            return true;
        }
        final InlineContentHandler handler = new InlineContentHandler(cids, inlineContents);
        new MailMessageParser().parseMailMessage(nestedMail, handler, id);
        return (inlineContents.size() < size);
    }

    @Override
    public boolean handlePriority(final int priority) throws OXException {
        return true;
    }

    @Override
    public boolean handleReceivedDate(final Date receivedDate) throws OXException {
        return true;
    }

    @Override
    public boolean handleSentDate(final Date sentDate) throws OXException {

        return true;
    }

    @Override
    public boolean handleSpecialPart(final MailPart part, final String baseContentType, final String fileName, final String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleSubject(final String subject) throws OXException {
        return true;
    }

    @Override
    public boolean handleSystemFlags(final int flags) throws OXException {
        return true;
    }

    @Override
    public boolean handleToRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleUserFlags(final String[] userFlags) throws OXException {
        return true;
    }

}
