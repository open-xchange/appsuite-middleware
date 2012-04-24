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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mail.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

/**
 * {@link CharsetDetector} - A charset detector based on <a href="http://jchardet.sourceforge.net/">jchardet</a> library.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CharsetDetector {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CharsetDetector.class));

    private static final String STR_US_ASCII = "US-ASCII";

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
            LOG.warn("RuntimeException while checking charset: " + charset, rte);
            return false;
        } catch (final Error e) {
            handleThrowable(e);
            LOG.warn("Error while checking charset: " + charset, e);
            return false;
        } catch (final Throwable t) {
            handleThrowable(t);
            LOG.warn("Unexpected error while checking charset: " + charset, t);
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
            LOG.fatal(" ---=== /!\\ ===--- Thread death ---=== /!\\ ===--- ", t);
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            LOG.fatal(
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
     * Detects the charset of specified part.
     *
     * @param p The part whose charset shall be detected
     * @return The detected part's charset
     * @throws MessagingException If an error occurs in part's getter methods
     */
    public static String detectPartCharset(final Part p) throws MessagingException {
        try {
            return detectCharset(p.getInputStream());
        } catch (final IOException e) {
            /*
             * Try to get data from raw input stream
             */
            final InputStream rawIn;
            if (p instanceof MimeBodyPart) {
                rawIn = ((MimeBodyPart) p).getRawInputStream();
            } else if (p instanceof MimeMessage) {
                rawIn = ((MimeMessage) p).getRawInputStream();
            } else {
                /*
                 * Neither a MimeBodyPart nor a MimeMessage
                 */
                LOG.error(e.getMessage(), e);
                return FALLBACK;
            }
            return detectCharset(rawIn);
        }
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
        if (null == in) {
            throw new NullPointerException("byte array input stream is null");
        }
        final nsDetector det = new nsDetector(nsPSMDetector.ALL);
        /*
         * Set an observer: The Notify() will be called when a matching charset is found.
         */
        final CharsetDetectionObserver observer = new CharsetDetectionObserver();
        det.Init(observer);

        final byte[] buf = new byte[1024];
        int len;
        boolean done = false;
        boolean isAscii = true;

        while ((len = in.read(buf, 0, buf.length)) != -1) {
            /*
             * Check if the stream is only ascii.
             */
            if (isAscii) {
                isAscii = det.isAscii(buf, len);
            }
            /*
             * DoIt if non-ascii and not done yet.
             */
            if (!isAscii && !done) {
                done = det.DoIt(buf, len, false);
            }
        }
        det.DataEnd();
        /*
         * Check if content is ascii
         */
        if (isAscii) {
            return STR_US_ASCII;
        }
        {
            /*
             * Check observer
             */
            final String charset = observer.getCharset();
            if (null != charset && Charset.isSupported(charset)) {
                return charset;
            }
        }
        /*-
         * Choose first possible charset but prefer:
         * 1. UTF-8
         * 2. WINDOWS-1252
         */
        final String prob[] = det.getProbableCharsets();
        String firstPossibleCharset = null;
        for (int i = 0; i < prob.length; i++) {
            if (Charset.isSupported(prob[i])) {
                if ("utf-8".equals(prob[i].toLowerCase(Locale.ENGLISH))) {
                    return prob[i];
                } else if ("windows-1252".equals(prob[i].toLowerCase(Locale.ENGLISH))) {
                    return prob[i];
                }
                if (null == firstPossibleCharset) {
                    firstPossibleCharset = prob[i];
                }
            }
        }
        return null == firstPossibleCharset ? FALLBACK : firstPossibleCharset;
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
        try {
            return detectCharsetFailOnError(in);
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
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
        if (null == in) {
            throw new NullPointerException("input stream is null");
        }
        final nsDetector det = new nsDetector(nsPSMDetector.ALL);
        /*
         * Set an observer: The Notify() will be called when a matching charset is found.
         */
        final CharsetDetectionObserver observer = new CharsetDetectionObserver();
        det.Init(observer);
        try {
            final byte[] buf = new byte[1024];
            int len;
            boolean done = false;
            boolean isAscii = true;

            while ((len = in.read(buf, 0, buf.length)) != -1) {
                /*
                 * Check if the stream is only ascii.
                 */
                if (isAscii) {
                    isAscii = det.isAscii(buf, len);
                }
                /*
                 * DoIt if non-ascii and not done yet.
                 */
                if (!isAscii && !done) {
                    done = det.DoIt(buf, len, false);
                }
            }
            det.DataEnd();
            /*
             * Check if content is ascii
             */
            if (isAscii) {
                return STR_US_ASCII;
            }
            {
                /*
                 * Check observer
                 */
                final String charset = observer.getCharset();
                if (null != charset && Charset.isSupported(charset)) {
                    return charset;
                }
            }
            /*-
             * Choose first possible charset but prefer:
             * 1. UTF-8
             * 2. WINDOWS-1252
             */
            final String prob[] = det.getProbableCharsets();
            String firstPossibleCharset = null;
            for (int i = 0; i < prob.length; i++) {
                final String lcs = prob[i].toLowerCase(Locale.US);
                if (Charset.isSupported(lcs)) {
                    if ("utf-8".equals(lcs)) {
                        return prob[i];
                    } else if ("windows-1252".equals(lcs)) {
                        return prob[i];
                    }
                    if (null == firstPossibleCharset) {
                        firstPossibleCharset = prob[i];
                    }
                } else if ("nomatch".equals(lcs)) {
                    /*
                     * Non-ASCII and nomatch
                     */
                    return FALLBACK;
                }
            }
            /*
             * Non-ASCII
             */
            return null == firstPossibleCharset ? FALLBACK : firstPossibleCharset;
        } finally {
            try {
                in.close();
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * {@link CharsetDetectionObserver} - A charset detection observer according to <a href="http://jchardet.sourceforge.net/">jcharset</a>
     * API
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    private static final class CharsetDetectionObserver implements nsICharsetDetectionObserver {

        private String charset;

        /**
         * Initializes a new {@link CharsetDetectionObserver}
         */
        public CharsetDetectionObserver() {
            super();
        }

        /*
         * (non-Javadoc)
         * @see org.mozilla.intl.chardet.nsICharsetDetectionObserver#Notify(java. lang.String)
         */
        @Override
        public void Notify(final String charset) {
            this.charset = charset;
        }

        /**
         * @return The charset applied to this observer
         */
        public String getCharset() {
            return charset;
        }
    }
}
