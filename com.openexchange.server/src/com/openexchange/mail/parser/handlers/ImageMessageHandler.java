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
import java.util.Iterator;
import java.util.Map.Entry;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.parser.ContentProvider;
import com.openexchange.mail.parser.MailMessageHandler;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.uuencode.UUEncodedPart;
import com.openexchange.version.Version;

/**
 * {@link ImageMessageHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImageMessageHandler implements MailMessageHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ImageMessageHandler.class);

    private final String cid;

    private MailPart imagePart;

    /**
     * Constructor
     */
    public ImageMessageHandler(final String cid) {
        super();
        if (cid == null || cid.length() == 0) {
            throw new IllegalArgumentException("Image's Content-ID must not be null or empty");
        }
        this.cid = cid;
    }

    /**
     * @return The image part or <code>null</code> if none matching image part has been found
     */
    public MailPart getImagePart() {
        return imagePart;
    }

    @Override
    public boolean handleMultipartEnd(final MailPart mp, final String id) throws OXException {
        return true;
    }

    private static final String IMAGE = "image/";
    private static final String MIME_APPL_OCTET = MimeTypes.MIME_APPL_OCTET;
    private static final String SUFFIX = "@" + Version.NAME;

    @Override
    public boolean handleAttachment(final MailPart part, final boolean isInline, final String baseContentType, final String fileName, final String id) throws OXException {
        if (part.getContentType().startsWith(IMAGE) || part.getContentType().startsWith(MIME_APPL_OCTET)) {
            String cid = part.getContentId();
            if (com.openexchange.java.Strings.isEmpty(cid)) {
                /*
                 * Try to read from headers
                 */
                cid = part.getFirstHeader(MessageHeaders.HDR_CONTENT_ID);
                if (com.openexchange.java.Strings.isEmpty(cid)) {
                    /*
                     * Compare with filename
                     */
                    final String realFilename = MimeMessageUtility.getRealFilename(part);
                    if (MimeMessageUtility.equalsCID(this.cid, realFilename, SUFFIX)) {
                        imagePart = part;
                        return false;
                    }
                    return true;
                }
            }
            if (MimeMessageUtility.equalsCID(this.cid, cid, SUFFIX)) {
                imagePart = part;
                return false;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleBccRecipient(javax.mail.internet.InternetAddress[])
     */
    @Override
    public boolean handleBccRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleCcRecipient(javax.mail.internet.InternetAddress[])
     */
    @Override
    public boolean handleCcRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleColorLabel(int)
     */
    @Override
    public boolean handleColorLabel(final int colorLabel) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleContentId(java.lang.String)
     */
    @Override
    public boolean handleContentId(final String contentId) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleFrom(javax.mail.internet.InternetAddress[])
     */
    @Override
    public boolean handleFrom(final InternetAddress[] fromAddrs) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleHeaders(int, java.util.Iterator)
     */
    @Override
    public boolean handleHeaders(final int size, final Iterator<Entry<String, String>> iter) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleImagePart(com.openexchange.mail.dataobjects.MailPart, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean handleImagePart(final MailPart part, final String imageCID, final String baseContentType, final boolean isInline, final String fileName, final String id) throws OXException {
        if (imageCID == null) {
            /*
             * Compare with filename
             */
            final String realFilename = MimeMessageUtility.getRealFilename(part);
            if (MimeMessageUtility.equalsCID(cid, realFilename)) {
                imagePart = part;
                return false;
            }
            return true;
        } else if (MimeMessageUtility.equalsCID(cid, imageCID)) {
            imagePart = part;
            return false;
        } else {
            /*
             * Compare with filename
             */
            final String realFilename = MimeMessageUtility.getRealFilename(part);
            if (MimeMessageUtility.equalsCID(cid, realFilename)) {
                imagePart = part;
                return false;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleInlineHtml(java.lang.String, com.openexchange.tools.mail.ContentType,
     * long, java.lang.String, java.lang.String)
     */
    @Override
    public boolean handleInlineHtml(final ContentProvider htmlContent, final ContentType contentType, final long size, final String fileName, final String id) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleInlinePlainText(java.lang.String, com.openexchange.tools.mail.ContentType,
     * long, java.lang.String, java.lang.String)
     */
    @Override
    public boolean handleInlinePlainText(final String plainTextContent, final ContentType contentType, final long size, final String fileName, final String id) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleInlineUUEncodedAttachment(com.openexchange.tools.mail.UUEncodedPart,
     * java.lang.String)
     */
    @Override
    public boolean handleInlineUUEncodedAttachment(final UUEncodedPart part, final String id) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleInlineUUEncodedPlainText(java.lang.String,
     * com.openexchange.tools.mail.ContentType, int, java.lang.String, java.lang.String)
     */
    @Override
    public boolean handleInlineUUEncodedPlainText(final String decodedTextContent, final ContentType contentType, final int size, final String fileName, final String id) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleMessageEnd(com.openexchange.mail.dataobjects.MailMessage)
     */
    @Override
    public void handleMessageEnd(final MailMessage mail) throws OXException {
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleMultipart(com.openexchange.mail.dataobjects.MailPart, int,
     * java.lang.String)
     */
    @Override
    public boolean handleMultipart(final MailPart mp, final int bodyPartCount, final String id) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleNestedMessage(com.openexchange.mail.dataobjects.MailMessage,
     * java.lang.String)
     */
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
        final ImageMessageHandler handler = new ImageMessageHandler(cid);
        new MailMessageParser().parseMailMessage(nestedMail, handler, id);
        if (handler.getImagePart() != null) {
            imagePart = handler.getImagePart();
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handlePriority(int)
     */
    @Override
    public boolean handlePriority(final int priority) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleMsgRef(java.lang.String)
     */
    @Override
    public boolean handleMsgRef(final String msgRef) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleDispositionNotification(javax.mail.internet.InternetAddress)
     */
    @Override
    public boolean handleDispositionNotification(final InternetAddress dispositionNotificationTo, final boolean seen) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleReceivedDate(java.util.Date)
     */
    @Override
    public boolean handleReceivedDate(final Date receivedDate) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleSentDate(java.util.Date)
     */
    @Override
    public boolean handleSentDate(final Date sentDate) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleSpecialPart(com.openexchange.mail.dataobjects.MailPart, java.lang.String,
     * java.lang.String)
     */
    @Override
    public boolean handleSpecialPart(final MailPart part, final String baseContentType, final String fileName, final String id) throws OXException {
        return handleAttachment(
            part,
            !Part.ATTACHMENT.equalsIgnoreCase(part.getContentDisposition().getDisposition()),
            baseContentType,
            fileName,
            id);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleSubject(java.lang.String)
     */
    @Override
    public boolean handleSubject(final String subject) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleSystemFlags(int)
     */
    @Override
    public boolean handleSystemFlags(final int flags) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleToRecipient(javax.mail.internet.InternetAddress[])
     */
    @Override
    public boolean handleToRecipient(final InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.parser.MailMessageHandler#handleUserFlags(java.lang.String[])
     */
    @Override
    public boolean handleUserFlags(final String[] userFlags) throws OXException {
        return true;
    }
}
