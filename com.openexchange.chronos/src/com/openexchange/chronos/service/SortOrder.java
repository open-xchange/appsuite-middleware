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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.service;

import com.openexchange.chronos.EventField;

/**
 * {@link SortOrder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class SortOrder {

    /**
     * Initializes a new ascending {@link SortOrder} for a specific event field.
     *
     * @param by The event field to use for ordering
     * @return The sort order
     */
    public static SortOrder ASC(EventField by) {
        return new SortOrder(by, false);
    }

    /**
     * Initializes a new descending {@link SortOrder} for a specific event field.
     *
     * @param by The event field to use for ordering
     * @return The sort order
     */
    public static SortOrder DESC(EventField by) {
        return new SortOrder(by, true);
    }

    private final EventField by;
    private final boolean descending;

    /**
     * Initializes a new {@link SortOrder}.
     *
     * @param by The event field to use for ordering
     * @param descending <code>true</code> if descending, <code>false</code>, otherwise
     */
    private SortOrder(EventField by, boolean descending) {
        super();
        this.by = by;
        this.descending = descending;
    }

    /**
     * Gets the event field to use for ordering.
     *
     * @return The event field to use for ordering
     */
    public EventField getBy() {
        return by;
    }

    /**
     * Gets a value indicating whether a descending or ascending direction is defined.
     *
     * @return <code>true</code> if descending, <code>false</code> if ascending
     */
    public boolean isDescending() {
        return descending;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((by == null) ? 0 : by.hashCode());
        result = prime * result + (descending ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SortOrder other = (SortOrder) obj;
        if (by != other.by)
            return false;
        if (descending != other.descending)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return by + (descending ? " DESC" : " ASC");
    }

}
