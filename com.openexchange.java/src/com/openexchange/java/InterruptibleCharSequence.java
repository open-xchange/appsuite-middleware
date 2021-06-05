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
