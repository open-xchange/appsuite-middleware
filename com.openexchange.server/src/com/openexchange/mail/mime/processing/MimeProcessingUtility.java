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

package com.openexchange.mail.mime.processing;

import static com.openexchange.mail.text.HtmlProcessing.htmlFormat;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.Strings;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mail.uuencode.UUEncodedMultiPart;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link MimeProcessingUtility} - Provides some utility methods for {@link MimeForward} and {@link MimeReply}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeProcessingUtility {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MimeProcessingUtility.class));

    /**
     * No instantiation
     */
    private MimeProcessingUtility() {
        super();
    }

    /**
     * Gets denoted folder's owner if it is shared.
     *
     * @param fullName The full name
     * @param accountId The account identifier
     * @param session The session
     * @return The owner or <code>null</code>
     */
    static final String getFolderOwnerIfShared(final String fullName, final int accountId, final Session session) {
        if (null == fullName) {
            return null;
        }
        MailAccess<?, ?> access = null;
        try {
            access = MailAccess.getInstance(session, accountId);
            access.connect(false);
            final MailFolder folder = access.getFolderStorage().getFolder(fullName);
            return folder.isShared() ? folder.getOwner() : null;
        } catch (final Exception e) {
            LOG.warn("Couldn't resolve owner for " + fullName, e);
            return null;
        } finally {
            if (null != access) {
                access.close(true);
            }
        }
    }

    /**
     * Formats specified date in given style with given locale and time zone.
     *
     * @param date The date to format
     * @param style The style to use
     * @param locale The locale
     * @param timeZone The time zone
     * @return The formatted date
     */
    static final String getFormattedDate(final Date date, final int style, final Locale locale, final TimeZone timeZone) {
        final DateFormat dateFormat = DateFormat.getDateInstance(style, locale);
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }

    /**
     * Formats specified time in given style with given locale and time zone.
     *
     * @param date The time to format
     * @param style The style to use
     * @param locale The locale
     * @param timeZone The time zone
     * @return The formatted time
     */
    static final String getFormattedTime(final Date date, final int style, final Locale locale, final TimeZone timeZone) {
        final DateFormat dateFormat = DateFormat.getTimeInstance(style, locale);
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }

    /**
     * Checks if given part's disposition is inline; meaning more likely a regular message body than an attachment.
     *
     * @param part The message's part
     * @param contentType The part's Content-Type header
     * @return <code>true</code> if given part is considered to be an inline part; otherwise <code>false</code>
     * @throws OXException If part's headers cannot be accessed or parsed
     */
    static boolean isInline(final MailPart part, final ContentType contentType) throws OXException {
        final ContentDisposition cd;
        final boolean hasDisposition;
        {
            final String[] hdr = part.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION);
            if (null == hdr) {
                cd = new ContentDisposition();
                hasDisposition = false;
            } else {
                cd = new ContentDisposition(hdr[0]);
                hasDisposition = true;
            }
        }
        return (hasDisposition && Part.INLINE.equalsIgnoreCase(cd.getDisposition())) || (!hasDisposition && !cd.containsFilenameParameter() && !contentType.containsParameter("name"));
    }

    /**
     * Checks if specified part's filename ends with given suffix.
     *
     * @param suffix The suffix to check against
     * @param part The part whose filename shall be checked
     * @param contentType The part's Content-Type header
     * @return <code>true</code> if part's filename is not absent and ends with given suffix; otherwise <code>false</code>
     * @throws OXException If part's filename cannot be determined
     */
    static boolean fileNameEndsWith(final String suffix, final MailPart part, final ContentType contentType) throws OXException {
        final String filename = getFileName(part, contentType);
        return null == filename ? false : filename.toLowerCase(Locale.ENGLISH).endsWith(suffix);
    }

    /**
     * Gets specified part's filename.
     *
     * @param part The part whose filename shall be returned
     * @param contentType The part's Content-Type header
     * @return The filename or <code>null</code>
     * @throws OXException If part's filename cannot be returned
     */
    private static String getFileName(final MailPart part, final ContentType contentType) throws OXException {
        final ContentDisposition cd;
        {
            final String[] hdr = part.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION);
            if (null == hdr) {
                cd = new ContentDisposition();
            } else {
                cd = new ContentDisposition(hdr[0]);
            }
        }
        String filename = cd.getFilenameParameter();
        if (null == filename) {
            filename = contentType.getParameter("name");
        }
        return MimeMessageUtility.decodeMultiEncodedHeader(filename);
    }

    /**
     * Determines the proper text version according to user's mail settings. Given content type is altered accordingly
     *
     * @param textPart The text part
     * @param contentType The text part's content type
     * @return The proper text version
     * @throws OXException If a mail error occurs
     * @throws IOException If an I/O error occurs
     */
    static String handleInlineTextPart(final MailPart textPart, final ContentType contentType, final boolean allowHTML) throws IOException, OXException {
        final String charset = getCharset(textPart, contentType);
        if (contentType.isMimeType(MimeTypes.MIME_TEXT_HTM_ALL)) {
            if (allowHTML) {
                return readContent(textPart, charset);
            }
            contentType.setBaseType("text/plain");
            final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
            return htmlService.html2text(readContent(textPart, charset), false);
            // return new Html2TextConverter().convertWithQuotes(MessageUtility.readMimePart(textPart, contentType));
        } else if (contentType.isMimeType(MimeTypes.MIME_TEXT_PLAIN)) {
            final String content = readContent(textPart, charset);
            final UUEncodedMultiPart uuencodedMP = new UUEncodedMultiPart(content);
            if (uuencodedMP.isUUEncoded()) {
                /*
                 * UUEncoded content detected. Extract normal text.
                 */
                return uuencodedMP.getCleanText();
            }
            return content;
        }
        return readContent(textPart, charset);
    }

    private static final String PRIMARY_TEXT= "text/";

    private static final String[] SUB_SPECIAL2 = { "rfc822-headers", "vcard", "x-vcard", "calendar", "x-vcalendar" };

    /**
     * Checks if content type matches one of special content types:
     * <ul>
     * <li><code>text/rfc822-headers</code></li>
     * <li><code>text/vcard</code></li>
     * <li><code>text/x-vcard</code></li>
     * <li><code>text/calendar</code></li>
     * <li><code>text/x-vcalendar</code></li>
     * </ul>
     *
     * @param contentType The content type
     * @return <code>true</code> if content type matches special; otherwise <code>false</code>
     */
    public static boolean isSpecial(final String contentType) {
        if (null == contentType) {
            return false;
        }
        final String ct = contentType.toLowerCase(Locale.US);
        if (ct.startsWith(PRIMARY_TEXT, 0)) {
            final int off = PRIMARY_TEXT.length();
            for (final String subtype : SUB_SPECIAL2) {
                if (ct.startsWith(subtype, off)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Reads specified mail part's content catching possible <code>java.io.CharConversionException</code>.
     *
     * @param mailPart The mail part
     * @param charset The charset to use
     * @return The mail part's content as a string
     * @throws OXException If a mail error occurs
     * @throws IOException If an I/O error occurs
     */
    static String readContent(final MailPart mailPart, final String charset) throws OXException, IOException {
        try {
            return MessageUtility.readMailPart(mailPart, charset);
        } catch (final java.io.CharConversionException e) {
            // Obviously charset was wrong or bogus implementation of character conversion
            final String fallback = "US-ASCII";
            if (LOG.isWarnEnabled()) {
                LOG.warn(new com.openexchange.java.StringAllocator("Character conversion exception while reading content with charset \"").append(charset).append(
                    "\". Using fallback charset \"").append(fallback).append("\" instead."), e);
            }
            return MessageUtility.readMailPart(mailPart, fallback);
        }
    }

    private static final String TEXT = "text/";

    private static String getCharset(final MailPart mailPart, final ContentType contentType) throws OXException {
        final String charset;
        if (mailPart.containsHeader(MessageHeaders.HDR_CONTENT_TYPE)) {
            String cs = contentType.getCharsetParameter();
            if (!CharsetDetector.isValid(cs)) {
                if (null != cs) {
                    LOG.warn("Illegal or unsupported encoding in a message detected: \"" + cs + '"', new UnsupportedEncodingException(cs));
                }
                if (contentType.startsWith(TEXT)) {
                    cs = CharsetDetector.detectCharset(mailPart.getInputStream());
                } else {
                    cs = MailProperties.getInstance().getDefaultMimeCharset();
                }
            }
            charset = cs;
        } else {
            if (contentType.startsWith(TEXT)) {
                charset = CharsetDetector.detectCharset(mailPart.getInputStream());
            } else {
                charset = MailProperties.getInstance().getDefaultMimeCharset();
            }
        }
        return charset;
    }

    /**
     * Creates a {@link String} from given array of {@link InternetAddress} instances through invoking
     * {@link InternetAddress#toUnicodeString()}
     *
     * @param addrs The array of {@link InternetAddress} instances
     * @return A comma-separated list of addresses as a {@link String}
     */
    static String addrs2String(final InternetAddress[] addrs) {
        final com.openexchange.java.StringAllocator tmp = new com.openexchange.java.StringAllocator(addrs.length << 4);
        boolean first = true;
        for (int i = 0; i < addrs.length; i++) {
            final String string = addr2String(addrs[i]);
            if (!isEmpty(string)) {
                if (first) {
                    first = false;
                } else {
                    tmp.append(", ");
                }
                tmp.append(string);
            }
        }
        return first ? "" : tmp.toString();
    }

    /**
     * Creates a {@link String} from given {@link InternetAddress} instance.
     *
     * @param addrs The {@link InternetAddress} instance
     * @return The address string
     */
    static String addr2String(final InternetAddress addr) {
        if (null == addr) {
            return "";
        }
        final String sAddress = addr.getAddress();
        final int pos = null == sAddress ? 0 : sAddress.indexOf('/');
        if (pos <= 0) {
            // No slash character present
            return addr.toUnicodeString();
        }
        final StringAllocator sb = new StringAllocator(32);
        final String personal = addr.getPersonal();
        if (null == personal) {
            sb.append(MimeProcessingUtility.prepareAddress(sAddress.substring(0, pos)));
        } else {
            sb.append(MimeProcessingUtility.preparePersonal(personal));
            sb.append(" <").append(MimeProcessingUtility.prepareAddress(sAddress.substring(0, pos))).append('>');
        }
        return sb.toString();
    }

    private static final String CT_TEXT_HTM = "text/htm";

    /**
     * Appends the appropriate text version dependent on root's content type and current text's content type
     *
     * @param rootType The root's content type
     * @param contentType Current text's content type
     * @param text The text content
     * @param textBuilder The text builder to append to
     */
    static void appendRightVersion(final ContentType rootType, final ContentType contentType, final String text, final StringBuilder textBuilder) {
        if (rootType.getBaseType().equalsIgnoreCase(contentType.getBaseType())) {
            textBuilder.append(text);
        } else if (rootType.startsWith(CT_TEXT_HTM)) {
            textBuilder.append(htmlFormat(text));
        } else {
            textBuilder.append(ServerServiceRegistry.getInstance().getService(HtmlService.class).html2text(text, false));
            // textBuilder.append(new Html2TextConverter().convertWithQuotes(text));
        }
    }

    /**
     * Prepares specified personal string by surrounding it with quotes if needed.
     *
     * @param personal The personal
     * @return The prepared personal
     */
    static String preparePersonal(final String personal) {
        return MimeMessageUtility.quotePhrase(personal, false);
    }

    private static final String DUMMY_DOMAIN = "@unspecified-domain";

    /**
     * Prepares given address string by checking for possible mail-safe encodings.
     *
     * @param address The address
     * @return The prepared address
     */
    static String prepareAddress(final String address) {
        final String decoded = MimeMessageUtility.decodeMultiEncodedHeader(address);
        final int pos = decoded.indexOf(DUMMY_DOMAIN);
        if (pos >= 0) {
            return decoded.substring(0, pos);
        }
        return decoded;
    }

    /** Check for an empty string */
    static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }


}
