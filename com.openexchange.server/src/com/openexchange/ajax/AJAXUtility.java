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

package com.openexchange.ajax;

import static com.openexchange.html.HtmlServices.fullUrlDecode;
import static com.openexchange.java.Streams.close;
import static com.openexchange.java.Strings.isEmpty;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.URI;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;


/**
 * {@link AJAXUtility} - A utility class for AJAX components/communication.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJAXUtility {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AJAXUtility.class);

    // ------------------------------------- START Helper classes-------------------------------------------- //

    private static final class URIExtended extends URI {

        /**
         * BitSet for URI-reference.
         * <p><blockquote><pre>
         * URI-reference = [ absoluteURI | relativeURI ] [ "#" fragment ]
         * </pre></blockquote><p>
         */
        public static BitSet URI_REFERENCE = URI_reference;
    }

    // ------------------------------------- STOP Helper classes-------------------------------------------- //

    private static final Tika TIKA;
    static {
        TIKA = new Tika(TikaConfig.getDefaultConfig());
    }

    /**
     * No instance.
     */
    private AJAXUtility() {
        super();
    }

    /**
     * Detects the MIME type from given input stream using <a href="http://tika.apache.org/">Apache Tika - a content analysis toolkit</a>.
     *
     * @param in The input stream
     * @return The detected input stream
     * @throws IOException If an I/O error occurs
     */
    public static String detectMimeType(final InputStream in) throws IOException {
        if (null == in) {
            return null;
        }
        try {
            return TIKA.detect(in);
        } finally {
            close(in);
        }
    }

    /**
     * BitSet of www-form-url safe characters.
     */
    protected static final BitSet WWW_FORM_URL;

    /**
     * BitSet of www-form-url safe characters including safe characters for an anchor.
     */
    protected static final BitSet WWW_FORM_URL_ANCHOR;

    // Static initializer for www_form_url
    static {
        {
            final BitSet bitSet = new BitSet(256);
            // alpha characters
            for (int i = 'a'; i <= 'z'; i++) {
                bitSet.set(i);
            }
            for (int i = 'A'; i <= 'Z'; i++) {
                bitSet.set(i);
            }
            // numeric characters
            for (int i = '0'; i <= '9'; i++) {
                bitSet.set(i);
            }
            // special chars
            bitSet.set('-');
            bitSet.set('_');
            bitSet.set('.');
            bitSet.set('*');
            // blank to be replaced with +
            bitSet.set(' ');
            WWW_FORM_URL = bitSet;
        }
        WWW_FORM_URL_ANCHOR = URIExtended.URI_REFERENCE;
    }

    private static final Pattern PATTERN_CONTROL = Pattern.compile("[\\x00-\\x1F\\x7F]");

    /**
     * Sanitizes specified String input.
     * <ul>
     * <li>Do URL decoding until fully decoded
     * <li>Drop ASCII control characters
     * <li>Escape using HTML entities
     * <li>Replace double slashes with single one
     * </ul>
     *
     * @param sInput The input to sanitize, can be <code>null</code> or empty
     * @return The sanitized input or the original value if <code>null</code> or empty
     */
    public static String sanitizeParam(String sInput) {
        if (isEmpty(sInput)) {
            return sInput;
        }

        String s = sInput;

        // Do URL decoding until fully decoded
        s = fullUrlDecode(s);

        // Drop ASCII control characters
        s = PATTERN_CONTROL.matcher(s).replaceAll("");

        // Escape using HTML entities
        s = org.apache.commons.lang.StringEscapeUtils.escapeHtml(s);

        // Replace double slashes with single one
        {
            final Pattern patternDslash = PATTERN_DSLASH;
            Matcher matcher = patternDslash.matcher(s);
            while (matcher.find()) {
                s = matcher.replaceAll("/");
                matcher = patternDslash.matcher(s);
            }
        }

        // Return result
        return s;
    }

    private static final Pattern PATTERN_DSLASH = Pattern.compile("(?://+)");
    private static final Pattern PATTERN_CRLF = Pattern.compile("\r?\n|\r|(?:%0[aA])?%0[dD]|%0[aA]");
    private static final Pattern PATTERN_DSLASH2 = Pattern.compile("(?:/|%2[fF]){2,}");

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     * @throws IllegalArgumentException If URL is invalid
     */
    public static String encodeUrl(final String s, final boolean forAnchor, final boolean forLocation, final String charsetName) {
        if (isEmpty(s)) {
            return s;
        }
        try {
            String prefix = null;
            // Strip possible "\r?\n" and/or "%0A?%0D"
            String retval = PATTERN_CRLF.matcher(s).replaceAll("");
            final Charset charset;
            {
                final String cs = isEmpty(charsetName) ? ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding) : charsetName;
                charset = isEmpty(cs) ? Charsets.UTF_8 : Charsets.forName(cs);
            }
            if (forAnchor) {
                // Prepare for being used as anchor/link
                retval = Charsets.toAsciiString(URLCodec.encodeUrl(WWW_FORM_URL_ANCHOR, retval.getBytes(charset)));
                final int pos = retval.length() > 6 ? retval.indexOf("://") : -1;
                if (pos > 0) { // Seems to contain protocol/scheme part; e.g "http://..."
                    final String tmp = Strings.toLowerCase(retval.substring(0, pos));
                    if ("https".equals(tmp)) {
                        prefix = "https://";
                        retval = retval.substring(pos + 3);
                    } else if ("http".equals(tmp)) {
                        prefix = "http://";
                        retval = retval.substring(pos + 3);
                    }
                }
            } else {
                retval = Charsets.toAsciiString(URLCodec.encodeUrl(WWW_FORM_URL, retval.getBytes(charset)));
            }
            // Again -- Strip possible "\r?\n" and/or "%0A?%0D"
            retval = PATTERN_CRLF.matcher(retval).replaceAll("");
            // Check for a relative URI
            Pattern dupSlashes = PATTERN_DSLASH;
            if (forLocation) {
                try {
                    final java.net.URI uri = new java.net.URI(retval);
                    if (uri.isAbsolute() || null != uri.getScheme() || null != uri.getHost()) {
                        throw new IllegalArgumentException("Illegal Location value: " + s);
                    }
                } catch (final URISyntaxException e) {
                    throw new IllegalArgumentException("Illegal Location value: " + s, e);
                }
                // Adapt pattern
                dupSlashes = PATTERN_DSLASH2;
            }
            // Replace double slashes with single one
            {
                Matcher matcher = dupSlashes.matcher(retval);
                while (matcher.find()) {
                    retval = matcher.replaceAll("/");
                    matcher = dupSlashes.matcher(retval);
                }
            }
            return null == prefix ? retval : new StringBuilder(prefix).append(retval).toString();
        } catch (final IllegalArgumentException e) {
            throw e;
        } catch (final RuntimeException e) {
            LOG.error("A runtime error occurred.", e);
            return s;
        }
    }

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     * @throws IllegalArgumentException If URL is invalid
     */
    public static String encodeUrl(final String s, final boolean forAnchor, final boolean forLocation) {
        return encodeUrl(s, forAnchor, forLocation, null);
    }

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     * @throws IllegalArgumentException If URL is invalid
     */
    public static String encodeUrl(final String s, final boolean forAnchor) {
        return encodeUrl(s, forAnchor, false);
    }

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     */
    public static String encodeUrl(final String s) {
        return encodeUrl(s, false);
    }

    private static final ConcurrentMap<String, URLCodec> URL_CODECS = new ConcurrentHashMap<String, URLCodec>(8, 0.9f, 1);

    private static URLCodec getUrlCodec(final String charset) {
        String cs = charset;
        if (null == cs) {
            final String defCharset = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
            if (null == defCharset) {
                return null;
            }
            cs = defCharset;
        }
        URLCodec urlCodec = URL_CODECS.get(cs);
        if (null == urlCodec) {
            final URLCodec nc = new URLCodec(cs);
            urlCodec = URL_CODECS.putIfAbsent(cs, nc);
            if (null == urlCodec) {
                urlCodec = nc;
            }
        }
        return urlCodec;
    }

    /**
     * URL decodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     */
    public static String decodeUrl(final String s, final String charset) {
        try {
            if (isEmpty(s)) {
                return s;
            }
            final String cs = isEmpty(charset) ? ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding) : charset;
            return getUrlCodec(cs).decode(s, cs);
        } catch (final DecoderException e) {
            return s;
        } catch (final UnsupportedEncodingException e) {
            return s;
        }
    }

    /**
     * Gets the writer from given HTTP response.
     *
     * @param resp The HTTP response
     * @return The writer or <code>null</code>
     * @throws IOException If an I/O error occurs
     */
    public static PrintWriter getWriter(final HttpServletResponse resp, final boolean tryFallBack) throws IOException {
        if (null == resp) {
            return null;
        }
        try {
            return resp.getWriter();
        } catch (final IllegalStateException e) {
            // ServletResponse.getOutputStream() was already called
            if (tryFallBack) {
                try {
                    String charenc = resp.getCharacterEncoding();
                    if (isEmpty(charenc)) {
                        charenc = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
                        if (isEmpty(charenc)) {
                            charenc = "UTF-8";
                        }
                    }
                    return new PrintWriter(new OutputStreamWriter(resp.getOutputStream(), charenc), false);
                } catch (final Exception x) {
                    LOG.warn("Unable to acquire Writer instance from HTTP response.", x);
                }
            } else {
                LOG.warn("Unable to acquire Writer instance from HTTP response.", e);
            }
            return null;
        }
    }

}
