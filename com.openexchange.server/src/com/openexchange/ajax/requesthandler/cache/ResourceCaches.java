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

package com.openexchange.ajax.requesthandler.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.java.Strings;

/**
 * {@link ResourceCaches} - Utility class for resource cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ResourceCaches {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ResourceCaches.class);

    /**
     * Initializes a new {@link ResourceCaches}.
     */
    private ResourceCaches() {
        super();
    }

    private static final AtomicReference<ResourceCache> CACHE_REF = new AtomicReference<ResourceCache>();

    /**
     * Sets the resource cache reference.
     *
     * @param ref The reference
     */
    public static void setResourceCache(final ResourceCache ref) {
        CACHE_REF.set(ref);
    }

    /**
     * Gets the resource cache reference.
     *
     * @return The preview cache or <code>null</code> if absent
     */
    public static ResourceCache getResourceCache() {
        return CACHE_REF.get();
    }

    private static final List<String> DEFAULT_THUMBNAIL_PARAMS = Arrays.asList("content_type", "context", "folder", "format", "height", "id", "scaleType", "version", "width");

    /**
     * Generates the key for preview cache.
     *
     * @param eTag The ETag identifier
     * @param requestData The request data
     * @return The appropriate cache key
     */
    public static String generateDefaultThumbnailCacheKey(String eTag, AJAXRequestData requestData) {
        return generateThumbnailCacheKey0(eTag, requestData, DEFAULT_THUMBNAIL_PARAMS);
    }

    /**
     * Generates the key for preview cache.
     *
     * @param eTag The ETag identifier
     * @param requestData The request data
     * @param paramNames The names of the parameters to consider
     * @return The appropriate cache key
     */
    public static String generateThumbnailCacheKey(String eTag, AJAXRequestData requestData, String... paramNames) {
        List<String> parameters = new ArrayList<String>(Arrays.asList(paramNames));
        Collections.sort(parameters);
        return generateThumbnailCacheKey0(eTag, requestData, parameters);
    }

    private static String generateThumbnailCacheKey0(String eTag, AJAXRequestData requestData, List<String> parameters) {
        StringBuilder sb = new StringBuilder(512);
        sb.append(requestData.getModule());
        sb.append('-').append(requestData.getAction());
        sb.append('-').append(requestData.getSession().getContextId());

        // Append sorted parameters
        for (String name : parameters) {
            if (isAcceptableParameter(name)) {
                sb.append('-').append(name);
                String parameter = requestData.getParameter(name);
                if (Strings.isNotEmpty(parameter)) {
                    sb.append('=').append(parameter);
                }
            }
        }

        return toMD5(eTag, sb);
    }

    /**
     * Generates the key for preview cache.
     *
     * @param eTag The ETag identifier
     * @param requestData The request data
     * @param optParameters Optional parameters to consider
     * @return The appropriate cache key
     */
    public static String generatePreviewCacheKey(String eTag, AJAXRequestData requestData, String... additionalParams) {
        final StringBuilder sb = new StringBuilder(512);
        sb.append(requestData.getModule());
        sb.append('-').append(requestData.getAction());
        sb.append('-').append(requestData.getSession().getContextId());

        // Append sorted parameters
        {
            List<String> parameters = new ArrayList<String>(requestData.getParameters().keySet());
            Collections.sort(parameters);
            for (String name : parameters) {
                if (isAcceptableParameter(name)) {
                    sb.append('-').append(name);
                    String parameter = requestData.getParameter(name);
                    if (Strings.isNotEmpty(parameter)) {
                        sb.append('=').append(parameter);
                    }
                }
            }
        }

        // Append additional parameters, if given
        if (null != additionalParams) {
            for (String additionalParam : additionalParams) {
                if (Strings.isNotEmpty(additionalParam)) {
                    sb.append('-').append(additionalParam);
                }
            }
        }

        // Generate MD5 sum
        return toMD5(eTag, sb);
    }

    private static String toMD5(String eTag, StringBuilder sb) {
        try {
            byte[] md5Bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            String hashedParams = asHex(MessageDigest.getInstance("MD5").digest(md5Bytes));
            String prefix = eTag;

            // Ensure key size does not exceed 128 characters (current database limit)
            if (prefix.length() + 33 > 128) {
                prefix = asHex(MessageDigest.getInstance("MD5").digest(prefix.getBytes(StandardCharsets.UTF_8)));
            }
            sb.setLength(0);
            return sb.append(prefix).append('-').append(hashedParams).toString();
        } catch (NoSuchAlgorithmException e) {
            // Shouldn't happen
            LOG.error("", e);
        }
        return sb.toString();
    }

    private static boolean isAcceptableParameter(String name) {
        if (Strings.isEmpty(name)) {
            return false;
        }
        String n = Strings.toLowerCase(name);
        return !"session".equals(n) && !"action".equals(n);
    }

    private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * Turns array of bytes into string representing each byte as unsigned hex number.
     *
     * @param hash Array of bytes to convert to hex-string
     * @return Generated hex string
     */
    public static String asHex(byte[] hash) {
        int length = hash.length;
        char[] buf = new char[length << 1];
        for (int i = 0, x = 0; i < length; i++) {
            buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
            buf[x++] = HEX_CHARS[hash[i] & 0xf];
        }
        return new String(buf);
    }

}
