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

package com.openexchange.mail.parser.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.UUEncodedAttachmentMailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.parser.ContentProvider;
import com.openexchange.mail.parser.MailMessageHandler;
import com.openexchange.mail.uuencode.UUEncodedPart;

/**
 * {@link NonInlineForwardPartHandler} - Gathers all occurring non-inline parts in a mail and makes them accessible through
 * {@link #getNonInlineParts()}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NonInlineForwardPartHandler implements MailMessageHandler {

    private final List<MailPart> nonInlineParts;
    private final Set<String> imageContentIds;
    private boolean iCalendarContent;

    /**
     * Initializes a new {@link NonInlineForwardPartHandler}
     */
    public NonInlineForwardPartHandler() {
        super();
        iCalendarContent = false;
        nonInlineParts = new ArrayList<MailPart>();
        imageContentIds = new HashSet<String>(1);
    }

    /**
     * Initializes a new {@link NonInlineForwardPartHandler}
     *
     * @param nonInlineParts The container for non-inline parts
     */
    public NonInlineForwardPartHandler(List<MailPart> nonInlineParts) {
        super();
        iCalendarContent = false;
        this.nonInlineParts = nonInlineParts;
        imageContentIds = new HashSet<String>(1);
    }

    /**
     * Checks if calendar content has been detected
     *
     * @return <code>true</code> if calendar content has been detected; otherwise <code>false</code>
     */
    public boolean hasCalendarContent() {
        return iCalendarContent;
    }

    /**
     * @return The gathered non-inline parts
     */
    public List<MailPart> getNonInlineParts() {
        return nonInlineParts;
    }

    /**
     * Sets the image content IDs.
     *
     * @param imageContentIds The content IDs
     */
    public void setImageContentIds(Collection<String> imageContentIds) {
        this.imageContentIds.clear();
        this.imageContentIds.addAll(imageContentIds);
    }

    @Override
    public boolean handleMultipartEnd(MailPart mp, String id) throws OXException {
        return true;
    }

    private static final String APPLICATION = "application/";

    @Override
    public boolean handleAttachment(MailPart part, boolean isInline, String baseContentType, String fileName, String id) throws OXException {
        if (!isInline || part.getContentDisposition().containsFilenameParameter() || part.getHeader(MessageHeaders.HDR_CONTENT_ID) != null || part.getContentType().startsWith(APPLICATION)) {
            if (!part.containsSequenceId()) {
                part.setSequenceId(id);
            }
            nonInlineParts.add(part);
        }
        return true;
    }

    @Override
    public boolean handleBccRecipient(InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleCcRecipient(InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleColorLabel(int colorLabel) throws OXException {
        return true;
    }

    @Override
    public boolean handleContentId(String contentId) throws OXException {
        return true;
    }

    @Override
    public boolean handleDispositionNotification(InternetAddress dispositionNotificationTo, boolean acknowledged) throws OXException {
        return true;
    }

    @Override
    public boolean handleFrom(InternetAddress[] fromAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleHeaders(int size, Iterator<Entry<String, String>> iter) throws OXException {
        return true;
    }

    @Override
    public boolean handleImagePart(MailPart part, String imageCID, String baseContentType, boolean isInline, String fileName, String id) throws OXException {
        if (imageCID == null && (!isInline || part.getContentDisposition().containsFilenameParameter())) {
            if (!part.containsSequenceId()) {
                part.setSequenceId(id);
            }
            nonInlineParts.add(part);
        } else if (imageCID != null && !imageContentIds.contains(prepareContentId(imageCID))) {
            /*
             * Image is not referenced in HTML content
             */
            if (!part.containsSequenceId()) {
                part.setSequenceId(id);
            }
            nonInlineParts.add(part);
        }
        /*-
         * Previous code
         *
        if (!isInline || imageCID != null || part.getContentDisposition().containsFilenameParameter()) {
            // Add if disposition is non-inline or a content ID is present
            nonInlineParts.add(part);
        }
         */
        return true;
    }

    private static String prepareContentId(String contentId) {
        if (null == contentId) {
            return null;
        }
        String cid = contentId;
        if (cid.charAt(0) == '<') {
            cid = cid.substring(1);
        }
        final int lastIndex = cid.length() - 1;
        if (cid.charAt(lastIndex) == '>') {
            cid = cid.substring(0, lastIndex);
        }
        return cid;
    }

    @Override
    public boolean handleInlineHtml(ContentProvider htmlContent, ContentType contentType, long size, String fileName, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleInlinePlainText(String plainTextContent, ContentType contentType, long size, String fileName, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleInlineUUEncodedAttachment(UUEncodedPart part, String id) throws OXException {
        final MailPart mailPart = new UUEncodedAttachmentMailPart(part);
        String ct = MimeType2ExtMap.getContentType(part.getFileName());
        if (ct == null || ct.length() == 0) {
            ct = MimeTypes.MIME_APPL_OCTET;
        }
        mailPart.setContentType(ct);
        mailPart.setSize(part.getFileSize());
        mailPart.setFileName(part.getFileName());
        mailPart.setSequenceId(id);
        nonInlineParts.add(mailPart);
        return true;
    }

    @Override
    public boolean handleInlineUUEncodedPlainText(String decodedTextContent, ContentType contentType, int size, String fileName, String id) throws OXException {
        return true;
    }

    @Override
    public void handleMessageEnd(MailMessage mail) throws OXException {
        // Nothing to do
    }

    @Override
    public boolean handleMsgRef(String msgRef) throws OXException {
        return true;
    }

    @Override
    public boolean handleMultipart(MailPart mp, int bodyPartCount, String id) throws OXException {
        return true;
    }

    @Override
    public boolean handleNestedMessage(MailPart mailPart, String id) throws OXException {
        /*
         * Force to add as attachment
         */
        mailPart.getContentDisposition().setDisposition(Part.ATTACHMENT);
        if (!mailPart.containsSequenceId()) {
            mailPart.setSequenceId(id);
        }
        nonInlineParts.add(mailPart);
        // final NonInlineForwardPartHandler handler = new
        // NonInlineForwardPartHandler(nonInlineParts);
        // new MailMessageParser().parseMailMessage(nestedMail, handler, id);
        return true;
    }

    @Override
    public boolean handlePriority(int priority) throws OXException {
        return true;
    }

    @Override
    public boolean handleReceivedDate(Date receivedDate) throws OXException {
        return true;
    }

    @Override
    public boolean handleSentDate(Date sentDate) throws OXException {
        return true;
    }

    @Override
    public boolean handleSpecialPart(MailPart part, String baseContentType, String fileName, String id) throws OXException {
        if (baseContentType.startsWith("text/")) {
            if (!part.containsSequenceId()) {
                part.setSequenceId(id);
            }
            nonInlineParts.add(part);
        } else {
            final String disposition =
                part.getContentDisposition() == null ? (part.getFileName() == null ? Part.INLINE : Part.ATTACHMENT) : part.getContentDisposition().getDisposition();
            if (!Part.INLINE.equalsIgnoreCase(disposition)) {
                if (!part.containsSequenceId()) {
                    part.setSequenceId(id);
                }
                nonInlineParts.add(part);
            }
        }
        if (baseContentType.indexOf("calendar") >= 0 || baseContentType.indexOf("ics") >= 0) {
            iCalendarContent = true;
        }
        return true;
    }

    @Override
    public boolean handleSubject(String subject) throws OXException {
        return true;
    }

    @Override
    public boolean handleSystemFlags(int flags) throws OXException {
        return true;
    }

    @Override
    public boolean handleToRecipient(InternetAddress[] recipientAddrs) throws OXException {
        return true;
    }

    @Override
    public boolean handleUserFlags(String[] userFlags) throws OXException {
        return true;
    }

}
