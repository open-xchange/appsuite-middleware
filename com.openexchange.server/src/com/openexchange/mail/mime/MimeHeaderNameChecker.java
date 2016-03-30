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

import java.io.InputStream;
import com.openexchange.ajax.helper.CombinedInputStream;
import com.openexchange.java.Charsets;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;


/**
 * {@link MimeHeaderNameChecker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeHeaderNameChecker {

    /**
     * Initializes a new {@link MimeHeaderNameChecker}.
     */
    private MimeHeaderNameChecker() {
        super();
    }

    private static final byte[] PATTERN_LFCRLF = { (byte)'\n', (byte)'\r', (byte)'\n' };

    private static final byte[] PATTERN_LFLF = { (byte)'\n', (byte)'\n' };

    /**
     * Sanitizes header names of specified RFC822 bytes
     *
     * @param bytes The RFC822 bytes to sanitize
     * @return The sanitized RFC822 bytes
     */
    public static InputStream sanitizeHeaderNames(final byte[] bytes) {
        final int length = bytes.length;
        // Look-up double line break
        int index = indexOf(bytes, PATTERN_LFCRLF, 0, length);
        if (index < 0) {
            index = indexOf(bytes, PATTERN_LFLF, 0, length);
            if (index < 0) {
                // No double line break found which delimiters header and body parts
                return new UnsynchronizedByteArrayInputStream(bytes);
            }
        }
        try {
            final HeaderCollection hc = new HeaderCollection(new UnsynchronizedByteArrayInputStream(bytes, 0, index));
            String hcStr = hc.toString();
            final byte[] csb;
            if (hcStr.endsWith("\r\n")) {
                byte[] src = hcStr.getBytes(Charsets.ISO_8859_1);
                csb = new byte[src.length - 2];
                System.arraycopy(src, 0, csb, 0, csb.length);
            } else {
                csb = hcStr.getBytes(Charsets.ISO_8859_1);
            }
            return new CombinedInputStream(csb, new UnsynchronizedByteArrayInputStream(bytes, index, length));
        } catch (final Exception e) {
            return new UnsynchronizedByteArrayInputStream(bytes);
        }
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
     * @throws IndexOutOfBoundsException If <code>beginIndex</code> and/or <code>endIndex</code> is invalid
     * @throws IllegalArgumentException If given pattern is <code>null</code>
     */
    private static int indexOf(final byte[] data, final byte[] pattern, final int beginIndex, final int endIndex) {
        if ((beginIndex < 0) || (beginIndex > data.length)) {
            throw new IndexOutOfBoundsException(String.valueOf(beginIndex));
        }
        if ((endIndex < 0) || (endIndex > data.length)) {
            throw new IndexOutOfBoundsException(String.valueOf(endIndex));
        }
        if ((beginIndex > endIndex)) {
            throw new IndexOutOfBoundsException(String.valueOf(endIndex - beginIndex));
        }

        final int[] failure = computeFailure(pattern);
        if (failure == null) {
            throw new IllegalArgumentException("pattern is null");
        }

        int j = 0;
        if (data.length == 0) {
            return -1;
        }

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
        final int[] failure = new int[pattern.length];

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
