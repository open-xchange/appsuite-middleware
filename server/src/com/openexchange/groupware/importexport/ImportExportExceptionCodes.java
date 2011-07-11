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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.groupware.importexport;

import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.BROKEN_CSV_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.CALENDAR_DISABLED_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.CANNOT_EXPORT_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.CANNOT_IMPORT_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.CONTACTS_DISABLED_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.DATA_AFTER_LAST_LINE_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.EMPTY_FILE_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.FILE_NOT_EXISTS_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.ICAL_CONVERSION_FAILED_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.ICAL_EMITTER_SERVICE_MISSING_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.ICAL_PARSER_SERVICE_MISSING_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.IOEXCEPTION_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.LOADING_CONTACTS_FAILED_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.LOADING_FOLDER_FAILED_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.NOT_FOUND_FIELD_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.NO_DATABASE_CONNECTION_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.NO_EXPORTER_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.NO_FIELD_FOR_NAMING_IN_LINE_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.NO_FIELD_FOR_NAMING_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.NO_FIELD_IMPORTED_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.NO_FOLDEROBJECT_CONSTANT_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.NO_IMPORTER_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.NO_TYPES_CONSTANT_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.NO_VALID_CSV_COLUMNS_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.NO_VCARD_FOUND_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.NUMBER_FAILED_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.ONLY_ONE_FILE_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.ONLY_ONE_FOLDER_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.RESOURCE_HARD_CONFLICT_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.SQL_PROBLEM_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.TASKS_DISABLED_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.UNKNOWN_FORMAT_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.UNKNOWN_VCARD_FORMAT_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.UTF8_ENCODE_FAILED_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.VCARD_CONVERSION_FAILED_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.VCARD_CONVERSION_PROBLEM_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.VCARD_PARSING_PROBLEM_MSG;
import static com.openexchange.groupware.importexport.ImportExportExceptionMessages.WARNINGS_MSG;
import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.internal.ImportExportExceptionFactory;

