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

package com.openexchange.calendar.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.session.Session;

/**
 * Factory class for getting new Instances of the type AppointmentSQLInterface.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class AppointmentSqlFactory implements AppointmentSqlFactoryService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AppointmentSqlFactory.class);

    private final List<CalendarFeature> features = new ArrayList<CalendarFeature>();

    public AppointmentSQLInterface createAppointmentSqlOmittingFeatures(Session session, String...omitFeatures) {
        Set<String> skip = new HashSet<String>(Arrays.asList(omitFeatures));
        AppointmentSQLInterface calendarSql = new TransactionallyCachingCalendar(new CalendarSql(session));

        for (CalendarFeature feature : features) {
            if (!skip.contains(feature.getId())) {
                try {
					calendarSql = feature.wrap(calendarSql, session);
				} catch (OXException e) {
					LOG.error("", e);
				}
            }
        }

        return calendarSql;
    }

    public void addCalendarFeature(CalendarFeature feature) {
        features.add(feature);
    }

    /**
     * Facrory method for creating a new Instance.
     *
     * @param session
     * @return
     */
    @Override
    public AppointmentSQLInterface createAppointmentSql(Session session) {
        return createAppointmentSqlOmittingFeatures(session);
    }

}
