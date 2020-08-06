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
