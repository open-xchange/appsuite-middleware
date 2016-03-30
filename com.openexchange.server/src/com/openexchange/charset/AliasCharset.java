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

    /*
     * (non-Javadoc)
     * @see java.nio.charset.Charset#contains(java.nio.charset.Charset)
     */
    @Override
    public boolean contains(final Charset cs) {
        return this.getClass().isInstance(cs) || delegate.contains(cs);
    }

    /*
     * (non-Javadoc)
     * @see java.nio.charset.Charset#canEncode()
     */
    @Override
    public boolean canEncode() {
        return delegate.canEncode();
    }

    /*
     * (non-Javadoc)
     * @see java.nio.charset.Charset#newDecoder()
     */
    @Override
    public CharsetDecoder newDecoder() {
        return delegate.newDecoder();
    }

    /*
     * (non-Javadoc)
     * @see java.nio.charset.Charset#newEncoder()
     */
    @Override
    public CharsetEncoder newEncoder() {
        return delegate.newEncoder();
    }

}
