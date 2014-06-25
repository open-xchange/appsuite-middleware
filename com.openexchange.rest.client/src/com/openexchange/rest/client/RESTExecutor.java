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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.rest.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import com.openexchange.java.Strings;

/**
 * {@link RESTExecutor}. Used to create, execute and parse the responses of REST requests to any REST API.
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class RESTExecutor {

    public static enum Method {
        PUT, GET, POST, DELETE;
    }

    private static final URLCodec URL_CODEC = new URLCodec(CharEncoding.UTF_8);

    /**
     * Initializes a new {@link RESTExecutor}.
     */
    private RESTExecutor() {
        super();
    }

    /**
     * Creates a URL for a REST request
     * 
     * @param host The host on which resides the REST API (i.e., API server, content server, or web server).
     * @param apiVersion The optional API version to use. Or <code>-1</code> to ignore
     * @param target The target path, starting with a '/'.
     * @param params Any URL parameters in a String array. with the even numbered elements the parameter names and odd numbered elements the
     *            values, e.g. <code>new String[] {"path", "/Public", "locale", "en"}</code>.
     * @return A full URL for making a request.
     */
    public static String buildURL(final String host, final int apiVersion, final String target, final String[] params) {
        String trgt = new String();
        if (Strings.isEmpty(target)) {
            if (params != null && params.length > 0) {
                final StringBuilder sb = new StringBuilder(params.length << 4);
                sb.append('?').append(urlencode(params));
                trgt = sb.toString();
            }
        } else {
            // Path is not empty
            trgt = target;
            if (!trgt.startsWith("/")) {
                trgt = new StringBuilder(trgt.length() + 1).append('/').append(trgt).toString();
            }

            try {
                // We have to encode the whole line, then remove + and / encoding to get a good OAuth URL.
                final StringBuilder versionBuilder = new StringBuilder(16);
                if (apiVersion > 0) {
                    versionBuilder.append("/v").append(apiVersion);
                }
                trgt = URLEncoder.encode(versionBuilder.append(trgt).toString(), "UTF-8");
                trgt = trgt.replace("%2F", "/");

                if (params != null && params.length > 0) {
                    final StringBuilder sb = new StringBuilder(trgt);
                    sb.append('?').append(urlencode(params));
                    trgt = sb.toString();
                }

                // These substitutions must be made to keep OAuth happy.
                trgt = trgt.replace("+", "%20").replace("*", "%2A");
            } catch (final UnsupportedEncodingException uce) {
                return null;
            }
        }
        return buildURL(host, trgt);
    }

    /**
     * Build an http(s) URL according to the prefix of the host.
     * 
     * @param host The host
     * @param target The target path, starting with a '/'.
     * @return The http(s) URL
     */
    private static String buildURL(final String host, final String target) {
        final StringBuilder sb = new StringBuilder(64);
        if (host.startsWith("http://") || host.startsWith("https://")) {
            sb.append(host);
        } else {
            sb.append("https://").append(host).append(":443");
        }
        return sb.append(target).toString();
    }

    /**
     * URL encodes an array of parameters into a query string.
     * 
     * @param params The parameters to encode
     * @return The encoded parameters as string
     */
    private static String urlencode(final String[] params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("Params must have an even number of elements.");
        }
        try {
            final StringBuilder result = new StringBuilder(params.length << 4);
            boolean firstTime = true;
            for (int i = 0; i < params.length; i += 2) {
                final String value = params[i + 1];
                if (null != value) {
                    if (firstTime) {
                        firstTime = false;
                    } else {
                        result.append('&');
                    }
                    result.append(encodeUrl(params[i])).append('=').append(encodeUrl(value));
                }
            }
            return result.toString().replace("*", "%2A");
        } catch (final RuntimeException e) {
            return null;
        }
    }

    /**
     * URL encodes given string.
     * 
     * @param s A string to encode
     * @return The encoded string
     */
    public static final String encodeUrl(String s) {
        try {
            return Strings.isEmpty(s) ? s : URL_CODEC.encode(s);
        } catch (EncoderException e) {
            return s;
        }
    }

}
