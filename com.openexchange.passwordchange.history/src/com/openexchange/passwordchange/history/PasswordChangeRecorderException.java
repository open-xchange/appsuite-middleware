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

package com.openexchange.passwordchange.history;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link PasswordChangeRecorderException} - Exception for PasswordChangeHistory bundle
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public enum PasswordChangeRecorderException implements OXExceptionCode {

    /**
     * Password change history for user %1$s in context %1$s is disabled
     */
    DISABLED("Password change history for user %1$s in context %2$s is disabled", Category.CATEGORY_PERMISSION_DENIED, 101),
    /**
     * Recorder for user %1$s in context %1$s is not configured. Therefore no history is created.
     */
    MISSING_CONFIGURATION("Recorder for user %1$s in context %2$s is not configured. Therefore no history is created.", Category.CATEGORY_CONFIGURATION, 102),
    /**
     * The recorder %1$s was not found in the registry
     */
    MISSING_RECORDER("The recorder %1$s was not found in the registry.", Category.CATEGORY_SERVICE_DOWN, 103),
    /**
     * SQL error: %1$s
     */
    SQL_ERROR("SQL error: %1$s", Category.CATEGORY_ERROR, 104),
    /**
     * No password change recording allowed for a guest user
     */
    DENIED_FOR_GUESTS("No password change recording allowed for a guest user", Category.CATEGORY_PERMISSION_DENIED, 105)

    ;

    private static final String PREFIX = "HCE";

    private final String message;
    private final Category category;
    private final int number;

    private PasswordChangeRecorderException(final String message, final Category category, final int number) {
        this.message = message;
        this.category = category;
        this.number = number;
    }

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public String getMessage() {
        return message;
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
