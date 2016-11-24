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

package com.openexchange.caldav.mixins;

import java.util.Calendar;
import java.util.Date;
import org.jdom2.Namespace;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.Tools;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;


/**
 * {@link MinDateTime}
 *
 * Provides a DATE-TIME value indicating the earliest date and
 * time (in UTC) that the server is willing to accept for any DATE or
 * DATE-TIME value in a calendar object resource stored in a calendar
 * collection.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class MinDateTime extends SingleXMLPropertyMixin {

    public static final String PROPERTY_NAME = "min-date-time";
    public static final Namespace NAMESPACE = CaldavProtocol.CAL_NS;

    private final GroupwareCaldavFactory factory;

    private Date minDateTime;

    /**
     * Initializes a new {@link MinDateTime}.
     *
     * @param factory The underlying CalDAV factory
     */
    public MinDateTime(GroupwareCaldavFactory factory) {
        super(NAMESPACE.getURI(), PROPERTY_NAME);
        this.factory = factory;
    }

    @Override
    protected String getValue() {
        Date minDateTime = getMinDateTime();
        return null != minDateTime ? Tools.formatAsUTC(minDateTime) : null;
    }

    /**
     * Gets the start time of the configured synchronization timeframe for CalDAV.
     *
     * @return The start of the configured synchronization interval
     */
    public Date getMinDateTime() {
        if (null == minDateTime) {
            String value = null;
            try {
                value = factory.getConfigValue("com.openexchange.caldav.interval.start", "one_month");
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(MinDateTime.class).warn("falling back to 'one_month' as interval start", e);
                value = "one_month";
            }
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            if ("one_year".equals(value)) {
                calendar.add(Calendar.YEAR, -1);
                calendar.set(Calendar.DAY_OF_YEAR, 1);
            } else if ("six_months".equals(value)) {
                calendar.add(Calendar.MONTH, -6);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
            } else {
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
            }
            minDateTime = calendar.getTime();
        }
        return minDateTime;
    }

}
