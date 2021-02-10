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
