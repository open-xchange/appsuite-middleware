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
                value = factory.getConfigValue("com.openexchange.caldav.interval.end", "one_year");
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(MaxDateTime.class).warn("falling back to 'one_year' as interval end", e);
                value = "one_year";
            }
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, "two_years".equals(value) ? 3 : 2);
            calendar.set(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            maxDateTime = calendar.getTime();
        }
        return maxDateTime;
    }

}
