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

package com.openexchange.mail.mime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link HeaderName} - Supports an ignore-case string implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HeaderName implements Serializable, Cloneable, Comparable<HeaderName>, CharSequence {

    private static final long serialVersionUID = -4841569785169326836L;

    /**
     * Internal cache for frequently requested headers
     */
    private static final HashMap<String, HeaderName> CACHE = new HashMap<String, HeaderName>(23);

    static {
        /*
         * Frequently requested headers
         */
        CACHE.put(MessageHeaders.HDR_BCC, MessageHeaders.BCC);
        CACHE.put(MessageHeaders.HDR_CC, MessageHeaders.CC);
        CACHE.put(MessageHeaders.HDR_CONTENT_DISPOSITION, MessageHeaders.CONTENT_DISPOSITION);
        CACHE.put(MessageHeaders.HDR_CONTENT_ID, MessageHeaders.CONTENT_ID);
        CACHE.put(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, MessageHeaders.CONTENT_TRANSFER_ENC);
        CACHE.put(MessageHeaders.HDR_CONTENT_TYPE, MessageHeaders.CONTENT_TYPE);
        CACHE.put(MessageHeaders.HDR_DATE, MessageHeaders.DATE);
        CACHE.put(MessageHeaders.HDR_DISP_NOT_TO, MessageHeaders.DISP_NOT_TO);
        CACHE.put(MessageHeaders.HDR_DISPOSITION, MessageHeaders.DISPOSITION);
        CACHE.put(MessageHeaders.HDR_FROM, MessageHeaders.FROM);
        CACHE.put(MessageHeaders.HDR_IN_REPLY_TO, MessageHeaders.IN_REPLY_TO);
        CACHE.put(MessageHeaders.HDR_MESSAGE_ID, MessageHeaders.MESSAGE_ID);
        CACHE.put(MessageHeaders.HDR_MIME_VERSION, MessageHeaders.MIME_VERSION);
        CACHE.put(MessageHeaders.HDR_ORGANIZATION, MessageHeaders.ORGANIZATION);
        CACHE.put(MessageHeaders.HDR_RECEIVED, MessageHeaders.RECEIVED);
        CACHE.put(MessageHeaders.HDR_REFERENCES, MessageHeaders.REFERENCES);
        CACHE.put(MessageHeaders.HDR_REPLY_TO, MessageHeaders.REPLY_TO);
        CACHE.put(MessageHeaders.HDR_SUBJECT, MessageHeaders.SUBJECT);
        CACHE.put(MessageHeaders.HDR_TO, MessageHeaders.TO);
        CACHE.put(MessageHeaders.HDR_X_MAILER, MessageHeaders.X_MAILER);
        CACHE.put(MessageHeaders.HDR_X_OX_MARKER, MessageHeaders.X_OX_MARKER);
        CACHE.put(MessageHeaders.HDR_X_OXMSGREF, MessageHeaders.X_OXMSGREF);
        CACHE.put(MessageHeaders.HDR_X_PRIORITY, MessageHeaders.X_PRIORITY);
        CACHE.put(MessageHeaders.HDR_X_SPAM_FLAG, MessageHeaders.X_SPAM_FLAG);
        /*
         * User flags
         */
        CACHE.put(MailMessage.USER_FORWARDED, new HeaderName(MailMessage.USER_FORWARDED));
        CACHE.put(MailMessage.USER_READ_ACK, new HeaderName(MailMessage.USER_READ_ACK));
    }

    private final String s;

    private final int hashcode;

    /**
     * No direct instantiation
     */
    private HeaderName(final String s) {
        super();
        this.s = s;
        hashcode = s.toLowerCase(Locale.ENGLISH).hashCode();
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            /*
             * Cannot not occur since Cloneable is implemented
             */
            throw new InternalError("CloneNotSupportedException although Cloneable is implemented");
        }
    }

    /**
     * Initializes a new header name from specified character sequence.
     * <p>
     * Yields significantly better space and time performance by caching frequently requested headers.
     * 
     * @param s The character sequence
     * @return The new header name.
     */
    public static HeaderName valueOf(final CharSequence s) {
        final String key = s.toString();
        final HeaderName cached = CACHE.get(key);
        if (cached == null) {
            return new HeaderName(key);
        }
        return cached;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof HeaderName)) {
            return s.equalsIgnoreCase(((HeaderName) other).s);
        }
        if ((other instanceof String)) {
            return s.equalsIgnoreCase((String) other);
        }
        return false;
    }

    @Override
    public String toString() {
        return s;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    public int compareTo(final HeaderName other) {
        return s.compareToIgnoreCase(other.s);
    }

    public char charAt(final int index) {
        return s.charAt(index);
    }

    public int length() {
        return s.length();
    }

    public CharSequence subSequence(final int start, final int end) {
        return s.subSequence(start, end);
    }

}
