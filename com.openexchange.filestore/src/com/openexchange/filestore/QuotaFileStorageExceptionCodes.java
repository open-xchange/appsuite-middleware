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

package com.openexchange.filestore;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * Error codes for the file storage exception.
 *
 * @author Steffen Templin
 */
public enum QuotaFileStorageExceptionCodes implements DisplayableOXExceptionCode {
    /**
     * Couldn't reach the filestore
     */
    INSTANTIATIONERROR("File store could not be accessed.", OXExceptionStrings.MESSAGE, Category.CATEGORY_SERVICE_DOWN, 21),
    /**
     * Database Query could not be realized
     */
    SQLSTATEMENTERROR("Database query failed.", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 23),
    /**
     * The allowed Quota is reached.
     */
    STORE_FULL("The allowed Quota is reached.", QuotaFileStorageExceptionMessage.STORE_FULL_MSG, Category.CATEGORY_USER_INPUT, 24),
    /**
     * Quota seems to be inconsistent. Please use consistency tool for context %1$s.
     */
    QUOTA_UNDERRUN("Quota seems to be inconsistent. Please use consistency tool for context %1$s.", OXExceptionStrings.MESSAGE, Category.CATEGORY_TRUNCATED, 25),
    /**
     * Quota seems to be inconsistent. Please use consistency tool for owner %1$s in context %2$s.
     */
    QUOTA_UNDERRUN_USER("Quota seems to be inconsistent. Please use consistency tool for owner %1$s in context %2$s.", OXExceptionStrings.MESSAGE, Category.CATEGORY_TRUNCATED, 25),
    /**
     * Quota usage is missing for context %1$s.
     */
    NO_USAGE("Quota usage is missing for context %1$s.", OXExceptionStrings.MESSAGE, Category.CATEGORY_ERROR, 26),
    /**
     * Quota usage is missing for owner %1$s in context %2$s.
     */
    NO_USAGE_USER("Quota usage is missing for owner %1$s in context %2$s.", OXExceptionStrings.MESSAGE, Category.CATEGORY_ERROR, 26),
    /**
     * Update of quota usage for context %1$s failed.
     */
    UPDATE_FAILED("Update of quota usage for context %1$s failed.", OXExceptionStrings.MESSAGE, Category.CATEGORY_ERROR, 27),
    /**
     * Update of quota usage for owner %1$s context %2$s failed.
     */
    UPDATE_FAILED_USER("Update of quota usage for owner %1$s in context %2$s failed.", OXExceptionStrings.MESSAGE, Category.CATEGORY_ERROR, 27),
    ;

    private final String message;
    private final String displayMessage;
    private final Category category;
    private final int number;

    /**
     * Initializes a new {@link QuotaFileStorageExceptionCodes}.
     */
    private QuotaFileStorageExceptionCodes(final String message, String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage;
        this.category = category;
        this.number = detailNumber;
    }

    @Override
    public String getPrefix() {
        return FileStorageCodes.prefix();
    }

    @Override
    public Category getCategory() {
        return category;
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
