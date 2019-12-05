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


package com.openexchange.gdpr.dataexport;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * The GDPR data export error codes.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum DataExportExceptionCode implements DisplayableOXExceptionCode {

    /**
     * Unexpected error: %1$s
     */
    UNEXPECTED_ERROR("Unexpected error: %1$s", CATEGORY_ERROR),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", CATEGORY_ERROR),
    /**
     * A JSON error occurred: %1$s
     */
    JSON_ERROR("A JSON error occurred: %1$s", CATEGORY_ERROR),
    /**
     * An SQL error occurred: %1$s
     */
    SQL_ERROR("An SQL error occurred: %1$s", CATEGORY_ERROR),
    /**
     * A data export task is already running for user %1$s in context %2$s.
     */
    TASK_ALREADY_RUNNING("A data export task is already running for user %1$s in context %2$s.", CATEGORY_USER_INPUT, DataExportExceptionMessage.TASK_ALREADY_RUNNING_MSG),
    /**
     * No such data export for user %1$s in context %2$s.
     */
    NO_SUCH_TASK("No such data export task for user %1$s in context %2$s.", CATEGORY_USER_INPUT, DataExportExceptionMessage.NO_SUCH_TASK_MSG),
    /**
     * No such data export with identifier %1$s.
     */
    NO_SUCH_TASK_FOR_ID("No such data export with identifier %1$s.", CATEGORY_ERROR),
    /**
     * There is already a data export for user %1$s in context %2$s.
     */
    DUPLICATE_TASK("There is already a data export for user %1$s in context %2$s.", CATEGORY_USER_INPUT, DataExportExceptionMessage.DUPLICATE_TASK_MSG),
    /**
     * Failed to cancel data export for user %1$s in context %2$s.
     */
    CANCEL_TASK_FAILED("Failed to cancel data export for user %1$s in context %2$s. Either there is no such task or task is already terminated.", CATEGORY_USER_INPUT, DataExportExceptionMessage.CANCEL_TASK_FAILED_MSG),
    /**
     * Failed to delete data export for user %1$s in context %2$s.
     */
    DELETE_TASK_FAILED("Failed to delete data export for user %1$s in context %2$s.", CATEGORY_USER_INPUT, DataExportExceptionMessage.DELETE_TASK_FAILED_MSG),
    /**
     * Data export for user %1$s in context %2$s is not yet completed.
     */
    TASK_NOT_COMPLETED("Data export for user %1$s in context %2$s is not yet completed.", CATEGORY_USER_INPUT, DataExportExceptionMessage.TASK_NOT_COMPLETED_MSG),
    /**
     * Data export for user %1$s in context %2$s failed.
     */
    TASK_FAILED("Data export for user %1$s in context %2$s failed.", CATEGORY_USER_INPUT, DataExportExceptionMessage.TASK_FAILED_MSG),
    /**
     * Data export for user %1$s in context %2$s aborted.
     */
    TASK_ABORTED("Data export for user %1$s in context %2$s aborted.", CATEGORY_USER_INPUT, DataExportExceptionMessage.TASK_ABORTED_MSG),
    /**
     * No such data export provider for module "%1$s".
     */
    NO_SUCH_PROVIDER("No such data export provider for module \"%1$s\".", CATEGORY_ERROR),
    /**
     * No file storage specified. Please set "com.openexchange.gdpr.dataexport.fileStorageId" configuration option.
     */
    NO_FILE_STORAGE_SPECIFIED("No file storage specified. Please set \"com.openexchange.gdpr.dataexport.fileStorageId\" configuration option.", CATEGORY_ERROR),
    /**
     * No schedule specified. Please set "com.openexchange.gdpr.dataexport.schedule" configuration option.
     */
    NO_SCHEDULE_SPECIFIED("No schedule specified. Please set \"com.openexchange.gdpr.dataexport.schedule\" configuration option.", CATEGORY_ERROR),
    /**
     * No modules to export specified.
     */
    NO_MODULES_SPECIFIED("No modules to export specified.", CATEGORY_USER_INPUT),
    /**
     * No such package %1$s for user %2$s in context %3$s.
     */
    NO_SUCH_RESULT_FILE("No such package %1$s for user %2$s in context %3$s.", CATEGORY_USER_INPUT, DataExportExceptionMessage.NO_SUCH_RESULT_FILE_MSG),
    /**
     * Invalid maximum file size specified. The minimum acceptable value is %1$s.
     */
    INVALID_FILE_SIZE("Invalid value given for maximum file size. The value must be equal to or greater than %1$s.", CATEGORY_USER_INPUT),
    ;

    private static final String PREFIX = "GDPR-EXPORT";

    private final String message;
    private final Category category;
    private final String displayMessage;

    /**
     * Initializes a new {@link DataExportExceptionCode}.
     *
     * @param message
     * @param category
     */
    private DataExportExceptionCode(final String message, final Category category) {
        this(message, category, null);
    }

    /**
     * Initializes a new {@link DataExportExceptionCode}.
     *
     * @param message
     * @param category
     * @param displayMessage
     */
    private DataExportExceptionCode(final String message, final Category category, final String displayMessage) {
        this.message = message;
        this.category = category;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return ordinal() + 1;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayMessage() {
        return this.displayMessage;
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
