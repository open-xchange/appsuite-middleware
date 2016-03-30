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

package com.openexchange.messaging.generic.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.activation.DataHandler;
import javax.mail.Header;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.datasource.StreamDataSource;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.messaging.BinaryContent;
import com.openexchange.messaging.ContentType;
import com.openexchange.messaging.ManagedFileContent;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.SimpleContent;
import com.openexchange.messaging.StringContent;

/**
 * {@link MimeMessagingPart} - The MIME messaging part.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class MimeMessagingPart implements MessagingPart {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MimeMessagingPart.class);

    /**
     * The <code>Content-Type</code> header name.
     */
    private static final HeaderName H_CONTENT_TYPE = HeaderName.valueOf(MessagingHeader.KnownHeader.CONTENT_TYPE.toString());

    private static final String CT_TEXT = "text/";

    private static final String CT_MSG_RFC822 = "message/rfc822";

    private static final String CT_MUL = "multipart/";

    /**
     * An {@link StreamDataSource.InputStreamProvider} backed by a {@link BinaryContent}.
     */
    private static final class BinaryContentISP implements StreamDataSource.InputStreamProvider {

        private final BinaryContent binaryContent;

        BinaryContentISP(final BinaryContent binaryContent) {
            super();
            this.binaryContent = binaryContent;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            try {
                return binaryContent.getData();
            } catch (final OXException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                if (cause instanceof javax.mail.MessagingException) {
                    final javax.mail.MessagingException me = (javax.mail.MessagingException) cause;
                    final Exception nextException = me.getNextException();
                    if (nextException instanceof IOException) {
                        throw (IOException) nextException;
                    }
                    final IOException ioException = new IOException(me.getMessage());
                    ioException.initCause(me);
                    throw ioException;
                }
                final IOException ioException = new IOException(e.getMessage());
                ioException.initCause(e);
                throw ioException;
            }
        }

        @Override
        public String getName() {
            return null;
        }

    } // End of BinaryContentISP class

    /**
     * An {@link StreamDataSource.InputStreamProvider} backed by a {@link BinaryContent}.
     */
    private static final class ManagedFileContentISP implements StreamDataSource.InputStreamProvider {

        private final ManagedFileContent fileContent;

        ManagedFileContentISP(final ManagedFileContent fileContent) {
            super();
            this.fileContent = fileContent;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            try {
                return fileContent.getData();
            } catch (final OXException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                if (cause instanceof javax.mail.MessagingException) {
                    final javax.mail.MessagingException me = (javax.mail.MessagingException) cause;
                    final Exception nextException = me.getNextException();
                    if (nextException instanceof IOException) {
                        throw (IOException) nextException;
                    }
                    final IOException ioException = new IOException(me.getMessage());
                    ioException.initCause(me);
                    throw ioException;
                }
                final IOException ioException = new IOException(e.getMessage());
                ioException.initCause(e);
                throw ioException;
            }
        }

        @Override
        public String getName() {
            return null;
        }

    } // End of ManagedFileContentISP class

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
        void handleHeader(Header header, Collection<MessagingHeader> collection) throws OXException;
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
        public void handleHeader(final Header header, final Collection<MessagingHeader> collection) throws OXException {
            try {
                collection.addAll(MimeAddressMessagingHeader.parseRFC822(name, header.getValue()));
            } catch (final OXException e) {
                /*
                 * Could not be parsed to a RFC822 address
                 */
                if (!MessagingExceptionCodes.ADDRESS_ERROR.equals(e)) {
                    throw e;
                }
            }
            collection.add(new MimeStringMessagingHeader(name, header.getValue()));
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
            public void handleHeader(final Header header, final Collection<MessagingHeader> collection) throws OXException {
                collection.add(new MimeContentDisposition(header.getValue()));
            }
        });

        m.put(HeaderName.valueOf(MessagingHeader.KnownHeader.DATE.toString()), new HeaderHandler() {

            private final String name = MessagingHeader.KnownHeader.DATE.toString();

            @Override
            public void handleHeader(final Header header, final Collection<MessagingHeader> collection) throws OXException {
                collection.add(new MimeDateMessagingHeader(name, header.getValue()));
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

    /*-
     * -------------------------------------- MEMBER SECTION --------------------------------------
     */

    /**
     * The underlying {@link MimePart MIME part}.
     */
    protected final MimePart part;

    private volatile ContentType cachedContentType;

    private boolean b_cachedContentType;

    private volatile MessagingContent cachedContent;

    private volatile Map<String, Collection<MessagingHeader>> headers;

    /**
     * The part identifier.
     */
    protected String sectionId;

    /**
     * The part's size. Default is <code>-1L</code> to obtain size from underlying {@link MimePart MIME part} when invoking
     * {@link #getSize()}.
     */
    protected long size;

    /**
     * Initializes a new {@link MimeMessagingPart}.
     */
    public MimeMessagingPart() {
        this(new MimeBodyPart());
    }

    /**
     * Initializes a new {@link MimeMessagingPart}.
     *
     * @param part The part
     */
    public MimeMessagingPart(final MimePart part) {
        super();
        this.part = part;
        size = -1L;
    }

    @Override
    public MessagingContent getContent() throws OXException {
        MessagingContent tmp = cachedContent;
        if (null == tmp) {
            // No need for synchronization
            /*
             * Get Content-Type
             */
            ContentType contentType = null;
            try {
                contentType = getContentType();
            } catch (final OXException e) {
                LOG.debug("Content-Type header could not be requested.", e);
            }
            if (null != contentType) {
                if (contentType.startsWith(CT_MUL)) {
                    final MimeMultipart content = getContentObject(MimeMultipart.class);
                    if (null != content) {
                        final MimeMultipartContent multipartContent = new MimeMultipartContent(content);
                        multipartContent.setSectionId(sectionId);
                        cachedContent = tmp = multipartContent;
                    }
                } else if (contentType.startsWith(CT_TEXT)) {
                    final String content = getContentObject(String.class);
                    if (null != content) {
                        cachedContent = tmp = new StringContent(content);
                    }
                } else if (contentType.startsWith(CT_MSG_RFC822)) {
                    final MimeMessage content = getContentObject(MimeMessage.class);
                    if (null != content) {
                        final MimeMessagingMessage message = new MimeMessagingMessage(content);
                        final boolean increaseSecId = false;
                        if (increaseSecId) {
                            message.setSectionId(sectionId == null ? "1" : new StringBuilder(8).append(sectionId).append('.').append(1).toString());
                        } else {
                            message.setSectionId(sectionId);
                        }
                        cachedContent = tmp = message;
                    }
                }
            }
            /*
             * Get binary content
             */
            if (null == tmp) {
                cachedContent = tmp = new MimeBinaryContent(part);
            }
        }
        return tmp;
    }

    private <O extends Object> O getContentObject(final Class<O> clazz) {
        try {
            return clazz.cast(part.getContent());
        } catch (final IOException e) {
            LOG.debug("{} content could not be obtained.", clazz.getSimpleName(), e);
            return null;
        } catch (final javax.mail.MessagingException e) {
            LOG.debug("{} content could not be obtained.", clazz.getSimpleName(), e);
            return null;
        } catch (final ClassCastException e) {
            LOG.debug("Content is not a {}.", clazz.getName(), e);
            return null;
        }
    }

    @Override
    public ContentType getContentType() throws OXException {
        if (!b_cachedContentType) {
            ContentType tmp = cachedContentType;
            if (null == tmp) {
                // No synchronization
                try {
                    final String[] s = part.getHeader(MimeContentType.getContentTypeName());
                    if (null == s || 0 == s.length) {
                        b_cachedContentType = true;
                        return null;
                    }
                    cachedContentType = tmp = new MimeContentType(s[0]);
                    b_cachedContentType = true;
                } catch (final javax.mail.MessagingException e) {
                    throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
                }
            }
            return tmp;
        }
        return cachedContentType;
    }

    @Override
    public String getDisposition() throws OXException {
        try {
            return part.getDisposition();
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String getFileName() throws OXException {
        try {
            return part.getFileName();
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MessagingHeader getFirstHeader(final String name) throws OXException {
        final Collection<MessagingHeader> collection = getHeader(name);
        return null == collection ? null : (collection.isEmpty() ? null : collection.iterator().next());
    }

    @Override
    public Collection<MessagingHeader> getHeader(final String name) throws OXException {
        try {
            return getHeaders().get(name);
        } catch (final OXException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public Map<String, Collection<MessagingHeader>> getHeaders() throws OXException {
        Map<String, Collection<MessagingHeader>> tmp = headers;
        if (null == tmp) {
            // No synchronization
            try {
                tmp = new ConcurrentHashMap<String, Collection<MessagingHeader>>();
                for (final Enumeration<?> allHeaders = part.getAllHeaders(); allHeaders.hasMoreElements();) {
                    final Header header = (Header) allHeaders.nextElement();
                    final String name = header.getName();
                    Collection<MessagingHeader> collection = tmp.get(name);
                    if (null == collection) {
                        collection = new ArrayList<MessagingHeader>(2);
                        tmp.put(name, collection);
                    }
                    final HeaderName headerName = HeaderName.valueOf(name);
                    final HeaderHandler hh = HHANDLERS.get(headerName);
                    if (null == hh) {
                        if (H_CONTENT_TYPE.equals(headerName)) {
                            final MimeContentType mct = new MimeContentType(header.getValue());
                            cachedContentType = mct;
                            b_cachedContentType = true;
                            collection.add(mct);
                        } else {
                            collection.add(new MimeStringMessagingHeader(name, header.getValue()));
                        }
                    } else {
                        hh.handleHeader(header, collection);
                    }
                }
                /*
                 * Seal collection
                 */
                for (final String name : new HashSet<String>(tmp.keySet())) {
                    tmp.put(name, Collections.unmodifiableCollection(tmp.get(name)));
                }
                tmp = Collections.unmodifiableMap(tmp);
                headers = tmp;
            } catch (final javax.mail.MessagingException e) {
                throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
            }
        }
        return tmp;
    }

    @Override
    public String getSectionId() {
        return sectionId;
    }

    /**
     * Sets the section identifier.
     *
     * @param sectionId The section identifier
     */
    public void setSectionId(final String sectionId) {
        this.sectionId = sectionId;
    }

    @Override
    public long getSize() throws OXException {
        if (size < 0) {
            /*
             * Determine part's size
             */
            try {
                return part.getSize();
            } catch (final javax.mail.MessagingException e) {
                throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
            }
        }
        return size;
    }

    /**
     * Sets this part's size. If passed argument is less than or equal to zero, specified size will not be set.
     *
     * @param size The size to set
     */
    public void setSize(final long size) {
        this.size = size <= 0 ? -1L : size;
    }

    @Override
    public void writeTo(final OutputStream os) throws IOException, OXException {
        try {
            part.writeTo(os);
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Adds given header collection to the existing headers of this messaging part.
     *
     * @param headers The headers to add
     * @throws OXException If adding headers fails
     */
    public void addAllHeaders(final Map<String, Collection<MessagingHeader>> headers) throws OXException {
        /*
         * Add headers
         */
        for (final Entry<String, Collection<MessagingHeader>> header : headers.entrySet()) {
            for (final MessagingHeader mh : header.getValue()) {
                addHeader(mh.getName(), mh.getValue());
            }
        }
        this.headers = null;
        b_cachedContentType = false;
        cachedContentType = null;
    }

    /**
     * Adds specified header value to the existing values for the associated header name.
     *
     * @param headerName The header name
     * @param headerValue The header value
     * @throws OXException If adding header fails
     */
    public void addHeader(final String headerName, final String headerValue) throws OXException {
        try {
            part.addHeader(headerName, headerValue);
            headers = null;
            b_cachedContentType = false;
            cachedContentType = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.READ_ONLY.create(e, e.getMessage());
        }
    }

    /**
     * Adds specified header value to the existing values for the associated header name.
     *
     * @param header The header to add
     * @throws OXException If adding header fails
     */
    public void addHeader(final MessagingHeader header) throws OXException {
        try {
            part.addHeader(header.getName(), header.getValue());
            headers = null;
            b_cachedContentType = false;
            cachedContentType = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.READ_ONLY.create(e, e.getMessage());
        }
    }

    /**
     * Remove all headers associated with specified name.
     *
     * @param headerName The header name
     * @throws OXException If header removal fails
     */
    public void removeHeader(final String headerName) throws OXException {
        try {
            part.removeHeader(headerName);
            headers = null;
            b_cachedContentType = false;
            cachedContentType = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.READ_ONLY.create(e, e.getMessage());
        }
    }

    /**
     * Sets the given multipart as this part's content.
     *
     * @param mp The multipart
     * @throws OXException If multipart cannot be set as content
     */
    public void setContent(final MimeMultipartContent mp) throws OXException {
        try {
            MessageUtility.setContent(mp.mimeMultipart, part);
            // part.setContent(mp.mimeMultipart);
            part.setHeader(H_CONTENT_TYPE.toString(), mp.mimeMultipart.getContentType());
            headers = null;
            cachedContent = null;
            b_cachedContentType = false;
            cachedContentType = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.READ_ONLY.create(e, e.getMessage());
        }
    }

    /**
     * Sets this part's content.
     *
     * @param content The content
     * @param type The content type
     * @throws OXException If content cannot be applied
     */
    public void setContent(final MessagingContent content, final String type) throws OXException {
        try {
            if (content instanceof MimeMessagingMessage) {
                MessageUtility.setContent(((MimeMessagingMessage) content).mimeMessage, part);
                // part.setContent(((MimeMessagingMessage) content).mimeMessage, type);
                part.setHeader(H_CONTENT_TYPE.toString(), type);
            } else if (content instanceof MimeMultipartContent) {
                MessageUtility.setContent(((MimeMultipartContent) content).mimeMultipart, part);
                // part.setContent(((MimeMultipartContent) content).mimeMultipart);
                part.setHeader(H_CONTENT_TYPE.toString(), type);
            } else if (content instanceof SimpleContent<?>) {
                if (content instanceof BinaryContent) {
                    part.setDataHandler(new DataHandler(new StreamDataSource(new BinaryContentISP((BinaryContent) content), type)));
                } else if (content instanceof ManagedFileContent) {
                    part.setDataHandler(new DataHandler(new StreamDataSource(new ManagedFileContentISP((ManagedFileContent) content), type)));
                } else if (content instanceof StringContent) {
                    final MimeContentType mct = new MimeContentType(type);
                    MessageUtility.setText(((StringContent) content).getData().toString(), mct.getCharsetParameter(), mct.getSubType(), part);
                    // part.setText(((StringContent) content).getData().toString(), mct.getCharsetParameter(), mct.getSubType());
                } else if (content instanceof SimpleContent) {
                    final Object data = ((SimpleContent<?>) content).getData();
                    if (data instanceof String) {
                        try {
                            part.setDataHandler(new DataHandler(new MessageDataSource(data.toString(), type)));
                        } catch (final UnsupportedEncodingException e) {
                            throw new javax.mail.MessagingException("Unsupported encosing.", e);
                        }
                    } else {
                        part.setContent(data, type);
                    }
                } else {
                    throw MessagingExceptionCodes.UNKNOWN_MESSAGING_CONTENT.create(content.getClass().getName());
                }
                part.setHeader(H_CONTENT_TYPE.toString(), type);
            } else {
                throw MessagingExceptionCodes.UNKNOWN_MESSAGING_CONTENT.create(content.getClass().getName());
            }
            headers = null;
            cachedContent = null;
            b_cachedContentType = false;
            cachedContentType = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.READ_ONLY.create(e, e.getMessage());
        }
    }

    /**
     * Set the disposition of this part.
     *
     * @param disposition The disposition to set
     * @throws OXException If setting disposition fails
     */
    public void setDisposition(final String disposition) throws OXException {
        try {
            part.setDisposition(disposition);
            headers = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.READ_ONLY.create(e, e.getMessage());
        }
    }

    /**
     * Sets this part's file name.
     *
     * @param filename The file name
     * @throws OXException If setting file name fails
     */
    public void setFileName(final String filename) throws OXException {
        try {
            part.setFileName(filename);
            headers = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.READ_ONLY.create(e, e.getMessage());
        }
    }

    /**
     * Sets given header collection to this messaging part.
     *
     * @param headers The headers to set
     * @throws OXException If setting headers fails
     */
    public void setAllHeaders(final Map<String, Collection<MessagingHeader>> headers) throws OXException {
        /*
         * Drop all existing headers
         */
        try {
            for (final Enumeration<?> allHeaders = part.getAllHeaders(); allHeaders.hasMoreElements();) {
                part.removeHeader(((Header) allHeaders.nextElement()).getName());
            }
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.READ_ONLY.create(e, e.getMessage());
        }
        /*
         * Set new headers
         */
        for (final Entry<String, Collection<MessagingHeader>> header : headers.entrySet()) {
            for (final MessagingHeader mh : header.getValue()) {
                addHeader(mh.getName(), mh.getValue());
            }
        }
        this.headers = null;
        b_cachedContentType = false;
        cachedContentType = null;
    }

    /**
     * Set the value for this header name. Replaces all existing header values associated with header name.
     *
     * @param headerName The header name
     * @param headerValue The header value
     * @throws OXException If setting header fails
     */
    public void setHeader(final String headerName, final String headerValue) throws OXException {
        try {
            part.setHeader(headerName, headerValue);
            headers = null;
            b_cachedContentType = false;
            cachedContentType = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.READ_ONLY.create(e, e.getMessage());
        }
    }

    /**
     * Set the value for this header name. Replaces all existing header values associated with header name.
     *
     * @param header The header to set
     * @throws OXException If setting header fails
     */
    public void setHeader(final MessagingHeader header) throws OXException {
        try {
            part.setHeader(header.getName(), header.getValue());
            headers = null;
            b_cachedContentType = false;
            cachedContentType = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.READ_ONLY.create(e, e.getMessage());
        }
    }

    /**
     * Convenience method that sets the given String as this part's content, with a MIME type of "text/plain". If the string contains non
     * US-ASCII characters. it will be encoded using the platform's default charset. The charset is also used to set the "charset"
     * parameter.
     * <p>
     * Note that there may be a performance penalty if <code>text</code> is large, since this method may have to scan all the characters to
     * determine what charset to use.
     * <p>
     * If the charset is already known, use the <code>setText</code> method that takes the charset parameter.
     *
     * @param text The text content to set
     * @throws OXException If text cannot be applied
     * @see #setText(String text, String charset)
     */
    public void setText(final String text) throws OXException {
        setText(text, null);
    }

    /**
     * Convenience method that sets the given String as this part's content, with a MIME type of "text/plain" and the specified charset. The
     * given Unicode string will be charset-encoded using the specified charset. The charset is also used to set the "charset" parameter.
     *
     * @param text The text content to set
     * @param charset The charset to use for the text
     * @throws OXException If text cannot be applied
     */
    public void setText(final String text, final String charset) throws OXException {
        setText(text, charset, "plain");
    }

    /**
     * Convenience method that sets the given String as this part's content, with a primary MIME type of "text" and the specified MIME
     * subtype. The given Unicode string will be charset-encoded using the specified charset. The charset is also used to set the "charset"
     * parameter.
     *
     * @param text The text content to set
     * @param charset The charset to use for the text
     * @param subtype The MIME subtype to use (e.g., "html")
     * @throws OXException If text cannot be applied
     */
    public void setText(final String text, final String charset, final String subtype) throws OXException {
        try {
            MessageUtility.setText(text, charset, subtype, part);
            // part.setText(text, charset, subtype);
            headers = null;
            cachedContent = null;
            b_cachedContentType = false;
            cachedContentType = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.READ_ONLY.create(e, e.getMessage());
        }
    }

}
