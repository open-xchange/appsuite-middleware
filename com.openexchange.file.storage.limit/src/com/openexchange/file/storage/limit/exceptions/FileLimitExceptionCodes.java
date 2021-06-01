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

package com.openexchange.file.storage.limit.exceptions;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 *
 * {@link FileLimitExceptionCodes}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public enum FileLimitExceptionCodes implements DisplayableOXExceptionCode {

    TYPE_NOT_AVAILABLE("Cannot check limit for type %1$s. No service available", Category.CATEGORY_USER_INPUT, null, 4001),

    TOO_MANY_FILES("The total number of files exceeds the maximum of %1$s.", Category.CATEGORY_USER_INPUT, LimitExceptionMessages.TOO_MANY_FILES_MSG, 4002),

    STORAGE_QUOTA_EXCEEDED("The total upload size (%1$s) exceeds the available space of %2$s.", Category.CATEGORY_USER_INPUT, LimitExceptionMessages.STORAGE_QUOTA_EXCEEDED_MSG, 4003),

    FILE_QUOTA_PER_REQUEST_EXCEEDED("'%1$s' (%2$s) exceeds the allowed size of %3$s per file.", Category.CATEGORY_USER_INPUT, LimitExceptionMessages.FILE_QUOTA_PER_REQUEST_EXCEEDED_MSG, 4004),

    NOT_ALLOWED("The user is not allowed to upload files to folder %1$s.", Category.CATEGORY_USER_INPUT, LimitExceptionMessages.NOT_ALLOWED_MSG, 4005),
    ;

    public static final String PREFIX = "FILE-LIMIT";

    private final Category category;

    private final int detailNumber;

    private final String message;

    private final String displayMessage;

    private FileLimitExceptionCodes(final String message, final Category category, final int detailNumber) {
        this(message, category, null, detailNumber);
    }

    private FileLimitExceptionCodes(final String message, final Category category, final String displayMessage, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
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
