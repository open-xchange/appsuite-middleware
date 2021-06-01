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

package com.openexchange.mail.mime.processing;

import static com.openexchange.java.Strings.quoteReplacement;
import java.text.DateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.session.Session;

/**
 * {@link AbstractMimeProcessing}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.3
 */
public abstract class AbstractMimeProcessing {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractMimeProcessing.class);

    /**
     * Initializes a new {@link AbstractMimeProcessing}.
     */
    protected AbstractMimeProcessing() {
        super();
    }

    private static final Pattern PATTERN_FROM = Pattern.compile(Pattern.quote("#FROM#"));

    private static final Pattern PATTERN_TO = Pattern.compile(Pattern.quote("#TO#"));

    private static final Pattern PATTERN_CC = Pattern.compile(Pattern.quote("#CC#"));

    private static final Pattern PATTERN_CCLINE = Pattern.compile(Pattern.quote("#CC_LINE#"));

    private static final Pattern PATTERN_DATE = Pattern.compile(Pattern.quote("#DATE#"));

    private static final Pattern PATTERN_TIME = Pattern.compile(Pattern.quote("#TIME#"));

    private static final Pattern PATTERN_SUBJECT = Pattern.compile(Pattern.quote("#SUBJECT#"));

    private static final Pattern PATTERN_SENDER = Pattern.compile(Pattern.quote("#SENDER#"));

    /**
     * Generates the prefix text using given string template.
     *
     * @param prefixTemplate The string template to replace in
     * @param ltz The locale that determines format of date and time strings and time zone as well
     * @param msg The original message
     * @param session The session identifying the user
     * @return The prefix text
     */
    protected static String generatePrefixText(String prefixTemplate, LocaleAndTimeZone ltz, MailMessage msg, Session session) {
        StringHelper strHelper = StringHelper.valueOf(ltz.locale);
        String prefix = strHelper.getString(prefixTemplate);

        if (prefix.indexOf("#FROM#") >= 0) {
            final InternetAddress[] from = msg.getFrom();
            prefix = PATTERN_FROM.matcher(prefix).replaceAll(from == null || from.length == 0 ? "" : quoteReplacement(MimeProcessingUtility.addr2String(from[0])));
        }
        if (prefix.indexOf("#TO#") >= 0) {
            final InternetAddress[] to = msg.getTo();
            prefix = PATTERN_TO.matcher(prefix).replaceAll(to == null || to.length == 0 ? "" : quoteReplacement(MimeProcessingUtility.addrs2String(to)));
        }
        if (prefix.indexOf("#CC#") >= 0) {
            final InternetAddress[] cc = msg.getCc();
            prefix = PATTERN_CC.matcher(prefix).replaceAll(cc == null || cc.length == 0 ? "" : quoteReplacement(MimeProcessingUtility.addrs2String(cc)));
        }
        if (prefix.indexOf("#CC_LINE#") >= 0) {
            final InternetAddress[] cc = msg.getCc();
            prefix = PATTERN_CCLINE.matcher(prefix).replaceAll(cc == null || cc.length == 0 ? "" : quoteReplacement(new StringBuilder(64).append("\nCc: ").append(MimeProcessingUtility.addrs2String(cc)).toString()));
        }
        {
            Date date = msg.getSentDate();
            if (prefix.indexOf("#DATE#") >= 0) {
                try {
                    prefix = PATTERN_DATE.matcher(prefix).replaceAll(date == null ? "" : quoteReplacement(MimeProcessingUtility.getFormattedDate(date, DateFormat.LONG, ltz.locale, ltz.timeZone, session)));
                } catch (Exception t) {
                    LOG.warn("", t);
                    prefix = PATTERN_DATE.matcher(prefix).replaceAll("");
                }
            }
            if (prefix.indexOf("#TIME#") >= 0) {
                try {
                    prefix = PATTERN_TIME.matcher(prefix).replaceAll(date == null ? "" : quoteReplacement(MimeProcessingUtility.getFormattedTime(date, DateFormat.SHORT, ltz.locale, ltz.timeZone, session)));
                } catch (Exception t) {
                    LOG.warn("", t);
                    prefix = PATTERN_TIME.matcher(prefix).replaceAll("");
                }

            }
        }
        if (prefix.indexOf("#SUBJECT#") >= 0) {
            final String decodedSubject = MimeMessageUtility.decodeMultiEncodedHeader(msg.getSubject());
            prefix = PATTERN_SUBJECT.matcher(prefix).replaceAll(decodedSubject == null ? "" : quoteReplacement(decodedSubject));
        }
        if (prefix.indexOf("#SENDER#") >= 0) {
            final InternetAddress[] from = msg.getFrom();
            prefix = PATTERN_SENDER.matcher(prefix).replaceAll(from == null || from.length == 0 ? "" : quoteReplacement(MimeProcessingUtility.addr2String(from[0])));
        }
        return prefix;
    }

}
