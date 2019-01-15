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

package com.openexchange.chronos.scheduling.common;

import java.util.Locale;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.user.UserService;

/**
 * {@link Utils}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private Utils() {}

    /**
     * Gets a value indicating whether a calendar user represents an <i>internal</i> entity, an internal user, group or resource , or not.
     *
     * @param calendarUser The calendar user to check
     * @return <code>true</code> if the calendar user is internal, <code>false</code>, otherwise
     */
    public static boolean isInternalCalendarUser(CalendarUser calendarUser) {
        if (Attendee.class.isAssignableFrom(calendarUser.getClass())) {
            Attendee attendee = (Attendee) calendarUser;
            return CalendarUtils.isInternal(attendee);
        }
        return CalendarUtils.isInternal(calendarUser, CalendarUserType.INDIVIDUAL);
    }

    /**
     * Get the correct time zone for the recipient. If the recipient is not an internal user, the time zone of the
     * originator is used
     *
     * @param userService The {@link UserService} to get the time zone information from
     * @param contextId The context identifier
     * @param originator The originator as {@link CalendarUser}
     * @param recipient The recipient as {@link CalendarUser}
     * @return The {@link TimeZone}
     */
    public static TimeZone getTimeZone(UserService userService, int contextId, CalendarUser originator, CalendarUser recipient) {
        if (null == userService) {
            return TimeZone.getDefault();
        }
        try {
            if (CalendarUtils.isInternal(recipient, CalendarUserType.INDIVIDUAL)) {
                User user = userService.getUser(recipient.getEntity(), contextId);
                return TimeZone.getTimeZone(user.getTimeZone());
            }
            User user = userService.getUser(originator.getEntity(), contextId);
            return TimeZone.getTimeZone(user.getTimeZone());
        } catch (OXException e) {
            LOGGER.debug("Unable to retrtive user information", e);
        }
        return TimeZone.getDefault();
    }

    /**
     * Get the correct locale for the recipient. If the recipient is not an internal user, the locale of the
     * originator is used
     *
     * @param userService The {@link UserService} to get the locale information from
     * @param contextId The context identifier
     * @param originator The originator as {@link CalendarUser}
     * @param recipient The recipient as {@link CalendarUser}
     * @return The {@link Locale}
     */
    public static Locale getLocale(UserService userService, int contextId, CalendarUser originator, CalendarUser recipient) {
        if (null == userService) {
            return Locale.getDefault();
        }
        try {
            if (CalendarUtils.isInternal(recipient, CalendarUserType.INDIVIDUAL)) {
                User user = userService.getUser(recipient.getEntity(), contextId);
                return user.getLocale();
            }
            User user = userService.getUser(originator.getEntity(), contextId);
            return user.getLocale();
        } catch (OXException e) {
            LOGGER.debug("Unable to retrtive user information", e);
        }
        return Locale.getDefault();
    }

}
