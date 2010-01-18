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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.Header;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.messaging.ContentType;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingException;
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

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MimeMessagingPart.class);

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static interface HeaderHandler {

        void handleHeader(Header header, Collection<MessagingHeader> collection) throws MessagingException;
    }

    private static final HeaderName H_CONTENT_TYPE = HeaderName.valueOf(MessagingHeader.KnownHeader.CONTENT_TYPE.toString());

    private static final Map<HeaderName, HeaderHandler> HHANDLERS;

    static {
        final Map<HeaderName, HeaderHandler> m = new HashMap<HeaderName, HeaderHandler>(8);

        m.put(HeaderName.valueOf(MimeContentDisposition.getContentDispositionName()), new HeaderHandler() {

            public void handleHeader(final Header header, final Collection<MessagingHeader> collection) throws MessagingException {
                collection.add(new MimeContentDisposition(header.getValue()));
            }
        });

        m.put(HeaderName.valueOf(MessagingHeader.KnownHeader.DATE.toString()), new HeaderHandler() {

            private final String name = MessagingHeader.KnownHeader.DATE.toString();

            public void handleHeader(final Header header, final Collection<MessagingHeader> collection) throws MessagingException {
                collection.add(new MimeDateMessagingHeader(name, header.getValue()));
            }
        });

        m.put(HeaderName.valueOf(MessagingHeader.KnownHeader.FROM.toString()), new HeaderHandler() {

            private final String name = MessagingHeader.KnownHeader.FROM.toString();

            public void handleHeader(final Header header, final Collection<MessagingHeader> collection) throws MessagingException {
                collection.addAll(MimeAddressMessagingHeader.parseRFC822(name, header.getValue()));
            }
        });

        m.put(HeaderName.valueOf(MessagingHeader.KnownHeader.TO.toString()), new HeaderHandler() {

            private final String name = MessagingHeader.KnownHeader.TO.toString();

            public void handleHeader(final Header header, final Collection<MessagingHeader> collection) throws MessagingException {
                collection.addAll(MimeAddressMessagingHeader.parseRFC822(name, header.getValue()));
            }
        });

        m.put(HeaderName.valueOf(MessagingHeader.KnownHeader.CC.toString()), new HeaderHandler() {

            private final String name = MessagingHeader.KnownHeader.CC.toString();

            public void handleHeader(final Header header, final Collection<MessagingHeader> collection) throws MessagingException {
                collection.addAll(MimeAddressMessagingHeader.parseRFC822(name, header.getValue()));
            }
        });

        m.put(HeaderName.valueOf(MessagingHeader.KnownHeader.BCC.toString()), new HeaderHandler() {

            private final String name = MessagingHeader.KnownHeader.BCC.toString();

            public void handleHeader(final Header header, final Collection<MessagingHeader> collection) throws MessagingException {
                collection.addAll(MimeAddressMessagingHeader.parseRFC822(name, header.getValue()));
            }
        });

        HHANDLERS = Collections.unmodifiableMap(m);
    }

    protected final MimePart part;

    private volatile ContentType cachedContentType;

    private boolean b_cachedContentType;

    private volatile MessagingContent cachedContent;

    private volatile Map<String, Collection<MessagingHeader>> headers;

    private String id;

    /**
     * Initializes a new {@link MimeMessagingPart}.
     */
    public MimeMessagingPart() {
        super();
        part = new MimeBodyPart();
    }

    /**
     * Initializes a new {@link MimeMessagingPart}.
     * 
     * @param part The part
     */
    public MimeMessagingPart(final MimePart part) {
        super();
        this.part = part;
    }

    public MessagingContent getContent() throws MessagingException {
        MessagingContent tmp = cachedContent;
        if (null == tmp) {
            synchronized (this) {
                tmp = cachedContent;
                if (null == tmp) {
                    /*
                     * Get Content-Type
                     */
                    ContentType contentType = null;
                    try {
                        contentType = getContentType();
                    } catch (final MessagingException e) {
                        if (DEBUG) {
                            LOG.debug("Content-Type header could not be requested.", e);
                        }
                    }
                    if (null != contentType) {
                        if (contentType.startsWith("text/")) {
                            final String content = getContentObject(String.class);
                            if (null != content) {
                                tmp = cachedContent = new StringContent(content);
                            }
                        } else if (contentType.startsWith("message/rfc822")) {
                            final MimeMessage content = getContentObject(MimeMessage.class);
                            if (null != content) {
                                tmp = cachedContent = new MimeMessagingMessage(content);
                            }
                        } else if (contentType.startsWith("multipart/")) {
                            final MimeMultipart content = getContentObject(MimeMultipart.class);
                            if (null != content) {
                                tmp = cachedContent = new MimeMultipartContent(content);
                            }
                        }
                    }
                    /*
                     * Get binary content
                     */
                    if (null == tmp) {
                        tmp = cachedContent = new MimeBinaryContent(part);
                    }
                }
            }
        }
        return tmp;
    }

    private <O extends Object> O getContentObject(final Class<O> clazz) {
        try {
            return clazz.cast(part.getContent());
        } catch (final IOException e) {
            if (DEBUG) {
                LOG.debug(clazz.getSimpleName() + " content could not be obtained.", e);
            }
            return null;
        } catch (final javax.mail.MessagingException e) {
            if (DEBUG) {
                LOG.debug(clazz.getSimpleName() + " content could not be obtained.", e);
            }
            return null;
        } catch (final ClassCastException e) {
            if (DEBUG) {
                LOG.debug("Content is not a " + clazz.getName() + '.', e);
            }
            return null;
        }
    }

    public ContentType getContentType() throws MessagingException {
        if (!b_cachedContentType) {
            ContentType tmp = cachedContentType;
            if (null == tmp) {
                synchronized (this) {
                    tmp = cachedContentType;
                    if (null == tmp) {
                        try {
                            final String[] s = part.getHeader(MimeContentType.getContentTypeName());
                            if (null == s || 0 == s.length) {
                                b_cachedContentType = true;
                                return null;
                            }
                            tmp = cachedContentType = new MimeContentType(s[0]);
                            b_cachedContentType = true;
                        } catch (final javax.mail.MessagingException e) {
                            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
                        }
                    }
                }
            }
            return tmp;
        }
        return cachedContentType;
    }

    public String getDisposition() throws MessagingException {
        try {
            return part.getDisposition();
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    public String getFileName() throws MessagingException {
        try {
            return part.getFileName();
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    public Collection<MessagingHeader> getHeader(final String name) throws MessagingException {
        try {
            return getHeaders().get(name);
        } catch (final MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    public Map<String, Collection<MessagingHeader>> getHeaders() throws MessagingException {
        Map<String, Collection<MessagingHeader>> tmp = headers;
        if (null == tmp) {
            synchronized (this) {
                tmp = headers;
                if (null == tmp) {
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
            }
        }
        return tmp;
    }

    public String getId() {
        return id;
    }

    /**
     * Sets the identifier.
     * 
     * @param id The identifier
     */
    public void setId(final String id) {
        this.id = id;
    }

    public long getSize() throws MessagingException {
        try {
            return part.getSize();
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    public void writeTo(final OutputStream os) throws IOException, MessagingException {
        try {
            part.writeTo(os);
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Add this value to the existing values for this header name.
     * 
     * @param headerName The header name
     * @param headerValue The header value
     * @throws MessagingException If adding header fails
     */
    public void addHeader(final String headerName, final String headerValue) throws MessagingException {
        try {
            part.addHeader(headerName, headerValue);
            headers = null;
            b_cachedContentType = false;
            cachedContentType = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Remove all headers associated with specified name.
     * 
     * @param headerName The header name
     * @throws MessagingException If header removal fails
     */
    public void removeHeader(final String headerName) throws MessagingException {
        try {
            part.removeHeader(headerName);
            headers = null;
            b_cachedContentType = false;
            cachedContentType = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Sets the given multipart as this part's content.
     * 
     * @param mp The multipart
     * @throws MessagingException If multipart cannot be set as content
     */
    public void setContent(final MimeMultipartContent mp) throws MessagingException {
        try {
            part.setContent(mp.mimeMultipart);
            headers = null;
            cachedContent = null;
            b_cachedContentType = false;
            cachedContentType = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Sets this part's content.
     * 
     * @param content The content
     * @param type The content type
     * @throws MessagingException
     */
    public void setContent(final MessagingContent content, final String type) throws MessagingException {
        try {
            if (content instanceof MimeMessagingMessage) {
                part.setContent(((MimeMessagingMessage) content).mimeMessage, type);
            } else if (content instanceof MimeMultipartContent) {
                part.setContent(((MimeMultipartContent) content).mimeMultipart, type);
            } else if (content instanceof SimpleContent<?>) {
                part.setContent(((SimpleContent<?>) content).getData(), type);
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
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Set the disposition of this part.
     * 
     * @param disposition The disposition to set
     * @throws MessagingException If setting disposition fails
     */
    public void setDisposition(final String disposition) throws MessagingException {
        try {
            part.setDisposition(disposition);
            headers = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Sets this part's file name.
     * 
     * @param filename The file name
     * @throws MessagingException If setting file name fails
     */
    public void setFileName(final String filename) throws MessagingException {
        try {
            part.setFileName(filename);
            headers = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Set the value for this header name. Replaces all existing header values associated with header name.
     * 
     * @param headerName The header name
     * @param headerValue The header value
     * @throws MessagingException If setting file name fails
     */
    public void setHeader(final String headerName, final String headerValue) throws MessagingException {
        try {
            part.setHeader(headerName, headerValue);
            headers = null;
            b_cachedContentType = false;
            cachedContentType = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
     * @throws MessagingException If text cannot be applied
     * @see #setText(String text, String charset)
     */
    public void setText(final String text) throws MessagingException {
        setText(text, null);
    }

    /**
     * Convenience method that sets the given String as this part's content, with a MIME type of "text/plain" and the specified charset. The
     * given Unicode string will be charset-encoded using the specified charset. The charset is also used to set the "charset" parameter.
     * 
     * @param text The text content to set
     * @param charset The charset to use for the text
     * @throws MessagingException If text cannot be applied
     */
    public void setText(final String text, final String charset) throws MessagingException {
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
     * @throws MessagingException If text cannot be applied
     */
    public void setText(final String text, final String charset, final String subtype) throws MessagingException {
        try {
            part.setText(text, charset, subtype);
            headers = null;
            cachedContent = null;
            b_cachedContentType = false;
            cachedContentType = null;
        } catch (final javax.mail.MessagingException e) {
            throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
        } catch (final IllegalStateException e) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
