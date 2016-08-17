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

package com.openexchange.file.storage;


/**
 * {@link Range} - Represents a requested range providing a <i>"from"</i> (inclusive) and a <i>"to"</i> (exclusive) position.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class Range {

    /**
     * Initializes a new {@link Range}.
     *
     * @param indexes The <i>"from"</i> (inclusive) and <i>"to"</i> (exclusive) positions
     * @return The range or <code>null</code>
     * @throws IllegalArgumentException If passed <i>"from"</i>/<i>"to"</i> positions are invalid
     */
    public static Range valueOf(int[] indexes) {
        return null == indexes || 2 != indexes.length ? null : new Range(indexes[0], indexes[1]);
    }

    /**
     * Initializes a new {@link Range}.
     *
     * @param from The <i>"from"</i> position (inclusive)
     * @param to The <i>"to"</i> position (exclusive)
     * @return The range
     * @throws IllegalArgumentException If passed <i>"from"</i>/<i>"to"</i> positions are invalid
     */
    public static Range valueOf(int from, int to) {
        return new Range(from, to);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    /** The <i>"from"</i> position (inclusive) */
    public final int from;

    /** The <i>"to"</i> position (exclusive) */
    public final int to;

    /** The internal hash code */
    private final int hash;

    /**
     * Initializes a new {@link Range}.
     *
     * @param from The <i>"from"</i> position (inclusive)
     * @param to The <i>"to"</i> position (exclusive)
     * @throws IllegalArgumentException If passed <i>"from"</i>/<i>"to"</i> positions are invalid
     */
    private Range(int from, int to) {
        super();
        if (from < 0 || to < 0 || to < from) {
            throw new IllegalArgumentException("Invalid from/to positions. From=" + from + ", To=" + to);
        }
        this.from = from;
        this.to = to;

        int prime = 31;
        int result = prime * 1 + from;
        result = prime * result + to;
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
        if (!(obj instanceof Range)) {
            return false;
        }
        Range other = (Range) obj;
        if (from != other.from) {
            return false;
        }
        if (to != other.to) {
            return false;
        }
        return true;
    }

}
