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

package com.openexchange.mail.mime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;

/**
 * {@link ExtendedMimeMessage} - Extends {@link MimeMessage} by some additional attributes to store message information such as its UID.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ExtendedMimeMessage extends MimeMessage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExtendedMimeMessage.class);

    private final String fullname;
    private long uid = -1;
    private Boolean hasAttachment;
    private int threadLevel;
    private BODYSTRUCTURE bodystructure;
    private int priority = -1;
    private Date receivedDate;
    private Integer size;
    private ContentType contentType;

    /**
     * Initializes a new {@link ExtendedMimeMessage}
     *
     * @param fullname The folder full name
     * @param msgnum The message number in folder
     */
    public ExtendedMimeMessage(final String fullname, final int msgnum) {
        super(MimeDefaultSession.getDefaultSession());
        this.fullname = fullname;
        this.msgnum = msgnum;
    }

    /**
     * Parse the input stream: setting the headers and content fields appropriately.
     *
     * @param in The input stream
     * @throws MessagingException If parsing the input stream fails
     */
    public void parseStream(final InputStream in) throws MessagingException {
        contentType = null;
        priority = -1;
        parse(in);
    }

    /**
     * Gets this message's folder fullname
     *
     * @return The fullname
     */
    public String getFullname() {
        return fullname;
    }

    private static final String MULTI = "multipart/";

    private static final String MULTI_SUBTYPE_MIXED = "MIXED";

    /**
     * The flag if this message has attachments
     *
     * @return <code>true</code> if this message has attachments; otherwise <code>false</code>
     */
    public boolean hasAttachment() {
        if (null == hasAttachment) {
            final ContentType ct = getContentType0();
            try {
                hasAttachment =
                    Boolean.valueOf(ct.startsWith(MULTI) && (MULTI_SUBTYPE_MIXED.equalsIgnoreCase(ct.getSubType()) || deepAttachmentCheck(ct.getSubType())));
            } catch (final OXException e) {
                LOG.error("", e);
                hasAttachment = Boolean.valueOf(ct.isMimeType(MimeTypes.MIME_MULTIPART_MIXED));
            } catch (final MessagingException e) {
                LOG.error("", e);
                hasAttachment = Boolean.valueOf(ct.isMimeType(MimeTypes.MIME_MULTIPART_MIXED));
            } catch (final IOException e) {
                LOG.error("", e);
                hasAttachment = Boolean.valueOf(ct.isMimeType(MimeTypes.MIME_MULTIPART_MIXED));
            }
        }
        return hasAttachment.booleanValue();
    }

    private boolean deepAttachmentCheck(final String subType) throws OXException, MessagingException, IOException {
        if (null != bodystructure) {
            /*
             * Body structure is available
             */
            return MimeMessageUtility.hasAttachments(bodystructure);
        } else if ((null != content) || (null != contentStream)) {
            /*
             * Message body is available
             */
            return MimeMessageUtility.hasAttachments((Multipart) getContent(), subType);
        }
        /*
         * Not enough information to deeply check for (file) attachments
         */
        return false;
    }

    /**
     * Sets the flag if this message has attachments
     *
     * @param hasAttachment <code>true</code> to mark this message to hold attachments; otherwise <code>false</code>
     */
    public void setHasAttachment(final boolean hasAttachment) {
        this.hasAttachment = Boolean.valueOf(hasAttachment);
    }

    /**
     * Gets the thread level
     *
     * @return the thread level
     */
    public int getThreadLevel() {
        return threadLevel;
    }

    /**
     * Sets the thread level
     *
     * @param threadLevel the thread level to set
     */
    public void setThreadLevel(final int threadLevel) {
        this.threadLevel = threadLevel;
    }

    /**
     * Gets the body structure.
     * <p>
     * This attribute is only available if underlying mail system as an IMAP server and fetch item <i>BODYSTRUCTURE</i> has been requested.
     * <p>
     * By now this attribute is only used to detect if message contains (file) attachment(s) in a more precise manner.
     *
     * @return The body structure
     */
    public BODYSTRUCTURE getBodystructure() {
        return bodystructure;
    }

    /**
     * Sets the body structure.
     * <p>
     * This attribute can only be set if underlying mail system as an IMAP server and fetch item <i>BODYSTRUCTURE</i> has been requested.
     * <p>
     * By now this attribute is only used to detect if message contains (file) attachment(s) in a more precise manner.
     *
     * @param bodystructure The body structure to set
     */
    public void setBodystructure(final BODYSTRUCTURE bodystructure) {
        this.bodystructure = bodystructure;
    }

    /**
     * Gets the priority
     *
     * @return The priority
     */
    public int getPriority() {
        if (priority == -1) {
            try {
                final String imp = getHeader(MessageHeaders.HDR_IMPORTANCE, null);
                if (null != imp) {
                    priority = MimeMessageConverter.parseImportance(imp);
                } else {
                    priority = parsePriority(getHeader(MessageHeaders.HDR_X_PRIORITY, null));
                }
            } catch (final MessagingException e) {
                LOG.warn("", e);
                priority = MailMessage.PRIORITY_NORMAL;
            }
        }
        return priority;
    }

    private static int parsePriority(final String priorityStr) {
        if (null != priorityStr) {
            final String[] tmp = priorityStr.split(" +");
            try {
                return Integer.parseInt(tmp[0]);
            } catch (final NumberFormatException nfe) {
                LOG.debug("Strange X-Priority header: {}", tmp[0], nfe);
                return MailMessage.PRIORITY_NORMAL;
            }
        }
        return MailMessage.PRIORITY_NORMAL;
    }

    /**
     * Sets the priority
     *
     * @param priority The priority to set
     */
    public void setPriority(final int priority) {
        this.priority = priority;
    }

    @Override
    public Date getReceivedDate() {
        return receivedDate;
    }

    /**
     * Sets the received date
     *
     * @param receivedDate the received date to set
     */
    public void setReceivedDate(final Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    @Override
    public int getSize() {
        if (size == null) {
            try {
                size = Integer.valueOf(super.getSize());
            } catch (final MessagingException e) {
                LOG.warn("", e);
                size = Integer.valueOf(-1);
            }
        }
        return size.intValue();
    }

    /**
     * Sets the size
     *
     * @param size the size to set
     */
    public void setSize(final int size) {
        this.size = Integer.valueOf(size);
    }

    private ContentType getContentType0() {
        if (contentType == null) {
            try {
                contentType = new ContentType(getHeader(MessageHeaders.HDR_CONTENT_TYPE, null));
            } catch (final MessagingException e) {
                LOG.warn("", e);
                contentType = new ContentType();
                contentType.setPrimaryType("text");
                contentType.setSubType("plain");
                contentType.setCharsetParameter("us-ascii");
            } catch (final OXException e) {
                LOG.warn("", e);
                contentType = new ContentType();
                contentType.setPrimaryType("text");
                contentType.setSubType("plain");
                contentType.setCharsetParameter("us-ascii");
            }
        }
        return contentType;
    }

    @Override
    public String getContentType() {
        return getContentType0().toString();
    }

    /**
     * Sets the content type
     *
     * @param contentType the content type to set
     */
    public void setContentType(final ContentType contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the UID
     *
     * @return The UID
     */
    public long getUid() {
        return uid;
    }

    /**
     * Sets the UID
     *
     * @param uid The UID to set
     */
    public void setUid(final long uid) {
        this.uid = uid;
    }

}
