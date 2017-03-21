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
 *    trademarks of the OX Software GmbH. group of companies.
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
