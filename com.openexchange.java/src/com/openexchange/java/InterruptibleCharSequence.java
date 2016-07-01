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

package com.openexchange.java;

/**
 * {@link InterruptibleCharSequence} - A <code>CharSequence</code> that can notice thread interrupts.
 * <p>
 * Originally developed by <a href="http://gojomo.blogspot.de/">Gordon Mohr (@gojomo)</a> in Heritrix project (<a
 * href="crawler.archive.org">crawler.archive.org</a>).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class InterruptibleCharSequence implements CharSequence {

    /** The special runtime exception signaling that reading from char sequence has been interrupted */
    public static final class InterruptedRuntimeException extends RuntimeException {

        private static final long serialVersionUID = 6627208506308108226L;

        /**
         * Initializes a new {@link InterruptedRuntimeException}.
         */
        InterruptedRuntimeException() {
            super("Interrupted while reading char sequence", new InterruptedException());
        }
    }

    /**
     * Gets an {@link InterruptibleCharSequence} instance for given {@link CharSequence} instance.
     *
     * @param charSequence The char sequence
     * @return An {@link InterruptibleCharSequence} instance for given {@link CharSequence} instance
     */
    public static InterruptibleCharSequence valueOf(final CharSequence charSequence) {
        if (null == charSequence) {
            return null;
        }
        if (charSequence instanceof InterruptibleCharSequence) {
            return (InterruptibleCharSequence) charSequence;
        }
        return new InterruptibleCharSequence(charSequence);
    }

    // ------------------------------------------------------------------------------------------------------------- //

    private final CharSequence inner;

    /**
     * Initializes a new {@link InterruptibleCharSequence}.
     *
     * @param cs The char sequence to delegate to
     */
    private InterruptibleCharSequence(final CharSequence cs) {
        super();
        this.inner = cs;
    }

    @Override
    public char charAt(final int index) {
        if (Thread.interrupted()) { // clears flag if set
            throw new InterruptedRuntimeException();
        }
        // counter++;
        return inner.charAt(index);
    }

    @Override
    public int length() {
        return inner.length();
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return new InterruptibleCharSequence(inner.subSequence(start, end));
    }

    @Override
    public String toString() {
        return inner.toString();
    }

}
