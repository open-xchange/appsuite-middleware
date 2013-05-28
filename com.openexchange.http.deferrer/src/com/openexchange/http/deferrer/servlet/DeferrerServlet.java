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

package com.openexchange.http.deferrer.servlet;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.logging.Log;
import com.openexchange.http.deferrer.CustomRedirectURLDetermination;
import com.openexchange.java.Charsets;

/**
 * {@link DeferrerServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DeferrerServlet extends HttpServlet {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DeferrerServlet.class);

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
            // Anchor characters
            bitSet.set('/');
            bitSet.set('#');
            bitSet.set('%');
            bitSet.set('?');
            bitSet.set('&');
            WWW_FORM_URL_ANCHOR = bitSet;
        }
    }

    private static final Pattern PATTERN_CRLF = Pattern.compile("\r?\n|(?:%0[aA])?%0[dD]");
    private static final Pattern PATTERN_DSLASH = Pattern.compile("(?:/|%2[fF]){2}");

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     */
    private String encodeUrl(final String s, final boolean forAnchor) {
        if (isEmpty(s)) {
            return s;
        }
        try {
            final String ascii;
            if (forAnchor) {
                // Prepare for being used as anchor/link
                ascii = Charsets.toAsciiString(URLCodec.encodeUrl(WWW_FORM_URL_ANCHOR, s.getBytes(Charsets.ISO_8859_1)));
            } else {
                ascii = Charsets.toAsciiString(URLCodec.encodeUrl(WWW_FORM_URL, s.getBytes(Charsets.ISO_8859_1)));
            }
            // Strip possible "\r?\n" and/or "%0A?%0D"
            String retval = PATTERN_CRLF.matcher(ascii).replaceAll("");
            // Check for a relative URI
            try {
                final java.net.URI uri = new java.net.URI(retval);
                if (uri.isAbsolute() || null != uri.getScheme() || null != uri.getHost()) {
                    throw new IllegalArgumentException("Illegal Location value: " + s);
                }
            } catch (final URISyntaxException e) {
                throw new IllegalArgumentException("Illegal Location value: " + s, e);
            }
            // Replace double slashes with single one
            {
                Matcher matcher = PATTERN_DSLASH.matcher(retval);
                while (matcher.find()) {
                    retval = matcher.replaceAll("/");
                    matcher = PATTERN_DSLASH.matcher(retval);
                }
            }
            return retval;
        } catch (final IllegalArgumentException e) {
            throw e;
        } catch (final RuntimeException e) {
            LOG.error("A runtime error occurred.", e);
            return s;
        }
    }

    public static final List<CustomRedirectURLDetermination> CUSTOM_HANDLERS = new CopyOnWriteArrayList<CustomRedirectURLDetermination>();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // create a new HttpSession if it's missing
        req.getSession(true);

        // get url to defer to
        // redirect
        String redirectURL = determineRedirectURL(req);
        if (redirectURL == null) {
            return;
        }
        char concat = '?';
        if (redirectURL.indexOf('?') >= 0) {
            concat = '&';
        }
        Enumeration parameterNames = req.getParameterNames();
        StringBuilder builder = new StringBuilder(redirectURL);

        while (parameterNames.hasMoreElements()) {
            String name = (String) parameterNames.nextElement();
            if (name.equals("redirect")) {
                continue;
            }
            String parameter = req.getParameter(name);
            builder.append(concat);
            concat = '&';
            builder.append(name).append('=').append(encodeUrl(parameter, true));
        }
        resp.sendRedirect(builder.toString());

    }

    private String determineRedirectURL(HttpServletRequest req) {
        for (CustomRedirectURLDetermination determination : CUSTOM_HANDLERS) {
            String url = determination.getURL(req);
            if (url != null) {
                return url;
            }
        }
        return req.getParameter("redirect");
    }

    private boolean isEmpty(final String string) {
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
}
