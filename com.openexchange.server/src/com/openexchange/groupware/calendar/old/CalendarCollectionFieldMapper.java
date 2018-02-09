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

package com.openexchange.groupware.calendar.old;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;

/**
 * {@link CalendarCollectionFieldMapper}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class CalendarCollectionFieldMapper {

    private static final Map<Integer, String> fieldMap = new HashMap<Integer, String>(24);
    static {
        fieldMap.put(Integer.valueOf(CalendarObject.TITLE), "field01");

        fieldMap.put(Integer.valueOf(Appointment.LOCATION), "field02");
        fieldMap.put(Integer.valueOf(CalendarObject.NOTE), "field04");
        fieldMap.put(Integer.valueOf(CalendarObject.RECURRENCE_TYPE), "field06");
        fieldMap.put(Integer.valueOf(CalendarObject.DELETE_EXCEPTIONS), "field07");
        fieldMap.put(Integer.valueOf(CalendarObject.CHANGE_EXCEPTIONS), "field08");
        fieldMap.put(Integer.valueOf(CommonObject.CATEGORIES), "field09");

        fieldMap.put(Integer.valueOf(CalendarObject.START_DATE), "timestampfield01");
        fieldMap.put(Integer.valueOf(CalendarObject.END_DATE), "timestampfield02");

        fieldMap.put(Integer.valueOf(DataObject.OBJECT_ID), "intfield01");
        fieldMap.put(Integer.valueOf(CalendarObject.RECURRENCE_ID), "intfield02");
        fieldMap.put(Integer.valueOf(CommonObject.COLOR_LABEL), "intfield03");
        fieldMap.put(Integer.valueOf(CalendarObject.RECURRENCE_CALCULATOR), "intfield04");
        fieldMap.put(Integer.valueOf(CalendarObject.RECURRENCE_POSITION), "intfield05");
        fieldMap.put(Integer.valueOf(Appointment.SHOWN_AS), "intfield06");
        fieldMap.put(Integer.valueOf(Appointment.FULL_TIME), "intfield07");
        fieldMap.put(Integer.valueOf(CommonObject.NUMBER_OF_ATTACHMENTS), "intfield08");
        fieldMap.put(Integer.valueOf(CommonObject.PRIVATE_FLAG), "pflag");

        fieldMap.put(Integer.valueOf(DataObject.CREATED_BY), "pd.created_from");
        fieldMap.put(Integer.valueOf(DataObject.MODIFIED_BY), "pd.changed_from");
        fieldMap.put(Integer.valueOf(DataObject.CREATION_DATE), "pd.creating_date");
        fieldMap.put(Integer.valueOf(DataObject.LAST_MODIFIED), "pd.changing_date");

        fieldMap.put(Integer.valueOf(FolderChildObject.FOLDER_ID), "fid");
        fieldMap.put(Integer.valueOf(Appointment.TIMEZONE), "timezone");

        fieldMap.put(Integer.valueOf(CalendarObject.ORGANIZER), "organizer");
        fieldMap.put(Integer.valueOf(CommonObject.UID), "uid");
        fieldMap.put(Integer.valueOf(CalendarObject.SEQUENCE), "sequence");
        fieldMap.put(Integer.valueOf(CalendarObject.ORGANIZER_ID), "organizerId");
        fieldMap.put(Integer.valueOf(CalendarObject.PRINCIPAL), "principal");
        fieldMap.put(Integer.valueOf(CalendarObject.PRINCIPAL_ID), "principalId");
        fieldMap.put(Integer.valueOf(CommonObject.FILENAME), "filename");
    }

    public static String getFieldName(final int fieldId) {
        return fieldMap.get(Integer.valueOf(fieldId));
    }

}
