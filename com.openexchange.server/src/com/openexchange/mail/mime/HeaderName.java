/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.mime;

import java.io.Serializable;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
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
    private static final Map<String, HeaderName> CACHE;

    static {
        /*
         * Frequently requested headers
         */
        ImmutableMap.Builder<String, HeaderName> cacheBuilder = ImmutableMap.builderWithExpectedSize(64);
        cacheBuilder.put("Bcc", new HeaderName("Bcc"));
        cacheBuilder.put("Cc", new HeaderName("Cc"));
        {
            final HeaderName headerName = new HeaderName("Content-ID");
            cacheBuilder.put("Content-ID", headerName);
            cacheBuilder.put("Content-Id", headerName);
        }
        cacheBuilder.put("Content-Transfer-Encoding", new HeaderName("Content-Transfer-Encoding"));
        cacheBuilder.put("Content-Type", new HeaderName("Content-Type"));
        cacheBuilder.put("Date", new HeaderName("Date"));
        cacheBuilder.put("Disposition-Notification-To", new HeaderName("Disposition-Notification-To"));
        cacheBuilder.put("Content-Disposition", new HeaderName("Content-Disposition"));
        cacheBuilder.put("From", new HeaderName("From"));
        cacheBuilder.put("ReplyTo", new HeaderName("ReplyTo"));
        cacheBuilder.put("In-Reply-To", new HeaderName("In-Reply-To"));
        {
            final HeaderName headerName = new HeaderName("Message-ID");
            cacheBuilder.put("Message-ID", headerName);
            cacheBuilder.put("Message-Id", headerName);
        }
        cacheBuilder.put("MIME-Version", new HeaderName("MIME-Version"));
        cacheBuilder.put("Organization", new HeaderName("Organization"));
        cacheBuilder.put("Received", new HeaderName("Received"));
        cacheBuilder.put("References", new HeaderName("References"));
        cacheBuilder.put("Reply-To", new HeaderName("Reply-To"));
        cacheBuilder.put("Subject", new HeaderName("Subject"));
        cacheBuilder.put("Sender", new HeaderName("Sender"));
        cacheBuilder.put("To", new HeaderName("To"));
        cacheBuilder.put("X-Mailer", new HeaderName("X-Mailer"));
        cacheBuilder.put("X-OX-Marker", new HeaderName("X-OX-Marker"));
        cacheBuilder.put("X-OXMsgref", new HeaderName("X-OXMsgref"));
        cacheBuilder.put("X-Priority", new HeaderName("X-Priority"));
        cacheBuilder.put("X-Spam-Flag", new HeaderName("X-Spam-Flag"));
        cacheBuilder.put("Return-Path", new HeaderName("Return-Path"));
        cacheBuilder.put("X-OX-VCard-Attached", new HeaderName("X-OX-VCard-Attached"));
        cacheBuilder.put("X-OX-Notification", new HeaderName("X-OX-Notification"));
        /*
         * User flags
         */
        cacheBuilder.put("$Forwarded", new HeaderName("$Forwarded"));
        cacheBuilder.put("$MDNSent", new HeaderName("$MDNSent"));
        cacheBuilder.put("NonJunk", new HeaderName("NonJunk"));
        /*
         * Some charsets
         */
        {
            final HeaderName headerName = new HeaderName("us-ascii");
            cacheBuilder.put("us-ascii", headerName);
            cacheBuilder.put("US-ASCII", headerName);
        }
        {
            final HeaderName headerName = new HeaderName("utf-8");
            cacheBuilder.put("utf-8", headerName);
            cacheBuilder.put("UTF-8", headerName);
        }
        {
            final HeaderName headerName = new HeaderName("iso-8859-1");
            cacheBuilder.put("iso-8859-1", headerName);
            cacheBuilder.put("ISO-8859-1", headerName);
        }
        {
            final HeaderName headerName = new HeaderName("windows-1258");
            cacheBuilder.put("windows-1258", headerName);
            cacheBuilder.put("WINDOWS-1258", headerName);
        }
        CACHE = cacheBuilder.build();
    }

    /**
     * Initializes header names from specified character sequences.
     * <p>
     * Yields significantly better space and time performance by caching frequently requested headers.
     *
     * @param names The character sequences
     * @return The header names
     */
    public static HeaderName[] valuesOf(CharSequence... names) {
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
    public static HeaderName valueOf(CharSequence s) {
        final String key = s.toString();
        final HeaderName cached = CACHE.get(key);
        if (cached == null) {
            return new HeaderName(key);
        }
        return cached;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String s;
    private final int hashcode;

    /**
     * No direct instantiation
     */
    private HeaderName(String s) {
        super();
        this.s = s;
        hashcode = Strings.asciiLowerCase(s).hashCode();
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            /*
             * Cannot not occur since Cloneable is implemented
             */
            throw new InternalError("CloneNotSupportedException although Cloneable is implemented");
        }
    }

    @Override
    public boolean equals(Object other) {
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
    public int compareTo(HeaderName other) {
        return s.compareToIgnoreCase(other.s);
    }

    @Override
    public char charAt(int index) {
        return s.charAt(index);
    }

    @Override
    public int length() {
        return s.length();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return s.subSequence(start, end);
    }

}
