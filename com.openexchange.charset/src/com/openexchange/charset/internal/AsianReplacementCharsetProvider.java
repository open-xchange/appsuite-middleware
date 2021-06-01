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
import java.util.Locale;

/**
 * {@link AsianReplacementCharsetProvider} - A charset provider which returns the "CP50220" charset when "ISO-2022-JP" is requested.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AsianReplacementCharsetProvider extends CharsetProvider {

    private static final class DelegatingCharset extends Charset {

        private final Charset encodeDelegate;
        private final Charset decodeDelegate;

        protected DelegatingCharset(final Charset decodeDelegate, final Charset encodeDelegate) {
            super(decodeDelegate.name(), decodeDelegate.aliases().toArray(new String[0]));
            this.decodeDelegate = decodeDelegate;
            this.encodeDelegate = encodeDelegate;
        }

        @Override
        public String displayName() {
            return decodeDelegate.displayName();
        }

        @Override
        public String displayName(final Locale locale) {
            return decodeDelegate.displayName(locale);
        }

        @Override
        public boolean contains(final Charset cs) {
            return decodeDelegate.contains(cs);
        }

        @Override
        public CharsetDecoder newDecoder() {
            return decodeDelegate.newDecoder();
        }

        @Override
        public CharsetEncoder newEncoder() {
            return encodeDelegate.newEncoder();
        }

        @Override
        public boolean canEncode() {
            return encodeDelegate.canEncode();
        }

    } // End of class DelegatingCharset

    private final CharsetProvider standardProvider;

    private final Charset cp50220;

    private final Charset delegatingCharset;

    /**
     * Initializes a new {@link AsianReplacementCharsetProvider}.
     *
     * @throws UnsupportedCharsetException If "CP50220" charset cannot be found
     */
    public AsianReplacementCharsetProvider(final CharsetProvider standardProvider) {
        super();
        this.standardProvider = standardProvider;
        cp50220 = Charset.forName("CP50220");
        // Delegating charset
        final Charset iso2022CN_Encoder = Charset.forName("ISO2022CN_GB");
        final Charset iso2022CN = Charset.forName("iso-2022-cn");
        delegatingCharset = new DelegatingCharset(iso2022CN, iso2022CN_Encoder);
        /*-
         * Retry with: "x-windows-50220", "MS50220"
         *
         * http://www.docjar.com/html/api/sun/nio/cs/ext/MS50220.java.html
         */
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
        if ("ISO-2022-JP".equalsIgnoreCase(charsetName)) {
            return cp50220;
        }
        if ("iso-2022-cn".equalsIgnoreCase(charsetName) || "ISO2022CN".equalsIgnoreCase(charsetName)) {
            return delegatingCharset;
        }
        /*
         * Delegate to standard provider
         */
        return standardProvider.charsetForName(charsetName);
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
