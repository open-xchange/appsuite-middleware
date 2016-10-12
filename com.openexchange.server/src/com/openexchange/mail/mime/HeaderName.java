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

package com.openexchange.mail.mime;

import java.io.Serializable;
import java.util.HashMap;
import com.openexchange.java.Strings;

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
        CACHE.put("Bcc", HeaderName.valueOf("Bcc"));
        CACHE.put("Cc", HeaderName.valueOf("Cc"));
        CACHE.put("Content-Disposition", HeaderName.valueOf("Content-Disposition"));
        {
            final HeaderName headerName = HeaderName.valueOf("Content-ID");
            CACHE.put("Content-ID", headerName);
            CACHE.put("Content-Id", headerName);
        }
        CACHE.put("Content-Transfer-Encoding", HeaderName.valueOf("Content-Transfer-Encoding"));
        CACHE.put("Content-Type", HeaderName.valueOf("Content-Type"));
        CACHE.put("Date", HeaderName.valueOf("Date"));
        CACHE.put("Disposition-Notification-To", HeaderName.valueOf("Disposition-Notification-To"));
        CACHE.put("Content-Disposition", HeaderName.valueOf("Content-Disposition"));
        CACHE.put("From", HeaderName.valueOf("From"));
        CACHE.put("ReplyTo", HeaderName.valueOf("ReplyTo"));
        CACHE.put("In-Reply-To", HeaderName.valueOf("In-Reply-To"));
        {
            final HeaderName headerName = HeaderName.valueOf("Message-ID");
            CACHE.put("Message-ID", headerName);
            CACHE.put("Message-Id", headerName);
        }
        CACHE.put("MIME-Version", HeaderName.valueOf("MIME-Version"));
        CACHE.put("Organization", HeaderName.valueOf("Organization"));
        CACHE.put("Received", HeaderName.valueOf("Received"));
        CACHE.put("References", HeaderName.valueOf("References"));
        CACHE.put("Reply-To", HeaderName.valueOf("Reply-To"));
        CACHE.put("Subject", HeaderName.valueOf("Subject"));
        CACHE.put("Sender", HeaderName.valueOf("Sender"));
        CACHE.put("To", HeaderName.valueOf("To"));
        CACHE.put("X-Mailer", HeaderName.valueOf("X-Mailer"));
        CACHE.put("X-OX-Marker", HeaderName.valueOf("X-OX-Marker"));
        CACHE.put("X-OXMsgref", HeaderName.valueOf("X-OXMsgref"));
        CACHE.put("X-Priority", HeaderName.valueOf("X-Priority"));
        CACHE.put("X-Spam-Flag", HeaderName.valueOf("X-Spam-Flag"));
        CACHE.put("Return-Path", HeaderName.valueOf("Return-Path"));
        CACHE.put("X-OX-VCard-Attached", HeaderName.valueOf("X-OX-VCard-Attached"));
        CACHE.put("X-OX-Notification", HeaderName.valueOf("X-OX-Notification"));
        /*
         * User flags
         */
        CACHE.put("$Forwarded", new HeaderName("$Forwarded"));
        CACHE.put("$MDNSent", new HeaderName("$MDNSent"));
        CACHE.put("NonJunk", new HeaderName("NonJunk"));
        /*
         * Some charsets
         */
        {
            final HeaderName headerName = new HeaderName("us-ascii");
            CACHE.put("us-ascii", headerName);
            CACHE.put("US-ASCII", headerName);
        }
        {
            final HeaderName headerName = new HeaderName("utf-8");
            CACHE.put("utf-8", headerName);
            CACHE.put("UTF-8", headerName);
        }
        {
            final HeaderName headerName = new HeaderName("iso-8859-1");
            CACHE.put("iso-8859-1", headerName);
            CACHE.put("ISO-8859-1", headerName);
        }
        {
            final HeaderName headerName = new HeaderName("windows-1258");
            CACHE.put("windows-1258", headerName);
            CACHE.put("WINDOWS-1258", headerName);
        }
    }

    /**
     * Initializes header names from specified character sequences.
     * <p>
     * Yields significantly better space and time performance by caching frequently requested headers.
     *
     * @param names The character sequences
     * @return The header names
     */
    public static HeaderName[] valuesOf(final CharSequence... names) {
        if (null == names) {
            return null;
        }
        final HeaderName[] ret = new HeaderName[names.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = valueOf(names[i]);
        }
        return ret;
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

    private final String s;

    private final int hashcode;

    /**
     * No direct instantiation
     */
    private HeaderName(final String s) {
        super();
        this.s = s;
        hashcode = Strings.asciiLowerCase(s).hashCode();
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

    @Override
    public int compareTo(final HeaderName other) {
        return s.compareToIgnoreCase(other.s);
    }

    @Override
    public char charAt(final int index) {
        return s.charAt(index);
    }

    @Override
    public int length() {
        return s.length();
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return s.subSequence(start, end);
    }

}
