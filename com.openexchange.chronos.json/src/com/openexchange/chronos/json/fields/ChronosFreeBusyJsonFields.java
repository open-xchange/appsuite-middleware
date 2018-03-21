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

package com.openexchange.chronos.json.fields;

import com.openexchange.chronos.service.FreeBusyResult;

/**
 * {@link ChronosFreeBusyJsonFields} contains all fields which are used by the freeBusy action
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ChronosFreeBusyJsonFields {

    /**
     * The attendee of the {@link FreeBusyResult}
     */
    public static final String ATTENDEE = "attendee";
    /**
     * The free busy times of the attendee. See {@link FreeBusyResult#getFreeBusyTimes()}.
     */
    public static final String FREE_BUSY_TIME = "freeBusyTime";

    /**
     * The warnings of the attendee. See {@link FreeBusyResult#getWarnings()}.
     */
    public static final String WARNINGS = "warnings";

    public static final class FreeBusyTime {
        /**
         * The start time of the free-busy time-slot. See {@link com.openexchange.chronos.FreeBusyTime#getStartTime()}.
         */
        public static final String START_TIME = "startTime";
        /**
         * The end time of the free-busy time-slot. See {@link com.openexchange.chronos.FreeBusyTime#getEndTime()}.
         */
        public static final String END_TIME = "endTime";
        /**
         * The type of the free-busy time-slot. See {@link com.openexchange.chronos.FreeBusyTime#getFbType()}.
         */
        public static final String FB_TYPE = "fbType";
        /**
         * The event the free-busy time-slot corresponds to. See {@link com.openexchange.chronos.FreeBusyTime#getEvent()}.
         */
        public static final String EVENT = "event";
    }
}

