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

package com.openexchange.caldav.mixins;

import java.util.Calendar;
import java.util.Date;
import org.jdom2.Namespace;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.Tools;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;


/**
 * {@link MaxDateTime}
 *
 * Provides a DATE-TIME value indicating the latest date and
 * time (in UTC) that the server is willing to accept for any DATE or
 * DATE-TIME value in a calendar object resource stored in a calendar
 * collection.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class MaxDateTime extends SingleXMLPropertyMixin {

    public static final String PROPERTY_NAME = "max-date-time";
    public static final Namespace NAMESPACE = CaldavProtocol.CAL_NS;

    private final GroupwareCaldavFactory factory;

    private Date maxDateTime;

    /**
     * Initializes a new {@link MaxDateTime}.
     *
     * @param factory The underlying CalDAV factory
     */
    public MaxDateTime(GroupwareCaldavFactory factory) {
        super(NAMESPACE.getURI(), PROPERTY_NAME);
        this.factory = factory;
    }

    @Override
    protected String getValue() {
        Date maxDateTime = getMaxDateTime();
        return null != maxDateTime ? Tools.formatAsUTC(maxDateTime) : null;
    }

    /**
     * Gets the end time of the configured synchronization timeframe for CalDAV.
     *
     * @return The end of the configured synchronization interval
     */
    public Date getMaxDateTime() {
        if (null == maxDateTime) {
            String value = null;
            try {
                value = factory.getConfigValue("com.openexchange.caldav.interval.end", "0");
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(MaxDateTime.class).warn("falling back to '0' as interval end", e);
                value = "0";
            }
            /*
             * try numerical value
             */
            try {
                int days = Integer.parseInt(value);
                if (0 >= days) {
                    return null;
                }
                Calendar calendar = CalendarUtils.initCalendar(TimeZones.UTC, null);
                calendar.add(Calendar.DATE, days);
                maxDateTime = CalendarUtils.truncateTime(calendar).getTime();
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                /*
                 * no numerical value, fall back to static constants, otherwise
                 */
                Calendar calendar = CalendarUtils.initCalendar(TimeZones.UTC, null);
                calendar.add(Calendar.YEAR, "two_years".equals(value) ? 3 : 2);
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                maxDateTime = CalendarUtils.truncateTime(calendar).getTime();
            }
        }
        return maxDateTime;
    }

}
