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

package com.openexchange.mail.mime.converters;

import java.io.IOException;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.ExtendedMimeMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;

/**
 * {@link HasAttachmentSetter}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class HasAttachmentSetter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HasAttachmentSetter.class);

    private MailConfig config;

    public HasAttachmentSetter(MailConfig config) {
        this.config = config;
    }

    public void setCustomFields() {
        // nothing to do
    }

    /**
     * Only set when attachment search is disabled
     * 
     * @param msg
     * @param bs
     */
    public void set(ExtendedMimeMessage msg, BODYSTRUCTURE bs) {
        if (!config.getCapabilities().hasAttachmentSearch()) {
            // the 'i have to jump through all parts and try to have a look at the content types to find out if there is an attachment' handling
            msg.setHasAttachment(MimeMessageUtility.hasAttachments(bs));
        }
        setCustomFields();
    }

    public void set(MailMessage msg, BODYSTRUCTURE bs) {
        if (!config.getCapabilities().hasAttachmentSearch()) {
            // the 'i have to jump through all parts and try to have a look at the content types to find out if there is an attachment' handling
            msg.setHasAttachment(MimeMessageUtility.hasAttachments(bs));
        }
        setCustomFields();
    }

    public void set(ExtendedMimeMessage mimeMessage, String[] userFlags) {
        if (config.getCapabilities().hasAttachmentSearch() && userFlags != null && userFlags.length > 0) {
            // the easy handling
            for (String flag : userFlags) {
                boolean hasAttachment = MailMessage.isHasAttachment(flag);
                if (hasAttachment) {
                    mimeMessage.setHasAttachment(true);
                    continue;
                }
            }
        }
        setCustomFields();
    }

    public void set(MailMessage mail, String[] userFlags) throws OXException {
        if (config.getCapabilities().hasAttachmentSearch() && userFlags != null) {
            handleViaAttachmentSearch(mail, userFlags);
        } else {
            // the 'i have to jump through all parts and try to have a look at the content types to find out if there is an attachment' handling
            handleViaContentType(mail);
        }
        setCustomFields();
    }

    private void handleViaAttachmentSearch(MailMessage mail, String[] userFlags) {
        if (userFlags != null && userFlags.length > 0) {
            // the easy handling
            for (String flag : userFlags) {
                boolean hasAttachment = MailMessage.isHasAttachment(flag);
                if (hasAttachment) {
                    mail.setHasAttachment(true);
                    return;
                }
            }
        }
    }

    private void handleViaContentType(MailMessage mail) throws OXException {
        ContentType ct = mail.getContentType();
        try {
            mail.setHasAttachment(hasAttachments(mail, ct.getSubType()));
        } catch (OXException e) {
            if (!MailExceptionCode.MESSAGING_ERROR.equals(e)) {
                throw e;
            }
            // A messaging error occurred
            LOG.debug("Parsing message's multipart/* content to check for file attachments caused a messaging error.\nGoing to mark message to have (file) attachments if Content-Type matches multipart/mixed.", e);
            mail.setHasAttachment(ct.startsWith(MimeTypes.MIME_MULTIPART_MIXED));
        } catch (ClassCastException e) {
            // Cast to javax.mail.Multipart failed
            LOG.debug("Message's Content-Type indicates to be multipart/* but its content is not an instance of javax.mail.Multipart but is not.\nIn case if IMAP it is due to a wrong BODYSTRUCTURE returned by IMAP server.\nGoing to mark message to have (file) attachments if Content-Type matches multipart/mixed.", e);
            mail.setHasAttachment(ct.startsWith(MimeTypes.MIME_MULTIPART_MIXED));
        } catch (MessagingException e) {
            // A messaging error occurred
            LOG.debug("Parsing message's multipart/* content to check for file attachments caused a messaging error: {}.\nGoing to mark message to have (file) attachments if Content-Type matches multipart/mixed.", e.getMessage());
            mail.setHasAttachment(ct.startsWith(MimeTypes.MIME_MULTIPART_MIXED));
        }
    }

    /**
     * Checks if given multipart contains (file) attachments
     *
     * @param mp The multipart to examine
     * @param subtype The multipart's subtype
     * @return <code>true</code> if given multipart contains (file) attachments; otherwise <code>false</code>
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If a mail error occurs
     * @throws IOException If an I/O error occurs
     */
    public static boolean hasAttachments(final MailPart mp, final String subtype) throws MessagingException, OXException {
        if (null == mp) {
            return false;
        }

        int count = mp.getEnclosedCount();
        return hasAttachments0(mp, count);
    }

    private static boolean hasAttachments0(MailPart mp, int count) throws MessagingException, OXException {
        if (count == MailPart.NO_ENCLOSED_PARTS) {
            return hasAttachmentInMetadata(mp);
        }

        boolean found = false;
        ContentType ct = new ContentType();
        for (int i = count; !found && i-- > 0;) {
            MailPart part = mp.getEnclosedMailPart(i);
            if (hasAttachmentInMetadata(part)) {
                return true;
            }
            String[] tmp = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
            if (tmp != null && tmp.length > 0) {
                ct.setContentType(MimeMessageUtility.unfold(tmp[0]));
                if (ct.startsWith("multipart/")) {
                    found |= hasAttachments(part, ct.getSubType());
                }
            }
        }
        return found;
    }

    private static boolean hasAttachmentInMetadata(MailPart part) {
        if (part.getContentType().getBaseType().toLowerCase().startsWith("application") && part.getContentType().getSubType().toLowerCase().endsWith("-signature")) {
            return false;
        }

        ContentDisposition contentDisposition = part.getContentDisposition();
        if (contentDisposition != null) {
            if (contentDisposition.getFilenameParameter() != null) {
                return true;
            }
            String disposition = contentDisposition.getDisposition();
            if (disposition != null && disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
                return true;
            }
        }
        return MimeTypes.isConsideredAttachment(part.getContentType().getBaseType() + "/" + part.getContentType().getSubType());
    }

    /**
     * Checks if given multipart contains (file) attachments
     *
     * @param mp The multipart to examine
     * @param subtype The multipart's subtype
     * @return <code>true</code> if given multipart contains (file) attachments; otherwise <code>false</code>
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If a mail error occurs
     * @throws IOException If an I/O error occurs
     */
    protected static boolean hasAttachments(final Part mp) throws MessagingException, OXException {
        if (null == mp) {
            return false;
        }

        if (mp.getContentType().toLowerCase().startsWith("multipart/")) {
            int count = ((Multipart) mp).getCount();
            return hasAttachments0((Multipart) mp, count);
        }
        return hasAttachmentInMetadata(mp);
    }

    private static boolean hasAttachments0(Multipart mp, int count) throws MessagingException, OXException {
        boolean found = false;
        ContentType ct = new ContentType();
        for (int i = count; !found && i-- > 0;) {
            BodyPart part = mp.getBodyPart(i);
            if (hasAttachmentInMetadata(part)) {
                return true;
            }
            String[] tmp = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
            if (tmp != null && tmp.length > 0) {
                ct.setContentType(MimeMessageUtility.unfold(tmp[0]));
                if (ct.isMimeType(MimeTypes.MIME_MULTIPART_ALL)) {
                    found |= hasAttachments(part);
                }
            }
        }
        return found;
    }

    private static boolean hasAttachmentInMetadata(Part part) throws MessagingException, OXException {
        if (part.getContentType().toLowerCase().startsWith("application") && part.getContentType().toLowerCase().endsWith("-signature")) {
            return false;
        }

        String disposition = part.getDisposition();
        if (disposition != null) {
            ContentDisposition cd = new ContentDisposition(disposition);
            if (cd.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
                return true;
            }
            if (cd.getFilenameParameter() != null) {
                return true;
            }
        }
        return MimeTypes.isConsideredAttachment(part.getContentType());
    }

    /**
     * Checks if given BODYSTRUCTURE item indicates to contain (file) attachments
     *
     * @param bodystructure The BODYSTRUCTURE item
     * @return <code>true</code> if given BODYSTRUCTURE item indicates to contain (file) attachments; otherwise <code>false</code>
     */
    protected static boolean hasAttachments(final BODYSTRUCTURE bodystructure) {
        // The value determined by this routine will outsmart exact examination
        // See bug 42695 & 42862
        if (bodystructure.isMulti()) {
            return hasAttachments0(bodystructure);
        }

        if (!(bodystructure.type.toLowerCase().startsWith("application") && bodystructure.subtype.toLowerCase().endsWith("-signature"))) {
            if (bodystructure.disposition != null && bodystructure.disposition.equals(Part.ATTACHMENT)) {
                return true;
            }

            if (bodystructure.dParams != null && bodystructure.dParams.get("filename") != null) {
                return true;
            }
        }
        return MimeTypes.isConsideredAttachment(bodystructure.type + "/" + bodystructure.subtype);
    }

    private static boolean hasAttachments0(final BODYSTRUCTURE bodystructure) {
        boolean found = false;

        BODYSTRUCTURE[] bodies = bodystructure.bodies;
        if (null != bodies) {
            for (int i = bodies.length; !found && i-- > 0;) {
                found |= hasAttachments(bodies[i]);
            }
        }

        return found;
    }
}
