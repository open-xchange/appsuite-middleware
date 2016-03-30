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

package com.openexchange.mail;

/**
 * {@link IndexRange} - A simple class representing an index range.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IndexRange {

    /**
     * The <code>null</code> index range
     */
    public static final IndexRange NULL = null;

    /**
     * The start index
     */
    public final int start;

    /**
     * The end index
     */
    public final int end;

    /**
     * Initializes a new {@link IndexRange}
     */
    public IndexRange(final int start, final int end) {
        super();
        if (start < 0) {
            throw new IllegalArgumentException("start index is less than zero");
        } else if (end < 0) {
            throw new IllegalArgumentException("end index is less than zero");
        } else if (end < start) {
            throw new IllegalArgumentException("end index is less than start index");
        }
        this.start = start;
        this.end = end;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + end;
        result = prime * result + start;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexRange other = (IndexRange) obj;
        if (end != other.end) {
            return false;
        }
        if (start != other.start) {
            return false;
        }
        return true;
    }

    /**
     * Gets the start index
     *
     * @return The start index
     */
    public int getStart() {
        return start;
    }

    /**
     * Gets the end index
     *
     * @return The end index
     */
    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString()).append(" start=").append(start).append(", end=").append(end).toString();
    }
}
