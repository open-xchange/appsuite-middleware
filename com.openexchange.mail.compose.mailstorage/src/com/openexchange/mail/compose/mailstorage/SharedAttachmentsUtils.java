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

package com.openexchange.mail.compose.mailstorage;

import static com.openexchange.java.Strings.quoteReplacement;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.slf4j.Logger;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.processing.MimeProcessingUtility;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link SharedAttachmentsUtils} - A utility class for shared attachments aka. Drive Mail.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class SharedAttachmentsUtils {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SharedAttachmentsUtils.class);
    }

    /**
     * Initializes a new {@link SharedAttachmentsUtils}.
     */
    private SharedAttachmentsUtils() {
        super();
    }

    /**
     * Builds the name for the folder carrying the shared attachments.
     *
     * @param subject The subject of the message (if any)
     * @param withDraftPrefix Whether to prepend the (localized) <code>"[Draft] "</code> prefix
     * @param session The session providing user information
     * @return The folder name
     */
    public static String buildFolderName(String subject, boolean withDraftPrefix, ServerSession session) {
        String subjectToUse = Strings.isEmpty(subject) ? buildFallbackFolderName(session) : subject;
        if (withDraftPrefix) {
            String prefix = StringHelper.valueOf(session.getUser().getLocale()).getString(MailStorageCompositionSpaceStrings.DRAFT_PREFIX);
            return new StringBuilder(subjectToUse.length() + prefix.length() + 3).append('[').append(prefix).append("] ").append(subjectToUse).toString();
        }
        return subjectToUse;
    }

    private static String buildFallbackFolderName(ServerSession session) {
        String fallbackName = StringHelper.valueOf(session.getUser().getLocale()).getString(MailStorageCompositionSpaceStrings.FALLBACK_FOLDER_NAME);

        User user = session.getUser();
        Locale locale = user.getLocale();
        TimeZone timeZone = TimeZoneUtils.getTimeZone(user.getTimeZone());

        Date currentDate = new Date();
        try {
            String sDate = quoteReplacement(MimeProcessingUtility.getFormattedDate(currentDate, DateFormat.LONG, locale, timeZone, session));
            fallbackName = Strings.replaceSequenceWith(fallbackName, "#DATE#", sDate);
        } catch (Exception t) {
            LoggerHolder.LOG.warn("", t);
            fallbackName = Strings.replaceSequenceWith(fallbackName, "#DATE#", "");
        }
        try {
            String sTime = quoteReplacement(MimeProcessingUtility.getFormattedTime(currentDate, DateFormat.SHORT, locale, timeZone, session));
            fallbackName = Strings.replaceSequenceWith(fallbackName, "#TIME#", sTime);
        } catch (Exception t) {
            LoggerHolder.LOG.warn("", t);
            fallbackName = Strings.replaceSequenceWith(fallbackName, "#TIME#", "");
        }

        return fallbackName;
    }

}
