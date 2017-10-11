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

package com.openexchange.java;

import static com.openexchange.java.Strings.toLowerCase;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link HTMLDetector} - Detects HTML tags in a byte sequence.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HTMLDetector {

    private static final Set<String> JS_EVENT_HANDLER = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        "onabort",
        "onblur",
        "onchange",
        "onclick",
        "ondblclick",
        "onerror",
        "onfocus",
        "onkeydown",
        "onkeypress",
        "onkeyup",
        "onload",
        "onmousedown",
        "onmousemove",
        "onmouseout",
        "onmouseover",
        "onmouseup",
        "onreset",
        "onselect",
        "onsubmit",
        "onunload")));

    /**
     * Initializes a new {@link HTMLDetector}.
     */
    private HTMLDetector() {
        super();
    }

    /**
     * Checks if given string contains common HTML tags.
     *
     * @param sequence The string to check
     * @param strict <code>true</code> for strict checking; otherwise <code>false</code>
     * @return <code>true</code> if given String contains common HTML tags; otherwise <code>false</code>
     */
    public static boolean containsHTMLTags(final String sequence, final boolean strict) {
        return strict ? containsHTMLTags(sequence, "<br", "<p>") : containsHTMLTags(sequence);
    }

    /**
     * Checks if given string contains common HTML tags.
     *
     * @param sequence The string to check
     * @param tags Additional tags to look for
     * @return <code>true</code> if given String contains common HTML tags; otherwise <code>false</code>
     */
    public static boolean containsHTMLTags(final String sequence, final String... tags) {
        if (sequence == null) {
            throw new NullPointerException();
        }

        String lc = Strings.asciiLowerCase(sequence);
        if (lc.indexOf('<') >= 0) {
            if ((lc.indexOf("html>") >= 0)) {
                return true;
            }
            if ((lc.indexOf("head>") >= 0)) {
                return true;
            }
            if ((lc.indexOf("body>") >= 0)) {
                return true;
            }
            if ((lc.indexOf("<script") >= 0)) {
                return true;
            }
            if ((lc.indexOf("<img") >= 0)) {
                return true;
            }
            if ((lc.indexOf("<object") >= 0)) {
                return true;
            }
            if ((lc.indexOf("<embed") >= 0)) {
                return true;
            }
            if (null != tags && tags.length > 0) {
                for (int i = tags.length; i-- > 0;) {
                    final String tag = tags[i];
                    if (!Strings.isEmpty(tag) && (lc.indexOf(tag) >= 0)) {
                        return true;
                    }
                }
            }
        }

        if ((lc.indexOf("javascript") >= 0)) {
            return true;
        }
        for (String jsEventHandler : JS_EVENT_HANDLER) {
            if (lc.indexOf(jsEventHandler) >= 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if given string contains specified HTML tag.
     *
     * @param sequence The string to check
     * @param tag The HTML tag; e.g. <code>"body"</code>
     * @return <code>true</code> if given String contains specified HTML tag; otherwise <code>false</code>
     */
    public static boolean containsHTMLTag(final String sequence, final String tag) {
        if (sequence == null || tag == null) {
            throw new NullPointerException();
        }
        return containsIgnoreCase(sequence, tag.startsWith("<") ? tag : new StringBuilder(tag.length() + 2).append('<').append(tag).append('>').toString());
    }

    /**
     * Checks if given string contains specified string.
     *
     * @param sequence The string to check
     * @param str The string
     * @return <code>true</code> if given String contains specified string; otherwise <code>false</code>
     */
    private static boolean containsIgnoreCase(final String sequence, final String str) {
        return (toLowerCase(sequence).indexOf(toLowerCase(str)) >= 0);
    }

    // ----------------------------------------------------------------------------------------- //

    /**
     * Checks if given byte sequence contains common HTML tags.
     *
     * @param in The byte stream to check
     * @param strict <code>true</code> for strict checking; otherwise <code>false</code>
     * @return <code>true</code> if given byte sequence contains common HTML tags; otherwise <code>false</code>
     * @throws IOException If reading from stream fails
     */
    public static boolean containsHTMLTags(InputStream in, boolean strict) throws IOException {
        return containsHTMLTags(in, strict, false);
    }

    /**
     * Checks if given byte sequence contains common HTML tags.
     *
     * @param in The byte stream to check
     * @param strict <code>true</code> for strict checking; otherwise <code>false</code>
     * @param oneShot <code>true</code> to only examine the first 8K chunk read from stream; otherwise <code>false</code> for full examination
     * @return <code>true</code> if given byte sequence contains common HTML tags; otherwise <code>false</code>
     * @throws IOException If reading from stream fails
     */
    public static boolean containsHTMLTags(InputStream in, boolean strict, boolean oneShot) throws IOException {
        if (null == in) {
            return false;
        }
        try {
            int buflen = 8192;
            byte[] buf = new byte[buflen];

            boolean found = false;
            for (int read; !found && (read = in.read(buf, 0, buflen)) > 0;) {
                found = containsHTMLTags(Charsets.toAsciiString(buf, 0, read), strict);
                if (oneShot) {
                    return found;
                }
            }
            return found;
        } finally {
            Streams.close(in);
        }
    }

    /**
     * Checks if given byte sequence contains common HTML tags.
     *
     * @param sequence The byte sequence to check
     * @return <code>true</code> if given byte sequence contains common HTML tags; otherwise <code>false</code>
     */
    public static boolean containsHTMLTags(final byte[] sequence) {
        if (sequence == null) {
            throw new NullPointerException();
        }
        return containsHTMLTags(Charsets.toAsciiString(sequence));
    }

    /**
     * Checks if given byte sequence contains common HTML tags.
     *
     * @param sequence The byte sequence to check
     * @param strict <code>true</code> for strict checking; otherwise <code>false</code>
     * @return <code>true</code> if given byte sequence contains common HTML tags; otherwise <code>false</code>
     */
    public static boolean containsHTMLTags(final byte[] sequence, final boolean strict) {
        if (sequence == null) {
            throw new NullPointerException();
        }
        return containsHTMLTags(Charsets.toAsciiString(sequence), strict);
    }

    /**
     * Checks if given byte sequence contains common HTML tags.
     *
     * @param sequence The byte sequence to check
     * @param tags Additional tags to look for
     * @return <code>true</code> if given byte sequence contains common HTML tags; otherwise <code>false</code>
     */
    public static boolean containsHTMLTags(final byte[] sequence, final String... tags) {
        if (sequence == null) {
            throw new NullPointerException();
        }
        return containsHTMLTags(Charsets.toAsciiString(sequence), tags);
    }

    /**
     * Checks if given byte sequence contains common HTML tags.
     *
     * @param sequence The byte sequence to check
     * @param off The offset within byte array
     * @param len The length of valid bytes starting from offset
     * @param tags Additional tags to look for
     * @return <code>true</code> if given byte sequence contains common HTML tags; otherwise <code>false</code>
     */
    public static boolean containsHTMLTags(final byte[] sequence, final int off, final int len, final String... tags) {
        if (sequence == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || len > sequence.length - off) {
            throw new IndexOutOfBoundsException();
        }
        return containsHTMLTags(Charsets.toAsciiString(sequence, off, len), tags);
    }

    /**
     * Checks if given byte sequence contains common HTML tags.
     *
     * @param sequence The byte sequence to check
     * @param off The offset within byte array
     * @param len The length of valid bytes starting from offset
     * @return <code>true</code> if given byte sequence contains common HTML tags; otherwise <code>false</code>
     */
    public static boolean containsHTMLTags(final byte[] sequence, final int off, final int len) {
        if (sequence == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || len > sequence.length - off) {
            throw new IndexOutOfBoundsException();
        }
        return containsHTMLTags(Charsets.toAsciiString(sequence, off, len));
    }

    /**
     * Checks if given byte sequence contains specified HTML tag.
     *
     * @param sequence The byte sequence to check
     * @param tag The HTML tag; e.g. <code>"body"</code>
     * @return <code>true</code> if given byte sequence contains specified HTML tag; otherwise <code>false</code>
     */
    public static boolean containsHTMLTag(final byte[] sequence, final String tag) {
        if (sequence == null) {
            throw new NullPointerException();
        }
        return containsHTMLTag(Charsets.toAsciiString(sequence), tag);
    }

    /**
     * Checks if given byte sequence contains specified HTML tag.
     *
     * @param sequence The byte sequence to check
     * @param off The offset within byte array
     * @param len The length of valid bytes starting from offset
     * @param tag The HTML tag; e.g. <code>"body"</code>
     * @return <code>true</code> if given byte sequence contains specified HTML tag; otherwise <code>false</code>
     */
    public static boolean containsHTMLTag(final byte[] sequence, final int off, final int len, final String tag) {
        if (sequence == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || len > sequence.length - off) {
            throw new IndexOutOfBoundsException();
        }
        return containsHTMLTag(Charsets.toAsciiString(sequence, off, len), tag);
    }

}
