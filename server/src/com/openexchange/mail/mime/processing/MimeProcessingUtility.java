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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static com.openexchange.mail.text.HTMLProcessing.getConformHTML;
import static com.openexchange.mail.text.HTMLProcessing.htmlFormat;
import java.io.IOException;
import java.util.Locale;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.utils.MIMEMessageUtility;
import com.openexchange.mail.text.parser.HTMLParser;
import com.openexchange.mail.text.parser.handler.HTML2TextHandler;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.CharsetDetector;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mail.uuencode.UUEncodedMultiPart;

/**
 * {@link MimeProcessingUtility} - Provides some utility methods for {@link MimeForward} and {@link MimeReply}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeProcessingUtility {

    /**
     * No instantiation
     */
    private MimeProcessingUtility() {
        super();
    }

    /**
     * Checks if given part's disposition is inline; meaning more likely a regular message body than an attachment.
     * 
     * @param part The message's part
     * @param contentType The part's Content-Type header
     * @return <code>true</code> if given part is considered to be an inline part; otherwise <code>false</code>
     * @throws MailException If part's headers cannot be accessed or parsed
     */
    static boolean isInline(final MailPart part, final ContentType contentType) throws MailException {
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
     * @throws MailException If part's filename cannot be determined
     */
    static boolean fileNameEndsWith(final String suffix, final MailPart part, final ContentType contentType) throws MailException {
        final String filename = getFileName(part, contentType);
        return null == filename ? false : filename.toLowerCase(Locale.ENGLISH).endsWith(suffix);
    }

    /**
     * Gets specified part's filename.
     * 
     * @param part The part whose filename shall be returned
     * @param contentType The part's Content-Type header
     * @return The filename or <code>null</code>
     * @throws MailException If part's filename cannot be returned
     */
    private static String getFileName(final MailPart part, final ContentType contentType) throws MailException {
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
        return MIMEMessageUtility.decodeMultiEncodedHeader(filename);
    }

    /**
     * Determines the proper text version according to user's mail settings. Given content type is altered accordingly
     * 
     * @param textPart The text part
     * @param contentType The text part's content type
     * @return The proper text version
     * @throws MessagingException
     * @throws IOException
     * @throws MailException
     */
    static String handleInlineTextPart(final MailPart textPart, final ContentType contentType, final UserSettingMail usm) throws MessagingException, IOException, MailException {
        String charset = contentType.getCharsetParameter();
        if (null == charset) {
            charset = CharsetDetector.detectCharset(textPart.getInputStream());
        }
        if (contentType.isMimeType(MIMETypes.MIME_TEXT_HTM_ALL)) {
            if (usm.isDisplayHtmlInlineContent()) {
                return MessageUtility.readMailPart(textPart, charset);
            }
            contentType.setBaseType("text/plain");
            final HTML2TextHandler handler = new HTML2TextHandler((int) textPart.getSize(), false);
            HTMLParser.parse(getConformHTML(MessageUtility.readMailPart(textPart, charset), contentType), handler);
            return handler.getText();
            // return new Html2TextConverter().convertWithQuotes(MessageUtility.readMimePart(textPart, contentType));
        } else if (contentType.isMimeType(MIMETypes.MIME_TEXT_PLAIN)) {
            final String content = MessageUtility.readMailPart(textPart, charset);
            final UUEncodedMultiPart uuencodedMP = new UUEncodedMultiPart(content);
            if (uuencodedMP.isUUEncoded()) {
                /*
                 * UUEncoded content detected. Extract normal text.
                 */
                return uuencodedMP.getCleanText();
            }
            return content;
        }
        return MessageUtility.readMailPart(textPart, charset);
    }

    /**
     * Creates a {@link String} from given array of {@link InternetAddress} instances through invoking
     * {@link InternetAddress#toUnicodeString()}
     * 
     * @param addrs The rray of {@link InternetAddress} instances
     * @return A comma-separated list of addresses as a {@link String}
     */
    static String addrs2String(final InternetAddress[] addrs) {
        final StringBuilder tmp = new StringBuilder(addrs.length << 4);
        tmp.append(addrs[0].toUnicodeString());
        for (int i = 1; i < addrs.length; i++) {
            tmp.append(", ").append(addrs[i].toUnicodeString());
        }
        return tmp.toString();
    }

    /**
     * Appends the appropriate text version dependent on root's content type and current text's content type
     * 
     * @param rootType The root's content type
     * @param contentType Current text's content type
     * @param text The text content
     * @param textBuilder The text builder to append to
     * @throws IOException
     */
    static void appendRightVersion(final ContentType rootType, final ContentType contentType, final String text, final StringBuilder textBuilder) throws IOException {
        if (rootType.getBaseType().equalsIgnoreCase(contentType.getBaseType())) {
            textBuilder.append(text);
        } else if (rootType.isMimeType(MIMETypes.MIME_TEXT_HTM_ALL)) {
            textBuilder.append(htmlFormat(text));
        } else {
            final HTML2TextHandler handler = new HTML2TextHandler(text.length(), false);
            HTMLParser.parse(getConformHTML(text, contentType), handler);
            textBuilder.append(handler.getText());
            // textBuilder.append(new Html2TextConverter().convertWithQuotes(text));
        }
    }

}
