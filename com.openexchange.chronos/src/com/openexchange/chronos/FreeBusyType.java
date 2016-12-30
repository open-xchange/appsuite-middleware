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

package com.openexchange.chronos;

import com.openexchange.java.Enums;

/**
 * {@link FreeBusyType}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.2.9">RFC 5545, section 3.2.9</a>
 */
public enum FreeBusyType implements FbType {

    /**
     * The time interval is free for scheduling.
     */
    FREE(FbType.FREE, 0),

    /**
     * The time interval is busy because one or more events have been scheduled for that interval.
     */
    BUSY(FbType.BUSY, 3),

    /**
     * The time interval is busy and that the interval can not be scheduled.
     */
    BUSY_UNAVAILABLE(FbType.BUSY_UNAVAILABLE, 4),

    /**
     * The time interval is busy because one or more events have been tentatively scheduled for that interval.
     */
    BUSY_TENTATIVE(FbType.BUSY_TENTATIVE, 2),

    ;

    private final String value;
    private final int order;

    /**
     * Initializes a new {@link FreeBusyType}.
     *
     * @param value The free or busy time type value
     * @param order The order of the type for sorting
     */
    private FreeBusyType(String value, int order) {
        this.value = value;
        this.order = order;
    }

    @Override
    public String getValue() {
        return value;
    }

    public boolean equals(FbType other) {
        return 0 == compareTo(other);
    }

    @Override
    public int compareTo(FbType other) {
        if (null == other) {
            return 1;
        }
        if (FreeBusyType.class.isInstance(other)) {
            return Integer.compare(order, ((FreeBusyType) other).order);
        }
        FreeBusyType parsedFbType = Enums.parse(FreeBusyType.class, other.getValue(), null);
        return Integer.compare(order, null == parsedFbType ? 1 : parsedFbType.order);
    }

}
