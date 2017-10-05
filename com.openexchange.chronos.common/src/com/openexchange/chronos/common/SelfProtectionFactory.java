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

package com.openexchange.chronos.common;

import java.util.Collection;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link SelfProtectionFactory}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class SelfProtectionFactory {

    private static final String PROPERTY_EVENT_LIMIT = "com.openexchange.chronos.maxEventResults";
    private static final String PROPERTY_ATTENDEE_LIMIT = "com.openexchange.chronos.maxAttendeesPerEvent";
    private static final String PROPERTY_ALARM_LIMIT = "com.openexchange.chronos.maxAlarmsPerEvent";
    private final LeanConfigurationService leanService;

    /**
     * Initializes a new {@link SelfProtectionFactory}.
     */
    public SelfProtectionFactory(LeanConfigurationService leanConfigurationService) {
        super();
        this.leanService = leanConfigurationService;
    }

    public SelfProtection createSelfProtection(Session session){
        return new SelfProtection(leanService, session);
    }

    public class SelfProtection {

        private final int eventLimit;
        private final int attendeeLimit;
        private final int alarmLimit;

        /**
         * Initializes a new {@link SelfProtectionFactory.SelfProtection}.
         */
        public SelfProtection(LeanConfigurationService leanConfigurationService, Session session) {
            super();
            Property prop = DefaultProperty.valueOf(PROPERTY_EVENT_LIMIT, 1000);
            eventLimit = leanConfigurationService.getIntProperty(session.getUserId(), session.getContextId(), prop);

            prop = DefaultProperty.valueOf(PROPERTY_ATTENDEE_LIMIT, 1000);
            attendeeLimit = leanConfigurationService.getIntProperty(session.getUserId(), session.getContextId(), prop);

            prop = DefaultProperty.valueOf(PROPERTY_ALARM_LIMIT, 100);
            alarmLimit = leanConfigurationService.getIntProperty(session.getUserId(), session.getContextId(), prop);

        }

        public void checkEventCollection(Collection<Event> collection) throws OXException {

            if (collection.size() > eventLimit) {
                throw CalendarExceptionCodes.TOO_MANY_EVENT_RESULTS.create();
            }
        }

        public void checkEvent(Event event) throws OXException {
            if (event.getAttendees() != null && event.getAttendees().size() > attendeeLimit) {
                throw CalendarExceptionCodes.TOO_MANY_ATTENDEES.create();
            }

            if (event.getAlarms() != null && event.getAlarms().size() > alarmLimit) {
                throw CalendarExceptionCodes.TOO_MANY_ALARMS.create();
            }
        }

    }

}
