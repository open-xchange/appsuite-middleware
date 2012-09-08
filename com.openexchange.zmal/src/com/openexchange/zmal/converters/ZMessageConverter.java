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

package com.openexchange.zmal.converters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.activation.DataHandler;
import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Flags.Flag;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.zmal.ZmalException;
import com.openexchange.zmal.config.ZmalConfig;
import com.zimbra.cs.zclient.ZEmailAddress;
import com.zimbra.cs.zclient.ZMessage;
import com.zimbra.cs.zclient.ZMessage.ZMimePart;

/**
 * {@link ZMessageConverter}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ZMessageConverter {

    private final String url;
    private final ZmalConfig config;

    /**
     * Initializes a new {@link ZMessageConverter}.
     */
    public ZMessageConverter(final String url, final ZmalConfig config) {
        super();
        this.url = url;
        this.config = config;
    }

    public MailMessage convert(ZMessage message) throws OXException {
        if (null == message) {
            return null;
        }
        try {
            boolean hasAttachments = false;
            final MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());
            for (ZEmailAddress zEmailAddress : message.getEmailAddresses()) {
                final QuotedInternetAddress addr = new QuotedInternetAddress(zEmailAddress.getFullAddress());
                if (zEmailAddress.isBcc()) {
                    mimeMessage.addRecipient(RecipientType.BCC, addr);
                } else if (zEmailAddress.isCc()) {
                    mimeMessage.addRecipient(RecipientType.CC, addr);
                } else if (zEmailAddress.isFrom()) {
                    mimeMessage.addFrom(new InternetAddress[] { addr });
                } else if (zEmailAddress.isReplyTo()) {
                    mimeMessage.setReplyTo(new InternetAddress[] { addr });
                } else if (zEmailAddress.isSender()) {
                    mimeMessage.setSender(addr);
                } else if (zEmailAddress.isTo()) {
                    mimeMessage.addRecipient(RecipientType.TO, addr);
                }
            }
            {
                final String sFlags = message.getFlags();
                if (!isEmpty(sFlags)) {
                    final Flags flags = new Flags();
                    if (sFlags.indexOf('u') < 0) {
                        flags.add(Flag.SEEN);
                    }
                    if (sFlags.indexOf('a') >= 0) {
                        hasAttachments = true;
                    }
                    if (sFlags.indexOf('f') >= 0) {
                        flags.add(Flag.FLAGGED);
                    }
                    if (sFlags.indexOf('d') >= 0) {
                        flags.add(Flag.DRAFT);
                    }
                    if (sFlags.indexOf('x') >= 0) {
                        flags.add(Flag.DELETED);
                    }
                    if (sFlags.indexOf('r') >= 0) {
                        flags.add(Flag.ANSWERED);
                    }
                    if (sFlags.indexOf('w') >= 0) {
                        flags.add(MailMessage.USER_FORWARDED);
                    }
                    if (sFlags.indexOf('n') >= 0) {
                        flags.add(MailMessage.USER_READ_ACK);
                    }
                    if (sFlags.indexOf('!') >= 0) {
                        mimeMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "High");
                    }
                    if (sFlags.indexOf('?') >= 0) {
                        mimeMessage.setHeader(MessageHeaders.HDR_IMPORTANCE, "Low");
                    }
                }
            }
            {
                final String sInReplyTo = message.getInReplyTo();
                if (!isEmpty(sInReplyTo)) {
                    mimeMessage.setHeader(MessageHeaders.HDR_IN_REPLY_TO, sInReplyTo);
                }
            }
            {
                final String s = message.getMessageIdHeader();
                if (!isEmpty(s)) {
                    mimeMessage.setHeader(MessageHeaders.HDR_MESSAGE_ID, s);
                }
            }
            {
                final String s = message.getSubject();
                if (!isEmpty(s)) {
                    mimeMessage.setSubject(s);
                }
            }
            {
                final long l = message.getSentDate();
                if (l > 0) {
                    mimeMessage.setSentDate(new Date(l));
                }
            }
            final String id = message.getId();
            final ZMimePart mimeStructure = message.getMimeStructure();
            if (null != mimeStructure) {
                final MimeBodyPart part = parsePart(mimeStructure, id);
                final String sct = part.getHeader("Content-Type", null);
                if (null != sct && sct.toLowerCase(Locale.US).startsWith("multipart/")) {
                    mimeMessage.setContent((Multipart) part.getContent());
                } else {
                    for (@SuppressWarnings("unchecked") final Enumeration<Header> headers = part.getAllHeaders(); headers.hasMoreElements();) {
                        final Header hdr = headers.nextElement();
                        mimeMessage.addHeader(hdr.getName(), hdr.getValue());
                    }
                    mimeMessage.setDataHandler(part.getDataHandler());
                }
            }

            // Convert to MailMessage instance
            final MailMessage mailMessage = MimeMessageConverter.convertMessage(mimeMessage, false);
            // Check folder
            {
                final String sFolder = message.getFolderId();
                if (!isEmpty(sFolder)) {
                    mailMessage.setFolder(sFolder);
                }
            }
            // Check folder
            {
                final String sOrigid = message.getOriginalId();
                if (!isEmpty(sOrigid)) {
                    mailMessage.setMsgref(new MailPath(0, mailMessage.getFolder(), sOrigid));
                }
            }
            mailMessage.setHasAttachment(hasAttachments);
            if (!isEmpty(id)) {
                mailMessage.setMailId(id);
            }
            return mailMessage;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public MimeBodyPart parsePart(final ZMimePart part, final String mailId) throws OXException {
        try {
            final ContentType contentType;
            {
                final String s = part.getContentType();
                contentType = isEmpty(s) ? new ContentType("text/plain; charset=IOS-8859-1") : new ContentType(s);
            }
            ContentDisposition contentDisposition = null;
            {
                String s = part.getContentDispostion();
                if (!isEmpty(s)) {
                    contentDisposition = new ContentDisposition(s);
                }
            }
            final MimeBodyPart bodyPart = new MimeBodyPart();
            {
                String s = part.getContentId();
                if (!isEmpty(s)) {
                    bodyPart.setHeader(MessageHeaders.HDR_CONTENT_ID, s);
                }
            }
            {
                final String value = part.getContentLocation();
                if (!isEmpty(value)) {
                    bodyPart.setHeader("Content-Location", value);
                }
            }
            // Check for sub-parts
            List<ZMimePart> children = part.getChildren();
            if (null != children && !children.isEmpty()) {
                if (!contentType.startsWith("multipart/")) {
                    contentType.setBaseType("multipart/mixed");
                }
                final MimeMultipart multipart = new MimeMultipart();
                for (final ZMimePart subpart : children) {
                    final MimeBodyPart part2 = parsePart(subpart, mailId);
                    if (null != part2) {
                        multipart.addBodyPart(part2);
                    }
                }
                bodyPart.setContent(multipart);
                return bodyPart;
            }
            /*-
             * ----------------- Not a multipart - read its content -----------------
             */
            if (!isEmpty(mailId)) {
                setDataHandler(part, contentType, bodyPart, mailId);
            }
            // Set headers
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType.toString());
            bodyPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
            if (null != contentDisposition) {
                bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, contentDisposition.toString());
                if (contentDisposition.isAttachment()) {
                    bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
                }
            }
            return bodyPart;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void setDataHandler(final ZMimePart part, final ContentType contentType, final MimeBodyPart bodyPart, final String messageId) throws OXException, MessagingException {
        HttpURLConnection conn = null;
        try {
            // /service/content/get?id={message-id}[&fmt={fmt-type}][&part={part-name}][&subId={subId}]
            final StringBuilder sb = new StringBuilder(url);
            sb.append("/service/content/get?id=").append(urlEncode(messageId));
            final String partName = part.getPartName();
            if (!isEmpty(partName)) {
                sb.append("&part=").append(urlEncode(partName));
            }
            final String subId = null;
            if (!isEmpty(subId)) {
                sb.append("&subId=").append(urlEncode(subId));
            }
            // Establish URL connection
            conn = (HttpURLConnection) (new URL(sb.toString())).openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setAllowUserInteraction(false);
            conn.setUseCaches(false);
            conn.setRequestProperty("User-Agent", "Open-Xchange-Http-Client");
            // timeouts
            final int connectTimeout = config.getZmalProperties().getZmalConnectionTimeout();
            if (connectTimeout >= 0) {
                conn.setConnectTimeout(connectTimeout);
            }

            final int readTimeout = config.getZmalProperties().getZmalTimeout();
            if (readTimeout >= 0) {
                conn.setReadTimeout(readTimeout);
            }

            // Authenticate
            {
                final Map<String, List<String>> httpHeaders = new HashMap<String, List<String>>(4);
                // Get user and password
                final String user = config.getLogin();
                final String password = config.getPassword();
                // If no user is set, don't set basic auth header
                if (user != null) {
                    httpHeaders.put("Authorization", createBasicAuthHeaderValue(user, password));
                }
                // Add them
                for (final Map.Entry<String, List<String>> header : httpHeaders.entrySet()) {
                    if (header.getValue() != null) {
                        for (final String value : header.getValue()) {
                            conn.addRequestProperty(header.getKey(), value);
                        }
                    }
                }
            }

            // connect
            conn.connect();

            // get stream, if present
            final int respCode = conn.getResponseCode();
            if ((respCode != 200) && (respCode != 201) && (respCode != 203) && (respCode != 206)) {
                throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, respCode + " - " + conn.getResponseMessage());
            }
            final InputStream inputStream = conn.getInputStream();
            try {
                final ByteArrayOutputStream out = Streams.newByteArrayOutputStream(8192);
                final byte[] buf = new byte[2048];
                for (int read = inputStream.read(buf, 0, buf.length); read > 0; read = inputStream.read(buf, 0, buf.length)) {
                    out.write(buf, 0, read);
                }
                out.flush();
                bodyPart.setDataHandler(new DataHandler(new MessageDataSource(out.toByteArray(), contentType.toString())));
            } finally {
                Streams.close(inputStream);
            }
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } finally {
            if (null != conn) {
                conn.disconnect();
            }
        }
    }

    /**
     * Creates a basic authentication header value from a username and a password.
     */
    private static List<String> createBasicAuthHeaderValue(final String username, final String password) {
        String pw = password;
        if (pw == null) {
            pw = "";
        }
        try {
            return Collections.singletonList("Basic " + org.apache.commons.codec.binary.Base64.encodeBase64((username + ":" + pw).getBytes(Charsets.ISO_8859_1)));
        } catch (final UnsupportedCharsetException e) {
            // shouldn't happen...
            return Collections.emptyList();
        }
    }

    private static final String ISO_8859_1 = "ISO-8859-1";

    /**
     * Generates URL-encoding of specified text.
     * 
     * @param text The text
     * @return The URL-encoded text
     */
    private static String urlEncode(final String text) {
        try {
            return URLEncoder.encode(text, ISO_8859_1);
        } catch (final UnsupportedEncodingException e) {
            // cannot occur
            return text;
        }
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
