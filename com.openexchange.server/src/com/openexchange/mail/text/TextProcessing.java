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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mail.text;

import java.io.IOException;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Strings;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link TextProcessing} - Various methods for text processing
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TextProcessing {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(TextProcessing.class));

    private static final String SPLIT_LINES = "\r?\n";

    private static final char CHAR_BREAK = '\n';

    private static String foldLine(final String line, final int linewrap, final String prefix) {
        int end;
        char c;
        /*
         * Strip trailing spaces and newlines
         */
        String s;
        final int used;
        {
            final String foldMe;
            if (null == prefix) {
                used = 0;
                foldMe = line;
            } else {
                used = prefix.length();
                foldMe = line.substring(used);
            }
            final int mlen = foldMe.length() - 1;
            for (end = mlen; end >= 0; end--) {
                c = foldMe.charAt(end);
                if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                    break;
                }
            }
            if (end != mlen) {
                s = foldMe.substring(0, end + 1);
            } else {
                s = foldMe;
            }
        }
        /*
         * Check if the string fits now
         */
        int total = used + s.length();
        if (total <= linewrap) {
            return used > 0 ? new com.openexchange.java.StringAllocator(total).append(prefix).append(s).toString() : s;
        }
        /*
         * Fold the string
         */
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(total);
        char lastc = 0;
        if (used > 0) {
            while (total > linewrap) {
                int lastspace = -1;
                for (int i = 0; i < s.length(); i++) {
                    if (lastspace != -1 && used + i > linewrap) {
                        break;
                    }
                    c = s.charAt(i);
                    if ((c == ' ' || c == '\t') && !(lastc == ' ' || lastc == '\t')) {
                        lastspace = i;
                    }
                    lastc = c;
                }
                if (lastspace < 0) {
                    /*
                     * No space, use the whole thing
                     */
                    sb.append(prefix).append(s);
                    return sb.toString();
                }
                sb.append(prefix);
                sb.append(s.substring(0, lastspace));
                sb.append(CHAR_BREAK);
                lastc = s.charAt(lastspace);
                // sb.append(lastc);
                s = s.substring(lastspace + 1);
                total = used + s.length();
            }
            sb.append(prefix).append(s);
            return sb.toString();
        }
        /*
         * No prefix given
         */
        while (s.length() > linewrap) {
            int lastspace = -1;
            for (int i = 0; i < s.length(); i++) {
                if (lastspace != -1 && i > linewrap) {
                    break;
                }
                c = s.charAt(i);
                if ((c == ' ' || c == '\t') && !(lastc == ' ' || lastc == '\t')) {
                    lastspace = i;
                }
                lastc = c;
            }
            if (lastspace < 0) {
                /*
                 * No space, use the whole thing
                 */
                sb.append(s);
                return sb.toString();
            }
            sb.append(s.substring(0, lastspace));
            sb.append(CHAR_BREAK);
            lastc = s.charAt(lastspace);
            // sb.append(lastc);
            s = s.substring(lastspace + 1);
        }
        sb.append(s);
        return sb.toString();
    }

    /**
     * Performs the line folding after specified number of characters through parameter <code>linewrap</code>. Occurring HTML links are
     * excluded.
     * <p>
     * If parameter <code>isHtml</code> is set to <code>true</code> the content is returned unchanged.
     *
     * @param content The plain text content to fold
     * @param linewrap The number of characters which may fit into a line
     * @return The line-folded content
     */
    public static String performLineFolding(final String content, final int linewrap) {
        if (linewrap <= 0) {
            return content;
        }
        final String[] lines = content.split(SPLIT_LINES);
        if (lines.length > 0) {
            final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(content.length() + 128);
            {
                final String foldMe = lines[0];
                sb.append(foldLine(foldMe, linewrap, getQuotePrefix(foldMe)));
            }
            for (int i = 1; i < lines.length; i++) {
                final String foldMe = lines[i];
                sb.append(CHAR_BREAK).append(foldLine(foldMe, linewrap, getQuotePrefix(foldMe)));
            }
            return sb.toString();
        }
        return content;
    }

    private static String getQuotePrefix(final String line) {
        if (line.length() == 0) {
            return null;
        }
        final int length = line.length();
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(8);
        int lastGT = -1;
        for (int i = 0; i < length; i++) {
            final char c = line.charAt(i);
            if (c == '>') {
                sb.append(c);
                lastGT = i;
            } else if (Strings.isWhitespace(c)) {
                sb.append(c);
            } else {
                break;
            }
        }
        if (lastGT == -1) {
            /*
             * No '>' character found
             */
            return null;
        }
        /*
         * Allow only 1 whitespace character after last '>' character
         */
        final int len = sb.length();
        if (lastGT + 2 < len) {
            sb.delete(lastGT + 2, len);
        }
        return sb.toString();
        // final Matcher m = PATTERN_QP.matcher(line);
        // return m.matches() ? new StringBuilder(m.group(1)).append(m.group(2)).toString() : null;
    }

    /**
     * Extracts plain-text content from specified mail.
     *
     * @param mail The mail
     * @return The extracted plain-text content
     * @throws OXException If text extraction fails
     */
    public static String extractTextFrom(final MailMessage mail) throws OXException {
        final MailPart mailPart = extractTextFrom(mail, 0);
        if (null == mailPart) {
            return "";
        }
        try {
            final ContentType contentType = mailPart.getContentType();
            if (!contentType.startsWith("text/htm")) {
                return readContent(mailPart, contentType);
            }
            /*
             * Handle HTML content
             */
            final String html = readContent(mailPart, contentType);
            final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
            return htmlService.html2text(html, true);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName())) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static MailPart extractTextFrom(final MailPart mailPart, final int altLevel) throws OXException {
        if (!mailPart.containsContentType()) {
            return null;
        }
        final ContentType contentType = mailPart.getContentType();
        if (contentType.startsWith("text/")) {
            return (altLevel <= 0) || contentType.startsWith("text/htm") ? mailPart : null;
        }
        if (contentType.startsWith("multipart/")) {
            final boolean isAlternative = contentType.startsWith("multipart/alternative");
            int alternative = altLevel;
            if (isAlternative) {
                alternative++;
            }
            final int count = mailPart.getEnclosedCount();
            MailPart textPart = null;
            for (int i = 0; i < count; i++) {
                final MailPart enclosedPart = mailPart.getEnclosedMailPart(i);
                final MailPart ret = extractTextFrom(enclosedPart, alternative);
                if (null != ret) {
                    return ret;
                }
                if (isAlternative && null == textPart && enclosedPart.getContentType().startsWith("text/")) {
                    textPart = enclosedPart;
                }
            }
            if (isAlternative) {
                alternative--;
                if (null != textPart) {
                    return textPart;
                }
            }
        }
        return null;
    }

    private static String readContent(final MailPart mailPart, final ContentType contentType) throws OXException, IOException {
        final String charset = getCharset(mailPart, contentType);
        try {
            return MessageUtility.readMailPart(mailPart, charset);
        } catch (final java.io.CharConversionException e) {
            // Obviously charset was wrong or bogus implementation of character conversion
            final String fallback = "US-ASCII";
            if (LOG.isWarnEnabled()) {
                LOG.warn(
                    new com.openexchange.java.StringAllocator("Character conversion exception while reading content with charset \"").append(charset).append(
                        "\". Using fallback charset \"").append(fallback).append("\" instead."),
                    e);
            }
            return MessageUtility.readMailPart(mailPart, fallback);
        }
    }

    private static String getCharset(final MailPart mailPart, final ContentType contentType) throws OXException {
        final String charset;
        if (mailPart.containsHeader(MessageHeaders.HDR_CONTENT_TYPE)) {
            String cs = contentType.getCharsetParameter();
            if (!CharsetDetector.isValid(cs)) {
                com.openexchange.java.StringAllocator sb = null;
                if (null != cs) {
                    sb = new com.openexchange.java.StringAllocator(64).append("Illegal or unsupported encoding: \"").append(cs).append("\".");
                }
                if (contentType.startsWith("text/")) {
                    cs = CharsetDetector.detectCharset(mailPart.getInputStream());
                    if (LOG.isWarnEnabled() && null != sb) {
                        sb.append(" Using auto-detected encoding: \"").append(cs).append('"');
                        LOG.warn(sb.toString());
                    }
                } else {
                    cs = MailProperties.getInstance().getDefaultMimeCharset();
                    if (LOG.isWarnEnabled() && null != sb) {
                        sb.append(" Using fallback encoding: \"").append(cs).append('"');
                        LOG.warn(sb.toString());
                    }
                }
            }
            charset = cs;
        } else {
            if (contentType.startsWith("text/")) {
                charset = CharsetDetector.detectCharset(mailPart.getInputStream());
            } else {
                charset = MailProperties.getInstance().getDefaultMimeCharset();
            }
        }
        return charset;
    }

    /**
     * Initializes a new {@link TextProcessing}
     */
    private TextProcessing() {
        super();
    }
}
