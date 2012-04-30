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

package com.openexchange.messaging.mail;

import static com.openexchange.mail.mime.MimeDefaultSession.getDefaultSession;
import static com.openexchange.mail.mime.converters.MimeMessageConverter.convertMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.utils.CharsetDetector;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.messaging.ContentType;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.ReferenceContent;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.StringMessageHeader;
import com.openexchange.messaging.generic.internet.MimeAddressMessagingHeader;
import com.openexchange.messaging.generic.internet.MimeContentDisposition;
import com.openexchange.messaging.generic.internet.MimeContentType;
import com.openexchange.messaging.generic.internet.MimeDateMessagingHeader;
import com.openexchange.messaging.generic.internet.MimeStringMessagingHeader;

/**
 * {@link MailMessagingPart}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18
 */
public class MailMessagingPart implements MessagingPart {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailMessagingPart.class));

    private static final boolean WARN_ENABLED = LOG.isWarnEnabled();

    private final MailPart mailPart;

    private volatile MessagingContent cachedContent;

    /**
     * Initializes a new {@link MailMessagingPart}.
     *
     * @param mailPart The mail part
     */
    public MailMessagingPart(final MailPart mailPart) {
        super();
        this.mailPart = mailPart;
    }

    private static final String CT_TEXT = "text/";

    private static final String CT_MSG_RFC822 = "message/rfc822";

    private static final String CT_MUL = "multipart/";

    @Override
    public MessagingContent getContent() throws OXException {
        MessagingContent tmp = cachedContent;
        if (null == tmp) {
            try {
                /*
                 * Get Content-Type
                 */
                final com.openexchange.mail.mime.ContentType contentType = mailPart.getContentType();
                if (null != contentType) {
                    if (contentType.startsWith(CT_MUL)) {
                        cachedContent = tmp = new MailMultipartContent(mailPart);
                    } else if (contentType.startsWith(CT_TEXT)) {
                        final String content = readContent(mailPart, contentType);
                        if (null != content) {
                            cachedContent = tmp = new StringContent(content);
                        }
                    } else if (contentType.startsWith(CT_MSG_RFC822)) {
                        final MailMessage nestedMail;
                        {
                            final Object content = mailPart.getContent();
                            if (content instanceof MailMessage) {
                                nestedMail = (MailMessage) content;
                            } else if (content instanceof InputStream) {
                                nestedMail = convertMessage(new MimeMessage(getDefaultSession(), (InputStream) content));
                            } else {
                                final StringBuilder sb = new StringBuilder(128);
                                sb.append("Ignoring nested message.").append(
                                    "Cannot handle part's content which should be a RFC822 message according to its content type: ");
                                sb.append((null == content ? "null" : content.getClass().getSimpleName()));
                                LOG.error(sb.toString());
                                nestedMail = null;
                            }
                        }
                        if (null != nestedMail) {
                            /*
                             * Assign
                             */
                            final MailMessagingMessage m = new MailMessagingMessage(nestedMail);
                            final String sectionId = mailPart.getSequenceId();
                            /*
                             * TODO: Examine this one
                             */
                            final boolean increaseSecId = false;
                            if (increaseSecId) {
                                if (sectionId == null) {
                                    m.setSectionId("1");
                                    nestedMail.setSequenceId("1");
                                } else {
                                    final String sid = new StringBuilder(8).append(sectionId).append('.').append(1).toString();
                                    m.setSectionId(sid);
                                    nestedMail.setSequenceId(sid);
                                }
                            } else {
                                m.setSectionId(sectionId);
                                nestedMail.setSequenceId(sectionId);
                            }
                            cachedContent = tmp = m;
                        }
                    }
                }
                /*
                 * Get binary content
                 */
                if (null == tmp) {
                    final String sequenceId = mailPart.getSequenceId();
                    cachedContent = tmp = new ReferenceContent(sequenceId == null ? "1" : sequenceId);
                    // cachedContent = tmp = new MailBinaryContent(mailPart);
                }
            } catch (final OXException e) {
                throw e;
            } catch (final IOException e) {
                throw MessagingExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } catch (final javax.mail.MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            }
        }
        return tmp;
    }

    private static String readContent(final MailPart mailPart, final com.openexchange.mail.mime.ContentType contentType) throws OXException, IOException {
        final String charset = getCharset(mailPart, contentType);
        try {
            return MessageUtility.readMailPart(mailPart, charset);
        } catch (final java.io.CharConversionException e) {
            // Obviously charset was wrong or bogus implementation of character conversion
            final String fallback = "US-ASCII";
            if (WARN_ENABLED) {
                LOG.warn(new StringBuilder("Character conversion exception while reading content with charset \"").append(charset).append(
                    "\". Using fallback charset \"").append(fallback).append("\" instead."), e);
            }
            return MessageUtility.readMailPart(mailPart, fallback);
        }
    }

    private static String getCharset(final MailPart mailPart, final com.openexchange.mail.mime.ContentType contentType) throws OXException {
        final String charset;
        if (mailPart.containsHeader(MessageHeaders.HDR_CONTENT_TYPE)) {
            String cs = contentType.getCharsetParameter();
            if (!CharsetDetector.isValid(cs)) {
                StringBuilder sb = null;
                if (null != cs && WARN_ENABLED) {
                    sb = new StringBuilder(64).append("Illegal or unsupported encoding: \"").append(cs).append("\".");
                    LOG.warn(sb.toString());
                }
                if (contentType.startsWith(CT_TEXT)) {
                    cs = CharsetDetector.detectCharset(mailPart.getInputStream());
                    if (WARN_ENABLED && null != sb) {
                        sb.append(" Using auto-detected encoding: \"").append(cs).append('"');
                        LOG.warn(sb.toString());
                    }
                } else {
                    cs = MailProperties.getInstance().getDefaultMimeCharset();
                    if (WARN_ENABLED && null != sb) {
                        sb.append(" Using fallback encoding: \"").append(cs).append('"');
                        LOG.warn(sb.toString());
                    }
                }
            }
            charset = cs;
        } else {
            if (contentType.startsWith(CT_TEXT)) {
                charset = CharsetDetector.detectCharset(mailPart.getInputStream());
            } else {
                charset = MailProperties.getInstance().getDefaultMimeCharset();
            }
        }
        return charset;
    }

    @Override
    public ContentType getContentType() throws OXException {
        final com.openexchange.mail.mime.ContentType contentType = mailPart.getContentType();
        return null == contentType ? null : new MailContentType(contentType);
    }

    @Override
    public String getDisposition() throws OXException {
        return mailPart.getContentDisposition().getDisposition();
    }

    @Override
    public String getFileName() throws OXException {
        return mailPart.getFileName();
    }

    @Override
    public MessagingHeader getFirstHeader(final String name) throws OXException {
        final String firstHeader = mailPart.getFirstHeader(name);
        if (null == firstHeader) {
            return null;
        }
        final HeaderHandler handler = HHANDLERS.get(HeaderName.valueOf(name));
        if (null == handler) {
            return new StringMessageHeader(name, firstHeader);
        }
        final List<MessagingHeader> tmp = new ArrayList<MessagingHeader>(1);
        handler.handleHeader(firstHeader, tmp);
        return tmp.remove(0);
    }

    @Override
    public Collection<MessagingHeader> getHeader(final String name) throws OXException {
        return convertTo(name);
    }

    @Override
    public Map<String, Collection<MessagingHeader>> getHeaders() throws OXException {
        final Map<String, Collection<MessagingHeader>> ret = new HashMap<String, Collection<MessagingHeader>>();
        for (final Iterator<String> iter = mailPart.getHeaders().getHeaderNames(); iter.hasNext();) {
            final String name = iter.next();
            final Collection<MessagingHeader> collection = convertTo(name);
            if (null != collection) {
                ret.put(name, collection);
            }
        }
        return ret;
    }

    private Collection<MessagingHeader> convertTo(final String name) throws OXException {
        final String[] headers = mailPart.getHeader(name);
        if (null == headers) {
            return null;
        }
        final List<MessagingHeader> ret = new ArrayList<MessagingHeader>(headers.length);
        final HeaderHandler handler = HHANDLERS.get(HeaderName.valueOf(name));
        if (null == handler) {
            for (final String header : headers) {
                ret.add(new StringMessageHeader(name, header));
            }
        } else {
            for (final String header : headers) {
                handler.handleHeader(header, ret);
            }
        }
        return ret;
    }

    @Override
    public String getSectionId() {
        return mailPart.getSequenceId();
    }

    /**
     * Sets the section identifier.
     *
     * @param sectionId The section identifier
     */
    public void setSectionId(final String sectionId) {
        mailPart.setSequenceId(sectionId);
    }

    @Override
    public long getSize() throws OXException {
        return mailPart.getSize();
    }

    @Override
    public void writeTo(final OutputStream os) throws IOException, OXException {
        mailPart.writeTo(os);
    }

    /*-
     * ------------------------------------------------------------------------------------------------------------
     */

    /**
     * Adds an appropriate {@link MessagingHeader} to a collection.
     */
    private static interface HeaderHandler {

        /**
         * Adds an appropriate {@link MessagingHeader} created from given header to specified collection
         *
         * @param header The header to convert to a {@link MessagingHeader} instance
         * @param collection The collection to add to
         * @throws OXException If adding header fails
         */
        void handleHeader(String header, Collection<MessagingHeader> collection) throws OXException;
    } // End of HeaderHandler interface

    /**
     * Adds an address header to a collection.
     */
    private static final class AddressHeaderHandler implements HeaderHandler {

        private final String name;

        AddressHeaderHandler(final String name) {
            super();
            this.name = name;
        }

        @Override
        public void handleHeader(final String header, final Collection<MessagingHeader> collection) throws OXException {
            try {
                collection.addAll(MimeAddressMessagingHeader.parseRFC822(name, header));
            } catch (final OXException e) {
                /*
                 * Could not be parsed to a RFC822 address
                 */
                if (!MessagingExceptionCodes.ADDRESS_ERROR.equals(e)) {
                    throw e;
                }
            }
            collection.add(new MimeStringMessagingHeader(name, header));
        }

    } // End of AddressHeaderHandler class

    /**
     * The static header handler map.
     */
    private static final Map<HeaderName, HeaderHandler> HHANDLERS;

    static {
        final Map<HeaderName, HeaderHandler> m = new HashMap<HeaderName, HeaderHandler>(8);

        m.put(HeaderName.valueOf(MimeContentDisposition.getContentDispositionName()), new HeaderHandler() {

            @Override
            public void handleHeader(final String header, final Collection<MessagingHeader> collection) throws OXException {
                collection.add(new MimeContentDisposition(header));
            }
        });

        m.put(HeaderName.valueOf(MimeContentType.getContentTypeName()), new HeaderHandler() {

            @Override
            public void handleHeader(final String header, final Collection<MessagingHeader> collection) throws OXException {
                collection.add(new MimeContentType(header));
            }
        });

        m.put(HeaderName.valueOf(MessagingHeader.KnownHeader.DATE.toString()), new HeaderHandler() {

            private final String name = MessagingHeader.KnownHeader.DATE.toString();

            @Override
            public void handleHeader(final String header, final Collection<MessagingHeader> collection) throws OXException {
                collection.add(new MimeDateMessagingHeader(name, header));
            }
        });

        String name = MessagingHeader.KnownHeader.FROM.toString();
        m.put(HeaderName.valueOf(name), new AddressHeaderHandler(name));

        name = MessagingHeader.KnownHeader.TO.toString();
        m.put(HeaderName.valueOf(name), new AddressHeaderHandler(name));

        name = MessagingHeader.KnownHeader.CC.toString();
        m.put(HeaderName.valueOf(name), new AddressHeaderHandler(name));

        name = MessagingHeader.KnownHeader.BCC.toString();
        m.put(HeaderName.valueOf(name), new AddressHeaderHandler(name));

        name = "Reply-To";
        m.put(HeaderName.valueOf(name), new AddressHeaderHandler(name));

        name = "Resent-Reply-To";
        m.put(HeaderName.valueOf(name), new AddressHeaderHandler(name));

        name = "Disposition-Notification-To";
        m.put(HeaderName.valueOf(name), new AddressHeaderHandler(name));

        name = "Resent-From";
        m.put(HeaderName.valueOf(name), new AddressHeaderHandler(name));

        name = "Sender";
        m.put(HeaderName.valueOf(name), new AddressHeaderHandler(name));

        name = "Resent-Sender";
        m.put(HeaderName.valueOf(name), new AddressHeaderHandler(name));

        name = "Resent-To";
        m.put(HeaderName.valueOf(name), new AddressHeaderHandler(name));

        name = "Resent-Cc";
        m.put(HeaderName.valueOf(name), new AddressHeaderHandler(name));

        name = "Resent-Bcc";
        m.put(HeaderName.valueOf(name), new AddressHeaderHandler(name));

        HHANDLERS = Collections.unmodifiableMap(m);
    }

}
