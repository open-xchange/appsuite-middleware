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

import static com.openexchange.java.Strings.isEmpty;
import static com.openexchange.java.Strings.toLowerCase;
import static com.openexchange.java.Strings.toUpperCase;
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
     * Checks if given String contains common HTML tags.
     *
     * @param sequence The String to check
     * @return <code>true</code> if given String contains common HTML tags; otherwise <code>false</code>
     */
    public static boolean containsHTMLTags(final String sequence) {
        if (sequence == null) {
            throw new NullPointerException();
        }
        final String lc = toLowerCase(sequence);
        if ((lc.indexOf("<html>") >= 0)) {
            return true;
        }
        if ((lc.indexOf("<head>") >= 0)) {
            return true;
        }
        if ((lc.indexOf("<body>") >= 0)) {
            return true;
        }
        if ((lc.indexOf("<script") >= 0)) {
            return true;
        }
        if ((lc.indexOf("javascript") >= 0)) {
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
        for (final String jsEventHandler : JS_EVENT_HANDLER) {
            if (lc.indexOf(jsEventHandler) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if given String contains common HTML tags.
     *
     * @param sequence The String to check
     * @param strict <code>true</code> for strict checking; otherwise <code>false</code>
     * @return <code>true</code> if given String contains common HTML tags; otherwise <code>false</code>
     */
    public static boolean containsHTMLTags(final String sequence, final boolean strict) {
        return strict ? containsHTMLTags(sequence, "<br", "<p>") : containsHTMLTags(sequence);
    }

    /**
     * Checks if given String contains common HTML tags.
     *
     * @param sequence The String to check
     * @param tags Additional tags to look for
     * @return <code>true</code> if given String contains common HTML tags; otherwise <code>false</code>
     */
    public static boolean containsHTMLTags(final String sequence, final String... tags) {
        if (sequence == null) {
            throw new NullPointerException();
        }
        final String lc = toLowerCase(sequence);
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
        if ((lc.indexOf("javascript") >= 0)) {
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
        for (final String jsEventHandler : JS_EVENT_HANDLER) {
            if (lc.indexOf(jsEventHandler) >= 0) {
                return true;
            }
        }
        if (null != tags) {
            for (int i = tags.length; i-- > 0;) {
                final String tag = tags[i];
                if (!Strings.isEmpty(tag) && (lc.indexOf(tag) >= 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if given String contains specified HTML tag.
     *
     * @param sequence The String to check
     * @param tag The HTML tag; e.g. <code>"body"</code>
     * @return <code>true</code> if given String contains specified HTML tag; otherwise <code>false</code>
     */
    public static boolean containsHTMLTag(final String sequence, final String tag) {
        if (sequence == null) {
            throw new NullPointerException();
        }
        return containsIgnoreCase(sequence, new StringBuilder(tag.length() + 2).append('<').append(tag).append('>').toString());
    }

    /**
     * Checks if given String contains specified string.
     *
     * @param sequence The String to check
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
     * @param sequence The byte sequence to check
     * @return <code>true</code> if given byte sequence contains common HTML tags; otherwise <code>false</code>
     */
    public static boolean containsHTMLTags(final byte[] sequence) {
        if (sequence == null) {
            throw new NullPointerException();
        }
        if (containsHTMLTag(sequence, "html")) {
            return true;
        }
        if (containsHTMLTag(sequence, "head")) {
            return true;
        }
        if (containsHTMLTag(sequence, "body")) {
            return true;
        }
        if (containsIgnoreCase(sequence, "<script")) {
            return true;
        }
        if (containsIgnoreCase(sequence, "javascript")) {
            return true;
        }
        if (containsIgnoreCase(sequence, "<img")) {
            return true;
        }
        if (containsIgnoreCase(sequence, "<object")) {
            return true;
        }
        if (containsIgnoreCase(sequence, "<embed")) {
            return true;
        }
        for (final String jsEventHandler : JS_EVENT_HANDLER) {
            if (containsIgnoreCase(sequence, jsEventHandler)) {
                return true;
            }
        }
        return false;
    }

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
                found = strict ? containsHTMLTags(buf, 0, read, "<br", "<p>") : containsHTMLTags(buf, 0, read);
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
     * @param strict <code>true</code> for strict checking; otherwise <code>false</code>
     * @return <code>true</code> if given byte sequence contains common HTML tags; otherwise <code>false</code>
     */
    public static boolean containsHTMLTags(final byte[] sequence, final boolean strict) {
        return strict ? containsHTMLTags(sequence, "<br", "<p>") : containsHTMLTags(sequence);
    }

    /**
     * Checks if given byte sequence contains common HTML tags.
     *
     * @param sequence The byte sequence to check
     * @param tags Additional tags to look for
     * @return <code>true</code> if given byte sequence contains common HTML tags; otherwise <code>false</code>
     */
    public static boolean containsHTMLTags(final byte[] sequence, final String... tags) {
        if (containsHTMLTags(sequence)) {
            return true;
        }
        if (null != tags) {
            for (int i = tags.length; i-- > 0;) {
                final String tag = tags[i];
                if (!isEmpty(tag) && containsIgnoreCase(sequence, tag)) {
                    return true;
                }
            }
        }
        return false;
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
        if (containsHTMLTags(sequence, off, len)) {
            return true;
        }
        if (null != tags) {
            for (int i = tags.length; i-- > 0;) {
                final String tag = tags[i];
                if (!isEmpty(tag) && containsIgnoreCase(sequence, off, len, tag)) {
                    return true;
                }
            }
        }
        return false;
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

        if (containsHTMLTag(sequence, off, len, "html")) {
            return true;
        }
        if (containsHTMLTag(sequence, off, len, "head")) {
            return true;
        }
        if (containsHTMLTag(sequence, off, len, "body")) {
            return true;
        }
        if (containsIgnoreCase(sequence, off, len, "<script")) {
            return true;
        }
        if (containsIgnoreCase(sequence, off, len, "javascript")) {
            return true;
        }
        if (containsIgnoreCase(sequence, off, len, "<img")) {
            return true;
        }
        if (containsIgnoreCase(sequence, off, len, "<object")) {
            return true;
        }
        if (containsIgnoreCase(sequence, off, len, "<embed")) {
            return true;
        }
        for (final String jsEventHandler : JS_EVENT_HANDLER) {
            if (containsIgnoreCase(sequence, off, len, jsEventHandler)) {
                return true;
            }
        }
        return false;
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
        return containsIgnoreCase(sequence, new StringBuilder(tag.length() + 2).append('<').append(tag).append('>').toString());
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
        return containsIgnoreCase(sequence, off, len, new StringBuilder(tag.length() + 2).append('<').append(tag).append('>').toString());
    }

    /**
     * Checks if given byte sequence contains specified string.
     *
     * @param sequence The byte sequence to check
     * @param str The string
     * @return <code>true</code> if given byte sequence contains specified string; otherwise <code>false</code>
     */
    private static boolean containsIgnoreCase(final byte[] sequence, final String str) {
        // lower-case
        if (indexOf(sequence, Charsets.toAsciiBytes(toLowerCase(str)), 0, sequence.length) >= 0) {
            return true;
        }
        // upper-case
        return (indexOf(sequence, Charsets.toAsciiBytes(toUpperCase(str)), 0, sequence.length) >= 0);
    }

    /**
     * Checks if given byte sequence contains specified string.
     *
     * @param sequence The byte sequence to check
     * @param str The string
     * @return <code>true</code> if given byte sequence contains specified string; otherwise <code>false</code>
     */
    private static boolean containsIgnoreCase(final byte[] sequence, final int off, final int len, final String str) {
        // lower-case
        if (indexOf(sequence, Charsets.toAsciiBytes(toLowerCase(str)), off, len) >= 0) {
            return true;
        }
        // upper-case
        return (indexOf(sequence, Charsets.toAsciiBytes(toUpperCase(str)), off, len) >= 0);
    }

    /**
     * Finds the first occurrence of the pattern in the byte (sub-)array using KMP algorithm.
     * <p>
     * The sub-array to search in begins at the specified <code>beginIndex</code> and extends to the byte at index <code>endIndex - 1</code>
     * . Thus the length of the sub-array is <code>endIndex-beginIndex</code>.
     *
     * @param data The byte array to search in
     * @param pattern The byte pattern to search for
     * @param beginIndex The beginning index, inclusive.
     * @param endIndex The ending index, exclusive.
     * @return The index of the first occurrence of the pattern in the byte array starting from given index or <code>-1</code> if none
     *         found.
     */
    private static int indexOf(final byte[] data, final byte[] pattern, final int beginIndex, final int endIndex) {
        if ((beginIndex < 0) || (beginIndex > data.length)) {
            throw new IndexOutOfBoundsException(Integer.toString(beginIndex));
        }
        if ((endIndex < 0) || (endIndex > data.length)) {
            throw new IndexOutOfBoundsException(Integer.toString(endIndex));
        }
        if ((beginIndex > endIndex)) {
            throw new IndexOutOfBoundsException(Integer.toString(endIndex - beginIndex));
        }

        if (data.length == 0) {
            return -1;
        }

        int[] failure = computeFailure(pattern);
        if (failure == null) {
            throw new IllegalArgumentException("pattern is null");
        }

        int j = 0;
        for (int i = beginIndex; i < endIndex; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    /**
     * Computes the failure function using a boot-strapping process, where the pattern matches against itself.
     *
     * @param pattern The pattern
     * @return The failures
     */
    private static int[] computeFailure(final byte[] pattern) {
        if (pattern == null) {
            return null;
        }

        int[] failure = new int[pattern.length];
        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }
        return failure;
    }

}
