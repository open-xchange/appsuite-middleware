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

package com.openexchange.html.internal;

import gnu.inet.encoding.IDNAException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.Serializer;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.html.internal.jericho.JerichoParser;
import com.openexchange.html.internal.jericho.JerichoParser.ParsingDeniedException;
import com.openexchange.html.internal.jericho.handler.FilterJerichoHandler;
import com.openexchange.html.internal.parser.HtmlParser;
import com.openexchange.html.internal.parser.handler.HTMLFilterHandler;
import com.openexchange.html.internal.parser.handler.HTMLImageFilterHandler;
import com.openexchange.html.internal.parser.handler.HTMLURLReplacerHandler;
import com.openexchange.html.services.ServiceRegistry;
import com.openexchange.java.Charsets;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.java.Charsets;
import com.openexchange.java.UnsynchronizedByteArrayInputStream;
import com.openexchange.proxy.ImageContentTypeRestriction;
import com.openexchange.proxy.ProxyRegistration;
import com.openexchange.proxy.ProxyRegistry;

/**
 * {@link HtmlServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HtmlServiceImpl implements HtmlService {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(HtmlServiceImpl.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final String CHARSET_UTF_8 = "UTF-8";

    // private static final Pattern PAT_META_CT = Pattern.compile("<meta[^>]*?http-equiv=\"?content-type\"?[^>]*?>", Pattern.CASE_INSENSITIVE);

    private static final String TAG_E_HEAD = "</head>";

    private static final String TAG_S_HEAD = "<head>";

    /*-
     * Member stuff
     */

    private final Map<Character, String> htmlCharMap;
    private final Map<String, Character> htmlEntityMap;
    private final Tika tika;

    /**
     * Initializes a new {@link HtmlServiceImpl}.
     *
     * @param tidyConfiguration The jTidy configuration
     * @param htmlCharMap The HTML entity to string map
     * @param htmlEntityMap The string to HTML entity map
     */
    public HtmlServiceImpl(final Map<Character, String> htmlCharMap, final Map<String, Character> htmlEntityMap) {
        super();
        this.htmlCharMap = htmlCharMap;
        this.htmlEntityMap = htmlEntityMap;
        tika = new Tika();
    }

    private static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public String replaceImages(final String content, final String sessionId) {
        if (null == content) {
            return null;
        }
        try {
            final Matcher imgMatcher = IMG_PATTERN.matcher(content);
            if (imgMatcher.find()) {
                /*
                 * Check presence of ProxyRegistry
                 */
                final ProxyRegistry proxyRegistry = ProxyRegistryProvider.getInstance().getProxyRegistry();
                if (null == proxyRegistry) {
                    LOG.warn("Missing ProxyRegistry service. Replacing image URL skipped.");
                    return content;
                }
                /*
                 * Start replacing
                 */
                final StringBuilder sb = new StringBuilder(content.length());
                int lastMatch = 0;
                do {
                    sb.append(content.substring(lastMatch, imgMatcher.start()));
                    final String imgTag = imgMatcher.group();
                    replaceSrcAttribute(imgTag, sessionId, sb, proxyRegistry);
                    lastMatch = imgMatcher.end();
                } while (imgMatcher.find());
                sb.append(content.substring(lastMatch));
                return sb.toString();
            }

        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return content;
    }

    private static final Pattern SRC_PATTERN = Pattern.compile(
        "(?:src=\"([^\"]*)\")|(?:src='([^']*)')|(?:src=[^\"']([^\\s>]*))",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static String replaceSrcAttribute(final String imgTag, final String sessionId, final StringBuilder sb, final ProxyRegistry proxyRegistry) {
        final Matcher srcMatcher = SRC_PATTERN.matcher(imgTag);
        int lastMatch = 0;
        if (srcMatcher.find()) {
            /*
             * 'src' attribute found
             */
            sb.append(imgTag.substring(lastMatch, srcMatcher.start()));
            try {
                /*
                 * Extract URL
                 */
                int group = 1;
                String urlStr = srcMatcher.group(group);
                if (urlStr == null) {
                    urlStr = srcMatcher.group(++group);
                    if (urlStr == null) {
                        urlStr = srcMatcher.group(++group);
                    }
                }
                /*
                 * Check for an inline image
                 */
                if (urlStr.toLowerCase(Locale.ENGLISH).startsWith("cid", 0)) {
                    sb.append(srcMatcher.group());
                } else {
                    /*
                     * Add proxy registration
                     */
                    final URL imageUrl = new URL(urlStr);
                    final URI uri = proxyRegistry.register(new ProxyRegistration(
                        imageUrl,
                        sessionId,
                        ImageContentTypeRestriction.getInstance()));
                    /*
                     * Compose replacement
                     */
                    sb.append("src=\"").append(uri.toString()).append('"');
                }
            } catch (final MalformedURLException e) {
                LOG.warn("Invalid URL found in \"img\" tag: " + imgTag, e);
                sb.append(srcMatcher.group());
            } catch (final OXException e) {
                LOG.warn("Proxy registration failed for \"img\" tag: " + imgTag, e);
                sb.append(srcMatcher.group());
            } catch (final Exception e) {
                LOG.warn("URL replacement failed for \"img\" tag: " + imgTag, e);
                sb.append(srcMatcher.group());
            }
            lastMatch = srcMatcher.end();
        }
        sb.append(imgTag.substring(lastMatch));
        return sb.toString();
    }

    @Override
    public String formatHrefLinks(final String content) {
        try {
            final Matcher m = PATTERN_LINK_WITH_GROUP.matcher(content);
            final StringBuilder targetBuilder = new StringBuilder(content.length());
            final StringBuilder sb = new StringBuilder(256);
            int lastMatch = 0;
            while (m.find()) {
                targetBuilder.append(content.substring(lastMatch, m.start()));
                final String url = m.group(1);
                sb.setLength(0);
                if ((url == null) || (isSrcAttr(content, m.start(1)))) {
                    targetBuilder.append(checkTarget(m.group(), sb));
                } else {
                    appendLink(url, sb);
                    targetBuilder.append(sb.toString());
                }
                lastMatch = m.end();
            }
            targetBuilder.append(content.substring(lastMatch));
            return targetBuilder.toString();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        } catch (final StackOverflowError error) {
            LOG.error(StackOverflowError.class.getName(), error);
        }
        return content;
    }

    private static final String STR_IMG_SRC = "src=";

    private static boolean isSrcAttr(final String line, final int urlStart) {
        return (urlStart >= 5) && ((STR_IMG_SRC.equalsIgnoreCase(line.substring(urlStart - 5, urlStart - 1))) || (STR_IMG_SRC.equalsIgnoreCase(line.substring(
            urlStart - 4,
            urlStart))));
    }

    private static final Pattern PATTERN_TARGET = Pattern.compile("(<a[^>]*?target=\"?)([^\\s\">]+)(\"?.*</a>)", Pattern.CASE_INSENSITIVE);

    private static final String STR_BLANK = "_blank";

    private static String checkTarget(final String anchorTag, final StringBuilder sb) {
        final Matcher m = PATTERN_TARGET.matcher(anchorTag);
        if (m.matches()) {
            if (!STR_BLANK.equalsIgnoreCase(m.group(2))) {
                return sb.append(m.group(1)).append(STR_BLANK).append(m.group(3)).toString();
            }
            return anchorTag;
        }
        /*
         * No target specified
         */
        final int pos = anchorTag.indexOf('>');
        if (pos == -1) {
            return anchorTag;
        }
        return sb.append(anchorTag.substring(0, pos)).append(" target=\"").append(STR_BLANK).append('"').append(anchorTag.substring(pos)).toString();
    }

    @Override
    public String formatURLs(final String content, final String comment) {
        try {
            final Matcher m = PATTERN_URL.matcher(content);
            final StringBuilder targetBuilder = new StringBuilder(content.length());
            final StringBuilder sb = new StringBuilder(256);
            int lastMatch = 0;
            while (m.find()) {
                final int startOpeningPos = m.start();
                targetBuilder.append(content.substring(lastMatch, startOpeningPos));
                sb.setLength(0);
                appendLink(m.group(), sb);
                targetBuilder.append("<!--").append(comment).append(' ').append(sb.toString()).append("-->");
                lastMatch = m.end();
            }
            targetBuilder.append(content.substring(lastMatch));
            return targetBuilder.toString();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        } catch (final StackOverflowError error) {
            LOG.error(StackOverflowError.class.getName(), error);
        }
        return content;
    }

    private static void appendLink(final String url, final StringBuilder builder) {
        try {
            final int mlen = url.length() - 1;
            if ((mlen > 0) && (')' == url.charAt(mlen))) { // Ends with a parenthesis
                /*
                 * Keep starting parenthesis if present
                 */
                if ('(' == url.charAt(0)) { // Starts with a parenthesis
                    builder.append('(');
                    appendAnchor(url.substring(1, mlen), builder);
                } else {
                    appendAnchor(url.substring(0, mlen), builder);
                }
                /*
                 * Append closing parenthesis
                 */
                builder.append(')');
            } else if ((mlen >= 0) && ('(' == url.charAt(0))) { // Starts with a parenthesis, but does not end with a parenthesis
                /*
                 * Append opening parenthesis
                 */
                builder.append('(');
                appendAnchor(url.substring(1), builder);
            } else {
                appendAnchor(url, builder);
            }
        } catch (final Exception e) {
            /*
             * Append as-is
             */
            LOG.warn(e.getMessage(), e);
            builder.append(url);
        }
    }

    private static void appendAnchor(final String url, final StringBuilder builder) throws IDNAException {
        try {
            final String checkedUrl = checkURL(url);
            builder.append("<a href=\"");
            if (url.startsWith("www") || url.startsWith("news")) {
                builder.append("http://");
            }
            builder.append(checkedUrl).append("\" target=\"_blank\">").append(url).append("</a>");
        } catch (final MalformedURLException e) {
            // Append as-is
            builder.append(url);
        }
    }

    /**
     * Checks if specified URL needs to be converted to its ASCII form.
     *
     * @param url The URL to check
     * @return The checked URL
     * @throws MalformedURLException If URL is malformed
     * @throws IDNAException If conversion fails
     */
    public static String checkURL(final String url) throws MalformedURLException, IDNAException {
        String urlStr = url;
        /*
         * Get the host part of URL. Ensure scheme is present before creating a java.net.URL instance
         */
        final String host = new URL(
            urlStr.startsWith("www.") || urlStr.startsWith("news.") ? new StringBuilder("http://").append(urlStr).toString() : urlStr).getHost();
        if (null != host && !isAscii(host)) {
            final String encodedHost = gnu.inet.encoding.IDNA.toASCII(host);
            urlStr = Pattern.compile(Pattern.quote(host)).matcher(urlStr).replaceFirst(com.openexchange.java.Strings.quoteReplacement(encodedHost));
        }
        /*
         * Still contains any non-ascii character?
         */
        final int len = urlStr.length();
        StringBuilder tmp = null;
        int lastpos = 0;
        int i;
        for (i = 0; i < len; i++) {
            final char c = urlStr.charAt(i);
            if (c >= 128) {
                if (null == tmp) {
                    tmp = new StringBuilder(len + 16);
                }
                tmp.append(urlStr.substring(lastpos, i)).append('%').append(Integer.toHexString(c).toUpperCase(Locale.ENGLISH));
                lastpos = i + 1;
            }
        }
        /*
         * Return
         */
        if (null == tmp) {
            return urlStr;
        }
        return (lastpos < len) ? tmp.append(urlStr.substring(lastpos)).toString() : tmp.toString();
    }

    /**
     * Checks whether the specified string's characters are ASCII 7 bit
     *
     * @param s The string to check
     * @return <code>true</code> if string's characters are ASCII 7 bit; otherwise <code>false</code>
     */
    private static boolean isAscii(final String s) {
        final int length = s.length();
        boolean isAscci = true;
        for (int i = 0; isAscci && (i < length); i++) {
            isAscci = (s.charAt(i) < 128);
        }
        return isAscci;
    }

    @Override
    public String filterWhitelist(final String htmlContent) {
        final HTMLFilterHandler handler = new HTMLFilterHandler(this, htmlContent.length());
        HtmlParser.parse(htmlContent, handler);
        return handler.getHTML();
    }

    @Override
    public String filterWhitelist(final String htmlContent, final String configName) {
        String confName = configName;
        if (!confName.endsWith(".properties")) {
            confName += ".properties";
        }
        final String definition = getConfiguration().getText(confName);
        if (definition == null) {
            // Apparently, the file was not found, so we'll just fall back to the default whitelist
            return filterWhitelist(htmlContent);
        }
        final HTMLFilterHandler handler = new HTMLFilterHandler(this, htmlContent.length(), definition);
        HtmlParser.parse(htmlContent, handler);
        return handler.getHTML();
    }

    protected ConfigurationService getConfiguration() {
        return ServiceRegistry.getInstance().getService(ConfigurationService.class);
    }

    @Override
    public String filterExternalImages(final String htmlContent, final boolean[] modified) {
        final HTMLImageFilterHandler handler = new HTMLImageFilterHandler(this, htmlContent.length());
        HtmlParser.parse(htmlContent, handler);
        modified[0] |= handler.isImageURLFound();
        return handler.getHTML();
    }

    @Override
    public String sanitize(final String htmlContent, final String optConfigName, final boolean dropExternalImages, final boolean[] modified, final String cssPrefix) {
        try {
            final long st = DEBUG ? System.currentTimeMillis() : 0L;
            String html = htmlContent;
            // Perform one-shot sanitizing
            html = replaceHexEntities(html);
            html = processDownlevelRevealedConditionalComments(html);
            html = dropDoubleAccents(html);
            // CSS- and tag-wise sanitizing
            {
                // Determine the definition to use
                final String definition;
                {
                    String confName = optConfigName;
                    if (null != confName && !confName.endsWith(".properties")) {
                        confName += ".properties";
                    }
                    definition = null == confName ? null : getConfiguration().getText(confName);
                }
                // Handle HTML content
                final FilterJerichoHandler handler = null == definition ? new FilterJerichoHandler(html.length()) : new FilterJerichoHandler(html.length(), definition);
                JerichoParser.parse(html, handler.setDropExternalImages(dropExternalImages).setCssPrefix(cssPrefix));
                if (dropExternalImages && null != modified) {
                    modified[0] |= handler.isImageURLFound();
                }
                html = handler.getHTML();
            }
            // Repetitive sanitizing until no further replacement/changes performed
            final boolean[] sanitized = new boolean[] { true };
            while (sanitized[0]) {
                sanitized[0] = false;
                // Start sanitizing round
                html = SaneScriptTags.saneScriptTags(html, sanitized);
            }
            if (DEBUG) {
                final long dur = System.currentTimeMillis() - st;
                LOG.debug("\tHTMLServiceImpl.sanitize() took " + dur + "msec.");
            }
            return html;
        } catch (final ParsingDeniedException e) {
            LOG.warn("HTML content will be returned un-sanitized. Reason: "+e.getMessage(), e);
            return htmlContent;
        }
    }

    private static final Pattern PATTERN_TAG = Pattern.compile("<\\w+?[^>]*>");
    private static final Pattern PATTERN_DOUBLE_ACCENTS = Pattern.compile(Pattern.quote("\u0060\u0060")+"|"+Pattern.quote("\u00b4\u00b4"));
    private static final Pattern PATTERN_ACCENT1 = Pattern.compile(Pattern.quote("\u0060"));
    private static final Pattern PATTERN_ACCENT2 = Pattern.compile(Pattern.quote("\u00b4"));

    private static String dropDoubleAccents(final String html) {
        if (null == html || (html.indexOf('\u0060') < 0 && html.indexOf('\u00b4') < 0)) {
            return html;
        }
        final Matcher m = PATTERN_TAG.matcher(html);
        if (!m.find()) {
            /*
             * No conditional comments found
             */
            return html;
        }
        int lastMatch = 0;
        final StringBuilder sb = new StringBuilder(html.length());
        do {
            sb.append(html.substring(lastMatch, m.start()));
            final String match = m.group();
            if (!isEmpty(match)) {
                if (match.indexOf('\u0060') < 0 && match.indexOf('\u00b4') < 0) {
                    sb.append(match);
                } else {
                    sb.append(PATTERN_DOUBLE_ACCENTS.matcher(match).replaceAll(""));
                }
            }
            lastMatch = m.end();
        } while (m.find());
        sb.append(html.substring(lastMatch));
        String ret = PATTERN_ACCENT1.matcher(sb.toString()).replaceAll("&#96;");
        ret = PATTERN_ACCENT2.matcher(ret).replaceAll("&#180;");
        return ret;
    }

    private static final Pattern PATTERN_HEADING_WS = Pattern.compile("(\r?\n|^) +");

    @Override
    public String html2text(final String htmlContent, final boolean appendHref) {
//        final HTML2TextHandler handler = new HTML2TextHandler(this, htmlContent.length(), appendHref);
//        HTMLParser.parse(htmlContent, handler);
//        return handler.getText();

        try {
            String prepared = prepareSignatureStart(htmlContent);
            prepared = prepareHrTag(prepared);
            prepared = insertBlockquoteMarker(prepared);
            prepared = insertSpaceMarker(prepared);
            String text = quoteText(new Renderer(new Segment(new Source(prepared), 0, prepared.length())).setMaxLineLength(9999).setIncludeHyperlinkURLs(appendHref).toString());
            // Drop heading whitespaces
            text = PATTERN_HEADING_WS.matcher(text).replaceAll("$1");
            // ... but keep enforced ones
            text = whitespaceText(text);
            return text;
        } catch (final StackOverflowError soe) {
            // Unfortunately it may happen...
            LOG.warn("Stack-overflow during processing HTML content.", soe);
            // Retry with Tika framework
            try {
                return extractFrom(new java.io.ByteArrayInputStream(htmlContent.getBytes(Charsets.ISO_8859_1)));
            } catch (final OXException e) {
                LOG.error("Error during processing HTML content.", e);
                return "";
            }
        }
    }

    private String extractFrom(final InputStream inputStream) throws OXException {
        try {
            final Metadata metadata = new Metadata();
            metadata.set(Metadata.CONTENT_ENCODING, "ISO-8859-1");
            metadata.set(Metadata.CONTENT_TYPE, "text/html");
            return tika.parseToString(inputStream, metadata);
        } catch (final IOException e) {
            throw new OXException(e);
        } catch (final TikaException e) {
            throw new OXException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private static final Pattern PATTERN_HR = Pattern.compile("<hr[^>]*>(.*?</hr>)?", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static String prepareHrTag(final String htmlContent) {
        final Matcher m = PATTERN_HR.matcher(htmlContent);
        if (!m.find()) {
            return htmlContent;
        }
        final StringBuffer sb = new StringBuffer(htmlContent.length());
        final String repl = "<br>---------------------------------------------<br>";
        do {
            final String tail = m.group(1);
            if (null == tail || "</hr>".equals(tail)) {
                m.appendReplacement(sb, com.openexchange.java.Strings.quoteReplacement(repl));
            } else {
                m.appendReplacement(sb, com.openexchange.java.Strings.quoteReplacement(repl + tail.substring(0, tail.length() - 5)));
            }
        } while (m.find());
        m.appendTail(sb);
        return sb.toString();
    }

    private static final Pattern PATTERN_SIGNATURE_START = Pattern.compile("(?:\r?\n|^)([ \t]*)-- (\r?\n)");
    private static String prepareSignatureStart(final String htmlContent) {
        final Matcher m = PATTERN_SIGNATURE_START.matcher(htmlContent);
        if (!m.find()) {
            return htmlContent;
        }
        final StringBuffer sb = new StringBuffer(htmlContent.length());
        m.appendReplacement(sb, "$1--&#160;$2");
        m.appendTail(sb);
        return sb.toString();
    }

    private static final String SPACE_MARKER = "--?space?--";

    private static final Pattern PATTERN_SPACE_MARKER = Pattern.compile(Pattern.quote(SPACE_MARKER));

    private static String whitespaceText(final String text) {
        return PATTERN_SPACE_MARKER.matcher(text).replaceAll(" ");
    }

    private static final Pattern PATTERN_HTML_MANDATORY_SPACE = Pattern.compile("(?:&nbsp;|&#160;)", Pattern.CASE_INSENSITIVE);

    private static String insertSpaceMarker(final String html) {
        return PATTERN_HTML_MANDATORY_SPACE.matcher(html).replaceAll(SPACE_MARKER);
    }

    private static final String CRLF = "\r\n";

    private static final String SPECIAL = "=?";

    private static final String END = "--";

    private static final String BLOCKQUOTE_MARKER = SPECIAL+UUID.randomUUID().toString();

    private static final String BLOCKQUOTE_MARKER_END = BLOCKQUOTE_MARKER + END;

    private static final Pattern PATTERN_MARKER = Pattern.compile(Pattern.quote(BLOCKQUOTE_MARKER) + "(?:" + Pattern.quote(END) + ")?");

    private static String quoteText(final String text) {
        if (text.indexOf(SPECIAL) < 0) {
            return text;
        }
        final String marker = BLOCKQUOTE_MARKER;
        final int len = marker.length();
        final String[] lines = text.split("\r?\n", 0);
        final StringBuilder sb = new StringBuilder(text.length());
        int quote = 0;
        String prefix = "";
        for (String line : lines) {
            final int pos = line.indexOf(marker);
            if (pos >= 0) {
                if (pos > 0) {
                    sb.append(prefix).append(line.substring(0, pos));
                }
                final int endPos = len + pos;
                if (line.length() >= endPos && line.startsWith(END, endPos)) { // Marker for blockquote end
                    quote--;
                    line = line.substring(endPos + 2).trim();
                } else {
                    quote++;
                    line = line.substring(endPos).trim();
                }
                prefix = getPrefixFor(quote);
                if (isEmpty(line)) {
                    continue;
                }
            }
            sb.append(prefix).append(line).append(CRLF);
        }
        final String retval = sb.toString();
        if (retval.indexOf(SPECIAL) < 0) {
            return retval;
        }
        return PATTERN_MARKER.matcher(retval).replaceAll("");
    }

    private static String getPrefixFor(final int quote) {
        if (quote <= 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < quote; i++) {
            sb.append("> ");
        }
        return sb.toString();
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private static final Pattern PATTERN_BLOCKQUOTE_START = Pattern.compile("(<blockquote.*?>)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_BLOCKQUOTE_END = Pattern.compile("(</blockquote>)", Pattern.CASE_INSENSITIVE);

    private static String insertBlockquoteMarker(final String html) {
        return PATTERN_BLOCKQUOTE_END.matcher(PATTERN_BLOCKQUOTE_START.matcher(html).replaceAll("$1"+BLOCKQUOTE_MARKER)).replaceAll("$1"+BLOCKQUOTE_MARKER_END);
    }

    private static final String HTML_BR = "<br />";

    private static final Pattern PATTERN_CRLF = Pattern.compile("\r?\n");

    @Override
    public String htmlFormat(final String plainText, final boolean withQuote, final String commentId) {
        return PATTERN_CRLF.matcher(escape(plainText, withQuote, commentId)).replaceAll(HTML_BR);
    }

    @Override
    public String htmlFormat(final String plainText, final boolean withQuote) {
        return PATTERN_CRLF.matcher(escape(plainText, withQuote, null)).replaceAll(HTML_BR);
    }

    private String escape(final String s, final boolean withQuote, final String commentId) {
        final int len = s.length();
        final StringBuilder sb = new StringBuilder(len);
        if (null == commentId) {
            escapePlain(s, withQuote, sb);
            return sb.toString();
        }
        /*
         * Specify pattern & matcher
         */
        final Pattern p = Pattern.compile(
            sb.append(Pattern.quote("<!--" + commentId + ' ')).append("(.+?)").append(Pattern.quote("-->")).toString(),
            Pattern.DOTALL);
        sb.setLength(0);
        final Matcher m = p.matcher(s);
        if (!m.find()) {
            escapePlain(s, withQuote, sb);
            return sb.toString();
        }
        int lastMatch = 0;
        do {
            escapePlain(s.substring(lastMatch, m.start()), withQuote, sb);
            sb.append(m.group(1));
            lastMatch = m.end();
        } while (m.find());
        escapePlain(s.substring(lastMatch), withQuote, sb);
        return sb.toString();
    }

    private void escapePlain(final String s, final boolean withQuote, final StringBuilder sb) {
        final int length = s.length();
        final Map<Character, String> htmlChar2EntityMap = htmlCharMap;
        if (withQuote) {
            for (int i = 0; i < length; i++) {
                final char c = s.charAt(i);
                final String entity = htmlChar2EntityMap.get(Character.valueOf(c));
                if (entity == null) {
                    sb.append(c);
                } else {
                    sb.append('&').append(entity).append(';');
                }
            }
        } else {
            for (int i = 0; i < length; i++) {
                final char c = s.charAt(i);
                if ('"' == c) {
                    sb.append(c);
                } else {
                    final String entity = htmlChar2EntityMap.get(Character.valueOf(c));
                    if (entity == null) {
                        sb.append(c);
                    } else {
                        sb.append('&').append(entity).append(';');
                    }
                }
            }
        }
    }

    /**
     * Formats plain text to HTML by escaping HTML special characters e.g. <code>&quot;&lt;&quot;</code> is converted to
     * <code>&quot;&amp;lt;&quot;</code>.
     * <p>
     * This is just a convenience method which invokes <code>{@link #htmlFormat(String, boolean)}</code> with latter parameter set to
     * <code>true</code>.
     *
     * @param plainText The plain text
     * @return The properly escaped HTML content
     * @see #htmlFormat(String, boolean)
     */
    @Override
    public String htmlFormat(final String plainText) {
        return htmlFormat(plainText, true);
    }

    private static final String REGEX_URL_SOLE = "\\b(?:https?://|ftp://|mailto:|news\\.|www\\.)[-\\p{L}\\p{Sc}0-9+&@#/%?=~_()|!:,.;]*[-\\p{L}\\p{Sc}0-9+&@#/%=~_()|]";

    /**
     * The regular expression to match URLs inside text:<br>
     * <code>\b(?:https?://|ftp://|mailto:|news\\.|www\.)[-\p{L}\p{Sc}0-9+&@#/%?=~_()|!:,.;]*[-\p{L}\p{Sc}0-9+&@#/%=~_()|]</code>
     * <p>
     * Parentheses, if present, are allowed in the URL -- The leading one is <b>not</b> absorbed.
     */
    public static final Pattern PATTERN_URL_SOLE = Pattern.compile(REGEX_URL_SOLE);

    private static final String REGEX_URL = "\\(?" + REGEX_URL_SOLE;

    /**
     * The regular expression to match URLs inside text:<br>
     * <code>\(?\b(?:https?://|ftp://|mailto:|news\\.|www\.)[-\p{L}\p{Sc}0-9+&@#/%?=~_()|!:,.;]*[-\p{L}\p{Sc}0-9+&@#/%=~_()|]</code>
     * <p>
     * Parentheses, if present, are allowed in the URL -- The leading one is absorbed, too.
     *
     * <pre>
     * String s = matcher.group();
     * int mlen = s.length() - 1;
     * if (mlen &gt; 0 &amp;&amp; '(' == s.charAt(0) &amp;&amp; ')' == s.charAt(mlen)) {
     *     s = s.substring(1, mlen);
     * }
     * </pre>
     */
    public static final Pattern PATTERN_URL = Pattern.compile(REGEX_URL);

    public Pattern getURLPattern() {
        return PATTERN_URL;
    }

    private static final String REGEX_ANCHOR = "<a\\s+href[^>]+>.*?</a>";

    private static final Pattern PATTERN_LINK = Pattern.compile(REGEX_ANCHOR + '|' + REGEX_URL);

    public Pattern getLinkPattern() {
        return PATTERN_LINK;
    }

    private static final Pattern PATTERN_LINK_WITH_GROUP = Pattern.compile(REGEX_ANCHOR + "|(" + REGEX_URL + ')');

    public Pattern getLinkWithGroupPattern() {
        return PATTERN_LINK_WITH_GROUP;
    }

    /**
     * Maps specified HTML entity - e.g. <code>&amp;uuml;</code> - to corresponding unicode character.
     *
     * @param entity The HTML entity
     * @return The corresponding unicode character or <code>null</code>
     */
    @Override
    public Character getHTMLEntity(final String entity) {
        if (null == entity) {
            return null;
        }
        String key = entity;
        if (key.charAt(0) == '&') {
            key = key.substring(1);
        }
        {
            final int lastPos = key.length() - 1;
            if (key.charAt(lastPos) == ';') {
                key = key.substring(0, lastPos);
            }
        }
        return htmlEntityMap.get(key);
    }

    private static final Pattern PAT_ENTITIES = Pattern.compile("&(?:#([0-9]+)|#x([0-9a-fA-F]+)|([a-zA-Z]+));");

    @Override
    public String replaceHTMLEntities(final String content) {
        final Matcher m = PAT_ENTITIES.matcher(content);
        final MatcherReplacer mr = new MatcherReplacer(m, content);
        final StringBuilder sb = new StringBuilder(content.length());
        while (m.find()) {
            /*
             * Try decimal syntax; e.g. &#39; (single-quote)
             */
            int numEntity = numOf(m.group(1), 10);
            if (numEntity >= 0) {
                /*
                 * Detected decimal value
                 */
                mr.appendLiteralReplacement(sb, String.valueOf((char) numEntity));
            } else {
                /*
                 * Try hexadecimal syntax; e.g. &#xFC;
                 */
                numEntity = numOf(m.group(2), 16);
                if (numEntity >= 0) {
                    /*
                     * Detected hexadecimal value
                     */
                    mr.appendLiteralReplacement(sb, String.valueOf((char) numEntity));
                } else {
                    /*
                     * No numeric entity syntax, assume a non-numeric entity like &quot; or &nbsp;
                     */
                    final Character entity = getHTMLEntity(m.group(3));
                    if (null != entity) {
                        mr.appendLiteralReplacement(sb, entity.toString());
                    }
                }
            }
        }
        mr.appendTail(sb);
        return sb.toString();
    }

    private static int numOf(final String possibleNum, final int radix) {
        if (null == possibleNum) {
            return -1;
        }
        try {
            return Integer.parseInt(possibleNum, radix);
        } catch (final NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public String prettyPrint(final String htmlContent) {
        if (null == htmlContent) {
            return htmlContent;
        }
        try {
            /*
             * Clean...
             */
            final TagNode htmlNode = HTML_CLEANER.clean(htmlContent);
            /*
             * Serialize
             */
            final AllocatingStringWriter writer = new AllocatingStringWriter(htmlContent.length());
            SERIALIZER.write(htmlNode, writer, "UTF-8");
            return writer.toString();
        } catch (final UnsupportedEncodingException e) {
            // Cannot occur
            LOG.error("Unsupported encoding: " + e.getMessage(), e);
            return htmlContent;
        } catch (final IOException e) {
            // Cannot occur
            LOG.error("I/O error: " + e.getMessage(), e);
            return htmlContent;
        } catch (final RuntimeException rte) {
            /*
             * HtmlCleaner failed horribly...
             */
            LOG.warn("HtmlCleaner library failed to pretty-print HTML content with: " + rte.getMessage(), rte);
            return htmlContent;
        }
    }

    private static final Pattern PATTERN_BODY_START = Pattern.compile(Pattern.quote("<body"), Pattern.CASE_INSENSITIVE);

    @Override
    public String checkBaseTag(final String htmlContent, final boolean externalImagesAllowed) {
        if (null == htmlContent) {
            return htmlContent;
        }
        /*
         * The <base> tag must be between the document's <head> tags. Also, there must be no more than one base element per document.
         */
        final Matcher m1 = PATTERN_BODY_START.matcher(htmlContent);
        return checkBaseTag(htmlContent, externalImagesAllowed, m1.find() ? m1.start() : htmlContent.length());
    }

    private static final Pattern PATTERN_BASE_TAG = Pattern.compile(
        "<base[^>]*href=\\s*(?:\"|')(\\S*?)(?:\"|')[^>]*/?>",
        Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final Pattern BACKGROUND_PATTERN = Pattern.compile(
        "(<[a-zA-Z]+[^>]*?)(?:(?:background=([^\\s>]*))|(?:background=\"([^\"]*)\"))([^>]*/?>)",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static String checkBaseTag(final String htmlContent, final boolean externalImagesAllowed, final int end) {
        Matcher m = PATTERN_BASE_TAG.matcher(htmlContent);
        if (!m.find() || m.end() >= end) {
            return htmlContent;
        }
        /*
         * Find bases
         */
        String base = m.group(1);
        if (base.endsWith("/")) {
            base = base.substring(0, base.length()-1);
        }
        /*
         * Convert to absolute URIs
         */
        String html = htmlContent.substring(0, m.start()) + htmlContent.substring(m.end());
        m = IMG_PATTERN.matcher(html);
        MatcherReplacer mr = new MatcherReplacer(m, html);
        final StringBuilder sb = new StringBuilder(html.length());
        if (m.find()) {
            /*
             * Replace images
             */
            do {
                final String imgTag = m.group();
                final int pos = imgTag.indexOf("src=");
                final int epos;
                if (pos >= 0) {
                    String href;
                    final char c = imgTag.charAt(pos+4);
                    if ('"' == c) {
                        epos = imgTag.indexOf('"', pos+5);
                        href = imgTag.substring(pos+5, epos);
                    } else if ('\'' == c) {
                        epos = imgTag.indexOf('\'', pos+5);
                        href = imgTag.substring(pos+5, epos);
                    } else {
                        epos = imgTag.indexOf('>', pos+4);
                        href = imgTag.substring(pos+4, epos);
                    }
                    if (!href.startsWith("cid") && !href.startsWith("http")) {
                        if (href.charAt(0) != '/') {
                            href = '/' + href;
                        }
                        final String replacement = imgTag.substring(0, pos) + "src=\"" + base + href + "\"" + imgTag.substring(epos);
                        mr.appendLiteralReplacement(sb, replacement);
                    }
                }
            } while (m.find());
        }
        mr.appendTail(sb);
        html = sb.toString();
        sb.setLength(0);
        m = BACKGROUND_PATTERN.matcher(html);
        mr = new MatcherReplacer(m, html);
        if (m.find()) {
            /*
             * Replace images
             */
            do {
                final String backgroundTag = m.group();
                /*
                 * Extract href
                 */
                int pos;
                int epos;
                String href = m.group(2);
                if (href == null) {
                    href = m.group(3);
                    pos = m.start(3);
                    epos = m.end(3);
                } else {
                    pos = m.start(2);
                    epos = m.end(2);
                }
                if (!href.startsWith("cid") && !href.startsWith("http")) {
                    if (href.charAt(0) != '/') {
                        href = '/' + href;
                    }
                    final String replacement = backgroundTag.substring(0, pos) + base + href + backgroundTag.substring(epos);
                    mr.appendLiteralReplacement(sb, replacement);
                }
            } while (m.find());
        }
        mr.appendTail(sb);
        html = sb.toString();
        return html;
    }

    @Override
    public String dropScriptTagsInHeader(final String htmlContent) {
        if (null == htmlContent || htmlContent.indexOf("<script") < 0) {
            return htmlContent;
        }
        final Matcher m1 = PATTERN_BODY_START.matcher(htmlContent);
        return dropScriptTagsInHeader(htmlContent, m1.find() ? m1.start() : htmlContent.length());
    }

    private static final Pattern PATTERN_SCRIPT_TAG = Pattern.compile(
        "<script[^>]*>" + ".*?" + "</script>",
        Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static String dropScriptTagsInHeader(final String htmlContent, final int end) {
        final Matcher m = PATTERN_SCRIPT_TAG.matcher(htmlContent);
        if (!m.find() || m.end() >= end) {
            return htmlContent;
        }
        final StringBuilder sb = new StringBuilder(htmlContent.length());
        final MatcherReplacer mr = new MatcherReplacer(m, htmlContent);
        do {
            mr.appendLiteralReplacement(sb, "");
        } while (m.find() && m.end() < end);
        mr.appendTail(sb);
        return sb.toString();
    }

    private static final Pattern PATTERN_STYLESHEET_FILE = Pattern.compile(
        "<link.*?(type=['\"]text/css['\"].*?href=['\"](.*?)['\"]|href=['\"](.*?)['\"].*?type=['\"]text/css['\"]).*?/>",
        Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN_STYLESHEET = Pattern.compile("<style.*?>(.*?)</style>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    @Override
    public String getCSSFromHTMLHeader(final String htmlContent) {
        final StringBuilder css = new StringBuilder(htmlContent.length() >> 1);
        final Matcher mStyle = PATTERN_STYLESHEET.matcher(htmlContent);
        while (mStyle.find()) {
            css.append(mStyle.group(1));
        }
        final Matcher mStyleFile = PATTERN_STYLESHEET_FILE.matcher(htmlContent);
        while (mStyleFile.find()) {
            final String cssFile = mStyleFile.group(2);
            final HttpClient client = new HttpClient();
            final GetMethod get = new GetMethod(cssFile);
            try {
                final int statusCode = client.executeMethod(get);
                if (statusCode != HttpStatus.SC_OK) {
                    // throw new OXException(); //TODO: set exceptioncode
                } else {
                    final byte[] responseBody = get.getResponseBody();
                    try {
                        final String charSet = get.getResponseCharSet();
                        css.append(new String(responseBody, null == charSet ? Charsets.ISO_8859_1 : Charsets.forName(charSet)));
                    } catch (final UnsupportedCharsetException e) {
                        css.append(new String(responseBody, Charsets.ISO_8859_1));
                    }
                }
            } catch (final HttpException e) {
                // throw new OXException(); //TODO: set exceptioncode
            } catch (final IOException e) {
                // throw new OXException(); //TODO: set exceptioncode
            } finally {
                get.releaseConnection();
            }
        }
        return css.toString();
    }

    @Override
    public String getConformHTML(final String htmlContent, final String charset) {
        return getConformHTML(htmlContent, charset, true);
    }

    @Override
    public String getConformHTML(final String htmlContent, final String charset, final boolean replaceUrls) {
        if (null == htmlContent || 0 == htmlContent.length()) {
            /*
             * Nothing to do...
             */
            return htmlContent;
        }
        /*
         * Validate
         */
        String html = validate(htmlContent);
        /*
         * Check for meta tag in validated HTML content which indicates documents content type. Add if missing.
         */
        final int headTagLen = TAG_S_HEAD.length();
        final int start = html.indexOf(TAG_S_HEAD) + headTagLen;
        if (start >= headTagLen) {
            final int end = html.indexOf(TAG_E_HEAD);
            // final Matcher m = PAT_META_CT.matcher(html.substring(start, end));
            if (!occursWithin(html, start, end, true, "http-equiv=\"content-type\"", "http-equiv=content-type")) {
                final StringBuilder sb = new StringBuilder(html);
                final String cs;
                if (null == charset) {
                    if (LOG.isDebugEnabled()) {
                        LOG.warn("Missing charset. Using fallback \"UTF-8\" instead.");
                    }
                    cs = CHARSET_UTF_8;
                } else {
                    cs = charset;
                }
                /*-
                 * In reverse order:
                 *
                 * "\r\n    <meta content=\"text/html; charset=" + <charset> + "\" http-equiv=\"Content-Type\" />\r\n "
                 *
                 */
                sb.insert(start, "\" http-equiv=\"Content-Type\" />\r\n ");
                sb.insert(start, cs);
                sb.insert(start, "\r\n    <meta content=\"text/html; charset=");
                html = sb.toString();
            }
        }
        html = processDownlevelRevealedConditionalComments(html);
        // html = removeXHTMLCData(html);
        /*
         * Check URLs
         */
        if (!replaceUrls) {
            return html;
        }
        final HTMLURLReplacerHandler handler = new HTMLURLReplacerHandler(this, html.length());
        HtmlParser.parse(html, handler);
        return handler.getHTML();
    }

    private static boolean occursWithin(final String str, final int start, final int end, final boolean ignorecase, final String... searchStrings) {
        final String source = ignorecase ? str.toLowerCase(Locale.US) : str;
        int index;
        for (final String searchString : searchStrings) {
            final String searchMe = ignorecase ? searchString.toLowerCase(Locale.US) : searchString;
            if (((index = source.indexOf(searchMe, start)) >= start) && ((index + searchMe.length()) < end)) {
                return true;
            }
        }
        return false;
    }

    private static final Pattern PATTERN_XHTML_CDATA;

    private static final Pattern PATTERN_UNQUOTE1;

    private static final Pattern PATTERN_UNQUOTE2;

    private static final Pattern PATTERN_XHTML_COMMENT;

    static {
        final String group1 = RegexUtility.group("<style[^>]*type=\"text/(?:css|javascript)\"[^>]*>\\s*", true);

        final String ignore1 = RegexUtility.concat(RegexUtility.quote("/*<![CDATA[*/"), "\\s*");

        final String commentStart = RegexUtility.group(RegexUtility.OR(RegexUtility.quote("<!--"), RegexUtility.quote("&lt;!--")), false);

        final String commentEnd = RegexUtility.concat(
            RegexUtility.group(RegexUtility.OR(RegexUtility.quote("-->"), RegexUtility.quote("--&gt;")), false),
            "\\s*");

        final String group2 = RegexUtility.group(RegexUtility.concat(commentStart, ".*?", commentEnd), true);

        final String ignore2 = RegexUtility.concat(RegexUtility.quote("/*]]>*/"), "\\s*");

        final String group3 = RegexUtility.group(RegexUtility.quote("</style>"), true);

        final String regex = RegexUtility.concat(group1, ignore1, group2, ignore2, group3);

        PATTERN_XHTML_CDATA = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        final String commentEnd2 = RegexUtility.group(RegexUtility.OR(RegexUtility.quote("-->"), RegexUtility.quote("--&gt;")), false);

        PATTERN_XHTML_COMMENT = Pattern.compile(RegexUtility.concat(commentStart, ".*?", commentEnd2), Pattern.DOTALL);

        PATTERN_UNQUOTE1 = Pattern.compile(RegexUtility.quote("&lt;!--"), Pattern.CASE_INSENSITIVE);

        PATTERN_UNQUOTE2 = Pattern.compile(RegexUtility.quote("--&gt;"), Pattern.CASE_INSENSITIVE);
    }

    /**
     * Removes unnecessary CDATA from CSS or JavaScript <code>style</code> elements:
     *
     * <pre>
     * &lt;style type=&quot;text/css&quot;&gt;
     * /*&lt;![CDATA[&#42;/
     * &lt;!--
     *  /* Some Definitions &#42;/
     * --&gt;
     * /*]]&gt;&#42;/
     * &lt;/style&gt;
     * </pre>
     *
     * is turned to
     *
     * <pre>
     * &lt;style type=&quot;text/css&quot;&gt;
     * &lt;!--
     *  /* Some Definitions &#42;/
     * --&gt;
     * &lt;/style&gt;
     * </pre>
     *
     * @param htmlContent The (X)HTML content possibly containing CDATA in CSS or JavaScript <code>style</code> elements
     * @return The (X)HTML content with CDATA removed
     */
    private static String removeXHTMLCData(final String htmlContent) {
        final Matcher m = PATTERN_XHTML_CDATA.matcher(htmlContent);
        if (m.find()) {
            final MatcherReplacer mr = new MatcherReplacer(m, htmlContent);
            final StringBuilder sb = new StringBuilder(htmlContent.length());
            final String endingComment = "-->";
            StringBuilder tmp = null;
            do {
                // Un-quote
                final String match = com.openexchange.java.Strings.quoteReplacement(PATTERN_UNQUOTE2.matcher(
                    PATTERN_UNQUOTE1.matcher(m.group(2)).replaceAll("<!--")).replaceAll("-->"));
                // Check for additional HTML comments
                if (PATTERN_XHTML_COMMENT.matcher(m.group(2)).replaceAll("").indexOf(endingComment) == -1) {
                    // No additional HTML comments
                    if (null == tmp) {
                        tmp = new StringBuilder(match.length() + 16);
                    } else {
                        tmp.setLength(0);
                    }
                    mr.appendReplacement(sb, tmp.append("$1").append(match).append("$3").toString());
                } else {
                    // Additional HTML comments
                    if (null == tmp) {
                        tmp = new StringBuilder(match.length() + 16);
                    } else {
                        tmp.setLength(0);
                    }
                    mr.appendReplacement(sb, tmp.append("$1<!--\n").append(match).append("$3").toString());
                }
            } while (m.find());
            mr.appendTail(sb);
            return sb.toString();
        }
        return htmlContent;
    }

    private static final Pattern PATTERN_CC = Pattern.compile("(<!(?:--)?\\[if)([^\\]]+\\](?:--!?)?>)(.*?)((?:<!\\[endif\\])?(?:--)?>)", Pattern.DOTALL);
    private static final Pattern PATTERN_CC2 = Pattern.compile("(<!(?:--)?\\[if)([^\\]]+\\](?:--!?)?>)(.*?)(<!\\[endif\\](?:--)?>)", Pattern.DOTALL);

    private static final String CC_START_IF = "<!-- [if";

    private static final String CC_END_IF = " -->";

    private static final String CC_ENDIF = "<!-- <![endif] -->";

    /**
     * Processes detected downlevel-revealed <a href="http://en.wikipedia.org/wiki/Conditional_comment">conditional comments</a> through
     * adding dashes before and after each <code>if</code> statement tag to complete them as a valid HTML comment and leaves center code
     * open to rendering on non-IE browsers:
     *
     * <pre>
     * &lt;![if !IE]&gt;
     * &lt;link rel=&quot;stylesheet&quot; type=&quot;text/css&quot; href=&quot;non-ie.css&quot;&gt;
     * &lt;![endif]&gt;
     * </pre>
     *
     * is turned to
     *
     * <pre>
     * &lt;!--[if !IE]&gt;--&gt;
     * &lt;link rel=&quot;stylesheet&quot; type=&quot;text/css&quot; href=&quot;non-ie.css&quot;&gt;
     * &lt;!--&lt;![endif]--&gt;
     * </pre>
     *
     * @param htmlContent The HTML content possibly containing downlevel-revealed conditional comments
     * @return The HTML content whose downlevel-revealed conditional comments contain valid HTML for non-IE browsers
     */
    private static String processDownlevelRevealedConditionalComments(final String htmlContent) {
        final String ret = processDownlevelRevealedConditionalComments0(htmlContent, PATTERN_CC2);
        return processDownlevelRevealedConditionalComments0(ret, PATTERN_CC);
    }

    private static String processDownlevelRevealedConditionalComments0(final String htmlContent, final Pattern p) {
        final Matcher m = p.matcher(htmlContent);
        if (!m.find()) {
            /*
             * No conditional comments found
             */
            return htmlContent;
        }
        int lastMatch = 0;
        final StringBuilder sb = new StringBuilder(htmlContent.length() + 128);
        do {
            sb.append(htmlContent.substring(lastMatch, m.start()));
            final String condition = m.group(2);
            if (isValidCondition(condition)) {
                sb.append(CC_START_IF).append(condition);
                final String wrappedContent = m.group(3);
                if (!wrappedContent.startsWith("-->", 0)) {
                    sb.append(CC_END_IF);
                }
                sb.append(wrappedContent);
                if (wrappedContent.endsWith("<!--")) {
                    sb.append(m.group(4));
                } else {
                    sb.append(CC_ENDIF);
                }
            }
            lastMatch = m.end();
        } while (m.find());
        sb.append(htmlContent.substring(lastMatch));
        return sb.toString();
    }

    private static final Pattern PAT_VALID_COND = Pattern.compile("[a-zA-Z_0-9 -!]+");

    private static boolean isValidCondition(final String condition) {
        if (isEmpty(condition)) {
            return false;
        }
        return PAT_VALID_COND.matcher(condition.substring(0, condition.indexOf(']'))).matches();
    }

    /**
     * Validates specified HTML content with <a href="http://tidy.sourceforge.net/">tidy html</a> library and falls back using <a
     * href="http://htmlcleaner.sourceforge.net/">HtmlCleaner</a> if any error occurs.
     *
     * @param htmlContent The HTML content
     * @return The validated HTML content
     */
    private static String validate(final String htmlContent) {
        return validateWithHtmlCleaner(replaceHexEntities(htmlContent));
    }

    private static final Pattern PAT_HEX_ENTITIES = Pattern.compile("&#x([0-9a-fA-F]+);", Pattern.CASE_INSENSITIVE);

    private static String replaceHexEntities(final String validated) {
        final Matcher m = PAT_HEX_ENTITIES.matcher(validated);
        if (!m.find()) {
            return validated;
        }
        final MatcherReplacer mr = new MatcherReplacer(m, validated);
        final StringBuilder builder = new StringBuilder(validated.length());
        final StringBuilder tmp = new StringBuilder(8).append("&#");
        do {
            try {
                tmp.setLength(2);
                tmp.append(Integer.parseInt(m.group(1), 16)).append(';');
                mr.appendLiteralReplacement(builder, tmp.toString());
            } catch (final NumberFormatException e) {
                tmp.setLength(0);
                tmp.append("&amp;#x").append(m.group(1)).append("&#59;");
                mr.appendLiteralReplacement(builder, tmp.toString());
                tmp.setLength(0);
                tmp.append("&#");
            }
        } while (m.find());
        mr.appendTail(builder);
        return builder.toString();
    }

    private static final Pattern PAT_HEX_NBSP = Pattern.compile(Pattern.quote("&#160;"));

    private static String replaceHexNbsp(final String validated) {
        return PAT_HEX_NBSP.matcher(validated).replaceAll("&nbsp;");
    }

    /**
     * The white-list of permitted HTML elements for <a href="http://jsoup.org/">jsoup</a> library.
     */
    // private static final Whitelist WHITELIST = Whitelist.relaxed();

    /**
     * Pre-process specified HTML content with <a href="http://jsoup.org/">jsoup</a> library.
     *
     * @param htmlContent The HTML content
     * @return The safe HTML content according to JSoup processing
     */
    private static String preprocessWithJSoup(final String htmlContent) {
        return Jsoup.parse(htmlContent).toString();
    }

    /**
     * The {@link HtmlCleaner} constant which is safe being used by multiple threads as of <a
     * href="http://htmlcleaner.sourceforge.net/javause.php#example2">this example</a>.
     */
    private static final HtmlCleaner HTML_CLEANER;

    /**
     * The {@link Serializer} constant which is safe being used by multiple threads as of <a
     * href="http://htmlcleaner.sourceforge.net/javause.php#example2">this example</a>.
     */
    private static final Serializer SERIALIZER;

    static {
        final CleanerProperties props = new CleanerProperties();
        props.setOmitDoctypeDeclaration(true);
        props.setOmitXmlDeclaration(true);
        props.setPruneTags("script");
        props.setTransSpecialEntitiesToNCR(true);
        props.setTransResCharsToNCR(true);
        props.setRecognizeUnicodeChars(false);
        props.setUseEmptyElementTags(false);
        props.setIgnoreQuestAndExclam(false);
        props.setUseCdataForScriptAndStyle(false);
        props.setIgnoreQuestAndExclam(true);
        HTML_CLEANER = new HtmlCleaner(props);
        SERIALIZER = new SimpleHtmlSerializer(props); // (props, " ");
    }

    private static final String DOCTYPE_DECL = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n\r\n";

    private static String validateWithHtmlCleaner(final String htmlContent) {
        try {
            /*-
             * http://stackoverflow.com/questions/238036/java-html-parsing
             *
             * Clean...
             */
            final TagNode htmlNode = HTML_CLEANER.clean(preprocessWithJSoup(htmlContent));
            /*
             * Check for presence of HTML namespace
             */
            if (!htmlNode.hasAttribute("xmlns")) {
                htmlNode.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");
            }
            /*
             * Serialize
             */
            final AllocatingStringWriter writer = new AllocatingStringWriter(htmlContent.length());
            SERIALIZER.write(htmlNode, writer, "UTF-8");
            final com.openexchange.java.StringAllocator builder = writer.getAllocator();
            /*
             * Insert DOCTYPE if absent
             */
            if (builder.indexOf("<!DOCTYPE") < 0) {
                builder.insert(0, DOCTYPE_DECL);
            }
            return builder.toString();
        } catch (final UnsupportedEncodingException e) {
            // Cannot occur
            LOG.error("HtmlCleaner library failed to pretty-print HTML content with an unsupported encoding: " + e.getMessage(), e);
            return htmlContent;
        } catch (final IOException e) {
            // Cannot occur
            LOG.error("HtmlCleaner library failed to pretty-print HTML content with I/O error: " + e.getMessage(), e);
            return htmlContent;
        } catch (final RuntimeException rte) {
            /*
             * HtmlCleaner failed horribly...
             */
            LOG.warn("HtmlCleaner library failed to pretty-print HTML content with: " + rte.getMessage(), rte);
            return htmlContent;
        }
    }

}
