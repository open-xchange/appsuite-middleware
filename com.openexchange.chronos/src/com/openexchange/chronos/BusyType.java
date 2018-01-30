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

package com.openexchange.chronos;

/**
 * {@link BusyType}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @see <a href="https://tools.ietf.org/html/rfc7953#section-3.2">RFC 7953, section 3.2</a>
 */
public enum BusyType {

    BUSY_TENTATIVE("BUSY-TENTATIVE"),
    BUSY_UNAVAILABLE("BUSY-UNAVAILABLE"),
    BUSY("BUSY"),
    ;

    private final String value;

    /**
     * Initialises a new {@link BusyType}.
     */
    private BusyType(String value) {
        this.value = value;
    }

    /**
     * Gets the value
     *
     * @return The value
     */
    public String getValue() {
        return value;
    }

    /**
     * Parses the specified value to a {@link BusyType} enum item
     * 
     * @param value The value to parse
     * @return The {@link BusyType} that corresponds to that value
     * @throws IllegalArgumentException if the specified value does not map to any known
     *             {@link BusyType} enum item
     */
    public static BusyType parseFromString(String value) {
        for (BusyType bt : values()) {
            if (bt.getValue().equals(value)) {
                return bt;
            }
        }
        throw new IllegalArgumentException("Unknown BusyType for value '" + value + "'");
    }
}
