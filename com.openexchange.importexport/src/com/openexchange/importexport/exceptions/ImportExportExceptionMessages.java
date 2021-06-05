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

package com.openexchange.importexport.exceptions;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ImportExportExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ImportExportExceptionMessages implements LocalizableStrings {

    public static final String CANNOT_EXPORT_MSG = "Could not export the folder %1$s.";

    public static final String LOADING_CONTACTS_FAILED_MSG = "Could not load contacts.";

    public static final String LOADING_FOLDER_FAILED_MSG = "Could not load folder %s.";

    public static final String ICAL_CONVERSION_FAILED_MSG = "Conversion to iCal failed.";

    public static final String VCARD_CONVERSION_FAILED_MSG = "Conversion to vCard failed.";

    public static final String CANNOT_IMPORT_MSG = "Cannot import the format %2$s into selected folder.";

    public static final String CALENDAR_DISABLED_MSG = "Calendar module is not enabled. Appointments can not be imported.";

    public static final String TASKS_DISABLED_MSG = "Tasks module is not enabled. Tasks cannot be imported.";

    public static final String RESOURCE_HARD_CONFLICT_MSG = "Importing the appointment failed due to a conflicting resource.";

    public static final String WARNINGS_MSG = "Warnings when importing file.";

    public static final String CONTACTS_DISABLED_MSG = "Contacts module is not enabled. Contacts cannot be imported.";

    public static final String NO_VCARD_FOUND_MSG = "No vCard to import found.";

    public static final String VCARD_PARSING_PROBLEM_MSG = "Problem while parsing the vcard. Reason: %s";

    public static final String VCARD_CONVERSION_PROBLEM_MSG = "Problem while converting the vcard to a contact. Reason: %s";

    public static final String ONLY_ONE_FOLDER_MSG = "Can only import into one folder at a time.";

    public static final String NOT_FOUND_FIELD_MSG = "Could not find the following fields %s";

    public static final String NO_VALID_CSV_COLUMNS_MSG = "Could not translate a single column title. Is this a valid CSV file?";

    public static final String NO_FIELD_IMPORTED_MSG = "Could not translate a single field of information. Entry %s has not been inserted.";

    public static final String NO_FIELD_FOR_NAMING_MSG = "File does not contain fields for assigning contact names: no name, company or E-Mail.";

    public static final String NO_FIELD_FOR_NAMING_IN_LINE_MSG = "No field was set that might give the contact in line %s a display name: no name, company or E-Mail.";

    public static final String ONLY_ONE_FILE_MSG = "Can only handle one file, not %s";

    public static final String UNKNOWN_FORMAT_MSG = "Unknown format: %s";

    public static final String EMPTY_FILE_MSG = "Empty file uploaded.";

    public static final String FILE_NOT_EXISTS_MSG = "The file you selected does not exist.";

    //  Ignoring invalid value for field "%1$s": %2$s
    public static final String IGNORE_FIELD_MSG = "Ignoring invalid value for field \"%1$s\": %2$s";

	public static final String NO_FILE_UPLOADED_MSG = "No file was uploaded";

	// Truncation error: Not using the standard one, because we cannot map between field name in .ics/.vcd/.csv file and OX field.
	public static final String TRUNCATION_MSG = "Could not import an entry because one or more fields are too big for the database: %s";

    // Pretty generic: Could not one element in an import
	public static final String COULD_NOT_CREATE_MSG = "Could not create the following element: %s";

	public static final String NEED_FOLDER_MSG = "Missing parameter for folder";

	public static final String IRREGULAR_COLUMN_ID_MSG ="Columns should be numbers, could not convert %s into number";

	public static final String NO_CONTENT_MSG = "The uploaded file did not contain any content";

	// Something went wrong reading from specified file. Please try again.
    public static final String IOEXCEPTION_RETRY_MSG = "Something went wrong reading from specified file. Please try again.";

    // Could not create: %s
    public static final String COULD_NOT_CREATE_EXT_MSG = "Could not create: %s";

    // Could not create: %s
    public static final String LIMIT_EXCEEDED_MSG = "Import limit exceeded. Only imported the first %1$s contacts";

    // The character encoding \"%1$s\" is not supported. Please choose another one.
    public static final String UNSUPPORTED_CHARACTER_ENCODING_MSG = "The character encoding \"%1$s\" is not supported. Please choose another one.";

    // The contact with uid %1$s is too similar to the existing contact with uid %2$s in folder %3$s
    public static final String CONTACT_TOO_SIMILAR = "The contact with uid %1$s is too similar to the existing contact with uid %2$s in folder %3$s";

    public static final String COULD_NOT_CREATE_FILE_NAME = "Could not create a proper name for the export file.";

    public static final String TRUNCATED_RESULTS_MSG = "Not all of the objects could be imported due to a configured limitation";

    public static final String WARNINGS_AND_TRUNCATED_RESULTS_MSG = "%1$sd warnings when importing file and not all of the objects could be imported due to a configured limitation";
    
    public static final String COULD_NOT_PARSE_JSON_BODY_MSG = "Could not parse json body parameters for folders and objects.";

    private ImportExportExceptionMessages() {
        super();
    }
}
