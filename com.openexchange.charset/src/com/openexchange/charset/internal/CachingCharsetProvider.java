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
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.charset.spi.CharsetProvider;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;
import org.cliffc.high_scale_lib.NonBlockingHashMap;

/**
 * {@link CachingCharsetProvider} - A charset provider which returns the "CP50220" charset when "ISO-2022-JP" is requested.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CachingCharsetProvider extends CharsetProvider {

    private static final Charset NULL = new Charset("x-null", new String[0]) {

        @Override
        public CharsetEncoder newEncoder() {
            return null;
        }

        @Override
        public CharsetDecoder newDecoder() {
            return null;
        }

        @Override
        public boolean contains(final Charset cs) {
            return false;
        }
    };

    private final CharsetProvider standardProvider;
    private final ConcurrentMap<String, Charset> cache;

    /**
     * Initializes a new {@link CachingCharsetProvider}.
     *
     * @throws UnsupportedCharsetException If "CP50220" charset cannot be found
     */
    public CachingCharsetProvider(final CharsetProvider standardProvider) {
        super();
        this.standardProvider = standardProvider;
        cache = new NonBlockingHashMap<String, Charset>(32);
    }

    @Override
    public int hashCode() {
        return standardProvider.hashCode();
    }

    @Override
    public Iterator<Charset> charsets() {
        return standardProvider.charsets();
    }

    @Override
    public Charset charsetForName(final String charsetName) {
        Charset charset = cache.get(charsetName);
        if (null == charset) {
            Charset ncharset = standardProvider.charsetForName(charsetName);
            if (null == ncharset) {
                ncharset = NULL;
            }
            charset = cache.putIfAbsent(charsetName, ncharset);
            if (null == charset) {
                charset = ncharset;
            }
        }
        return NULL == charset ? null : charset;
    }

    @Override
    public boolean equals(final Object obj) {
        return standardProvider.equals(obj);
    }

    @Override
    public String toString() {
        return standardProvider.toString();
    }

}
