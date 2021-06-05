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

package com.openexchange.ajax.writer;

import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.ReminderFields;
import com.openexchange.groupware.reminder.ReminderObject;

/**
 * {@link ReminderWriter} - Writer for reminder
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class ReminderWriter extends DataWriter {

	/**
	 * Initializes a new {@link ReminderWriter}
	 *
	 * @param timeZone
	 *            The user time zone
	 */
	public ReminderWriter(final TimeZone timeZone) {
		super(timeZone, null);
	}

	public void writeObject(final ReminderObject reminderObj, final JSONObject jsonObj) throws JSONException {
	    if (reminderObj.getModule()==1){
	        // Construct an artificial parameter for event reminder
	        long id = (long) reminderObj.getObjectId() << 32 | reminderObj.getTargetId() & 0xFFFFFFFFL;
	        writeParameter(DataFields.ID, id, jsonObj);
	    } else {
	        writeParameter(DataFields.ID, reminderObj.getObjectId(), jsonObj);
	    }
		writeParameter(DataFields.LAST_MODIFIED, reminderObj.getLastModified(), jsonObj);
		writeParameter(ReminderFields.TARGET_ID, reminderObj.getTargetId(), jsonObj);
		writeParameter(ReminderFields.FOLDER, reminderObj.getFolder(), jsonObj);
		writeParameter(ReminderFields.ALARM, reminderObj.getDate(), timeZone, jsonObj);
		writeParameter(ReminderFields.MODULE, reminderObj.getModule(), jsonObj);
		writeParameter(ReminderFields.USER_ID, reminderObj.getUser(), jsonObj);
		writeParameter(CalendarFields.RECURRENCE_POSITION, reminderObj.getRecurrencePosition(), jsonObj);
		writeParameter(ReminderFields.SERVER_TIME, new Date(), timeZone, jsonObj);
	}
}
