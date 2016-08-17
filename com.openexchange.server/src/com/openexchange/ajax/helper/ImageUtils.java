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

package com.openexchange.ajax.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link ImageUtils} - Image utility class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ImageUtils {

    /**
     * Initializes a new {@link ImageUtils}.
     */
    private ImageUtils() {
        super();
    }

    /**
     * Checks if specified image data indicate an SVG image.
     *
     * @param bytes The image data to check
     * @return <code>true</code> if SVG data; otherwise <code>false</code>
     */
    public static boolean isSvg(byte[] bytes) {
        byte[] pattern = new byte[] { '<', 's', 'v', 'g', ' ' };
        int pos = indexOf(bytes, pattern, 0, bytes.length);
        if (pos >= 0) {
            return true;
        }

        return Strings.asciiLowerCase(new String(bytes, Charsets.ISO_8859_1)).indexOf("<svg ") >= 0;
    }

    /**
     * Checks if passed input stream carries an animated .gif image.
     *
     * @param in The input stream to examine
     * @return <code>true</code> if data appears to be an animated gif image; otherwise <code>false</code>
     * @throws IOException If an I/O error occurs
     */
    public static boolean isAnimatedGif(final InputStream in) throws IOException {
        try {
            return isAnimatedGif(in, null);
        } finally {
            Streams.close(in);
        }
    }

    /**
     * Checks if passed input stream carries an animated .gif image.
     *
     * @param in The input stream to examine
     * @param newStreamRef The reference to store input stream to read from after method invocation
     * @return <code>true</code> if data appears to be an animated gif image; otherwise <code>false</code>
     * @throws IOException If an I/O error occurs
     */
    public static boolean isAnimatedGif(final InputStream in, final AtomicReference<InputStream> newStreamRef) throws IOException {
        if (null == in) {
            return false;
        }
        final ByteArrayOutputStream sink = null == newStreamRef ? null : new ByteArrayOutputStream(8192);
        final int blen = 2048;
        final byte[] buf = new byte[blen];
        final byte magic1 = (byte) 0x21;
        final byte magic2 = (byte) 0xf9;
        final byte magic3 = (byte) 0x04;
        final byte[] frameStart = new byte[] { magic1, magic2, magic3 };

        int frames = 0;
        boolean eof = false;
        boolean first = true;
        boolean checkStartsWithMagic23 = false;
        boolean checkStartsWithMagic3 = false;

        while (!eof && frames <= 1) {
            final int read = in.read(buf, 0, blen);
            if (read <= 0) {
                eof = true;
            } else {
                if (null != sink) {
                    sink.write(buf, 0, read);
                }
                if (first) {
                    if (((byte) 'G' != buf[0]) || ((byte) 'I' != buf[1]) || ((byte) 'F' != buf[2])) {
                        if (null != newStreamRef && null != sink) {
                            newStreamRef.set(new CombinedInputStream(sink.toByteArray(), in));
                        }
                        return false;
                    }
                    first = false;
                }
                int start = 0;
                if (checkStartsWithMagic3) {
                    if (buf[0] == magic3) {
                        frames++;
                        start = 1;
                    }
                    checkStartsWithMagic3 = false;
                } else if (checkStartsWithMagic23) {
                    if ((buf[0] == magic2) && (buf[1] == magic3)) {
                        frames++;
                        start = 2;
                    }
                    checkStartsWithMagic23 = false;
                }
                // Check for further frames
                int pos = indexOf(buf, frameStart, start, read);
                if (pos >= 0) {
                    frames++;
                    // Check for more frames in byte range
                    do {
                        pos = indexOf(buf, frameStart, pos + 1, read);
                        if (pos >= 0) {
                            if (null != newStreamRef && null != sink) {
                                newStreamRef.set(new CombinedInputStream(sink.toByteArray(), in));
                            }
                            return true;
                        } else if (buf[read - 1] == magic1) {
                            checkStartsWithMagic23 = true;
                        } else if ((buf[read - 2] == magic1) && (buf[read - 1] == magic2)) {
                            checkStartsWithMagic3 = true;
                        }
                    } while (pos >= 0);
                } else if (buf[read - 1] == magic1) {
                    checkStartsWithMagic23 = true;
                } else if ((buf[read - 2] == magic1) && (buf[read - 1] == magic2)) {
                    checkStartsWithMagic3 = true;
                }
            }
        }

        if (null != newStreamRef && null != sink) {
            newStreamRef.set(new CombinedInputStream(sink.toByteArray(), in));
        }
        return frames > 1;
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
