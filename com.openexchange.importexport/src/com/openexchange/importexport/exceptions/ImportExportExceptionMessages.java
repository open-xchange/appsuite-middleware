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

    public static final String CANNOT_IMPORT_MSG = "Can not import the format %2$s into folder %1$s.";

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

    private ImportExportExceptionMessages() {
        super();
    }
}
