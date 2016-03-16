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

package com.openexchange.messaging.generic;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.mail.internet.MailDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.text.HtmlProcessing;
import com.openexchange.messaging.generic.internal.TimeZoneUtils;

/**
 * {@link Utility} - Utility class for <i>com.openexchange.messaging.generic</i> bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class Utility {

    /**
     * Initializes a new {@link Utility}.
     */
    private Utility() {
        super();
    }

    /**
     * Gets the {@link TimeZone} instance for the given ID.
     *
     * @param id The ID for a <code>TimeZone</code>, either an abbreviation such as "PST", a full name such as "America/Los_Angeles", or a
     *            custom ID such as "GMT-8:00".
     * @return The specified {@link TimeZone} instance, or the GMT zone if the given ID cannot be understood.
     */
    public static TimeZone getTimeZone(final String id) {
        return TimeZoneUtils.getTimeZone(id);
    }

    /**
     * Adds the time zone offset to given date millis.
     *
     * @param date The date millis
     * @param timeZone The time zone identifier
     * @return The date millis with time zone offset added
     */
    public static long addTimeZoneOffset(final long date, final String timeZone) {
        return (date + TimeZoneUtils.getTimeZone(timeZone).getOffset(date));
    }

    /**
     * Adds the time zone offset to given date millis.
     *
     * @param date The date millis
     * @param timeZone The time zone
     * @return The date millis with time zone offset added
     */
    public static long addTimeZoneOffset(final long date, final TimeZone timeZone) {
        return (date + timeZone.getOffset(date));
    }

    private static final ConcurrentMap<String, Future<MailDateFormat>> MDF_MAP = new ConcurrentHashMap<String, Future<MailDateFormat>>();

    private static final MailDateFormat DEFAULT_MAIL_DATE_FORMAT;

    static {
        DEFAULT_MAIL_DATE_FORMAT = new MailDateFormat();
        DEFAULT_MAIL_DATE_FORMAT.setTimeZone(TimeZoneUtils.getTimeZone("GMT"));
    }

    /**
     * Gets the default {@link SimpleDateFormat} instance configured with GMT time zone.
     * <p>
     * Note that returned instance of {@link SimpleDateFormat} is shared, therefore use a surrounding synchronized block to preserve thread
     * safety:
     *
     * <pre>
     * ...
     * final MailDateFormat mdf = Utility.getDefaultMailDateFormat();
     * synchronized(mdf) {
     *  mdf.format(date);
     * }
     * ...
     * </pre>
     *
     * @return The default {@link SimpleDateFormat} instance configured with GMT time zone
     */
    public static SimpleDateFormat getDefaultMailDateFormat() {
        return DEFAULT_MAIL_DATE_FORMAT;
    }

    /**
     * Gets the {@link SimpleDateFormat} for specified time zone identifier.
     * <p>
     * Note that returned instance of {@link SimpleDateFormat} is shared, therefore use a surrounding synchronized block to preserve thread
     * safety:
     *
     * <pre>
     * ...
     * final MailDateFormat mdf = Utility.getMailDateFormat(timeZoneId);
     * synchronized(mdf) {
     *  mdf.format(date);
     * }
     * ...
     * </pre>
     *
     * @param timeZoneId The time zone identifier
     * @return The {@link SimpleDateFormat} for specified time zone identifier
     */
    public static SimpleDateFormat getMailDateFormat(final String timeZoneId) {
        Future<MailDateFormat> future = MDF_MAP.get(timeZoneId);
        if (null == future) {
            final FutureTask<MailDateFormat> ft = new FutureTask<MailDateFormat>(new Callable<MailDateFormat>() {

                @Override
                public MailDateFormat call() throws Exception {
                    final MailDateFormat mdf = new MailDateFormat();
                    mdf.setTimeZone(TimeZoneUtils.getTimeZone(timeZoneId));
                    return mdf;
                }
            });
            future = MDF_MAP.putIfAbsent(timeZoneId, ft);
            if (null == future) {
                future = ft;
                ft.run();
            }
        }
        try {
            return future.get();
        } catch (final InterruptedException e) {
            org.slf4j.LoggerFactory.getLogger(Utility.class).error("", e);
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            org.slf4j.LoggerFactory.getLogger(Utility.class).error("", cause);
            return DEFAULT_MAIL_DATE_FORMAT;
        }
    }

    /**
     * Decodes a "Subject" header obtained from ENVELOPE fetch item.
     *
     * @param subject The subject obtained from ENVELOPE fetch item
     * @return The decoded subject value
     */
    public static String decodeEnvelopeSubject(final String subject) {
        return MimeMessageUtility.decodeEnvelopeSubject(subject);
    }

    /**
     * Decodes a string header obtained from ENVELOPE fetch item.
     *
     * @param headerValue The header value
     * @return The decoded header value
     */
    public static String decodeEnvelopeHeader(final String headerValue) {
        return MimeMessageUtility.decodeEnvelopeHeader(headerValue);
    }

    /**
     * Decodes a multi-mime-encoded header value using the algorithm specified in RFC 2047, Section 6.1.
     * <p>
     * If the charset-conversion fails for any sequence, an {@link UnsupportedEncodingException} is thrown.
     * <p>
     * If the String is not a RFC 2047 style encoded header, it is returned as-is
     *
     * @param headerValue The possibly encoded header value
     * @return The possibly decoded header value
     */
    public static String decodeMultiEncodedHeader(final String headerValue) {
        return MimeMessageUtility.decodeMultiEncodedHeader(headerValue);
    }

    /**
     * Folds a string at linear whitespace so that each line is no longer than 76 characters, if possible. If there are more than 76
     * non-whitespace characters consecutively, the string is folded at the first whitespace after that sequence. The parameter
     * <tt>used</tt> indicates how many characters have been used in the current line; it is usually the length of the header name.
     * <p>
     * Note that line breaks in the string aren't escaped; they probably should be.
     *
     * @param used The characters used in line so far
     * @param foldMe The string to fold
     * @return The folded string
     */
    public static String fold(final int used, final String foldMe) {
        return MimeMessageUtility.fold(used, foldMe);
    }

    /**
     * Unfolds a folded header. Any line breaks that aren't escaped and are followed by whitespace are removed.
     *
     * @param headerLine The header line to unfold
     * @return The unfolded string
     */
    public static String unfold(final String headerLine) {
        return MimeMessageUtility.unfold(headerLine);
    }

    /**
     * Formats plain text to HTML by escaping HTML special characters e.g. <code>&quot;&lt;&quot;</code> is converted to
     * <code>&quot;&amp;lt;&quot;</code>.
     * <p>
     * This is just a convenience method which invokes <code>{@link #htmlFormat(String, boolean)}</code> with latter parameter set to
     * <code>true</code>.
     *
     * @param plainText The plain text
     * @return properly escaped HTML content
     * @see #htmlFormat(String, boolean)
     */
    public static String htmlFormat(final String plainText) {
        return HtmlProcessing.htmlFormat(plainText);
    }

    /**
     * Formats plain text to HTML by escaping HTML special characters e.g. <code>&quot;&lt;&quot;</code> is converted to
     * <code>&quot;&amp;lt;&quot;</code>.
     *
     * @param plainText The plain text
     * @param withQuote Whether to escape quotes (<code>&quot;</code>) or not
     * @return properly escaped HTML content
     */
    public static String htmlFormat(final String plainText, final boolean withQuote) {
        return HtmlProcessing.htmlFormat(plainText, withQuote);
    }

    /**
     * Formats HTML to plain text.
     *
     * @param htmlContent The HTML content
     * @return The converted plain text
     */
    public static String textFormat(final String htmlContent) {
        if (htmlContent == null || htmlContent.length() == 0) {
            return "";
        }
        return HtmlProcessing.html2text(htmlContent, true);
    }

    /**
     * Searches for non-HTML links and convert them to valid HTML links.
     * <p>
     * Example: <code>http://www.somewhere.com</code> is converted to
     * <code>&lt;a&nbsp;href=&quot;http://www.somewhere.com&quot;&gt;http://www.somewhere.com&lt;/a&gt;</code>.
     *
     * @param content The content to search in
     * @return The given content with all non-HTML links converted to valid HTML links
     */
    public static String formatHrefLinks(final String content) {
        return HtmlProcessing.formatHrefLinks(content);
    }

    /**
     * Creates valid HTML from specified HTML content conform to W3C standards.
     *
     * @param htmlContent The HTML content
     * @param charset The charset parameter
     * @return The HTML content conform to W3C standards
     */
    public static String getConformHTML(final String htmlContent, final String charset) {
        return HtmlProcessing.getConformHTML(htmlContent, charset);
    }

    /**
     * Creates a {@link Document DOM document} from specified XML/HTML string.
     *
     * @param string The XML/HTML string
     * @return A newly created DOM document or <code>null</code> if given string cannot be transformed to a DOM document
     */
    public static Document createDOMDocument(final String string) {
        return HtmlProcessing.createDOMDocument(string);
    }

    /**
     * Pretty-prints specified XML/HTML string.
     *
     * @param string The XML/HTML string to pretty-print
     * @return The pretty-printed XML/HTML string
     */
    public static String prettyPrintXML(final String string) {
        return HtmlProcessing.prettyPrintXML(string);
    }

    /**
     * Pretty-prints specified XML/HTML node.
     *
     * @param node The XML/HTML node pretty-print
     * @return The pretty-printed XML/HTML node
     */
    public static String prettyPrintXML(final Node node) {
        return HtmlProcessing.prettyPrintXML(node);
    }

    /**
     * Gets the MIME type associated with given file.
     *
     * @param file The file
     * @return The MIME type associated with given file or <code>application/octet-stream</code> if none found
     */
    public static String getContentType(final File file) {
        return getContentType(file.getName());
    }

    /**
     * Gets the MIME type associated with given file name.
     *
     * @param fileName The file name; e.g. <code>"file.html"</code>
     * @return The MIME type associated with given file name or <code>application/octet-stream</code> if none found
     */
    public static String getContentType(final String fileName) {
        return MimeType2ExtMap.getContentType(fileName);
    }

    /**
     * Gets the MIME type associated with given file extension.
     *
     * @param extension The file extension; e.g. <code>"txt"</code>
     * @return The MIME type associated with given file extension or <code>application/octet-stream</code> if none found
     */
    public static String getContentTypeByExtension(final String extension) {
        return MimeType2ExtMap.getContentTypeByExtension(extension);
    }

    /**
     * Gets the file extension for given MIME type.
     *
     * @param mimeType The MIME type
     * @return The file extension for given MIME type or <code>dat</code> if none found
     */
    public static List<String> getFileExtensions(final String mimeType) {
        return MimeType2ExtMap.getFileExtensions(mimeType);
    }
}
