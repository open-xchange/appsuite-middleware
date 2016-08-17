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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.mozilla.universalchardet.UniversalDetector;

/**
 * {@link CharsetDetector} - A charset detector based on <a href="https://code.google.com/p/juniversalchardet/">juniversalchardet</a>
 * library, as included in Apache Tika bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CharsetDetector {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CharsetDetector.class);

    private static final String FALLBACK = "ISO-8859-1";

    /**
     * Initializes a new {@link CharsetDetector}
     */
    private CharsetDetector() {
        super();
    }

    /**
     * Gets the fall-back charset name.
     *
     * @return The fall-back charset name
     */
    public static String getFallback() {
        return FALLBACK;
    }

    /**
     * Convenience method to check if given name is valid; meaning not <code>null</code>, a legal charset name and supported as indicated by
     * {@link Charset#isSupported(String)}.
     *
     * @param charset The charset name whose validity shall be checked
     * @return <code>true</code> if given name is valid; otherwise <code>false</code>
     */
    public static boolean isValid(final String charset) {
        try {
            return (null != charset && checkName(charset) && Charset.isSupported(charset));
        } catch (final RuntimeException rte) {
            LOG.warn("RuntimeException while checking charset: {}", charset, rte);
            return false;
        } catch (final Error e) {
            handleThrowable(e);
            LOG.warn("Error while checking charset: {}", charset, e);
            return false;
        } catch (final Throwable t) {
            handleThrowable(t);
            LOG.warn("Unexpected error while checking charset: {}", charset, t);
            return false;
        }
    }

    /**
     * Checks whether the supplied <tt>Throwable</tt> is one that needs to be rethrown and swallows all others.
     *
     * @param t The <tt>Throwable</tt> to check
     */
    private static void handleThrowable(final Throwable t) {
        if (t instanceof ThreadDeath) {
            LOG.error(" ---=== /!\\ ===--- Thread death ---=== /!\\ ===--- ", t);
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            LOG.error(
                " ---=== /!\\ ===--- The Java Virtual Machine is broken or has run out of resources necessary for it to continue operating. ---=== /!\\ ===--- ",
                t);
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }

    /**
     * Checks that the given string is a legal charset name.
     *
     * @param s The charset name
     * @throws NullPointerException If given name is <code>null</code>
     * @return <code>true</code> if the given name is a legal charset name; otherwise <code>false</code>
     */
    public static boolean checkName(final String s) {
        if (s == null) {
            throw new NullPointerException("name is null");
        }
        final int n = s.length();
        if (n == 0) {
            return false;
        }
        boolean legal = true;
        for (int i = 0; legal && i < n; i++) {
            final char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                continue;
            }
            if (c >= 'a' && c <= 'z') {
                continue;
            }
            if (c >= '0' && c <= '9') {
                continue;
            }
            if (c == '-') {
                continue;
            }
            if (c == ':') {
                continue;
            }
            if (c == '_') {
                continue;
            }
            if (c == '.') {
                continue;
            }
            legal = false;
        }
        return legal;
    }

    /**
     * Detects the charset of specified byte array.
     *
     * @param in The byte array to examine
     * @throws NullPointerException If byte array is <code>null</code>
     * @return The detected charset or <i>US-ASCII</i> if no matching/supported charset could be found
     */
    public static String detectCharset(final byte[] in) {
        return detectCharset(in, in.length);
    }

    /**
     * Detects the charset of specified byte array.
     *
     * @param in The byte array to examine
     * @param len The bytes length
     * @throws NullPointerException If byte array is <code>null</code>
     * @return The detected charset or <i>US-ASCII</i> if no matching/supported charset could be found
     */
    public static String detectCharset(final byte[] in, final int len) {
        if (null == in) {
            throw new NullPointerException("byte array input stream is null");
        }
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(in, 0, len);
        detector.dataEnd();
        return getResultingCharset(detector);
    }

    /**
     * Detects the charset of specified byte array input stream's data.
     * <p>
     * <b>Note</b>: Specified input stream is going to be closed in this method.
     *
     * @param in The byte array input stream to examine
     * @throws NullPointerException If input stream is <code>null</code>
     * @return The detected charset or <i>US-ASCII</i> if no matching/supported charset could be found
     */
    public static String detectCharset(final ByteArrayInputStream in) {
        return detectCharset((InputStream)in);
    }

    /**
     * Detects the charset of specified input stream's data.
     * <p>
     * <b>Note</b>: Specified input stream is going to be closed in this method.
     *
     * @param in The input stream to examine
     * @throws NullPointerException If input stream is <code>null</code>
     * @return The detected charset or <i>US-ASCII</i> if no matching/supported charset could be found
     */
    public static String detectCharset(final InputStream in) {
        return detectCharset(in, getFallback(), true);
    }

    /**
     * Detects the charset of specified input stream's data.
     *
     * @param in The input stream to examine
     * @param fallback The fallback charset to return if detection was not successful
     * @param close <code>true</code> to close the input stream after detection, <code>false</code>, otherwise
     * @throws NullPointerException If input stream is <code>null</code>
     * @return The detected charset or <i>US-ASCII</i> if no matching/supported charset could be found
     */
    public static String detectCharset(final InputStream in, String fallback, boolean close) {
        try {
            return detectCharsetFailOnError(in, fallback, close);
        } catch (final IOException e) {
            LOG.error("", e);
            return FALLBACK;
        }
    }

    /**
     * Detects the charset of specified input stream's data.
     * <p>
     * <b>Note</b>: Specified input stream is going to be closed in this method.
     *
     * @param in The input stream to examine
     * @throws NullPointerException If input stream is <code>null</code>
     * @throws IOException If reading from stream fails
     * @return The detected charset or <i>US-ASCII</i> if no matching/supported charset could be found
     */
    public static String detectCharsetFailOnError(final InputStream in) throws IOException {
        return detectCharsetFailOnError(in, getFallback(), true);
    }

    /**
     * Detects the charset of specified input stream's data.
     *
     * @param in The input stream to examine
     * @param fallback The fallback charset to return if detection was not successful
     * @param close <code>true</code> to close the input stream after detection, <code>false</code>, otherwise
     * @throws NullPointerException If input stream is <code>null</code>
     * @throws IOException If reading from stream fails
     * @return The detected charset or the supplied fallback if no matching/supported charset could be found
     */
    public static String detectCharsetFailOnError(final InputStream in, String fallback, boolean close) throws IOException {
        if (null == in) {
            throw new NullPointerException("input stream is null");
        }
        UniversalDetector detector = new UniversalDetector(null);
        try {
            byte[] buffer = new byte[4096];
            int read;
            while (0 < (read = in.read(buffer)) && false == detector.isDone()) {
                detector.handleData(buffer, 0, read);
            }
        } catch (IOException e) {
            LOG.warn("", e);
        } finally {
            if (close) {
                Streams.close(in);
            }
        }
        detector.dataEnd();
        return getResultingCharset(detector, fallback);
    }

    private static String getResultingCharset(UniversalDetector detector) {
        String detectedCharset = detector.getDetectedCharset();
        if (null == detectedCharset || false == isValid(detectedCharset)) {
            return FALLBACK;
        }
        return detectedCharset;
    }

    private static String getResultingCharset(UniversalDetector detector, String fallback) {
        String detectedCharset = detector.getDetectedCharset();
        if (null == detectedCharset || false == isValid(detectedCharset)) {
            return fallback;
        }
        return detectedCharset;
    }

}
