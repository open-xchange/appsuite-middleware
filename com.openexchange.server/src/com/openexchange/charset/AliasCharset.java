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

package com.openexchange.charset;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * {@link AliasCharset} - An alias charset that delegates an unknown charset name to a supported charset.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AliasCharset extends Charset {

    private final Charset delegate;

    /**
     * Initializes a new alias charset
     *
     * @param canonicalName The canonical name of the alias charset
     * @param aliases An array of this charset's aliases, or <code>null</code> if it has no aliases
     * @param delegate The delegate charset
     */
    public AliasCharset(final String canonicalName, final String[] aliases, final Charset delegate) {
        super(canonicalName, aliases);
        this.delegate = delegate;
    }

    @Override
    public boolean contains(final Charset cs) {
        return this.getClass().isInstance(cs) || delegate.contains(cs);
    }

    @Override
    public boolean canEncode() {
        return delegate.canEncode();
    }

    @Override
    public CharsetDecoder newDecoder() {
        return delegate.newDecoder();
    }

    @Override
    public CharsetEncoder newEncoder() {
        return delegate.newEncoder();
    }

}
