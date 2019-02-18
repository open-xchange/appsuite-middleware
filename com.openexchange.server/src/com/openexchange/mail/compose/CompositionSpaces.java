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

package com.openexchange.mail.compose;

import static com.openexchange.java.util.UUIDs.getUnformattedString;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.vcard.VCardUtil;
import com.openexchange.contact.vcard.VCardExport;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.image.ImageActionFactory;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.text.HtmlProcessing;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CompositionSpaces} - Utility class for composition space.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class CompositionSpaces {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CompositionSpaces.class);
    }

    /**
     * Initializes a new {@link CompositionSpaces}.
     */
    private CompositionSpaces() {
        super();
    }

    /**
     * Gets the vCard for the user associated with specified session.
     *
     * @param session The session providing user information
     * @return The vCard as byte array
     * @throws OXException
     */
    public static byte[] getUserVCardBytes(Session session) throws OXException {
        ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
        Contact contact = contactService.getUser(session, session.getUserId());
        VCardExport vCardExport = null;
        try {
            vCardExport = VCardUtil.exportContact(contact, session);
            return vCardExport.toByteArray();
        } finally {
            Streams.close(vCardExport);
        }
    }

    /**
     * Gets the file name of the vCard file for the user associated with specified session.
     *
     * @param session The session providing user information
     * @return The vCard file name
     * @throws OXException
     */
    public static String getUserVCardFileName(Session session) throws OXException {
        String displayName;
        if (session instanceof ServerSession) {
            displayName = ((ServerSession) session).getUser().getDisplayName();
        } else {
            displayName = UserStorage.getInstance().getUser(session.getUserId(), session.getContextId()).getDisplayName();
        }
        String saneDisplayName = Strings.replaceWhitespacesWith(displayName, "");
        return saneDisplayName + ".vcf";
    }

    /**
     * Gets the vCard information for the user associated with specified session.
     *
     * @param session The session providing user information
     * @return The vCard information
     * @throws OXException
     */
    public static VCardAndFileName getUserVCard(Session session) throws OXException {
        return new VCardAndFileName(getUserVCardBytes(session), getUserVCardFileName(session));
    }

    /*
     * The display of a users given and sur name name in e.g. notification mails (Hello John Doe, ...).
     * The placeholders mean $givenname $surname.
     */
    private static final String USER_NAME = "%1$s %2$s";

    /**
     * Gets the vCard for the given contact.
     *
     * @param contactId The identifier of the contact
     * @param folderId The identifier of the folder in which the contact resides
     * @param session The session providing user information
     * @return The vCard as byte array
     * @throws OXException
     */
    public static VCardAndFileName getContactVCard(String contactId, String folderId, Session session) throws OXException {
        ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
        Contact contact = contactService.getContact(session, folderId, contactId);

        byte[] vcard;
        {
            VCardExport vCardExport = null;
            try {
                vCardExport = VCardUtil.exportContact(contact, session);
                vcard = vCardExport.toByteArray();
            } finally {
                Streams.close(vCardExport);
            }
        }

        String displayName = contact.getDisplayName();
        if (Strings.isEmpty(displayName)) {
            TranslatorFactory translatorFactory = ServerServiceRegistry.getInstance().getService(TranslatorFactory.class);

            User user;
            if (session instanceof ServerSession) {
                user = ((ServerSession) session).getUser();
            } else {
                user = UserStorage.getInstance().getUser(session.getUserId(), session.getContextId());
            }

            Translator translator = translatorFactory.translatorFor(user.getLocale());

            String givenName = contact.getGivenName();
            String surname = contact.getSurName();
            displayName = String.format(translator.translate(USER_NAME), givenName, surname);
        }
        String saneDisplayName = Strings.replaceWhitespacesWith(displayName, "");
        String fileName = saneDisplayName + ".vcf";

        return new VCardAndFileName(vcard, fileName);
    }

    /**
     * Parses a composition space's UUID from specified unformatted string.
     *
     * @param id The composition space identifier as an unformatted string; e.g. <code>067e61623b6f4ae2a1712470b63dff00</code>
     * @return The UUID
     * @throws OXException If passed string in invalid
     */
    public static UUID parseCompositionSpaceId(String id) throws OXException {
        try {
            return UUIDs.fromUnformattedString(id);
        } catch (IllegalArgumentException e) {
            throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(e, id);
        }
    }

    /**
     * Parses an attachment's UUID from specified unformatted string.
     *
     * @param id The attachment identifier as an unformatted string; e.g. <code>067e61623b6f4ae2a1712470b63dff00</code>
     * @return The UUID
     * @throws OXException If passed string in invalid
     */
    public static UUID parseAttachmentId(String id) throws OXException {
        try {
            return UUIDs.fromUnformattedString(id);
        } catch (IllegalArgumentException e) {
            throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_RESOURCE.create(e, id);
        }
    }

    /**
     * Parses an attachment's UUID from specified unformatted string.
     *
     * @param id The attachment identifier as an unformatted string; e.g. <code>067e61623b6f4ae2a1712470b63dff00</code>
     * @return The UUID or <code>null</code> if passed string in invalid
     */
    public static UUID parseAttachmentIdIfValid(String id) {
        try {
            return UUIDs.fromUnformattedString(id);
        } catch (@SuppressWarnings("unused") IllegalArgumentException x) {
            return null;
        }
    }

    /**
     * Checks if specified mail part has the vCard marker.
     *
     * @param mailPart The mail part to check
     * @param session The session
     * @return <code>true</code> if vCard marker is present; otherwise <code>false</code>
     */
    public static boolean hasVCardMarker(MailPart mailPart, Session session) {
        String userId = new StringBuilder(16).append(session.getUserId()).append('@').append(session.getContextId()).toString();
        return userId.equals(mailPart.getFirstHeader(MessageHeaders.HDR_X_OX_VCARD));
    }

    private static final Pattern PATTERN_SRC = MimeMessageUtility.PATTERN_SRC;

    /**
     * Replaces &lt;img&gt; tags providing an inline image through exchanging <code>"src"</code> value appropriately.
     * <p>
     * <code>&lt;img src="cid:123456"&gt;</code> is converted to<br>
     * <code>&lt;img src="/ajax/image/mail/compose/image?uid=71ff23e06f424cc5bcb08a92e006838a"&gt;</code>
     *
     * @param htmlContent The HTML content to replace in
     * @param contentId2InlineAttachments The detected inline images
     * @param imageDataSource The image data source to use
     * @param session The session providing user information
     * @return The (possibly) processed HTML content
     * @throws OXException If replacing &lt;img&gt; tags fails
     */
    public static String replaceCidInlineImages(String htmlContent, Map<String, Attachment> contentId2InlineAttachments, ImageDataSource imageDataSource, Session session) throws OXException {
        Matcher matcher = PATTERN_SRC.matcher(htmlContent);
        if (!matcher.find()) {
            return htmlContent;
        }

        StringBuffer sb = new StringBuffer(htmlContent.length());
        do {
            String imageTag = matcher.group();
            String srcValue = matcher.group(1);
            if (srcValue.startsWith("cid:")) {
                String contentId = MimeMessageUtility.trimContentId(srcValue.substring(4));
                Attachment attachment = contentId2InlineAttachments.get(contentId);
                if (null == attachment) {
                    // No such inline image... Yield a blank "src" attribute for current <img> tag
                    LoggerHolder.LOG.warn("No such inline image found for Content-Id {}", contentId);
                    int st = matcher.start(1);
                    int end = matcher.end(1);
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(imageTag.substring(0, st) + imageTag.substring(end)));
                } else {
                    ImageLocation imageLocation = new ImageLocation.Builder(getUnformattedString(attachment.getId())).optImageHost(HtmlProcessing.imageHost()).build();
                    String imageUrl = imageDataSource.generateUrl(imageLocation, session);
                    int st = matcher.start(1) - matcher.start();
                    int end = matcher.end(1) - matcher.start();
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(imageTag.substring(0, st) + imageUrl + imageTag.substring(end)));
                }
            }
        } while (matcher.find());
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static final Pattern PATTERN_IMAGE_ID = Pattern.compile("[?&]" + AJAXServlet.PARAMETER_UID + "=([^&]+)");

    private static final String UTF_8 = "UTF-8";

    /*
     * Something like "/ajax/image/mail/compose/image..."
     */
    private static final Pattern PATTERN_IMAGE_SRC_START_BY_IMAGE = Pattern
        .compile("[a-zA-Z_0-9&-.]+/(?:[a-zA-Z_0-9&-.]+/)*" + ImageActionFactory.ALIAS_APPENDIX + AttachmentStorage.IMAGE_DATA_SOURCE_ALIAS, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /*
     * Something like "/appsuite/api/mail/compose/5e9f9b6d15a94a31a8ba175489e5363a/attachments/8f119070e6af4143bd2f3c74bd8973a9..."
     */
    private static final Pattern PATTERN_IMAGE_SRC_START_BY_URL = Pattern.compile("[a-zA-Z_0-9&-.]+/(?:[a-zA-Z_0-9&-.]+/)*" + "mail/compose/" + "([a-fA-F0-9]+)" + "/attachments/" + "([a-fA-F0-9]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Replaces &lt;img&gt; tags providing an inline image through exchanging <code>"src"</code> value appropriately.
     * <p>
     * <code>&lt;img src="/ajax/image/mail/compose/image?uid=71ff23e06f424cc5bcb08a92e006838a"&gt;</code> is converted to<br>
     * <code>&lt;img src="cid:123456"&gt;</code>
     *
     * @param htmlContent The HTML content to replace in
     * @param attachmentId2inlineAttachments The detected inline images
     * @param contentId2InlineAttachment The map to fill with actually used inline attachments
     * @param fileAttachments The complete attachment mapping from which to remove actually used inline attachments
     * @return The (possibly) processed HTML content
     */
    public static String replaceLinkedInlineImages(String htmlContent, Map<String, Attachment> attachmentId2inlineAttachments, Map<String, Attachment> contentId2InlineAttachment, Map<UUID, Attachment> fileAttachments) {
        Matcher matcher = PATTERN_SRC.matcher(htmlContent);
        if (!matcher.find()) {
            return htmlContent;
        }

        StringBuffer sb = new StringBuffer(htmlContent.length());
        Matcher mailComposeUrlMatcher;
        do {
            String imageTag = matcher.group();
            String srcValue = matcher.group(1);
            if (PATTERN_IMAGE_SRC_START_BY_IMAGE.matcher(srcValue).find()) {
                Matcher attachmentIdMatcher = PATTERN_IMAGE_ID.matcher(srcValue);
                if (attachmentIdMatcher.find()) {
                    String attachmentId = AJAXUtility.decodeUrl(attachmentIdMatcher.group(1), UTF_8);
                    replaceLinkedInlineImage(attachmentId, imageTag, sb, matcher, attachmentId2inlineAttachments, contentId2InlineAttachment, fileAttachments);
                }
            } else if ((mailComposeUrlMatcher = PATTERN_IMAGE_SRC_START_BY_URL.matcher(srcValue)).find()) {
                String attachmentId = AJAXUtility.decodeUrl(mailComposeUrlMatcher.group(2), UTF_8);
                replaceLinkedInlineImage(attachmentId, imageTag, sb, matcher, attachmentId2inlineAttachments, contentId2InlineAttachment, fileAttachments);
            }
        } while (matcher.find());
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static void replaceLinkedInlineImage(String attachmentId, String imageTag, StringBuffer sb, Matcher matcher, Map<String, Attachment> attachmentId2inlineAttachments, Map<String, Attachment> contentId2InlineAttachment, Map<UUID, Attachment> fileAttachments) {
        Attachment attachment = attachmentId2inlineAttachments.get(attachmentId);
        if (null == attachment) {
            // No such inline image... Yield a blank "src" attribute for current <img> tag
            LoggerHolder.LOG.warn("No such inline image found for attachment identifier {}", attachmentId);
            matcher.appendReplacement(sb, "");
        } else {
            String imageUrl = "cid:" + attachment.getContentId();
            int st = matcher.start(1) - matcher.start();
            int end = matcher.end(1) - matcher.start();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(imageTag.substring(0, st) + imageUrl + imageTag.substring(end)));

            contentId2InlineAttachment.put(attachment.getContentId(), attachment);
            fileAttachments.remove(attachment.getId());
        }
    }

}
