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

package com.openexchange.image;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;
import jonelo.jacksum.algorithm.MD;
import org.slf4j.Logger;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.session.Session;

/**
 * {@link ImageUtility} - Utility class image module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImageUtility {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ImageUtility.class);

    private static final AtomicReference<DispatcherPrefixService> DPS_REF = new AtomicReference<DispatcherPrefixService>();

    /**
     * Initializes a new {@link ImageUtility}.
     */
    private ImageUtility() {
        super();
    }

    private static final String UTF_8 = "UTF-8";

    private static final Pattern SPLIT = Pattern.compile("&amp;|&");

    private static interface ValueHandler {

        void handleValue(String value, ImageLocation.Builder builder);
    }

    private static final Map<String, ValueHandler> NVP_HANDLERS;
    static {
        Map<String, ValueHandler> map = new HashMap<String, ValueHandler>(8);

        map.put("accountId", new ValueHandler() {

            @Override
            public void handleValue(String value, ImageLocation.Builder builder) {
                builder.accountId(value);
            }
        });

        map.put(AJAXServlet.PARAMETER_FOLDERID, new ValueHandler() {

            @Override
            public void handleValue(String value, ImageLocation.Builder builder) {
                builder.folder(value);
            }
        });

        map.put(AJAXServlet.PARAMETER_ID, new ValueHandler() {

            @Override
            public void handleValue(String value, ImageLocation.Builder builder) {
                builder.id(value);
            }
        });

        map.put(AJAXServlet.PARAMETER_UID, new ValueHandler() {

            @Override
            public void handleValue(String value, ImageLocation.Builder builder) {
                builder.imageId(value);
            }
        });

        map.put(AJAXServlet.PARAMETER_TIMESTAMP, new ValueHandler() {

            @Override
            public void handleValue(String value, ImageLocation.Builder builder) {
                builder.timestamp(value);
            }
        });

        map.put("source", new ValueHandler() {

            @Override
            public void handleValue(String value, ImageLocation.Builder builder) {
                builder.registrationName(value);
            }
        });

        NVP_HANDLERS = map;
    }

    public static void setDispatcherPrefixService(DispatcherPrefixService service) {
        DPS_REF.set(service);
    }

    /**
     * Parses image location from specified image URI.
     *
     * @param imageUri The image URI
     * @return The parsed image location
     * @throws IllegalArgumentException If no such registration name can be found
     */
    public static ImageLocation parseImageLocationFrom(String imageUri) {
        if (null == imageUri) {
            return null;
        }
        int queryStringStart = imageUri.indexOf('?');
        if (queryStringStart < 0) {
            return new ImageLocation(new ImageLocation.Builder());
        }

        String[] nvps = SPLIT.split(imageUri.substring(queryStringStart + 1/*Consume starting '?'*/), 0);
        ImageLocation.Builder builder = new ImageLocation.Builder();

        for (String nvp : nvps) {
            // Look-up character '='
            int pos = (nvp = nvp.trim()).indexOf('=');
            if (pos >= 0) {
                String name = Strings.asciiLowerCase(nvp.substring(0, pos));
                ValueHandler handler = NVP_HANDLERS.get(name);
                if (null != handler) {
                    handler.handleValue(decodeQueryStringValue(nvp.substring(pos + 1)), builder);
                }
            }
        }

        ImageLocation il = builder.build();
        String registrationName = builder.registrationName;
        if (null == registrationName) {
            registrationName = ImageActionFactory.getRegistrationNameFor(imageUri);
            if (null == registrationName) {
                throw new IllegalArgumentException("No known registration name for: " + imageUri);
            }
        }
        il.setRegistrationName(registrationName);
        return il;
    }

    /**
     * Parses image location from given request data
     *
     * @param requestData The request data to parse from
     * @return The parsed image location
     * @throws IllegalArgumentException If there is no matching registration name for request's URI (the URI part after path to the Servlet)
     */
    public static ImageLocation parseImageLocationFrom(final AJAXRequestData requestData) {
        if (requestData == null) {
            return null;
        }

        final String accountId = requestData.getParameter("accountId");
        final String folder = requestData.getParameter(AJAXServlet.PARAMETER_FOLDERID);
        final String id = requestData.getParameter(AJAXServlet.PARAMETER_ID);
        final String imageId = requestData.getParameter(AJAXServlet.PARAMETER_UID);
        final String timestamp = requestData.getParameter(AJAXServlet.PARAMETER_TIMESTAMP);
        String registrationName = requestData.getParameter("source");

        final ImageLocation il = new ImageLocation.Builder(imageId).accountId(accountId).folder(folder).id(id).timestamp(timestamp).build();
        if (null == registrationName) {
            registrationName = ImageActionFactory.getRegistrationNameFor(requestData.getSerlvetRequestURI());
            if (null == registrationName) {
                throw new IllegalArgumentException("No known registration name for: " + requestData.getSerlvetRequestURI());
            }
        }
        il.setRegistrationName(registrationName);
        return il;
    }

    private static String decodeQueryStringValue(final String queryStringValue) {
        try {
            return AJAXUtility.decodeUrl(queryStringValue, UTF_8);
        } catch (final RuntimeException e) {
            return queryStringValue;
        }
    }

    /**
     * Starts the image URL in given {@link StringBuilder} instance.
     *
     * @param imageLocation The image location
     * @param session The session
     * @param imageDataSource The data source
     * @param preferRelativeUrl Whether to prefer a relative image URL
     * @param sb The string builder to write to
     */
    public static void startImageUrl(final ImageLocation imageLocation, final Session session, final ImageDataSource imageDataSource, final boolean preferRelativeUrl, final StringBuilder sb) {
        startImageUrl(imageLocation, session, imageDataSource, preferRelativeUrl, false, sb);
    }

    /**
     * Starts the image URL in given {@link StringBuilder} instance.
     *
     * @param imageLocation The image location
     * @param session The session
     * @param imageDataSource The data source
     * @param preferRelativeUrl Whether to prefer a relative image URL
     * @param addRoute <code>true</code> to add route; otherwise <code>false</code>
     * @param sb The string builder to write to
     */
    public static void startImageUrl(final ImageLocation imageLocation, final Session session, final ImageDataSource imageDataSource, final boolean preferRelativeUrl, final boolean addRoute, final StringBuilder sb) {
        boolean optImageHostSet = false;
        final String prefix;
        final String httpSessionID;
        String publicSessionId = null;
        {
            final HostData hostData = (HostData) session.getParameter(HostnameService.PARAM_HOST_DATA);
            if (hostData == null) {
                /*
                 * Compose relative URL
                 */
                final String optImageHost = imageLocation.getOptImageHost();
                if (Strings.isEmpty(optImageHost)) {
                    prefix = "";
                } else {
                    publicSessionId = (String) session.getParameter(Session.PARAM_ALTERNATIVE_ID);
                    if (Strings.isEmpty(publicSessionId)) {
                        LOGGER.warn("Cannot use configured image host \"{}\" as associated session has no public session identifier set.", optImageHost);
                        prefix = "";
                    } else {
                        final String tmp = Strings.toLowerCase(optImageHost);
                        prefix = tmp.startsWith("http") ? optImageHost : new StringBuilder(32).append("http://").append(optImageHost).toString();
                        optImageHostSet = true;
                    }
                }
                httpSessionID = LogProperties.getLogProperty(LogProperties.Name.GRIZZLY_HTTP_SESSION);
            } else {
                /*
                 * Compose absolute URL if a relative one is not preferred
                 */
                final String optImageHost = imageLocation.getOptImageHost();
                if (Strings.isEmpty(optImageHost)) {
                    if (preferRelativeUrl) {
                        prefix = "";
                    } else {
                        sb.append(hostData.isSecure() ? "https://" : "http://");
                        sb.append(hostData.getHost());
                        final int port = hostData.getPort();
                        if ((hostData.isSecure() && port != 443) || (!hostData.isSecure() && port != 80)) {
                            sb.append(':').append(port);
                        }
                        prefix = sb.toString();
                        sb.setLength(0);
                    }
                } else {
                    publicSessionId = (String) session.getParameter(Session.PARAM_ALTERNATIVE_ID);
                    if (Strings.isEmpty(publicSessionId)) {
                        LOGGER.warn("Cannot use configured image host \"{}\" as associated session has no public session identifier set.", optImageHost);
                        if (preferRelativeUrl) {
                            prefix = "";
                        } else {
                            sb.append(hostData.isSecure() ? "https://" : "http://");
                            sb.append(hostData.getHost());
                            final int port = hostData.getPort();
                            if ((hostData.isSecure() && port != 443) || (!hostData.isSecure() && port != 80)) {
                                sb.append(':').append(port);
                            }
                            prefix = sb.toString();
                            sb.setLength(0);
                        }
                    } else {
                        final String tmp = Strings.toLowerCase(optImageHost);
                        prefix = tmp.startsWith("http") ? optImageHost : new StringBuilder(32).append("http://").append(optImageHost).toString();
                        optImageHostSet = true;
                    }
                }
                httpSessionID = null != hostData.getHTTPSession() ? hostData.getHTTPSession() : "0123456789." + hostData.getRoute();
            }
        }
        /*
         * Compose URL parameters
         */
        sb.append(prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix);
        sb.append(getDispatcherPrefix());
        sb.append(ImageDataSource.ALIAS_APPENDIX);
        final String alias = imageDataSource.getAlias();
        if (null != alias) {
            sb.append(alias);
        }
        if (optImageHostSet) {
            if (null != httpSessionID) {
                sb.append(";jsessionid=").append(httpSessionID);
            }
        } else if (addRoute ) {
            final Boolean noRoute = (Boolean) imageLocation.getProperty(ImageLocation.PROPERTY_NO_ROUTE);
            if ((null == noRoute || !noRoute.booleanValue()) && null != httpSessionID) {
                sb.append(";jsessionid=").append(httpSessionID);
            }
        }
        boolean first = true;
        if (null != publicSessionId) {
            sb.append('?').append(AJAXServlet.PARAMETER_PUBLIC_SESSION).append('=').append(urlEncodeSafe(publicSessionId));
            first = false;
        }
        if (null == alias) {
            sb.append(first ? '?' : '&').append("source=").append(urlEncodeSafe(imageDataSource.getRegistrationName()));
            first = false;
        }
        /*
         * Image location data
         */
        final String folder = imageLocation.getFolder();
        if (null != folder) {
            sb.append(first ? '?' : '&').append(AJAXServlet.PARAMETER_FOLDERID).append('=').append(urlEncodeSafe(folder));
            first = false;
        }
        final String objectId = imageLocation.getId();
        if (null != objectId) {
            sb.append(first ? '?' : '&').append(AJAXServlet.PARAMETER_ID).append('=').append(urlEncodeSafe(objectId));
            first = false;
        }
        final String imageId = imageLocation.getImageId();
        if (null != imageId) {
            sb.append(first ? '?' : '&').append(AJAXServlet.PARAMETER_UID).append('=').append(urlEncodeSafe(imageId));
            first = false;
        }
        final String timestamp = imageLocation.getTimestamp();
        if (null != timestamp) {
            sb.append(first ? '?' : '&').append(AJAXServlet.PARAMETER_TIMESTAMP).append('=').append(urlEncodeSafe(timestamp));
            first = false;
        }
        final String accountId = imageLocation.getAccountId();
        if (null != accountId) {
            sb.append(first ? '?' : '&').append("accountId=").append(urlEncodeSafe(accountId));
            first = false;
        }
    }

    /**
     * Translates specified string into application/x-www-form-urlencoded format using a specific encoding scheme. This method uses the
     * supplied encoding scheme to obtain the bytes for unsafe characters.
     *
     * @param text The string to be translated.
     * @return The translated string or the string itself if any error occurred
     */
    private static String urlEncodeSafe(final String text) {
        try {
            return URLEncoder.encode(text, UTF_8);
        } catch (final UnsupportedEncodingException e) {
            // Cannot occur
            org.slf4j.LoggerFactory.getLogger(ImageUtility.class).error("", e);
            return text;
        }
    }

    /**
     * Gets the MD5 hash of specified string using <a href="http://www.jonelo.de/java/jacksum/index.html">Jacksum 1.7.0</a>.
     *
     * @param string The string to hash
     * @param encoding The encoding; e.g <code>base64</code>, <code>hex</code>, <code>dec</code>, etc.
     * @return The MD5 hash
     */
    public static String getMD5(final String string, final String encoding) {
        try {
            final AbstractChecksum checksum = new MD("MD5");
            checksum.setEncoding(encoding);
            checksum.update(string.getBytes(Charsets.UTF_8));
            return checksum.getFormattedValue();
        } catch (final NoSuchAlgorithmException e) {
            org.slf4j.LoggerFactory.getLogger(ImageUtility.class).error("", e);
        }
        return null;
    }

    /**
     * Gets the SHA-256 hash of specified string using <a href="http://www.jonelo.de/java/jacksum/index.html">Jacksum 1.7.0</a>.
     * <p>
     * Supported algorithms:<br>
     *
     * <pre>
     * Adler32, BSD sum, Bzip2's CRC-32, POSIX cksum, CRC-8, CRC-16, CRC-24, CRC-32 (FCS-32), CRC-64, ELF-32, eMule/eDonkey, FCS-16, GOST R
     * 34.11-94, HAS-160, HAVAL (3/4/5 passes, 128/160/192/224/256 bits), MD2, MD4, MD5, MPEG-2's CRC-32, RIPEMD-128, RIPEMD-160,
     * RIPEMD-256, RIPEMD-320, SHA-0, SHA-1, SHA-224, SHA-256, SHA-384, SHA-512, Tiger-128, Tiger-160, Tiger, Tiger2, Tiger Tree Hash,
     * Tiger2 Tree Hash, Unix System V sum, sum8, sum16, sum24, sum32, Whirlpool-0, Whirlpool-1, Whirlpool, and xor8
     * </pre>.
     *
     * @param string The string to hash
     * @param algorithm The hash algorithm to use; e.g. <code>sha-1</code>, <code>sha-256</code>, <code>md5</code>, <code>crc32</code>,
     *            <code>adler32</code>, ...
     * @param encoding The encoding; e.g <code>base64</code>, <code>hex</code>, <code>dec</code>, etc.
     * @return The SHA-256 hash or <code>null</code> if hash could not be generated
     */
    public static String getHash(final String string, final String algorithm, final String encoding) {
        try {
            final AbstractChecksum checksum = JacksumAPI.getChecksumInstance(algorithm);
            checksum.setEncoding(encoding);
            checksum.update(string.getBytes(UTF_8));
            return checksum.getFormattedValue();
        } catch (final NoSuchAlgorithmException e) {
            org.slf4j.LoggerFactory.getLogger(ImageUtility.class).error("", e);
        } catch (final UnsupportedEncodingException e) {
            org.slf4j.LoggerFactory.getLogger(ImageUtility.class).error("", e);
        }
        return null;
    }

    /**
     * The radix for base <code>10</code>.
     */
    private static final int RADIX = 10;

    /**
     * Parses a positive <code>int</code> value from passed {@link String} instance.
     *
     * @param s The string to parse
     * @return The parsed positive <code>int</code> value or <code>-1</code> if parsing failed
     */
    public static final int getUnsignedInteger(final String s) {
        if (s == null) {
            return -1;
        }

        final int max = s.length();

        if (max <= 0) {
            return -1;
        }
        if (s.charAt(0) == '-') {
            return -1;
        }

        final int limit = -Integer.MAX_VALUE;
        final int multmin = limit / RADIX;

        int result = 0;
        int i = 0;
        int digit;

        if (i < max) {
            digit = digit(s.charAt(i++));
            if (digit < 0) {
                return -1;
            }
            result = -digit;
        }
        while (i < max) {
            /*
             * Accumulating negatively avoids surprises near MAX_VALUE
             */
            digit = digit(s.charAt(i++));
            if (digit < 0) {
                return -1;
            }
            if (result < multmin) {
                return -1;
            }
            result *= RADIX;
            if (result < limit + digit) {
                return -1;
            }
            result -= digit;
        }
        return -result;
    }

    private static int digit(final char c) {
        switch (c) {
        case '0':
            return 0;
        case '1':
            return 1;
        case '2':
            return 2;
        case '3':
            return 3;
        case '4':
            return 4;
        case '5':
            return 5;
        case '6':
            return 6;
        case '7':
            return 7;
        case '8':
            return 8;
        case '9':
            return 9;
        default:
            return -1;
        }
    }

    static String getDispatcherPrefix() {
        DispatcherPrefixService dispatcherPrefixService = DPS_REF.get();
        if (dispatcherPrefixService == null) {
            throw new IllegalStateException(ImageUtility.class.getName() + " has not been initialized. DispatcherPrefixService is not set!");
        }

        return dispatcherPrefixService.getPrefix();
    }

}
