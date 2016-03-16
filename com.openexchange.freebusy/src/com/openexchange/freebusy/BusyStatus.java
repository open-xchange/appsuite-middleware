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

package com.openexchange.freebusy;

/**
 * {@link BusyStatus}
 *
 * Enumeration of the possible "shown as" types for a free/busy time.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public enum BusyStatus {

    /**
     * No data
     */
    UNKNOWN(0),

    /**
     * Free / 4 / <code>Appointment.FREE</code>
     */
    FREE(4),

    /**
     * Tentative / 2 /<code>Appointment.TEMPORARY</code>
     */
    TEMPORARY(2),

    /**
     * Busy / 1 / <code>Appointment.RESERVED</code>
     */
    RESERVED(1),

    /**
     * Out of Office (OOF) / 3 / <code>Appointment.ABSENT</code>
     */
    ABSENT(3),
    ;


    /**
     * Gets the busy status value of the supplied 'shown as' constant.
     *
     * @param shownAs The shown as
     * @return The busy status
     */
    public static BusyStatus valueOf(int shownAs) {
        switch (shownAs) {
        case 1:
            return BusyStatus.RESERVED;
        case 2:
            return BusyStatus.TEMPORARY;
        case 3:
            return BusyStatus.ABSENT;
        case 4:
            return BusyStatus.FREE;
        default:
            return BusyStatus.UNKNOWN;
        }
    }

    /**
     * Gets the underlying "shown as" value as defined in {@link Appointment}.
     *
     * @return The value
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets a value indicating whether this busy status is more conflicting than another one, i.e. it denotes a 'more busy' state
     * (in the order: <code>ABSENT > RESERVED > TEMPORARY > FREE > UNKNOWN</code>).
     *
     * @param other The busy status to compare
     * @return <code>true</code>, if this status is more conflicting, <code>false</code>, otherwise
     */
    public boolean isMoreConflicting(BusyStatus other) {
        return 0 < this.compareTo(other);
    }

    private final int value;

    private BusyStatus(int value) {
        this.value = value;
    }

}
