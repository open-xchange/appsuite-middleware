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
