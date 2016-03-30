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
