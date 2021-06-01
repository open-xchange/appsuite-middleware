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
import java.nio.charset.UnsupportedCharsetException;
import java.nio.charset.spi.CharsetProvider;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * {@link ASCIIReplacementCharsetProvider} - A charset provider which returns the "WINDOWS-1252" charset when "ISO-8859-1" is requested.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ASCIIReplacementCharsetProvider extends CharsetProvider {

    private final Set<String> aliases;

    private final CharsetProvider standardProvider;

    private final Charset windows1252;

    private final Locale english;

    /**
     * Initializes a new {@link ASCIIReplacementCharsetProvider}.
     *
     * @throws UnsupportedCharsetException If "WINDOWS-1252" charset cannot be found
     */
    public ASCIIReplacementCharsetProvider(final CharsetProvider standardProvider) {
        super();
        this.standardProvider = standardProvider;
        windows1252 = Charset.forName("WINDOWS-1252");

        final Charset ascii = Charset.forName("US-ASCII");
        english = Locale.ENGLISH;
        aliases = new HashSet<String>(16);
        aliases.add("US-ASCII");
        for (final String alias : ascii.aliases()) {
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
