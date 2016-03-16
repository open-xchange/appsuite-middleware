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

package com.openexchange.http.deferrer.servlet;

import static com.openexchange.java.Strings.isEmpty;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import com.openexchange.configuration.ServerConfig;

/**
 * {@link Utility} - Provides some utility methods.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Utility {

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
     * Initializes a new {@link Utility}
     */
    private Utility() {
        super();
    }

    private static final Pattern P_DOT = Pattern.compile("\\.");
    private static final Pattern P_MINUS = Pattern.compile("-");

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     */
    public static String urlEncode(final String s, final String charset) {
        try {
            if (isEmpty(s)) {
                return s;
            }
            final URLCodec urlCodec = getUrlCodec(null == charset ? ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding) : charset);
            return P_MINUS.matcher(P_DOT.matcher(urlCodec.encode(s)).replaceAll("%2E")).replaceAll("%2D");
        } catch (final EncoderException e) {
            return s;
        }
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

}
