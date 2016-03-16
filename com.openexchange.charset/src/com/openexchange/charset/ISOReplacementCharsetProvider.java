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
import java.nio.charset.UnsupportedCharsetException;
import java.nio.charset.spi.CharsetProvider;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * {@link ISOReplacementCharsetProvider} - A charset provider which returns the "WINDOWS-1252" charset when "ISO-8859-1" is requested.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ISOReplacementCharsetProvider extends CharsetProvider {

    private final Set<String> aliases;

    private final CharsetProvider standardProvider;

    private final Charset windows1252;

    private final Locale english;

    /**
     * Initializes a new {@link ISOReplacementCharsetProvider}.
     *
     * @throws UnsupportedCharsetException If "WINDOWS-1252" charset cannot be found
     */
    public ISOReplacementCharsetProvider(final CharsetProvider standardProvider) {
        super();
        this.standardProvider = standardProvider;
        windows1252 = Charset.forName("WINDOWS-1252");

        final Charset iso_8859_1 = Charset.forName("ISO-8859-1");
        english = Locale.ENGLISH;
        aliases = new HashSet<String>(16);
        aliases.add("ISO-8859-1");
        for (final String alias : iso_8859_1.aliases()) {
            aliases.add(alias.toUpperCase(english));
        }
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
        if (aliases.contains(charsetName.toUpperCase(english))) {
            return windows1252;
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
