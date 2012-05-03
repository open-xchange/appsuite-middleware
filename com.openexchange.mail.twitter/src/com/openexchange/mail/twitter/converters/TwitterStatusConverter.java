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

package com.openexchange.mail.twitter.converters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.text.HtmlProcessing;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.twitter.Status;
import com.openexchange.twitter.User;

/**
 * {@link TwitterStatusConverter} - Converts twitter status to a mail message.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterStatusConverter {

    private static final Pattern PATTERN_BODY = Pattern.compile("(<body[^>]*>)");

    private static final Pattern PATTERN_USERREF = Pattern.compile("@([\\d\\w]+)");

    /**
     * Initializes a new {@link TwitterStatusConverter}.
     */
    private TwitterStatusConverter() {
        super();
    }

    /**
     * Converts given twitter status to a mail message.
     *
     * @param status The twitter status
     * @param accountId The account ID
     * @param accountName The account name
     * @return An appropriate mail message
     * @throws OXException If conversion fails
     */
    public static MailMessage convertStatus2Message(final Status status, final int accountId, final String accountName) throws OXException {
        try {
            final byte[] asciiBytes;
            {
                /*
                 * Use JavaMail library to compose an appropriate message
                 */
                final MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());
                final String text = status.getText();
                /*
                 * Headers
                 */
                final User statusUser = status.getUser();
                mimeMessage.setFrom(new QuotedInternetAddress(
                    statusUser.getName().replaceAll("[ \t]+", "_") + "@twitter.com",
                    statusUser.getScreenName(),
                    "UTF-8"));
                mimeMessage.setSubject(getStartingText(text), "UTF-8");
                mimeMessage.setRecipient(RecipientType.TO, new QuotedInternetAddress("all@twitter.com"));
                mimeMessage.setSentDate(status.getCreatedAt());
                /*
                 * Content
                 */
                final MimeMultipart mimeMultipart = new MimeMultipart("alternative");
                /*
                 * Text part
                 */
                {
                    final MimeBodyPart textPart = new MimeBodyPart();
                    textPart.setText(text, "UTF-8", "plain");
                    textPart.setHeader("Content-Type", "text/plain; charset=UTF-8");
                    textPart.setHeader("Content-Disposition", "inline");
                    textPart.setHeader("MIME-Version", "1.0");
                    mimeMultipart.addBodyPart(textPart);
                }
                /*
                 * HTML part
                 */
                {
                    String htmlContent =
                        HtmlProcessing.getConformHTML(HtmlProcessing.formatHrefLinks(HtmlProcessing.htmlFormat(text)), "UTF-8");

                    {
                        final Matcher userMat = PATTERN_USERREF.matcher(htmlContent);
                        htmlContent = userMat.replaceAll("<a href=\"http://twitter.com/$1\">$0</a>");
                    }

                    {
                        final Matcher m = PATTERN_BODY.matcher(htmlContent);
                        htmlContent = m.replaceAll(MessageFormat.format("$1\r\n    <img src=\"{0}\" />", statusUser.getProfileImageURL()));
                    }

                    final MimeBodyPart htmlPart = new MimeBodyPart();
                    htmlPart.setText(htmlContent, "UTF-8", "html");
                    htmlPart.setHeader("Content-Type", "text/html; charset=UTF-8");
                    htmlPart.setHeader("Content-Disposition", "inline");
                    htmlPart.setHeader("MIME-Version", "1.0");
                    mimeMultipart.addBodyPart(htmlPart);
                }
                mimeMessage.setContent(mimeMultipart);
                mimeMessage.saveChanges();
                /*
                 * Write
                 */
                final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(2048);
                mimeMessage.writeTo(out);
                asciiBytes = out.toByteArray();
            }
            /*
             * Get a MailMessage object
             */
            final MailMessage mm = MimeMessageConverter.convertMessage(asciiBytes);
            mm.setMailId(Long.toString(status.getId()));
            mm.setFolder("INBOX");
            mm.setAccountId(accountId);
            mm.setAccountName(accountName);
            mm.setReceivedDate(status.getCreatedAt());
            mm.setSize(asciiBytes.length);
            return mm;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final UnsupportedEncodingException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static String getStartingText(final String text) {
        final int len = text.length();
        if (len <= 32) {
            return text;
        }
        return text.substring(0, 32).trim();
    }

    private static byte[] stream2ByteArray(final InputStream is) throws IOException {
        final UnsynchronizedByteArrayOutputStream tmp = new UnsynchronizedByteArrayOutputStream(8192 * 2);
        final byte[] buf = new byte[8192];
        int read = -1;
        while ((read = is.read(buf, 0, buf.length)) != -1) {
            tmp.write(buf, 0, read);
        }
        return tmp.toByteArray();
    }

}
