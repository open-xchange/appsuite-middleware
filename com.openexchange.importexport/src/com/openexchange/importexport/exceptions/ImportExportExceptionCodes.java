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

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.groupware.EnumComponent;


/**
 * {@link ImportExportExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum ImportExportExceptionCodes implements DisplayableOXExceptionCode {

    /** Could not export the folder %1$s in the format %2$s. */
    CANNOT_EXPORT("Could not export the folder %1$s in the format %2$s.", CATEGORY_PERMISSION_DENIED, 100,
        ImportExportExceptionMessages.CANNOT_EXPORT_MSG),
    /** Could not load contacts */
    LOADING_CONTACTS_FAILED("Could not load contacts", CATEGORY_SERVICE_DOWN, 102, ImportExportExceptionMessages.LOADING_CONTACTS_FAILED_MSG),
    /** Could not encode as UTF-8 */
    UTF8_ENCODE_FAILED("Could not encode as UTF-8", CATEGORY_ERROR, 104, null),
    /** Can not get connection to database. */
    NO_DATABASE_CONNECTION("Can not get connection to database.", CATEGORY_SERVICE_DOWN, 110, OXExceptionStrings.SQL_ERROR_MSG),
    /** Invalid SQL Query: %s */
    SQL_PROBLEM("Invalid SQL Query: %s", CATEGORY_ERROR, 200, OXExceptionStrings.SQL_ERROR_MSG),
    /** Could not load folder %s */
    LOADING_FOLDER_FAILED("Could not load folder %s", CATEGORY_ERROR, 204, ImportExportExceptionMessages.LOADING_FOLDER_FAILED_MSG),
    /** The necessary iCal emitter serivce is missing. */
    ICAL_EMITTER_SERVICE_MISSING("The necessary iCal emitter service is missing.", CATEGORY_ERROR, 206, null),
    /** Parsing %1$s to a number failed. */
    NUMBER_FAILED("Parsing %1$s to a number failed.", CATEGORY_ERROR, 207, null),
    /** Conversion to iCal failed. */
    ICAL_CONVERSION_FAILED("Conversion to iCal failed.", CATEGORY_ERROR, 208, ImportExportExceptionMessages.ICAL_CONVERSION_FAILED_MSG),
    /** Conversion to vCard failed. */
    VCARD_CONVERSION_FAILED("Conversion to vCard failed.", CATEGORY_ERROR, 304, ImportExportExceptionMessages.VCARD_CONVERSION_FAILED_MSG),
    /** Can not import the format %2$s into folder %1$s. */
    CANNOT_IMPORT("Can not import the format %2$s into folder %1$s.", CATEGORY_ERROR, 500,
        ImportExportExceptionMessages.CANNOT_IMPORT_MSG),
    /** Module calendar not enabled for user, cannot import appointments. */
    CALENDAR_DISABLED("Calendar module is not enabled for the user. Appointments can not be imported.", CATEGORY_PERMISSION_DENIED, 507,
        ImportExportExceptionMessages.CALENDAR_DISABLED_MSG),
    /** Module tasks not enabled for user, cannot import tasks. */
    TASKS_DISABLED("Tasks module is not enabled for the user. Tasks cannot be imported.", CATEGORY_PERMISSION_DENIED, 508,
        ImportExportExceptionMessages.TASKS_DISABLED_MSG),
    /** The necessary iCal parser service is missing. */
    ICAL_PARSER_SERVICE_MISSING("The necessary iCal parser service is missing.", CATEGORY_ERROR, 512, null),
    /** Failed importing appointment due to hard conflicting resource. */
    RESOURCE_HARD_CONFLICT("Importing the appointment failed due to a conflicting resource.", CATEGORY_USER_INPUT, 513,
        ImportExportExceptionMessages.RESOURCE_HARD_CONFLICT_MSG),
    /** Warnings when importing file: %d warnings */
    WARNINGS("Warnings when importing file: %d warnings", CATEGORY_WARNING, 514, ImportExportExceptionMessages.WARNINGS_MSG),
    /** Could not recognize format of the following data: %s */
    UNKNOWN_VCARD_FORMAT("Could not recognize format of the following data: %s", CATEGORY_USER_INPUT, 605,
        "Unknown format: %s"),
    /** Module contacts not enabled for user, cannot import contacts. */
    CONTACTS_DISABLED("Contacts module is not enabled for the user. Contacts cannot be imported.", CATEGORY_PERMISSION_DENIED, 607,
        ImportExportExceptionMessages.CONTACTS_DISABLED_MSG),
    /** No VCard to import found. */
    NO_VCARD_FOUND("No vCard to import found.", CATEGORY_USER_INPUT, 608, ImportExportExceptionMessages.NO_VCARD_FOUND_MSG),
    /** Problem while parsing the vcard, reason: %s */
    VCARD_PARSING_PROBLEM("Problem while parsing the vcard. Reason: %s", CATEGORY_USER_INPUT, 609,
        ImportExportExceptionMessages.VCARD_PARSING_PROBLEM_MSG),
    /** Problem while converting the vcard to a contact, reason: %s */
    VCARD_CONVERSION_PROBLEM("Problem while converting the vcard to a contact. Reason: %s", CATEGORY_USER_INPUT, 610,
        ImportExportExceptionMessages.VCARD_CONVERSION_FAILED_MSG),
    /** Can only import into one folder at a time. */
    ONLY_ONE_FOLDER("Can only import into one folder at a time.", CATEGORY_USER_INPUT, 800,
        ImportExportExceptionMessages.ONLY_ONE_FOLDER_MSG),
    /** Could not find the following fields %s */
    NOT_FOUND_FIELD("Could not find the following fields %s", CATEGORY_WARNING, 803, ImportExportExceptionMessages.NOT_FOUND_FIELD_MSG),
    /** Could not translate a single column title. Is this a valid CSV file? */
    NO_VALID_CSV_COLUMNS("Could not translate a single column title. Is this a valid CSV file?", CATEGORY_USER_INPUT, 804,
        ImportExportExceptionMessages.NO_VALID_CSV_COLUMNS_MSG),
    /** Could not translate a single field of information, did not insert entry %s. */
    NO_FIELD_IMPORTED("Could not translate a single field of information. Entry %s has not been inserted.", CATEGORY_USER_INPUT, 805,
        ImportExportExceptionMessages.NO_FIELD_IMPORTED_MSG),
    /** No field can be found that could be used to name contacts in this file: no name, no company nor e-mail. */
    NO_FIELD_FOR_NAMING("File does not contain fields for assigning contact names: no name, company or E-Mail.", CATEGORY_USER_INPUT, 807,
        ImportExportExceptionMessages.NO_FIELD_FOR_NAMING_MSG),
    /** No field was set that might give the contact in line %s a display name: no name, no company nor e-mail. */
    NO_FIELD_FOR_NAMING_IN_LINE("No field was set that might give the contact in line %s a display name: no name, company or E-Mail.",
        CATEGORY_USER_INPUT, 808, ImportExportExceptionMessages.NO_FIELD_FOR_NAMING_IN_LINE_MSG),
    /** Could not read InputStream as string */
    IOEXCEPTION("Could not read InputStream as string", CATEGORY_ERROR, 902, null),
    /** Cannot find an importer for format %s into folders %s */
    NO_IMPORTER("Cannot find an importer for format %s into folders %s", CATEGORY_SERVICE_DOWN, 1100, null),
    /** Cannot find an exporter for folder %s to format %s */
    NO_EXPORTER("Cannot find an exporter for folder %s to format %s", CATEGORY_SERVICE_DOWN, 1101, null),
    /** Cannot translate id=%d to a constant from Types. */
    NO_TYPES_CONSTANT("Cannot translate id=%d to a constant from Types.", CATEGORY_ERROR, 1200, null),
    /** Cannot translate id=%d to a constant from FolderObject. */
    NO_FOLDEROBJECT_CONSTANT("Cannot translate id=%d to a constant from FolderObject.", CATEGORY_ERROR, 1201, null),
    /** Can only handle one file, not %s */
    ONLY_ONE_FILE("Can only handle one file, not %s", CATEGORY_USER_INPUT, 1300, ImportExportExceptionMessages.ONLY_ONE_FILE_MSG),
    /** Unknown format: %s */
    UNKNOWN_FORMAT("Unknown format: %s", CATEGORY_USER_INPUT, 1301, ImportExportExceptionMessages.UNKNOWN_FORMAT_MSG),
    /** Empty file uploaded. */
    EMPTY_FILE("Empty file uploaded.", CATEGORY_USER_INPUT, 1303, ImportExportExceptionMessages.EMPTY_FILE_MSG),
    /** The file you selected does not exist. */
    FILE_NOT_EXISTS("The file you selected does not exist.", CATEGORY_USER_INPUT, 1304, ImportExportExceptionMessages.FILE_NOT_EXISTS_MSG),
    /** Ignoring invalid value for field "%1$s": %2$s  */
    IGNORE_FIELD("Ignoring invalid value for field \"%1$s\": %2$s", CATEGORY_WARNING, 1306,
        ImportExportExceptionMessages.IGNORE_FIELD_MSG),
    CONTACT_INTERFACE_MISSING("Could not load ContactInterface to write storage contacts.", Category.CATEGORY_ERROR, 1307, null),
    COULD_NOT_WRITE("Could not write entry into database.", Category.CATEGORY_ERROR, 1308, OXExceptionStrings.SQL_ERROR_MSG),
    NO_FILE_UPLOADED("No file was uploaded", Category.CATEGORY_USER_INPUT, 1309, ImportExportExceptionMessages.NO_FILE_UPLOADED_MSG),
    COULD_NOT_CREATE("Could not create the following element: %s", Category.CATEGORY_USER_INPUT, 1310,
        ImportExportExceptionMessages.COULD_NOT_CREATE_MSG),
    COULD_NOT_CREATE_EXT("Could not create: %s", Category.CATEGORY_USER_INPUT, 1310, ImportExportExceptionMessages.COULD_NOT_CREATE_EXT_MSG),
    TRUNCATION("Could not import an entry because one or more fields are too big for the database: %s", Category.CATEGORY_TRUNCATED, 1311,
        ImportExportExceptionMessages.TRUNCATION_MSG),
    TEMP_FILE_NOT_FOUND("Could not find the temp file needed for the conversion.", Category.CATEGORY_ERROR, 1312, null),
    NEED_FOLDER("Missing parameter for folder", Category.CATEGORY_USER_INPUT, 1313, ImportExportExceptionMessages.NEED_FOLDER_MSG),
    IRREGULAR_COLUMN_ID("Columns should be numbers, could not convert %s into number", Category.CATEGORY_USER_INPUT, 1314,
        ImportExportExceptionMessages.IRREGULAR_COLUMN_ID_MSG),
    NO_CONTENT("The uploaded file did not contain any content", Category.CATEGORY_USER_INPUT, 1315,
        ImportExportExceptionMessages.NO_CONTENT_MSG),
    /** Something went wrong reading from specified file. Please try again. */
    IOEXCEPTION_RETRY("Something went wrong reading from specified file. Please try again.", CATEGORY_TRY_AGAIN, 1316,
        ImportExportExceptionMessages.IOEXCEPTION_RETRY_MSG),
    LIMIT_EXCEEDED("Import limit exceeded. Only imported the first %1$s contacts", CATEGORY_WARNING, 1317,
        ImportExportExceptionMessages.LIMIT_EXCEEDED_MSG),
    /** The character encoding \"%1$s\" is not supported. Please choose another one. */
    UNSUPPORTED_CHARACTER_ENCODING("Unsupported charset: \"%1$s\"", CATEGORY_USER_INPUT, 1318, ImportExportExceptionMessages.UNSUPPORTED_CHARACTER_ENCODING_MSG),

    /** The contact with uid %1$s is too similar to the existing contact with uid %2$s in folder %3$s */
    CONTACT_TOO_SIMILAR("The contact with uid %1$s is too similar to the existing contact with uid %2$s in folder %3$s", CATEGORY_CONFLICT, 1319, ImportExportExceptionMessages.CONTACT_TOO_SIMILAR),

    ;

    private String message;
    private Category category;
    private int number;
    private String displayMessage;

    private ImportExportExceptionCodes(final String message, final Category category, final int number, String displayMessage) {
        this.message = message;
        this.category = category;
        this.number = number;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public String getPrefix() {
        return EnumComponent.IMPORT_EXPORT.getAbbreviation();
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
