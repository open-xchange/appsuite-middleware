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

package com.openexchange.webdav;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.config.lean.Property;

/**
 * {@link FreeBusyProperty}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.4
 */
public enum FreeBusyProperty implements Property {

    /**
     * Enables the internet free busy REST endpoint for public access.
     * Default: false
     */
    ENABLE_INTERNET_FREEBUSY("enableInternetFreeBusy", Boolean.FALSE),

    /**
     * Defines whether the free busy data is published for the free busy servlet or not.
     * Default: false
     */
    PUBLISH_INTERNET_FREEBUSY("publishInternetFreeBusy", Boolean.FALSE),

    /**
     * Defines the maximum time range into the past in weeks that can be requested by free busy servlet. Default value is 12 weeks into the past.
     * Default: 12
     */
    INTERNET_FREEBUSY_MAXIMUM_TIMERANGE_PAST("internetFreeBusyMaximumTimerangePast", I(12)),

    /**
     * Defines the maximum time range into the future in weeks that can be requested by free busy servlet. Default value is 26 weeks into the future.
     * Default: 26
     */
    INTERNET_FREEBUSY_MAXIMUM_TIMERANGE_FUTURE("internetFreeBusyMaximumTimerangeFuture", I(26));

    private Object defaultValue;
    private String suffix;
    private static final String PREFIX = "com.openexchange.calendar.";

    private FreeBusyProperty(String suffix, Object defaultValue) {
        this.defaultValue = defaultValue;
        this.suffix = suffix;
    }

    @Override
    public String getFQPropertyName() {
        return PREFIX + this.suffix;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}
