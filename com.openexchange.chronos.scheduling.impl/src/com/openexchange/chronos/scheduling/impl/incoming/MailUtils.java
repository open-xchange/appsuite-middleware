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

package com.openexchange.chronos.scheduling.impl.incoming;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.crypto.CryptographicAwareMailAccessFactory;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.session.Session;

/**
 * {@link MailUtils}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.6
 */
public final class MailUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailUtils.class);

    /**
     * Initializes a new {@link MailUtils}.
     */
    private MailUtils() {
        super();
    }

    /**
     * Get the mail access for the given account
     *
     * @param cryptoMailAccessFactory The optional service to get access from
     * @param session The user session
     * @param accountId The account identifier to get access for
     * @return The mail access
     * @throws OXException In case access can't be initialized
     */
    public static MailAccess<?, ?> getMailAccess(CryptographicAwareMailAccessFactory cryptoMailAccessFactory, Session session, int accountId) throws OXException {
        MailAccess<?, ?> mailAccess;
        mailAccess = MailAccess.getInstance(session, accountId);
        if (cryptoMailAccessFactory != null) {
            mailAccess = cryptoMailAccessFactory.createAccess((MailAccess<IMailFolderStorage, IMailMessageStorage>) mailAccess, session, null);
        }
        mailAccess.connect();
        return mailAccess;
    }

    /**
     * Safely closes the access
     *
     * @param mailAccess The access to close
     */
    public static void closeMailAccess(MailAccess<?, ?> mailAccess) {
        if (null != mailAccess) {
            mailAccess.close(true);
        }
    }

    /**
     * Gets the mail part aka. the attachment fitting to the given identifier
     *
     * @param mail The mail to get the attachment from
     * @param fullQualifiedContentId The content identifier of the attachment to retrieve
     * @return The part or <code>null</code>
     * @throws OXException In case of error
     */
    public static MailPart getAttachmentPart(MailMessage mail, String fullQualifiedContentId) throws OXException {
        if (false == isMulipartMail(mail)) {
            return null;
        }
        String cid = getContentId(fullQualifiedContentId);
        if (Strings.isEmpty(cid)) {
            return null;
        }
        /*
         * Search over all attachments
         */
        for (int i = 0; i < mail.getEnclosedCount(); i++) {
            MailPart part = mail.getEnclosedMailPart(i);
            ContentType contentType = part.getContentType();
            if (null == contentType) {
                continue;
            }
            String contentId = part.getContentId();
            if (Strings.isNotEmpty(contentId) && (cid.equals(contentId) || cid.equals(removeCidPrefix(contentId)))) {
                return part;
            }
        }
        return null;
    }

    /**
     * Gets the calendar object resource for the scheduling mail
     *
     * @param mail The mail to get the .ics attachment from
     * @return The part or <code>null</code>
     * @throws OXException In case of error
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc6047#section-4.6">RFC 6047 Section 4.6</a>
     */
    public static MailPart getIcalAttachmentPart(MailPart mail) throws OXException {
        if (false == isMulipartMail(mail)) {
            return null;
        }
        /*
         * Search over all attachments
         */
        return getIcalAttachmentPart(mail, 0);
    }

    private static MailPart getIcalAttachmentPart(MailPart mail, int regression) throws OXException {
        for (int i = 0; i < mail.getEnclosedCount(); i++) {
            MailPart part = mail.getEnclosedMailPart(i);
            ContentType contentType = part.getContentType();
            if (null == contentType) {
                continue;
            }
            if (contentType.matchesBaseType("multipart", "*alternative*")) {
                /*
                 * Handle inner iCAL files, see RFC. However only search on first depth level
                 */
                if (regression >= 1) {
                    continue;
                }
                MailPart inner = getIcalAttachmentPart(part, regression + 1);
                if (null != inner) {
                    return inner;
                }
            } else if (contentType.matchesBaseType("text", "*calendar*")) {
                /*
                 * Check if file is decorated with the method and not referenced as attachment
                 */
                String[] header = part.getHeader("Content-ID");
                if (contentType.containsParameter("method") && (null == header || header.length == 0)) {
                    return part;
                }
            }
        }
        return null;
    }

    private static boolean isMulipartMail(MailPart mail) {
        return null != mail && null != mail.getContentType() && mail.getContentType().startsWith("multipart/");
    }

    /**
     * Converts a "cid" URL to its corresponding <code>Content-ID</code> message header,
     *
     * @param cidUrl The "cid" URL to convert
     * @return The corresponding contentId, or the passed value as-is if not possible
     */
    private static String getContentId(String cidUrl) {
        if (Strings.isEmpty(cidUrl)) {
            return cidUrl;
        }
        /*
         * https://tools.ietf.org/html/rfc2392#section-2:
         * A "cid" URL is converted to the corresponding Content-ID message header [MIME] by removing the "cid:" prefix, converting the
         * % encoded character to their equivalent US-ASCII characters, and enclosing the remaining parts with an angle bracket pair,
         * "<" and ">".
         */
        String contentId = cidUrl;
        if (contentId.toLowerCase().startsWith("cid:")) {
            contentId = contentId.substring(4);
        }
        try {
            contentId = URLDecoder.decode(contentId, Charsets.UTF_8_NAME);
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Unexpected error decoding {}", contentId, e);
        }
        if (Strings.isEmpty(contentId)) {
            return contentId;
        }
        if ('<' != contentId.charAt(0)) {
            contentId = '<' + contentId;
        }
        if ('>' != contentId.charAt(contentId.length() - 1)) {
            contentId = contentId + '>';
        }
        return contentId;
    }

    private static String removeCidPrefix(String uri) {
        if (Strings.isNotEmpty(uri) && uri.toUpperCase().startsWith("CID:")) {
            return uri.substring(4);
        }
        return uri;
    }
}
