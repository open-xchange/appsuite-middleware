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
import java.io.BufferedInputStream;
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
//import org.apache.commons.httpclient.URI;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.CompositeDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
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
     * @param stream The input stream
     * @return The detected input stream
     * @throws IOException If an I/O error occurs
     */
    public static String detectMimeType(final InputStream stream) throws IOException {
        if (null == stream) {
            return null;
        }

        InputStream in = stream;
        InputStream inToUse= null;
        try {
            Detector detector = TIKA.getDetector();
            if (false == (detector instanceof CompositeDetector)) {
                if (in.markSupported()) {
                    return detector.detect(in, new Metadata()).toString();
                }
                return detector.detect(new BufferedInputStream(in), new Metadata()).toString();
            }

            if (in.markSupported()) {
                inToUse = in;
                in = null;
            } else {
                inToUse = new BufferedInputStream(in);
                in = null;
            }
            for (Detector d : ((CompositeDetector) detector).getDetectors()) {
                if (d instanceof MimeTypes) {
                    MediaType mediaType = d.detect(inToUse, new Metadata());
                    if (null != mediaType && false == MediaType.OCTET_STREAM.equals(mediaType)) {
                        return mediaType.toString();
                    }
                }
            }

            // As last resort
            return TIKA.detect(inToUse);
        } finally {
            close(inToUse, in);
        }
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
                } catch (URISyntaxException e) {
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
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException e) {
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
            URLCodec codec = getUrlCodec(cs);
            return null == codec ? s : codec.decode(s, cs);
        } catch (DecoderException e) {
            return s;
        } catch (UnsupportedEncodingException e) {
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
        } catch (IllegalStateException e) {
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
                } catch (Exception x) {
                    LOG.warn("Unable to acquire Writer instance from HTTP response.", x);
                }
            } else {
                LOG.warn("Unable to acquire Writer instance from HTTP response.", e);
            }
            return null;
        }
    }

    /**
     * Returns an {@link Escaper} instance that escapes HTML metacharacters as
     * specified by <a href="http://www.w3.org/TR/html4/">HTML 4.01</a>. The
     * resulting strings can be used both in attribute values and in <em>most</em>
     * elements' text contents, provided that the HTML document's character
     * encoding can encode any non-ASCII code points in the input (as UTF-8 and
     * other Unicode encodings can).
     * <p>
     * In addition also CRLF sequences are replaced with an empty string.
     *
     * <p><b>Note:</b> This escaper only performs minimal escaping to make content
     * structurally compatible with HTML. Specifically, it does not perform entity
     * replacement (symbolic or numeric), so it does not replace non-ASCII code
     * points with character references. This escaper escapes only the following
     * five ASCII characters: {@code '"&<>}.
     */
    public static Escaper htmlEscaper() {
      return HTML_ESCAPER_WITH_CRLF;
    }

    private static final Escaper HTML_ESCAPER_WITH_CRLF =
        Escapers.builder()
            .addEscape('"', "&quot;")
            // Note: "&apos;" is not defined in HTML 4.01.
            .addEscape('\'', "&#39;")
            .addEscape('&', "&amp;")
            .addEscape('<', "&lt;")
            .addEscape('>', "&gt;")
            .addEscape('\r', "")
            .addEscape('\n', "")
            .build();

    // ----------------------------------------------------- Bit sets ----------------------------------------------------------------------

    /**
     * The percent "%" character always has the reserved purpose of being the
     * escape indicator, it must be escaped as "%25" in order to be used as
     * data within a URI.
     */
    private static final BitSet PERCENT = new BitSet(256);
    // Static initializer for percent
    static {
        PERCENT.set('%');
    }

    /**
     * BitSet for digit.
     * <p><blockquote><pre>
     * digit    = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" |
     *            "8" | "9"
     * </pre></blockquote><p>
     */
    private static final BitSet DIGIT = new BitSet(256);
    // Static initializer for digit
    static {
        for (int i = '0'; i <= '9'; i++) {
            DIGIT.set(i);
        }
    }


    /**
     * BitSet for alpha.
     * <p><blockquote><pre>
     * alpha         = lowalpha | upalpha
     * </pre></blockquote><p>
     */
    private static final BitSet ALPHA = new BitSet(256);
    // Static initializer for alpha
    static {
        for (int i = 'a'; i <= 'z'; i++) {
            ALPHA.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            ALPHA.set(i);
        }
    }

    /**
     * BitSet for alphanum (join of alpha &amp; digit).
     * <p><blockquote><pre>
     *  alphanum      = alpha | digit
     * </pre></blockquote><p>
     */
    private static final BitSet ALPHANUM = new BitSet(256);
    // Static initializer for alphanum
    static {
        ALPHANUM.or(ALPHA);
        ALPHANUM.or(DIGIT);
    }

    /**
     * BitSet for hex.
     * <p><blockquote><pre>
     * hex           = digit | "A" | "B" | "C" | "D" | "E" | "F" |
     *                         "a" | "b" | "c" | "d" | "e" | "f"
     * </pre></blockquote><p>
     */
    private static final BitSet HEX = new BitSet(256);
    // Static initializer for hex
    static {
        HEX.or(DIGIT);
        for (int i = 'a'; i <= 'f'; i++) {
            HEX.set(i);
        }
        for (int i = 'A'; i <= 'F'; i++) {
            HEX.set(i);
        }
    }

    /**
     * BitSet for escaped.
     * <p><blockquote><pre>
     * escaped       = "%" hex hex
     * </pre></blockquote><p>
     */
    private static final BitSet ESCAPED = new BitSet(256);
    // Static initializer for escaped
    static {
        ESCAPED.or(PERCENT);
        ESCAPED.or(HEX);
    }


    /**
     * BitSet for mark.
     * <p><blockquote><pre>
     * mark          = "-" | "_" | "." | "!" | "~" | "*" | "'" |
     *                 "(" | ")"
     * </pre></blockquote><p>
     */
    private static final BitSet MARK = new BitSet(256);
    // Static initializer for mark
    static {
        MARK.set('-');
        MARK.set('_');
        MARK.set('.');
        MARK.set('!');
        MARK.set('~');
        MARK.set('*');
        MARK.set('\'');
        MARK.set('(');
        MARK.set(')');
    }

    /**
     * Data characters that are allowed in a URI but do not have a reserved
     * purpose are called unreserved.
     * <p><blockquote><pre>
     * unreserved    = alphanum | mark
     * </pre></blockquote><p>
     */
    private static final BitSet UNRESERVED = new BitSet(256);
    // Static initializer for unreserved
    static {
        UNRESERVED.or(ALPHANUM);
        UNRESERVED.or(MARK);
    }

    /**
     * BitSet for reserved.
     * <p><blockquote><pre>
     * reserved      = ";" | "/" | "?" | ":" | "@" | "&amp;" | "=" | "+" |
     *                 "$" | ","
     * </pre></blockquote><p>
     */
    private static final BitSet RESERVED = new BitSet(256);
    // Static initializer for reserved
    static {
        RESERVED.set(';');
        RESERVED.set('/');
        RESERVED.set('?');
        RESERVED.set(':');
        RESERVED.set('@');
        RESERVED.set('&');
        RESERVED.set('=');
        RESERVED.set('+');
        RESERVED.set('$');
        RESERVED.set(',');
    }

    /**
     * BitSet for uric.
     * <p><blockquote><pre>
     * uric          = reserved | unreserved | escaped
     * </pre></blockquote><p>
     */
    private static final BitSet URIC = new BitSet(256);
    // Static initializer for uric
    static {
        URIC.or(RESERVED);
        URIC.or(UNRESERVED);
        URIC.or(ESCAPED);
    }

    /**
     * BitSet for fragment (alias for uric).
     * <p><blockquote><pre>
     * fragment      = *uric
     * </pre></blockquote><p>
     */
    private static final BitSet FRAGMENT = URIC;

    /**
     * BitSet for query (alias for uric).
     * <p><blockquote><pre>
     * query         = *uric
     * </pre></blockquote><p>
     */
    private static final BitSet QUERY = URIC;

    /**
     * BitSet for pchar.
     * <p><blockquote><pre>
     * pchar         = unreserved | escaped |
     *                 ":" | "@" | "&amp;" | "=" | "+" | "$" | ","
     * </pre></blockquote><p>
     */
    private static final BitSet PCHAR = new BitSet(256);
    // Static initializer for pchar
    static {
        PCHAR.or(UNRESERVED);
        PCHAR.or(ESCAPED);
        PCHAR.set(':');
        PCHAR.set('@');
        PCHAR.set('&');
        PCHAR.set('=');
        PCHAR.set('+');
        PCHAR.set('$');
        PCHAR.set(',');
    }

    /**
     * BitSet for param (alias for pchar).
     * <p><blockquote><pre>
     * param         = *pchar
     * </pre></blockquote><p>
     */
    private static final BitSet PARAM = PCHAR;

    /**
     * BitSet for segment.
     * <p><blockquote><pre>
     * segment       = *pchar *( ";" param )
     * </pre></blockquote><p>
     */
    private static final BitSet SEGMENT = new BitSet(256);
    // Static initializer for segment
    static {
        SEGMENT.or(PCHAR);
        SEGMENT.set(';');
        SEGMENT.or(PARAM);
    }


    /**
     * BitSet for path segments.
     * <p><blockquote><pre>
     * path_segments = segment *( "/" segment )
     * </pre></blockquote><p>
     */
    private static final BitSet PATH_SEGMENTS = new BitSet(256);
    // Static initializer for path_segments
    static {
        PATH_SEGMENTS.set('/');
        PATH_SEGMENTS.or(SEGMENT);
    }

    /**
     * URI absolute path.
     * <p><blockquote><pre>
     * abs_path      = "/"  path_segments
     * </pre></blockquote><p>
     */
    private static final BitSet ABS_PATH = new BitSet(256);
    // Static initializer for abs_path
    static {
        ABS_PATH.set('/');
        ABS_PATH.or(PATH_SEGMENTS);
    }

    /**
     * Bitset that combines digit and dot fo IPv$address.
     * <p><blockquote><pre>
     * IPv4address   = 1*digit "." 1*digit "." 1*digit "." 1*digit
     * </pre></blockquote><p>
     */
    private static final BitSet IPv4_ADDRESS = new BitSet(256);
    // Static initializer for IPv4address
    static {
        IPv4_ADDRESS.or(DIGIT);
        IPv4_ADDRESS.set('.');
    }


    /**
     * RFC 2373.
     * <p><blockquote><pre>
     * IPv6address = hexpart [ ":" IPv4address ]
     * </pre></blockquote><p>
     */
    private static final BitSet IPV6_ADDRESS = new BitSet(256);
    // Static initializer for IPv6address reference
    static {
        IPV6_ADDRESS.or(HEX); // hexpart
        IPV6_ADDRESS.set(':');
        IPV6_ADDRESS.or(IPv4_ADDRESS);
    }


    /**
     * RFC 2732, 2373.
     * <p><blockquote><pre>
     * IPv6reference   = "[" IPv6address "]"
     * </pre></blockquote><p>
     */
    private static final BitSet IPv6_REFERENCE = new BitSet(256);
    // Static initializer for IPv6reference
    static {
        IPv6_REFERENCE.set('[');
        IPv6_REFERENCE.or(IPV6_ADDRESS);
        IPv6_REFERENCE.set(']');
    }

    /**
     * BitSet for toplabel.
     * <p><blockquote><pre>
     * toplabel      = alpha | alpha *( alphanum | "-" ) alphanum
     * </pre></blockquote><p>
     */
    private static final BitSet TOP_LABEL = new BitSet(256);
    // Static initializer for toplabel
    static {
        TOP_LABEL.or(ALPHANUM);
        TOP_LABEL.set('-');
    }

    /**
     * BitSet for hostname.
     * <p><blockquote><pre>
     * hostname      = *( domainlabel "." ) toplabel [ "." ]
     * </pre></blockquote><p>
     */
    private static final BitSet HOSTNAME = new BitSet(256);
    // Static initializer for hostname
    static {
        HOSTNAME.or(TOP_LABEL);
        // hostname.or(domainlabel);
        HOSTNAME.set('.');
    }

    /**
     * BitSet for host.
     * <p><blockquote><pre>
     * host          = hostname | IPv4address | IPv6reference
     * </pre></blockquote><p>
     */
    private static final BitSet HOST = new BitSet(256);
    // Static initializer for host
    static {
        HOST.or(HOSTNAME);
        // host.or(IPv4address);
        HOST.or(IPv6_REFERENCE); // IPv4address
    }

    /**
     * Port, a logical alias for digit.
     */
    private static final BitSet PORT = DIGIT;

    /**
     * BitSet for hostport.
     * <p><blockquote><pre>
     * hostport      = host [ ":" port ]
     * </pre></blockquote><p>
     */
    private static final BitSet HOSTPORT = new BitSet(256);
    // Static initializer for hostport
    static {
        HOSTPORT.or(HOST);
        HOSTPORT.set(':');
        HOSTPORT.or(PORT);
    }


    /**
     * Bitset for userinfo.
     * <p><blockquote><pre>
     * userinfo      = *( unreserved | escaped |
     *                    ";" | ":" | "&amp;" | "=" | "+" | "$" | "," )
     * </pre></blockquote><p>
     */
    private static final BitSet USERINFO = new BitSet(256);
    // Static initializer for userinfo
    static {
        USERINFO.or(UNRESERVED);
        USERINFO.or(ESCAPED);
        USERINFO.set(';');
        USERINFO.set(':');
        USERINFO.set('&');
        USERINFO.set('=');
        USERINFO.set('+');
        USERINFO.set('$');
        USERINFO.set(',');
    }

    /**
     * Bitset for server.
     * <p><blockquote><pre>
     * server        = [ [ userinfo "@" ] hostport ]
     * </pre></blockquote><p>
     */
    private static final BitSet SERVER = new BitSet(256);
    // Static initializer for server
    static {
        SERVER.or(USERINFO);
        SERVER.set('@');
        SERVER.or(HOSTPORT);
    }

    /**
     * BitSet for reg_name.
     * <p><blockquote><pre>
     * reg_name      = 1*( unreserved | escaped | "$" | "," |
     *                     ";" | ":" | "@" | "&amp;" | "=" | "+" )
     * </pre></blockquote><p>
     */
    private static final BitSet REG_NAME = new BitSet(256);
    // Static initializer for reg_name
    static {
        REG_NAME.or(UNRESERVED);
        REG_NAME.or(ESCAPED);
        REG_NAME.set('$');
        REG_NAME.set(',');
        REG_NAME.set(';');
        REG_NAME.set(':');
        REG_NAME.set('@');
        REG_NAME.set('&');
        REG_NAME.set('=');
        REG_NAME.set('+');
    }

    /**
     * BitSet for authority.
     * <p><blockquote><pre>
     * authority     = server | reg_name
     * </pre></blockquote><p>
     */
    private static final BitSet AUTHORITY = new BitSet(256);
    // Static initializer for authority
    static {
        AUTHORITY.or(SERVER);
        AUTHORITY.or(REG_NAME);
    }

    /**
     * BitSet for rel_segment.
     * <p><blockquote><pre>
     * rel_segment   = 1*( unreserved | escaped |
     *                     ";" | "@" | "&amp;" | "=" | "+" | "$" | "," )
     * </pre></blockquote><p>
     */
    private static final BitSet REL_SEGMENT = new BitSet(256);
    // Static initializer for rel_segment
    static {
        REL_SEGMENT.or(UNRESERVED);
        REL_SEGMENT.or(ESCAPED);
        REL_SEGMENT.set(';');
        REL_SEGMENT.set('@');
        REL_SEGMENT.set('&');
        REL_SEGMENT.set('=');
        REL_SEGMENT.set('+');
        REL_SEGMENT.set('$');
        REL_SEGMENT.set(',');
    }

    /**
     * BitSet for rel_path.
     * <p><blockquote><pre>
     * rel_path      = rel_segment [ abs_path ]
     * </pre></blockquote><p>
     */
    private static final BitSet rel_path = new BitSet(256);
    // Static initializer for rel_path
    static {
        rel_path.or(REL_SEGMENT);
        rel_path.or(ABS_PATH);
    }

    /**
     * BitSet for net_path.
     * <p><blockquote><pre>
     * net_path      = "//" authority [ abs_path ]
     * </pre></blockquote><p>
     */
    private static final BitSet NET_PATH = new BitSet(256);
    // Static initializer for net_path
    static {
        NET_PATH.set('/');
        NET_PATH.or(AUTHORITY);
        NET_PATH.or(ABS_PATH);
    }


    /**
     * BitSet for hier_part.
     * <p><blockquote><pre>
     * hier_part     = ( net_path | abs_path ) [ "?" query ]
     * </pre></blockquote><p>
     */
    private static final BitSet HIER_PART = new BitSet(256);
    // Static initializer for hier_part
    static {
        HIER_PART.or(NET_PATH);
        HIER_PART.or(ABS_PATH);
        // hier_part.set('?'); aleady included
        HIER_PART.or(QUERY);
    }

    /**
     * URI bitset for encoding typical non-slash characters.
     * <p><blockquote><pre>
     * uric_no_slash = unreserved | escaped | ";" | "?" | ":" | "@" |
     *                 "&amp;" | "=" | "+" | "$" | ","
     * </pre></blockquote><p>
     */
    private static final BitSet URIC_NO_SLASH = new BitSet(256);
    // Static initializer for uric_no_slash
    static {
        URIC_NO_SLASH.or(UNRESERVED);
        URIC_NO_SLASH.or(ESCAPED);
        URIC_NO_SLASH.set(';');
        URIC_NO_SLASH.set('?');
        URIC_NO_SLASH.set(';');
        URIC_NO_SLASH.set('@');
        URIC_NO_SLASH.set('&');
        URIC_NO_SLASH.set('=');
        URIC_NO_SLASH.set('+');
        URIC_NO_SLASH.set('$');
        URIC_NO_SLASH.set(',');
    }

    /**
     * URI bitset that combines uric_no_slash and uric.
     * <p><blockquote><pre>
     * opaque_part   = uric_no_slash *uric
     * </pre></blockquote><p>
     */
    private static final BitSet OPAQUE_PART = new BitSet(256);
    // Static initializer for opaque_part
    static {
        // it's generous. because first character must not include a slash
        OPAQUE_PART.or(URIC_NO_SLASH);
        OPAQUE_PART.or(URIC);
    }

    /**
     * BitSet for scheme.
     * <p><blockquote><pre>
     * scheme        = alpha *( alpha | digit | "+" | "-" | "." )
     * </pre></blockquote><p>
     */
    private static final BitSet SCHEME = new BitSet(256);
    // Static initializer for scheme
    static {
        SCHEME.or(ALPHA);
        SCHEME.or(DIGIT);
        SCHEME.set('+');
        SCHEME.set('-');
        SCHEME.set('.');
    }

    /**
     * BitSet for absoluteURI.
     * <p><blockquote><pre>
     * absoluteURI   = scheme ":" ( hier_part | opaque_part )
     * </pre></blockquote><p>
     */
    private static final BitSet ABSOLUTE_URI = new BitSet(256);
    // Static initializer for absoluteURI
    static {
        ABSOLUTE_URI.or(SCHEME);
        ABSOLUTE_URI.set(':');
        ABSOLUTE_URI.or(HIER_PART);
        ABSOLUTE_URI.or(OPAQUE_PART);
    }

    /**
     * BitSet for relativeURI.
     * <p><blockquote><pre>
     * relativeURI   = ( net_path | abs_path | rel_path ) [ "?" query ]
     * </pre></blockquote><p>
     */
    private static final BitSet RELATIVE_URI = new BitSet(256);
    // Static initializer for relativeURI
    static {
        RELATIVE_URI.or(NET_PATH);
        RELATIVE_URI.or(ABS_PATH);
        RELATIVE_URI.or(rel_path);
        // relativeURI.set('?'); aleady included
        RELATIVE_URI.or(QUERY);
    }

    /**
     * BitSet for URI-reference.
     * <p><blockquote><pre>
     * URI-reference = [ absoluteURI | relativeURI ] [ "#" fragment ]
     * </pre></blockquote><p>
     */
    private static final BitSet URI_REFERENCE = new BitSet(256);

    // Static initializer for URI_reference
    static {
        URI_REFERENCE.or(ABSOLUTE_URI);
        URI_REFERENCE.or(RELATIVE_URI);
        URI_REFERENCE.set('#');
        URI_REFERENCE.or(FRAGMENT);
    }

    /**
     * BitSet of www-form-url safe characters.
     */
    private static final BitSet WWW_FORM_URL = new BitSet(256);
    // Static initializer for www_form_url
    static {
        WWW_FORM_URL.or(ALPHA);
        WWW_FORM_URL.or(DIGIT);
        // special chars
        WWW_FORM_URL.set('-');
        WWW_FORM_URL.set('_');
        WWW_FORM_URL.set('.');
        WWW_FORM_URL.set('*');
        // blank to be replaced with +
        WWW_FORM_URL.set(' ');
    }

    /**
     * BitSet of www-form-url safe characters including safe characters for an anchor.
     */
    private static final BitSet WWW_FORM_URL_ANCHOR = URI_REFERENCE;

}
