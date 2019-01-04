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

package com.openexchange.chronos.provider;

/**
 * {@link CalendarProviders}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarProviders {

    /** The identifier for the iCal calendar provider */
    public static final String ID_ICAL = "ical";

    /** The identifier for the schedjoules calendar provider */
    public static final String ID_SCHEDJOULES = "schedjoules";

    /** The identifier for the birthdays calendar provider */
    public static final String ID_BIRTHDAYS = "birthdays";

    /** The identifier for the internal chronos calendar provider */
    public static final String ID_CHRONOS = "chronos";

    /**
     * Gets the name of the declared capability for a specific calendar provider.
     *
     * @param provider The calendar provider to generate the capability name for
     * @return The capability name
     */
    public static String getCapabilityName(CalendarProvider provider) {
        return "calendar_" + provider.getId();
    }

    /**
     * Gets the name of the declared capability for a specific calendar provider.
     *
     * @param providerId The identifier of the calendar provider to generate the capability name for
     * @return The capability name
     */
    public static String getCapabilityName(String providerId) {
        return "calendar_" + providerId;
    }

    /**
     * Gets the name of the property that is used to evaluate whether a calendar provider is enabled or not.
     *
     * @param provider The calendar provider to generate the <i>enabled</i> property name for
     * @return The <i>enabled</i> property name
     */
    public static String getEnabledPropertyName(CalendarProvider provider) {
        return "com.openexchange.calendar." + provider.getId() + ".enabled";
    }

    /**
     * Gets the name of the property that is used to restrict the maximum number of accounts of a specific provider.
     *
     * @param provider The calendar provider to generate the <i>maxAccounts</i> property name for
     * @return The <i>maxAccounts</i> property name
     */
    public static String getMaxAccountsPropertyName(CalendarProvider provider) {
        return "com.openexchange.calendar." + provider.getId() + ".maxAccounts";
    }

    /**
     * Gets the name of the property that is used to enable or disable a calendar provider for synchronization.
     *
     * @param provider The identifier of the calendar provider to generate the <i>usedForSync</i> property name for
     * @return The <i>usedForSync</i> property name
     */
    public static String getUsedForSyncPropertyName(String providerId) {
        return "com.openexchange.calendar." + providerId + ".usedForSync";
    }

    /**
     * Initializes a new {@link CalendarProviders}.
     */
    private CalendarProviders() {
        super();
    }

}
