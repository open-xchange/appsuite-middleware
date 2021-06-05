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

package com.openexchange.json.cache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.java.Charsets;

/**
 * {@link JsonCaches} - Utility class for JSON cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JsonCaches {

    private static final String ALGORITHM_MD5 = "MD5";
    private static final String ALGORITHM_SHA1 = "SHA1";

    /**
     * The cache reference for fast look-up.
     *
     * @see #getCache()
     */
    public static final AtomicReference<JsonCacheService> CACHE_REFERENCE = new AtomicReference<JsonCacheService>();

    /**
     * Initializes a new {@link JsonCaches}.
     */
    private JsonCaches() {
        super();
    }

    /**
     * Gets the cache.
     *
     * @return The cache or <code>null</code> if absent
     */
    public static JsonCacheService getCache() {
        return CACHE_REFERENCE.get();
    }

    /**
     * Gets the MD5 sum of passed arguments.
     *
     * @param args The arguments
     * @return The MD5 sum's hex representation
     */
    public static String getMD5Sum(final String... args) {
        if (null == args || 0 == args.length) {
            return "";
        }
        return getCheckSum(ALGORITHM_MD5, args);
    }

    /**
     * Gets the SHA1 sum of passed arguments.
     *
     * @param args The arguments
     * @return The SHA1 sum's hex representation
     */
    public static String getSHA1Sum(final String... args) {
        if (null == args || 0 == args.length) {
            return "";
        }
        return getCheckSum(ALGORITHM_SHA1, args);
    }

    private static String getCheckSum(final String algorithm, final String[] args) {
        try {
            final MessageDigest md = MessageDigest.getInstance(algorithm);
            for (final String arg : args) {
                if (null != arg) {
                    md.update(arg.getBytes(Charsets.UTF_8));
                }
            }
            return asHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * Turns array of bytes into string representing each byte as unsigned hex number.
     *
     * @param hash Array of bytes to convert to hex-string
     * @return Generated hex string
     */
    public static String asHex(final byte[] hash) {
        final int length = hash.length;
        final char[] buf = new char[length << 1];
        for (int i = 0, x = 0; i < length; i++) {
            buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
            buf[x++] = HEX_CHARS[hash[i] & 0xf];
        }
        return new String(buf);
    }

    /**
     * Checks if given JSON values are equal.
     *
     * @param a The first JSON value
     * @param b The second JSON value
     * @return <code>true</code> if considered equal; otherwise <code>false</code>
     */
    public static boolean areEqual(final JSONValue a, final JSONValue b) {
        try {
            if (a == b) {
                return true;
            }
            if (null == a) {
                return null == b;
            }
            if (null == b) {
                return false;
            }
            // Check length/size
            final int aLen = a.length();
            final int bLen = b.length();
            if (aLen != bLen) {
                return false;
            }
            // In-depth comparison
            if (a.isArray()) { // ... by JSON array
                if (!b.isArray()) {
                    return false;
                }
                final JSONArray aj = a.toArray();
                final JSONArray bj = b.toArray();
                boolean bool = true;
                for (int i = 0; bool && i < aLen; i++) {
                    final Object ao = aj.get(i);
                    final Object bo = bj.get(i);
                    if (areJSONArrays(ao, bo)) {
                        bool = areEqual((JSONArray) ao, (JSONArray) bo);
                    } else if (areJSONObjects(ao, bo)) {
                        bool = areEqual((JSONObject) ao, (JSONObject) bo);
                    } else {
                        bool = ao.equals(bo);
                    }
                }
                return bool;
            }
            // Only thing left: JSON object
            if (!b.isObject()) {
                return false;
            }
            final JSONObject aj = a.toObject();
            final JSONObject bj = b.toObject();
            for (final Map.Entry<String, Object> entry : aj.entrySet()) {
                final String name = entry.getKey();
                if (!bj.has(name)) {
                    return false;
                }
                final Object ao = entry.getValue();
                final Object bo = bj.get(name);
                if (areJSONArrays(ao, bo)) {
                    if (!areEqual((JSONArray) ao, (JSONArray) bo)) {
                        return false;
                    }
                } else if (areJSONObjects(ao, bo)) {
                    if (!areEqual((JSONObject) ao, (JSONObject) bo)) {
                        return false;
                    }
                } else {
                    if (!ao.equals(bo)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    private static boolean areJSONArrays(final Object... objects) {
        boolean b = true;
        for (int i = 0; b && i < objects.length; i++) {
            b = (objects[i] instanceof JSONArray);
        }
        return b;
    }

    private static boolean areJSONObjects(final Object... objects) {
        boolean b = true;
        for (int i = 0; b && i < objects.length; i++) {
            b = (objects[i] instanceof JSONObject);
        }
        return b;
    }

}
