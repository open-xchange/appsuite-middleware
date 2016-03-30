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

package com.openexchange.charset;

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