/**
 * {@link ImportExportExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum ImportExportExceptionCodes implements OXErrorMessage {

    /** Could not export the folder %1$s in the format %2$s. */
    CANNOT_EXPORT(CANNOT_EXPORT_MSG, Category.PERMISSION, 100),
    /** Could not load contacts */
    LOADING_CONTACTS_FAILED(LOADING_CONTACTS_FAILED_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 102),
    /** Could not encode as UTF-8 */
    UTF8_ENCODE_FAILED(UTF8_ENCODE_FAILED_MSG, Category.CODE_ERROR, 104),
    /** Can not get connection to database. */
    NO_DATABASE_CONNECTION(NO_DATABASE_CONNECTION_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 110),
    /** Invalid SQL Query: %s */
    SQL_PROBLEM(SQL_PROBLEM_MSG, Category.CODE_ERROR, 200),
    /** Could not load folder %s */
    LOADING_FOLDER_FAILED(LOADING_FOLDER_FAILED_MSG, Category.CODE_ERROR, 204),
    /** The necessary iCal emitter serivce is missing. */
    ICAL_EMITTER_SERVICE_MISSING(ICAL_EMITTER_SERVICE_MISSING_MSG, Category.CODE_ERROR, 206),
    /** Parsing %1$s to a number failed. */
    NUMBER_FAILED(NUMBER_FAILED_MSG, Category.CODE_ERROR, 207),
    /** Conversion to iCal failed. */
    ICAL_CONVERSION_FAILED(ICAL_CONVERSION_FAILED_MSG, Category.CODE_ERROR, 208),
    /** Conversion to vCard failed. */
    VCARD_CONVERSION_FAILED(VCARD_CONVERSION_FAILED_MSG, Category.CODE_ERROR, 304),
    /** Can not import the format %2$s into folder %1$s. */
    CANNOT_IMPORT(CANNOT_IMPORT_MSG, Category.CODE_ERROR, 500),
    /** Module calendar not enabled for user, cannot import appointments. */
    CALENDAR_DISABLED(CALENDAR_DISABLED_MSG, Category.PERMISSION, 507),
    /** Module tasks not enabled for user, cannot import tasks. */
    TASKS_DISABLED(TASKS_DISABLED_MSG, Category.PERMISSION, 508),
    /** The necessary iCal parser service is missing. */
    ICAL_PARSER_SERVICE_MISSING(ICAL_PARSER_SERVICE_MISSING_MSG, Category.CODE_ERROR, 512),
    /** Failed importing appointment due to hard conflicting resource. */
    RESOURCE_HARD_CONFLICT(RESOURCE_HARD_CONFLICT_MSG, Category.USER_INPUT, 513),
    /** Warnings when importing file: %i warnings */
    WARNINGS(WARNINGS_MSG, Category.WARNING, 514),
    /** Could not recognize format of the following data: %s */
    UNKNOWN_VCARD_FORMAT(UNKNOWN_VCARD_FORMAT_MSG, Category.USER_INPUT, 605),
    /** Module contacts not enabled for user, cannot import contacts. */
    CONTACTS_DISABLED(CONTACTS_DISABLED_MSG, Category.PERMISSION, 607),
    /** No VCard to import found. */
    NO_VCARD_FOUND(NO_VCARD_FOUND_MSG, Category.USER_INPUT, 608),
    /** Problem while parsing the vcard, reason: %s */
    VCARD_PARSING_PROBLEM(VCARD_PARSING_PROBLEM_MSG, Category.USER_INPUT, 609),
    /** Problem while converting the vcard to a contact, reason: %s */
    VCARD_CONVERSION_PROBLEM(VCARD_CONVERSION_PROBLEM_MSG, Category.USER_INPUT, 610),
    /** Can only import into one folder at a time. */
    ONLY_ONE_FOLDER(ONLY_ONE_FOLDER_MSG, Category.USER_INPUT, 800),
    /** Could not find the following fields %s */
    NOT_FOUND_FIELD(NOT_FOUND_FIELD_MSG, Category.WARNING, 803),
    /** Could not translate a single column title. Is this a valid CSV file? */
    NO_VALID_CSV_COLUMNS(NO_VALID_CSV_COLUMNS_MSG, Category.USER_INPUT, 804),
    /** Could not translate a single field of information, did not insert entry %s. */
    NO_FIELD_IMPORTED(NO_FIELD_IMPORTED_MSG, Category.USER_INPUT, 805),
    /** No field can be found that could be used to name contacts in this file: no name, no company nor e-mail. */
    NO_FIELD_FOR_NAMING(NO_FIELD_FOR_NAMING_MSG, Category.USER_INPUT, 807),
    /** No field was set that might give the contact in line %s a display name: no name, no company nor e-mail. */
    NO_FIELD_FOR_NAMING_IN_LINE(NO_FIELD_FOR_NAMING_IN_LINE_MSG, Category.USER_INPUT, 808),
    /** Could not read InputStream as string */
    IOEXCEPTION(IOEXCEPTION_MSG, Category.CODE_ERROR, 902),
    /** Broken CSV file: Lines have different number of cells, line #1 has %d, line #%d has %d. Is this really a CSV file? */
    BROKEN_CSV(BROKEN_CSV_MSG, Category.USER_INPUT, 1000),
    /** Illegal state: Found data after presumed last line. */
    DATA_AFTER_LAST_LINE(DATA_AFTER_LAST_LINE_MSG, Category.CODE_ERROR, 1001),
    /** Cannot find an importer for format %s into folders %s */
    NO_IMPORTER(NO_IMPORTER_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 1100),
    /** Cannot find an exporter for folder %s to format %s */
    NO_EXPORTER(NO_EXPORTER_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 1101),
    /** Cannot translate id=%d to a constant from Types. */
    NO_TYPES_CONSTANT(NO_TYPES_CONSTANT_MSG, Category.CODE_ERROR, 1200),
    /** Cannot translate id=%d to a constant from FolderObject. */
    NO_FOLDEROBJECT_CONSTANT(NO_FOLDEROBJECT_CONSTANT_MSG, Category.CODE_ERROR, 1201),
    /** Can only handle one file, not %s */
    ONLY_ONE_FILE(ONLY_ONE_FILE_MSG, Category.USER_INPUT, 1300),
    /** Unknown format: %s */
    UNKNOWN_FORMAT(UNKNOWN_FORMAT_MSG, Category.USER_INPUT, 1301),
    /** Empty file uploaded. */
    EMPTY_FILE(EMPTY_FILE_MSG, Category.USER_INPUT, 1303),
    /** The file you selected does not exist. */
    FILE_NOT_EXISTS(FILE_NOT_EXISTS_MSG, Category.USER_INPUT, 1304),
    /** Invalid date format detected: "%1$s". Ignoring value. */
    INVALID_DATE(ImportExportExceptionMessages.INVALID_DATE_MSG, Category.USER_INPUT, 1305),
    /** Ignoring invalid value for field "%1$s": %2$s  */
    IGNORE_FIELD(ImportExportExceptionMessages.IGNORE_FIELD_MSG, Category.USER_INPUT, 1306),
    
    CONTACT_INTERFACE_MISSING(ImportExportExceptionMessages.CONTACT_INTERFACE_MISSING, Category.INTERNAL_ERROR, 1307),
    COULD_NOT_WRITE(ImportExportExceptionMessages.COULD_NOT_WRITE, Category.INTERNAL_ERROR, 1308),    
    ;

    private String message;
    private Category category;
    private int number;

    private ImportExportExceptionCodes(final String message, final Category category, final int number) {
        this.message = message;
        this.category = category;
        this.number = number;
    }

    public int getDetailNumber() {
        return number;
    }

    public String getMessage() {
        return message;
    }

    public String getHelp() {
        return null;
    }

    public Category getCategory() {
        return category;
    }

    public ImportExportException create(final Object... args) {
        return ImportExportExceptionFactory.getInstance().create(this, args);
    }

    public ImportExportException create(final Throwable cause, final Object... args) {
        return ImportExportExceptionFactory.getInstance().create(this, cause, args);
    }
}
