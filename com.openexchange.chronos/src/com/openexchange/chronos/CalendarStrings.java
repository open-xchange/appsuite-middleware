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

package com.openexchange.chronos;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link CalendarStrings}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarStrings implements LocalizableStrings {

    /** The summary that is inserted for <i>private</i> events the requesting user has no access to */
    public static final String SUMMARY_PRIVATE = "Private";

    /** The displayed name for the {@link EventField#SUMMARY} property of an event */
    public static final String FIELD_SUMMARY = "Subject";

    /** The displayed name for the {@link EventField#LOCATION} property of an event */
    public static final String FIELD_LOCATION = "Location";

    /** The displayed name for the {@link EventField#START_DATE} property of an event */
    public static final String FIELD_START_DATE = "Starts on";

    /** The displayed name for the {@link EventField#END_DATE} property of an event */
    public static final String FIELD_END_DATE = "Ends on";

    /** The displayed name for the {@link EventField#RECURRENCE_RULE} property of an event */
    public static final String FIELD_RECURRENCE_RULE = "Repeat";

    /** The displayed name for the {@link EventField#DESCRIPTION} property of an event */
    public static final String FIELD_DESCRIPTION = "Description";

    /** The displayed name for the {@link EventField#ATTENDEES} property of an event */
    public static final String FIELD_ATTENDEES = "Participants";

    /** The displayed name for the {@link EventField#ALARMS} property of an event */
    public static final String FIELD_ALARMS = "Reminder";

    /** The displayed name for the {@link EventField#CLASSIFICATION} property of an event */
    public static final String FIELD_CLASSIFICATION = "Visibility";

    /** The displayed name for the {@link EventField#COLOR} property of an event */
    public static final String FIELD_COLOR = "Color";

    /** The displayed name for the {@link EventField#TRANSP} property of an event */
    public static final String FIELD_TRANSP = "Show as";

    /** The displayed name for the {@link EventField#ATTACHMENTS} property of an event */
    public static final String FIELD_ATTACHMENTS = "Attachments";

    /**
     * Prevent instantiation.
     */
    private CalendarStrings() {
        super();
    }

}
