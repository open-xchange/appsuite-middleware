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

package com.openexchange.data.conversion.ical;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ConversionWarningMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class ConversionWarningMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link ConversionWarningMessage}.
     */
    private ConversionWarningMessage() {
        super();
    }

    // Unable to convert task status "%1$s".
    public final static String INVALID_STATUS_MSG = "Unable to convert task status \"%1$s\".";

    // Unable to convert task priority %1$d.
    public final static String INVALID_PRIORITY_MSG = "Unable to convert task priority %d.";

    // Parsing error parsing ical: %s
    public final static String PARSE_EXCEPTION_MSG = "Parsing error parsing ical: %s";

    // Unknown Class: %1$s
    public final static String UNKNOWN_CLASS_MSG = "Unknown Class: %1$s";

    // Cowardly refusing to convert confidential classified objects.
    public final static String CLASS_CONFIDENTIAL_MSG = "Cowardly refusing to convert confidential classified objects.";

    // Missing DTStart in appointment
    public final static String MISSING_DTSTART_MSG = "Missing DTSTART";

    // Private Appointments can not have attendees. Removing attendees and accepting appointment anyway.
    public final static String PRIVATE_APPOINTMENTS_HAVE_NO_PARTICIPANTS_MSG = "Private appointments can not have attendees. Removing attendees and accepting appointment anyway.";

    // Not supported recurrence pattern: BYMONTH
    public final static String BYMONTH_NOT_SUPPORTED_MSG = "Not supported recurrence pattern: BYMONTH";

    // This does not look like an iCal file. Please check the file.
    public final static String DOES_NOT_LOOK_LIKE_ICAL_FILE_MSG = "This does not look like an iCal file. Please check the file.";

    // Empty "CLASS" element.
    public final static String EMPTY_CLASS_MSG = "Empty \"CLASS\" element.";

    public final static String TRUNCATION_WARNING_MSG = "Element truncated: %s";

    public final static String INVALID_MAIL_ADDRESS_MSG = "Invalid mail address for external participant: %1$s";

    public final static String NO_FOLDER_FOR_APPOINTMENTS = "The conversion yields some objects which could not be stored due to missing folder for appointments.";

    public final static String NO_FOLDER_FOR_TASKS = "The conversion yields some objects which could not be stored due to missing folder for tasks.";

    public static final String TRUNCATED_ITEMS = "The object could not be stored due to a configured limitation";

}
