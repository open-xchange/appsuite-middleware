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

package com.openexchange.session;

import java.util.NoSuchElementException;

/**
 * {@link SessionAttribute} - Represents a value (allowing <code>null</code>) for a session attribute that is supposed to be changed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class SessionAttribute<V> {

    /** The common instance for an unset session attribute */
    private static final SessionAttribute<?> UNSET = new SessionAttribute<>(null, false);

    /**
     * Returns an unset {@code SessionAttribute} instance. No value is present for this SessionAttribute.
     *
     * @param <T> Type of the unset value
     * @return An unset {@code SessionAttribute}
     */
    public static <V> SessionAttribute<V> unset() {
        @SuppressWarnings("unchecked") SessionAttribute<V> t = (SessionAttribute<V>) UNSET;
        return t;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /** The common instance for session attribute with a <code>null</code> value */
    private static final SessionAttribute<?> EMPTY = new SessionAttribute<>(null, true);

    /**
     * Returns an empty {@code SessionAttribute} instance. A <code>null</code> value is present for this SessionAttribute.
     *
     * @param <T> Type of the non-existent value
     * @return An empty {@code SessionAttribute}
     */
    public static <V> SessionAttribute<V> empty() {
        @SuppressWarnings("unchecked") SessionAttribute<V> t = (SessionAttribute<V>) EMPTY;
        return t;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new session attribute carrying specified value.
     *
     * @param <V> The value type
     * @param value The value
     * @return The session attribute
     */
    public static <V> SessionAttribute<V> valueOf(V value) {
        return value == null ? empty() : new SessionAttribute<V>(value, true);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final V value;
    private final boolean set;

    /**
     * Initializes a new {@link SessionAttribute}.
     *
     * @param value The value for this session attribute
     */
    public SessionAttribute(V value) {
        this(value, true);
    }

    /**
     * Initializes a new {@link SessionAttribute}.
     *
     * @param value The value for this session attribute
     * @param set <code>true</code> if value has been explicitly set; otherwise <code>false</code>
     */
    private SessionAttribute(V value, boolean set) {
        super();
        this.value = value;
        this.set = set;
    }

    /**
     * Checks if this session attribute has been set.
     *
     * @return code>true</code> if set; otherwise <code>false</code>
     */
    public boolean isSet() {
        return set;
    }

    /**
     * Gets the value set for this session attribute.
     *
     * @return The value or <code>null</code>
     * @throws NoSuchElementException If no value has been set; that is invoking {@link #isSet()} returns <code>false</code>
     */
    public V get() {
        if (!set) {
            throw new NoSuchElementException("No value has been set");
        }
        return value;
    }

    @Override
    public String toString() {
        return set ? (value == null ? "null" : value.toString()) : "<empty>";
    }

}
