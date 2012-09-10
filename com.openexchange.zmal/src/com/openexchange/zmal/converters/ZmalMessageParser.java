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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.activation.DataHandler;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
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
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.zmal.ZmalException;
import com.openexchange.zmal.ZmalSoapPerformer;
import com.openexchange.zmal.ZmalSoapResponse;
import com.openexchange.zmal.ZmalException.Code;
import com.openexchange.zmal.config.ZmalConfig;
import com.zimbra.common.soap.Element;

/**
 * {@link ZmalMessageParser}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ZmalMessageParser {

    private final ZmalSoapPerformer performer;

    /**
     * Initializes a new {@link ZmalMessageParser}.
     */
    public ZmalMessageParser(final ZmalSoapPerformer performer) {
        super();
        this.performer = performer;
    }

    /**
     * Parses messages from specified SOAP response.
     * 
     * @param response The SOAP response
     * @return The parsed messages
     * @throws OXException If parsing fails
     */
    public List<MailMessage> parseMessages(final ZmalSoapResponse response) throws OXException {
        final List<Element> results = response.getResults();
        final List<MailMessage> mails = new ArrayList<MailMessage>(results.size());
        for (final Element element : results) {
            mails.addAll(parseElement(element));
        }
        return mails;
    }

    /**
     * Parses specified element to its mail representation(s).
     * 
     * @param element The element
     * @return The mail representation(s)
     * @throws OXException If parsing element fails
     */
    public List<MailMessage> parseElement(final Element element) throws OXException {
        if (null == element) {
            return null;
        }
        // Check if current element denotes a message
        if (isMessage(element)) {
            return Collections.singletonList(parseSingleMessage(element));
        }
        final List<Element> elements = element.listElements();
        final List<MailMessage> mails = new ArrayList<MailMessage>(elements.size());
        for (final Element sub : elements) {
            final MailMessage mailMessage = parseSingleMessage(sub);
            if (null != mailMessage) {
                mails.add(mailMessage);
            }
        }
        return mails;
    }

    /**
     * <pre>
     * &lt;m id="{message-id}" f="{flags}" s="{size}" d="{date}" cid="{conv-id}" l="{folder} origid="{original-id}"&gt;
     *    &lt;content&gt;....&lt;/content&gt;*
     *    &lt;e .../&gt;*
     *    &lt;su&gt;{subject}&lt;/su&gt;
     *    &lt;fr&gt;{fragment}&lt;/fr&gt;
     *    
     *    &lt;mid&gt;{Message-ID header}&lt;/mid&gt;
     *    [&lt;inv&gt;...&lt;/inv&gt;]
     *    [&lt;mp&gt;...&lt;/mp&gt;]
     *    [&lt;content (url="{url}")&gt;...&lt;/content&gt;]
     *   &lt;/m&gt;
     * 
     *   {content} = complete rfc822 message. only present during certain operations that deal with the raw content
     *               of a messasage.  There is at most 1 content element.
     *   {conv-id}  = converstation id. only present if &lt;m&gt; is not enclosed within a &lt;c&gt; element
     *   {size}     = size in bytes
     *   {flags}    = (u)nread, (f)lagged, has (a)ttachment, (r)eplied, (s)ent by me, for(w)arded,
     *                (d)raft, deleted (x), (n)otification sent
     *   {date}     = secs since epoch, from date header in message
     *   {original-id} = message id of message being replied to/forwarded (outbound messages only)
     *   {url}      = content servlet relative url for retrieving message content
     *   {subject}  = subject of the message, only returned on an expanded message
     *   {fragment} = first n-bytes of the message (probably between 40-100)
     *   &lt;e .../&gt;*  = zero or more addresses in the message, indentified by
     *   type (t="f|t|c")
     *   &lt;inv ...&gt;...&lt;/inv&gt; = Parsed out iCal invite.  See soap-calendar.txt
     *   &lt;mp ...&gt;...&lt;/mp&gt; =  The root MIME part of the message.  There is exactly 1 MIME part under
     *                a message element.  The "body" will be tagged with body="1", and the content 
     *                of the body will also be present
     *   &lt;content&gt;  = the raw content of the message.  cannot have more than one of &lt;mp&gt;, &lt;content&gt; url, and &lt;content&gt; body.
     * </pre>
     * 
     * @param element
     * @return
     * @throws OXException 
     */
    private MailMessage parseSingleMessage(final Element element) throws OXException {
        if (!isMessage(element)) {
            return null;
        }
        try {
            final MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());
            boolean hasAttachments = false;
            {
                final String sFlags = element.getAttribute("f", "");
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
                final String sDate = element.getAttribute("d", "");
                if (!isEmpty(sDate)) {
                    try {
                        long l = Long.parseLong(sDate);
                        l *= 1000; // to msec
                        mimeMessage.setSentDate(new Date(l));
                    } catch (final NumberFormatException e) {
                        // Ignore
                    }
                }
            }
            // Check addresses
            for (final Element addrElement : element.listElements("e")) {
                parseAddress(addrElement, mimeMessage);
            }
            // Check subject
            {
                final Element subjectElement = element.getOptionalElement("su");
                if (null != subjectElement) {
                    mimeMessage.setSubject(subjectElement.getText());
                }
            }
            // Check Message-Id
            {
                final Element midElement = element.getOptionalElement("mid");
                if (null != midElement) {
                    mimeMessage.setHeader(MessageHeaders.HDR_MESSAGE_ID, midElement.getText());
                }
            }
            // Check part
            {
                final Element mpElement = element.getOptionalElement("mp");
                if (null != mpElement) {
                    final MimeBodyPart part = parsePart(mpElement);
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
            }
            // Convert to MailMessage instance
            final MailMessage mailMessage = MimeMessageConverter.convertMessage(mimeMessage, false);
            // Check folder
            {
                final String sFolder = element.getAttribute("l", "");
                if (!isEmpty(sFolder)) {
                    mailMessage.setFolder(sFolder);
                }
            }
            // Check folder
            {
                final String sOrigid = element.getAttribute("origid", "");
                if (!isEmpty(sOrigid)) {
                    mailMessage.setMsgref(new MailPath(0, mailMessage.getFolder(), sOrigid));
                }
            }
            mailMessage.setHasAttachment(hasAttachments);
            final String id = element.getAttribute("id", "");
            if (!isEmpty(id)) {
                mailMessage.setMailId(id);
            }
            return mailMessage;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    /*-
     * <m id="{message-id}" f="{flags}" s="{size}" d="{date}" cid="{conv-id}" l="{folder} origid="{original-id}">
       <content>....</content>*
       <e .../>*
       <su>{subject}</su>
       <fr>{fragment}</fr>
       
       <mid>{Message-ID header}</mid>
       [<inv>...</inv>]
       [<mp>...</mp>]
       [<content (url="{url}")>...</content>]
      </m>
    
      {content} = complete rfc822 message. only present during certain operations that deal with the raw content
                  of a messasage.  There is at most 1 content element.
      {conv-id}  = converstation id. only present if <m> is not enclosed within a <c> element
      {size}     = size in bytes
      {flags}    = (u)nread, (f)lagged, has (a)ttachment, (r)eplied, (s)ent by me, for(w)arded,
                   (d)raft, deleted (x), (n)otification sent
      {date}     = secs since epoch, from date header in message
      {original-id} = message id of message being replied to/forwarded (outbound messages only)
      {url}      = content servlet relative url for retrieving message content
      {subject}  = subject of the message, only returned on an expanded message
      {fragment} = first n-bytes of the message (probably between 40-100)
      <e .../>*  = zero or more addresses in the message, indentified by
      type (t="f|t|c")
      <inv ...>...</inv> = Parsed out iCal invite.  See soap-calendar.txt
      <mp ...>...</mp> =  The root MIME part of the message.  There is exactly 1 MIME part under
                   a message element.  The "body" will be tagged with body="1", and the content 
                   of the body will also be present
      <content>  = the raw content of the message.  cannot have more than one of <mp>, <content> url, and <content> body.
     */
    public static boolean isMessage(final Element element) {
        if (null == element) {
            return false;
        }
        return "m".equals(element.getName());
    }

    /*-
     * <mp part="{mime-part-name}" body="{is-body}" s="{size-in-bytes} mid="{message-id} cid="{conv-id}" [truncated="1"]
           ct="{content-type}" name="{name}" cd="{content-disposition}" filename="{filename} ci="{content-id} cl="{content-location}">
        [<content>{content}</content>]
            <mp part="..." ...>
                <mp part="..." ...>
                </mp>
            </mp>
        </mp>
    
       {mime-part-name} = MIME part, "" means top-level part, 1 first part, 1.1 first part of a multipart inside of 1.
       truncated="1"  = the caller requested a maximum length (max="...") for inlined <content>, and this part's content was truncated down to that length
    
       {content-type} = MIME Content-Type. The mime type is the content of the element.
       {name}         = name attribute from the Content-Type param list
       {cont-disp}    = MIME Content-Disposition
       {filename}     = filename attribute from the Content-Disposition param list
       {content-id}   = MIME Content-ID (for display of embedded images)
       {content-location} = MIME/Microsoft Content-Location (for display of embedded images)   
       {cont-desc} = MIME Content-Description.  Note cont-desc is not currently used in the code.
       {content} = the content of the part, if requested
       {is-body} = set to 1, if this part is considered to be the "body" of the message for display
                   purposes.
       {message-id} = item id of the enclosing message, only present if <mp> is not enclosed within a <m> element
     */
    private MimeBodyPart parsePart(final Element partElement) throws OXException {
        if (null == partElement || !"mp".equals(partElement.getName())) {
            return null;
        }
        try {
            final ContentType contentType = new ContentType(partElement.getAttribute("ct", "text/plain; charset=ISO-8859-1"));
            {
                final String value = partElement.getAttribute("name", "");
                if (!isEmpty(value)) {
                    contentType.setNameParameter(value);
                }
            }
            final ContentDisposition contentDisposition;
            {
                final String scd = partElement.getAttribute("cd", "");
                if (isEmpty(scd)) {
                    contentDisposition = null;
                } else {
                    contentDisposition = new ContentDisposition(scd);
                    final String value = partElement.getAttribute("filename", "");
                    if (!isEmpty(value)) {
                        contentDisposition.setFilenameParameter(value);
                    }
                }
            }
            final MimeBodyPart bodyPart = new MimeBodyPart();
            {
                final String value = partElement.getAttribute("ci", "");
                if (!isEmpty(value)) {
                    bodyPart.setHeader(MessageHeaders.HDR_CONTENT_ID, value);
                }
            }
            {
                final String value = partElement.getAttribute("cl", "");
                if (!isEmpty(value)) {
                    bodyPart.setHeader("Content-Location", value);
                }
            }
            // Check for sub-parts
            final List<Element> parts = partElement.listElements("mp");
            if (null != parts && !parts.isEmpty()) {
                if (!contentType.startsWith("multipart/")) {
                    contentType.setBaseType("multipart/mixed");
                }
                final MimeMultipart multipart = new MimeMultipart();
                for (final Element subpart : parts) {
                    final MimeBodyPart part = parsePart(subpart);
                    if (null != part) {
                        multipart.addBodyPart(part);
                    }
                }
                bodyPart.setContent(multipart);
                return bodyPart;
            }
            /*-
             * ----------------- Not a multipart - read its content -----------------
             */
            final String messageId = partElement.getAttribute("mid", "");
            if (!isEmpty(messageId)) {
                setDataHandler(partElement, contentType, bodyPart, messageId);
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
        }
    }

    private void setDataHandler(final Element partElement, final ContentType contentType, final MimeBodyPart bodyPart, final String messageId) throws OXException, MessagingException {
        HttpURLConnection conn = null;
        try {
            // /service/content/get?id={message-id}[&fmt={fmt-type}][&part={part-name}][&subId={subId}]
            final ZmalConfig config = performer.getConfig();
            final StringBuilder sb = new StringBuilder(64).append(config.isSecure() ? "https" : "http");
            sb.append("://").append(config.getServer());
            final int port = config.getPort();
            if (port > 0 && port != URIDefaults.IMAP.getPort()) {
                sb.append(port);
            }
            sb.append("/service/content/get?id=").append(urlEncode(messageId));
            final String partName = partElement.getAttribute("part", "");
            if (!isEmpty(partName)) {
                sb.append("&part=").append(urlEncode(partName));
            }
            final String subId = partElement.getAttribute("subId", "");
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

    /*-
     *  <e [t="{type}"] p="{personal-name}" a="{email-address}" d="{display-name}">{content}</e>

       {type} = (f)rom, (t)o, (c)c, (b)cc, (r)eply-to, (s)ender, read-receipt (n)otification
     */
    private static void parseAddress(final Element addrElement, final MimeMessage mimeMessage) throws OXException {
        if (null == addrElement || !"e".equals(addrElement.getName())) {
            return;
        }
        final QuotedInternetAddress address = new QuotedInternetAddress();
        boolean personal = false;
        String displayName = null;
        final List<Element> listElements = addrElement.listElements();
        for (final Element sub : listElements) {
            final String name = sub.getName();
            if ("p".equals(name)) {
                try {
                    address.setPersonal(sub.getText(), "UTF-8");
                    personal = true;
                } catch (final UnsupportedEncodingException e) {
                    // Cannot occur
                }
            } else if ("a".equals(name)) {
                address.setAddress(sub.getText());
            } else if ("d".equals(name)) {
                displayName = sub.getText();
            }
        }
        if (isEmpty(address.getAddress())) {
            // Address is empty
            return;
        }
        if (!personal && null != displayName) {
            try {
                address.setPersonal(displayName, "UTF-8");
            } catch (final UnsupportedEncodingException e) {
                // Cannot occur
            }
        }
        // Add to MIME message
        for (final Element sub : listElements) {
            if ("t".equals(sub.getName())) {
                final String text = sub.getText();
                if (!isEmpty(text)) {
                    try {
                        final char c = text.charAt(0);
                        if ('f' == c) {
                            mimeMessage.addFrom(new InternetAddress[] { address });
                        } else if ('t' == c) {
                            mimeMessage.addRecipient(RecipientType.TO, address);
                        } else if ('c' == c) {
                            mimeMessage.addRecipient(RecipientType.CC, address);
                        } else if ('b' == c) {
                            mimeMessage.addRecipient(RecipientType.BCC, address);
                        } else if ('s' == c) {
                            mimeMessage.setSender(address);
                        } else if ('n' == c) {
                            mimeMessage.setHeader(MessageHeaders.HDR_DISP_TO, address.toString());
                        }
                    } catch (final MessagingException e) {
                        throw MimeMailException.handleMessagingException(e);
                    }
                }
                break;
            }
            
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

    /**
     * Creates a basic authentication header value from a username and a
     * password.
     */
    private static List<String> createBasicAuthHeaderValue(final String username, final String password) {
        String pw = password;
        if (pw == null) {
            pw = "";
        }
        try {
            return Collections.singletonList("Basic "
                    + org.apache.commons.codec.binary.Base64.encodeBase64((username + ":" + pw).getBytes(Charsets.ISO_8859_1)));
        } catch (final UnsupportedCharsetException e) {
            // shouldn't happen...
            return Collections.emptyList();
        }
    }

}
