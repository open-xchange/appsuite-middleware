/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
            if (urlCodec == null) {
                return s;
            }
            return P_MINUS.matcher(P_DOT.matcher(urlCodec.encode(s)).replaceAll("%2E")).replaceAll("%2D");
        } catch (EncoderException e) {
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
            URLCodec urlCodec = getUrlCodec(cs);
            if (urlCodec == null) {
                return s;
            }
            return urlCodec.decode(s, cs);
        } catch (DecoderException e) {
            return s;
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

}
