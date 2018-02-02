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

package com.openexchange.mail.mime.converters;

import static com.openexchange.mail.mime.utils.MimeMessageUtility.unfold;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeFilter;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.PlainTextAddress;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;

/**
 * {@link MimeMessageUtils}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class MimeMessageUtils {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MimeMessageUtils.class);

    /**
     * Gets the first header denoted by specified header name and decodes its value to a unicode string if necessary.
     *
     * <pre>
     * &quot;=?UTF-8?Q?=C3=BCber?=&quot; is decoded to &quot;&amp;uumlber&quot;
     * </pre>
     *
     * @param name The header name
     * @param message The message providing the header
     * @return The decoded header
     * @throws MessagingException If a messaging error occurs
     */
    public static String getSubject(final Message message) throws MessagingException {
        final String[] valueArr = message.getHeader(MessageHeaders.HDR_SUBJECT);
        if (null == valueArr || valueArr.length == 0) {
            return null;
        }
        return MimeMessageUtility.decodeEnvelopeSubject(valueArr[0]);
    }

    /**
     * Gets the first header denoted by specified header name and decodes its value to a unicode string if necessary.
     *
     * <pre>
     * &quot;=?UTF-8?Q?=C3=BCber?=&quot; is decoded to &quot;&amp;uumlber&quot;
     * </pre>
     *
     * @param name The header name
     * @param message The message providing the header
     * @return The decoded header
     */
    public static String getSubject(final MailMessage message) {
        final String[] valueArr = message.getHeader(MessageHeaders.HDR_SUBJECT);
        if (null == valueArr || valueArr.length == 0) {
            return null;
        }
        return MimeMessageUtility.decodeEnvelopeSubject(valueArr[0]);
    }

    /**
     * Parses the value of header <code>X-Priority</code>.
     *
     * @param priorityStr The header value
     * @param mailMessage The mail message to fill
     */
    public static void parsePriority(final String priorityStr, final MailMessage mailMessage) {
        mailMessage.setPriority(parsePriority(priorityStr));
    }

    /**
     * Parses the value of header <code>X-Priority</code>.
     *
     * @param priorityStr The header value
     */
    public static int parsePriority(final String priorityStr) {
        int priority = MailMessage.PRIORITY_NORMAL;
        if (null != priorityStr) {
            final String[] tmp = priorityStr.split(" +");
            try {
                priority = Integer.parseInt(tmp[0]);
            } catch (final NumberFormatException nfe) {
                LOG.debug("Assuming priority NORMAL due to strange X-Priority header: {}", priorityStr);
                priority = MailMessage.PRIORITY_NORMAL;
            }
        }
        return priority;
    }

    /**
     * Parses the value of header <code>Importance</code>.
     *
     * @param importance The header value
     * @param mailMessage The mail message to fill
     */
    public static void parseImportance(final String importance, final MailMessage mailMessage) {
        mailMessage.setPriority(parseImportance(importance));
    }

    /**
     * Parses the value of header <code>Importance</code>.
     *
     * @param importance The header value
     */
    public static int parseImportance(final String importance) {
        int priority = MailMessage.PRIORITY_NORMAL;
        if (null != importance) {
            final String imp = importance.trim();
            if ("Low".equalsIgnoreCase(imp)) {
                priority = MailMessage.PRIORITY_LOWEST;
            } else if ("Medium".equalsIgnoreCase(imp) || "Normal".equalsIgnoreCase(imp)) {
                priority = MailMessage.PRIORITY_NORMAL;
            } else if ("High".equalsIgnoreCase(imp)) {
                priority = MailMessage.PRIORITY_HIGHEST;
            } else {
                LOG.debug("Assuming priority NORMAL due to strange Importance header: {}", importance);
                priority = MailMessage.PRIORITY_NORMAL;
            }
        }
        return priority;
    }

    private static final String[] EMPTY_STRS = new String[0];

    /**
     * Parses specified {@link Flags flags} to given {@link MailMessage mail}.
     *
     * @param flags The flags to parse
     * @param mailMessage The mail to apply the flags to
     * @throws OXException If a mail error occurs
     */
    public static void parseFlags(final Flags flags, final MailMessage mailMessage) throws OXException {
        int retval = 0;
        if (flags.contains(Flags.Flag.ANSWERED)) {
            retval |= MailMessage.FLAG_ANSWERED;
        }
        if (flags.contains(Flags.Flag.DELETED)) {
            retval |= MailMessage.FLAG_DELETED;
        }
        if (flags.contains(Flags.Flag.DRAFT)) {
            retval |= MailMessage.FLAG_DRAFT;
        }
        if (flags.contains(Flags.Flag.FLAGGED)) {
            retval |= MailMessage.FLAG_FLAGGED;
        }
        if (flags.contains(Flags.Flag.RECENT)) {
            retval |= MailMessage.FLAG_RECENT;
        }
        if (flags.contains(Flags.Flag.SEEN)) {
            retval |= MailMessage.FLAG_SEEN;
        }
        if (flags.contains(Flags.Flag.USER)) {
            retval |= MailMessage.FLAG_USER;
        }
        final String[] userFlags = flags.getUserFlags();
        if (userFlags != null) {
            /*
             * Mark message to contain user flags
             */
            mailMessage.addUserFlags(EMPTY_STRS);
            for (final String userFlag : userFlags) {
                if (MailMessage.isColorLabel(userFlag)) {
                    mailMessage.setColorLabel(MailMessage.getColorLabelIntValue(userFlag));
                } else if (MailMessage.USER_FORWARDED.equalsIgnoreCase(userFlag)) {
                    retval |= MailMessage.FLAG_FORWARDED;
                } else if (MailMessage.USER_READ_ACK.equalsIgnoreCase(userFlag)) {
                    retval |= MailMessage.FLAG_READ_ACK;
                } else {
                    mailMessage.addUserFlag(userFlag);
                }
            }
        }
        /*
         * Set system flags
         */
        mailMessage.setFlags(retval);
    }

    /**
     * Gets the address headers denoted by specified header name in a safe manner.
     * <p>
     * If strict parsing of address headers yields a {@link AddressException}, then a plain-text version is generated to display broken
     * address header as it is.
     *
     * @param name The address header name
     * @param message The message providing the address header
     * @return The parsed address headers as an array of {@link InternetAddress} instances
     * @throws MessagingException If a messaging error occurs
     */
    public static InternetAddress[] getAddressHeader(final String name, final Message message) throws MessagingException {
        final String[] addressArray = message.getHeader(name);
        if (null == addressArray || addressArray.length == 0) {
            return null;
        }
        final String addresses;
        if (addressArray.length > 1) {
            final StringBuilder sb = new StringBuilder(addressArray[0]);
            for (int i = 1; i < addressArray.length; i++) {
                sb.append(',').append(addressArray[i]);
            }
            addresses = sb.toString();
        } else {
            addresses = addressArray[0];
        }
        try {
            return QuotedInternetAddress.parseHeader(addresses, true);
        } catch (final AddressException e) {
            return getAddressHeaderNonStrict(addresses, addressArray);
        }
    }

    /**
     * Gets the address headers denoted by specified header name in a safe manner.
     * <p>
     * If strict parsing of address headers yields a {@link AddressException}, then a plain-text version is generated to display broken
     * address header as it is.
     *
     * @param name The address header name
     * @param message The message providing the address header
     * @return The parsed address headers as an array of {@link InternetAddress} instances
     */
    public static InternetAddress[] getAddressHeader(final String name, final MailMessage message) {
        final String[] addressArray = message.getHeader(name);
        if (null == addressArray || addressArray.length == 0) {
            return null;
        }
        final String addresses;
        if (addressArray.length > 1) {
            final StringBuilder sb = new StringBuilder(addressArray[0]);
            for (int i = 1; i < addressArray.length; i++) {
                sb.append(',').append(addressArray[i]);
            }
            addresses = sb.toString();
        } else {
            addresses = addressArray[0];
        }
        try {
            return QuotedInternetAddress.parseHeader(addresses, true);
        } catch (final AddressException e) {
            return getAddressHeaderNonStrict(addresses, addressArray);
        }
    }

    private static InternetAddress[] getAddressHeaderNonStrict(final String addressStrings, final String[] addressArray) {
        try {
            final InternetAddress[] addresses = QuotedInternetAddress.parseHeader(addressStrings, false);
            final List<InternetAddress> addressList = new ArrayList<InternetAddress>(addresses.length);
            for (final InternetAddress internetAddress : addresses) {
                try {
                    addressList.add(new QuotedInternetAddress(internetAddress.toString()));
                } catch (final AddressException e) {
                    addressList.add(internetAddress);
                }
            }
            return addressList.toArray(new InternetAddress[addressList.size()]);
        } catch (final AddressException e) {
            LOG.debug("Internet addresses could not be properly parsed. Using plain addresses' string representation instead.", e);
            return getAddressesOnParseError(addressArray);
        }
    }

    /**
     * Gets the address header from given address header value.
     *
     * @param addresses The address header value
     * @return The parsed addresses
     */
    public static InternetAddress[] getAddressHeader(final String addresses) {
        try {
            return QuotedInternetAddress.parseHeader(addresses, true);
        } catch (final AddressException e) {
            LOG.debug("Internet addresses could not be properly parsed. Using plain addresses' string representation instead.", e);
            return PlainTextAddress.parseAddresses(addresses);
        }
    }

    private static InternetAddress[] getAddressesOnParseError(final String[] addrs) {
        List<InternetAddress> list = new LinkedList<InternetAddress>();
        for (int i = 0; i < addrs.length; i++) {
            InternetAddress[] plainAddresses = PlainTextAddress.parseAddresses(addrs[i]);
            if (null != plainAddresses && plainAddresses.length > 0) {
                list.addAll(Arrays.asList(plainAddresses));
            }
        }
        return list.toArray(new InternetAddress[list.size()]);
    }

    /**
     * Returns the value of the RFC 822 "Date" field. This is the date on which this message was sent. Returns <code>null</code> if this
     * field is unavailable or its value is absent.
     *
     * @param part The mail part
     * @return The sent Date
     */
    public static Date getSentDate(final MailPart part) {
        final String s = part.getHeader("Date", null);
        if (s != null) {
            try {
                final MailDateFormat mailDateFormat = MimeMessageUtility.getDefaultMailDateFormat();
                synchronized (mailDateFormat) {
                    return mailDateFormat.parse(s);
                }
            } catch (final ParseException pex) {
                return null;
            }
        }

        return null;
    }

    /**
     * Returns the value of the RFC 822 "Date" field. This is the date on which this message was sent. Returns <code>null</code> if this
     * field is unavailable or its value is absent.
     *
     * @param mimeMessage The MIME message
     * @return The sent Date
     * @throws MessagingException If sent date cannot be returned
     */
    public static Date getSentDate(MimeMessage mimeMessage) throws MessagingException {
        String s = mimeMessage.getHeader("Date", null);
        if (s != null) {
            try {
                MailDateFormat mailDateFormat = MimeMessageUtility.getDefaultMailDateFormat();
                synchronized (mailDateFormat) {
                    return mailDateFormat.parse(s);
                }
            } catch (ParseException pex) {
                return null;
            }
        }

        return null;
    }

    /**
     * Gets the Content-Type for given part.
     *
     * @param part The part
     * @return The parsed Content-Type
     * @throws OXException If parsing fails
     */
    public static ContentType getContentType(final Part part) throws OXException {
        try {
            final String[] tmp = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
            return (tmp != null) && (tmp.length > 0) ? new ContentType(tmp[0]) : new ContentType(MimeTypes.MIME_DEFAULT);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Gets the multipart from passed message.
     *
     * @param message The message
     * @param contentType The message's Content-Type
     * @return The appropriate multipart
     * @throws OXException If content cannot be presented as a multipart
     */
    public static Multipart multipartFor(final MimeMessage message, final ContentType contentType) throws OXException {
        return multipartFor(message, contentType, true);
    }

    private static Multipart multipartFor(final MimeMessage message, final ContentType contentType, final boolean reparse) throws OXException {
        try {
            return multipartFor(message.getContent(), contentType);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final javax.mail.internet.ParseException e) {
            if (!reparse) {
                throw MimeMailException.handleMessagingException(e);
            }
            // Sanitize parameterized headers
            try {
                final String sContentType = message.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null);
                message.setHeader(MessageHeaders.HDR_CONTENT_TYPE, new ContentType(sContentType).toString(true));
                MimeMessageUtility.saveChanges(message);
            } catch (final Exception x) {
                // Content-Type cannot be sanitized
                org.slf4j.LoggerFactory.getLogger(MimeFilter.class).debug("Content-Type cannot be sanitized.", x);
                throw MimeMailException.handleMessagingException(e);
            }
            return multipartFor(message, contentType, false);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Gets the multipart from passed content object.
     *
     * @param content The content object
     * @param contentType The content type
     * @return The appropriate multipart
     * @throws OXException If content cannot be presented as a multipart
     */
    public static Multipart multipartFor(final Object content, final ContentType contentType) throws OXException {
        if (null == content) {
            return null;
        }
        if (content instanceof Multipart) {
            return (Multipart) content;
        }
        if (content instanceof InputStream) {
            try {
                return new MimeMultipart(new MessageDataSource((InputStream) content, contentType));
            } catch (final MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            } catch (final IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                }
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        }
        if (content instanceof String) {
            try {
                return new MimeMultipart(new MessageDataSource(Streams.newByteArrayInputStream(((String) content).getBytes(Charsets.ISO_8859_1)), contentType));
            } catch (final MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            } catch (final IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                }
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        }
        throw MailExceptionCode.MESSAGING_ERROR.create("Content is not of type javax.mail.Multipart, but " + content.getClass().getName());
    }

    /**
     * Converts specified flags bit mask to an instance of {@link Flags}.
     *
     * @param flags The flags bit mask
     * @return The corresponding instance of {@link Flags}
     */
    public static Flags convertMailFlags(int flags) {
        final Flags flagsObj = new Flags();
        if ((flags & MailMessage.FLAG_ANSWERED) > 0) {
            flagsObj.add(Flags.Flag.ANSWERED);
        }
        if ((flags & MailMessage.FLAG_DELETED) > 0) {
            flagsObj.add(Flags.Flag.DELETED);
        }
        if ((flags & MailMessage.FLAG_DRAFT) > 0) {
            flagsObj.add(Flags.Flag.DRAFT);
        }
        if ((flags & MailMessage.FLAG_FLAGGED) > 0) {
            flagsObj.add(Flags.Flag.FLAGGED);
        }
        if ((flags & MailMessage.FLAG_RECENT) > 0) {
            flagsObj.add(Flags.Flag.RECENT);
        }
        if ((flags & MailMessage.FLAG_SEEN) > 0) {
            flagsObj.add(Flags.Flag.SEEN);
        }
        if ((flags & MailMessage.FLAG_USER) > 0) {
            flagsObj.add(Flags.Flag.USER);
        }
        if ((flags & MailMessage.FLAG_SPAM) > 0) {
            flagsObj.add(MailMessage.USER_SPAM);
        }
        if ((flags & MailMessage.FLAG_FORWARDED) > 0) {
            flagsObj.add(MailMessage.USER_FORWARDED);
        }
        if ((flags & MailMessage.FLAG_READ_ACK) > 0) {
            flagsObj.add(MailMessage.USER_READ_ACK);
        }
        return flagsObj;
    }

    public static boolean isEmpty(final String value) {
        final int length = value.length();
        boolean empty = true;
        for (int i = 0; empty && i < length; i++) {
            final char c = value.charAt(i);
            empty = ((c == ' ') || (c == '\t'));
        }
        return empty;
    }

    private static final Pattern PATTERN_PARSE_HEADER = Pattern.compile("(\\S+):\\p{Blank}?(.*)(?:(?:\r?\n)|(?:$))");

    /**
     * Parses given message source's headers into a {@link HeaderCollection collection} until EOF or 2 subsequent CRLFs occur.
     *
     * @param messageSrc The message source
     * @return The parsed headers as a {@link HeaderCollection collection}.
     */
    public static HeaderCollection loadHeaders(final String messageSrc) {
        /*
         * Determine position of double line break
         */
        final int len = messageSrc.length();
        int i;
        NextRead: for (i = 0; i < len; ++i) {
            char c = messageSrc.charAt(i);
            final int prevPos = i;
            int count = 0;
            while ((c == '\r') || (c == '\n')) {
                if ((c == '\n') && (++count >= 2)) {
                    i = prevPos;
                    break NextRead;
                }
                if (++i >= len) {
                    i = prevPos;
                    break NextRead;
                }
                c = messageSrc.charAt(i);
            }
        }
        /*
         * Parse single headers
         */
        final Matcher m = PATTERN_PARSE_HEADER.matcher(unfold(messageSrc.substring(0, i)));
        final HeaderCollection headers = new HeaderCollection();
        while (m.find()) {
            final String value = m.group(2);
            if (value == null || isEmpty(value)) {
                headers.addHeader(m.group(1), "");
            } else {
                headers.addHeader(m.group(1), value);
            }
        }
        return headers;
    }

}
