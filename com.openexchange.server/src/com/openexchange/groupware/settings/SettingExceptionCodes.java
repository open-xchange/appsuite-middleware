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

package com.openexchange.groupware.settings;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * The error codes for settings.
 */
public enum SettingExceptionCodes implements DisplayableOXExceptionCode {

    /** Cannot get connection to database. */
    NO_CONNECTION("Cannot get connection to database.", Category.CATEGORY_SERVICE_DOWN, 1),

    /** An SQL problem occured while reading information from the config database. */
    SQL_ERROR("An SQL problem occured while reading information from the config database.", OXExceptionStrings.SQL_ERROR_MSG,
        Category.CATEGORY_ERROR, 2),

    /** Writing the setting %1$s is not permitted. */
    NO_WRITE("Writing the setting %1$s is not permitted.", SettingExceptionMessage.NO_WRITE_MSG, Category.CATEGORY_PERMISSION_DENIED, 3),

    /** Unknown setting path %1$s. */
    UNKNOWN_PATH("Unknown setting path %1$s.", Category.CATEGORY_ERROR, 4),

    /** Setting "%1$s" is not a leaf one. */
    NOT_LEAF("Setting \"%1$s\" is not a leaf.", Category.CATEGORY_ERROR, 5),

    /** Exception while parsing JSON. */
    JSON_READ_ERROR("Exception while parsing JSON.", Category.CATEGORY_ERROR, 6),

    /** Problem while initialising configuration tree. */
    INIT("Problem while initialising configuration tree.", Category.CATEGORY_ERROR, 8),

    /** Invalid value %s written to setting %s. */
    INVALID_VALUE("Invalid value %s written to setting %s.", SettingExceptionMessage.INVALID_VALUE_MSG, Category.CATEGORY_USER_INPUT, 9),

    /** Found duplicate database identifier %d. Not adding preferences item. */
    DUPLICATE_ID("Found duplicate database identifier %d. Not adding preferences item.", Category.CATEGORY_ERROR, 10),

    /** Found duplicate path %s. */
    DUPLICATE_PATH("Found duplicate path %s.", Category.CATEGORY_ERROR, 12),

    /** Subsystem error. */
    SUBSYSTEM("Error during use of a subsystem", Category.CATEGORY_SERVICE_DOWN, 13),

    /** Not allowed operation. */
    NOT_ALLOWED("Not allowed operation.", Category.CATEGORY_ERROR, 14),

    /** Reached maximum retries writing setting %s. */
    MAX_RETRY("Reached maximum retries for writing the setting %s.", OXExceptionStrings.MESSAGE_RETRY, Category.CATEGORY_TRY_AGAIN, 15),

    /** Setting "%1$s" cannot be stored into the database because is too big */
    DATA_TRUNCATION("Setting \"%1$s\" cannot be stored into the database because is too big", OXExceptionStrings.DATA_TRUNCATION_ERROR_MSG, Category.CATEGORY_ERROR, 16),

    ;

    private String message;
    private String displayMessage;
    private Category category;
    private int detailNumber;

    private SettingExceptionCodes(String message, Category category, int detailNumber) {
        this(message, null, category, detailNumber);
    }

    private SettingExceptionCodes(String message, String displayMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.category = category;
        this.detailNumber = detailNumber;
    }

    @Override
    public String getPrefix() {
        return "USS";
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return detailNumber;
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
    public boolean equals(OXException e) {
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
    public OXException create(Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Throwable cause, Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
