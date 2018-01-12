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

package com.openexchange.java.util;


/**
 * {@link ImmutablePair} - An immutable pair.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public final class ImmutablePair<T, V> {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static <T, V> Builder<T, V> builder() {
        return new Builder<>();
    }

    /** A builder for an <code>ImmutablePair</code> */
    public static final class Builder<T, V> {

        private T first;
        private V second;

        Builder() {
            super();
        }

        /**
         * Sets the first element.
         *
         * @param first The first element to set
         * @return This builder
         */
        public Builder<T, V> first(T first) {
            this.first = first;
            return this;
        }

        /**
         * Sets the second element.
         *
         * @param second The second element to set
         * @return This builder
         */
        public Builder<T, V> second(V second) {
            this.second = second;
            return this;
        }

        /**
         * Spawns a <code>ImmutablePair</code> instance from this builder's arguments.
         *
         * @return The resulting <code>ImmutablePair</code> instance
         */
        public ImmutablePair<T, V> build() {
            return new ImmutablePair<T, V>(first, second);
        }
    }

    // -------------------------------------------------------------------------------

    private final T first;
    private final V second;
    private final int hash;

    /**
     * Initializes a new {@link ImmutablePair}.
     */
    ImmutablePair(T first, V second) {
        super();
        this.first = first;
        this.second = second;
        int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        this.hash = result;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ImmutablePair other = (ImmutablePair) obj;
        if (first == null) {
            if (other.first != null) {
                return false;
            }
        } else if (!first.equals(other.first)) {
            return false;
        }
        if (second == null) {
            if (other.second != null) {
                return false;
            }
        } else if (!second.equals(other.second)) {
            return false;
        }
        return true;
    }

}
