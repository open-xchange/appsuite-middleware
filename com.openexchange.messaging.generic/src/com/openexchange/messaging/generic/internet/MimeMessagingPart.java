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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.mail.Header;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.openexchange.messaging.ContentType;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.StringContent;

/**
 * {@link MimeMessagingPart}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class MimeMessagingPart implements MessagingPart {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MimeMessagingPart.class);

    private static final boolean DEBUG = LOG.isDebugEnabled();

    protected final Part part;

    private volatile ContentType cachedContentType;

    private boolean b_cachedContentType;

    private volatile MessagingContent cachedContent;

    private volatile ConcurrentMap<String, Collection<MessagingHeader>> headers;

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
    public MimeMessagingPart(final Part part) {
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
        ConcurrentMap<String, Collection<MessagingHeader>> tmp = headers;
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
                                collection = new ArrayList<MessagingHeader>(4);
                                tmp.put(name, collection);
                            }
                            if (MimeContentType.getContentTypeName().equalsIgnoreCase(name)) {
                                collection.add(getContentType());
                            } else if (MimeContentDisposition.getContentDispositionName().equalsIgnoreCase(name)) {
                                collection.add(new MimeContentDisposition(header.getValue()));
                            } else if (MessagingHeader.KnownHeader.DATE.toString().equalsIgnoreCase(name)) {
                                collection.add(new MimeDateMessagingHeader(name, header.getValue()));
                            } else if (MessagingHeader.KnownHeader.FROM.toString().equalsIgnoreCase(name)) {
                                collection.addAll(MimeAddressMessagingHeader.parse(name, header.getValue()));
                            } else if (MessagingHeader.KnownHeader.TO.toString().equalsIgnoreCase(name)) {
                                collection.addAll(MimeAddressMessagingHeader.parse(name, header.getValue()));
                            } else if (MessagingHeader.KnownHeader.CC.toString().equalsIgnoreCase(name)) {
                                collection.addAll(MimeAddressMessagingHeader.parse(name, header.getValue()));
                            } else if (MessagingHeader.KnownHeader.BCC.toString().equalsIgnoreCase(name)) {
                                collection.addAll(MimeAddressMessagingHeader.parse(name, header.getValue()));
                            } else if ("Sender".equalsIgnoreCase(name)) {
                                collection.addAll(MimeAddressMessagingHeader.parse(name, header.getValue()));
                            } else {
                                collection.add(new MimeStringMessagingHeader(name, header.getValue()));
                            }
                        }
                        headers = tmp;
                    } catch (final javax.mail.MessagingException e) {
                        throw MessagingExceptionCodes.MESSAGING_ERROR.create(e, e.getMessage());
                    }
                }
            }
        }
        return Collections.unmodifiableMap(tmp);
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

}
