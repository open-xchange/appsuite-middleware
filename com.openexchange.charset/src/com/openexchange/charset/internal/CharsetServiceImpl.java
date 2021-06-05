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

package com.openexchange.charset.internal;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.spi.CharsetProvider;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.charset.CharsetService;

/**
 * {@link CharsetServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class CharsetServiceImpl implements CharsetService {

    /**
     * Initializes a new {@link CharsetServiceImpl}.
     */
    public static CharsetServiceImpl newInstance(CharsetProvider standardProvider, CharsetProvider extendedProvider) {
        CharsetServiceImpl instance = new CharsetServiceImpl(standardProvider, extendedProvider);
        CharsetServiceUtility.setIso8859CS(instance.getRawCharsetFor("ISO-8859-1"));
        return instance;
    }

    // ------------------------------------------------------------------------------

    private final CharsetProvider standardProvider;
    private final CharsetProvider extendedProvider;
    private final ConcurrentMap<String, Charset> cache;

    /**
     * Initializes a new {@link CharsetServiceImpl}.
     */
    private CharsetServiceImpl(CharsetProvider standardProvider, CharsetProvider extendedProvider) {
        super();
        cache = new ConcurrentHashMap<>(32, 0.9F, 1);
        this.standardProvider = standardProvider;
        this.extendedProvider = extendedProvider;
    }

    @Override
    public Charset getRawCharsetFor(String charsetName) {
        if (null == charsetName) {
            throw new IllegalArgumentException("charset name is null.");
        }
        Charset cs = cache.get(charsetName);
        if (null == cs) {
            if ((cs = standardProvider.charsetForName(charsetName)) != null || (cs = lookupExtendedCharset(charsetName)) != null) {
                cache.put(charsetName, cs);
                return cs;
            }
        }

        checkName(charsetName);
        return null;
    }

    private Charset lookupExtendedCharset(String charsetName) {
        CharsetProvider ecp = extendedProvider;
        return (ecp != null) ? ecp.charsetForName(charsetName) : null;
    }

    /**
     * Checks that the given string is a legal charset name. </p>
     *
     * @param s
     *            A purported charset name
     *
     * @throws IllegalCharsetNameException
     *             If the given name is not a legal charset name
     */
    private static void checkName(String s) {
        int n = s.length();
        if (n == 0) {
            throw new IllegalCharsetNameException(s);
        }
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                continue;
            }
            if (c >= 'a' && c <= 'z') {
                continue;
            }
            if (c >= '0' && c <= '9') {
                continue;
            }
            if (c == '-' && i != 0) {
                continue;
            }
            if (c == '+' && i != 0) {
                continue;
            }
            if (c == ':' && i != 0) {
                continue;
            }
            if (c == '_' && i != 0) {
                continue;
            }
            if (c == '.' && i != 0) {
                continue;
            }
            throw new IllegalCharsetNameException(s);
        }
    }

}
