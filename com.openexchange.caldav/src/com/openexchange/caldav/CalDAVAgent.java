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

package com.openexchange.caldav;

/**
 * {@link CalDAVAgent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public enum CalDAVAgent {

    MAC_CALENDAR,
    IOS_CALENDAR,
    THUNDERBIRD_LIGHTNING,
    EM_CLIENT,
    SMOOTH_SYNC,
    WINDOWS_PHONE,
    WINDOWS,
    UNKNOWN
    ;

    /**
     * Parses a user-agent string from a HTTP request and tries to map it to a known CalDAV client.
     *
     * @param userAgent The user agent string to parse
     * @return The parsed CalDAV agent, or {@link CalDAVAgent#UNKNOWN} if not recognized or unknown
     */
    public static CalDAVAgent parse(String userAgent) {
        if (null != userAgent) {
            if (userAgent.contains("Lightning") && userAgent.contains("Thunderbird") && userAgent.contains("Mozilla")) {
                return THUNDERBIRD_LIGHTNING;
            }
            if (userAgent.contains("iOS") && userAgent.contains("dataaccessd")) {
                return IOS_CALENDAR;
            }
            if (userAgent.startsWith("MSFT-WP")) {
                return WINDOWS_PHONE;
            }
            if (userAgent.startsWith("MSFT-WIN")) {
                return WINDOWS;
            }
            if ((userAgent.contains("Mac OS") || userAgent.contains("Mac+OS")) &&
                (userAgent.contains("CalendarStore") || (userAgent.contains("CalendarAgent")))) {
                return MAC_CALENDAR;
            }
            if (userAgent.contains("eM Client")) {
                return EM_CLIENT;
            }
            if (userAgent.contains("SmoothSync")) {
                return SMOOTH_SYNC;
            }
        }
        return UNKNOWN;
    }

}
