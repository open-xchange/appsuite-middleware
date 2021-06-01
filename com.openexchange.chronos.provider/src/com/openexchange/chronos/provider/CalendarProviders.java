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

    /** The identifier of the provider for cross-context shares */
    public static final String ID_XCTX = "xctx2"; // from com.openexchange.groupware.modules.Module.CALENDAR

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
     * Gets the name of the property that is used to evaluate whether a calendar provider is enabled or not.
     *
     * @param provider The calendar provider id to generate the <i>enabled</i> property name for
     * @return The <i>enabled</i> property name
     */
    public static String getEnabledPropertyName(String provider) {
        return "com.openexchange.calendar." + provider + ".enabled";
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
