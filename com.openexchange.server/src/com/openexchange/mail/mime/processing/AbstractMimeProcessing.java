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
     * @return The prefix text
     */
    protected static String generatePrefixText(String prefixTemplate, LocaleAndTimeZone ltz, MailMessage msg) {
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
                    prefix = PATTERN_DATE.matcher(prefix).replaceAll(date == null ? "" : quoteReplacement(MimeProcessingUtility.getFormattedDate(date, DateFormat.LONG, ltz.locale, ltz.timeZone)));
                } catch (final Exception t) {
                    LOG.warn("", t);
                    prefix = PATTERN_DATE.matcher(prefix).replaceAll("");
                }
            }
            if (prefix.indexOf("#TIME#") >= 0) {
                try {
                    prefix = PATTERN_TIME.matcher(prefix).replaceAll(date == null ? "" : quoteReplacement(MimeProcessingUtility.getFormattedTime(date, DateFormat.SHORT, ltz.locale, ltz.timeZone)));
                } catch (final Exception t) {
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
