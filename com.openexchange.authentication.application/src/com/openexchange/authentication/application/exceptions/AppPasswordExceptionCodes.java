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

package com.openexchange.authentication.application.exceptions;

import static com.openexchange.authentication.application.exceptions.AppPasswordExceptionMessages.MISSING_CAPABILITY_MSG;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link AppPasswordExceptionCodes}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public enum AppPasswordExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * <li>This account isn't authorized to perform this action.</li>
     * <li>This account isn't authorized to perform this action [required scope: %1$s]</li>
     */
    NOT_AUTHORIZED("This account isn't authorized to perform this action [required scope: %1$s]", AppPasswordExceptionMessages.NOT_AUTHORIZED, Category.CATEGORY_PERMISSION_DENIED, 1),
    /**
     * <li>Unknown application type $1%s</li>
     * <li>Type of application $1%s is unknown</li>
     */
    UNKNOWN_APPLICATION_TYPE("Type of application $1%s is unkown", AppPasswordExceptionMessages.UNKNOWN_APPLICATION_TYPE, Category.CATEGORY_ERROR, 2),
    /**
     * <li>Error while reading/writing data from/to the database.</li>
     * <li>Error executing application password database action</li>
     */
    DATABASE_ERROR("Error excuting application password database action", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 3),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>Configuration $1%s missing or invalid</li>
     */
    MISSING_CONFIGURATION("Configuration $1%s missing or invalid", Category.CATEGORY_CONFIGURATION, 4),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>Application Password error %s</li>
     */
    APPLICATION_PASSWORD_GENERIC_ERROR("Appplication Password error %s", Category.CATEGORY_ERROR, 5),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>No application storage available for application type $1%s.</li>
     */
    NO_APPLICATION_PASSWORD_STORAGE("No application storage available for application type $1%s.", Category.CATEGORY_SERVICE_DOWN, 6),
    /**
     * <li>Unable to update application passwords during password change. You may need to remove then recreate the application passwords.</li>
     * <li>Unable to change application passwords during password change</li>
     */
    UNABLE_UPDATE_PASSWORD("Unable to change application passwords during password change", AppPasswordExceptionMessages.UNABLE_TO_UPDATE_APP_PASSWORD, Category.CATEGORY_ERROR, 7),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>The database connection is missing.</li>
     */
    MISSING_DATABASE_CONNECTION("The database connection is missing.", Category.CATEGORY_ERROR, 10),
    /**
     * <li>The operation could not be completed due to insufficient capabilities.</li>
     * <li>Missing capability [%1$s]</li>
     */
    MISSING_CAPABILITY("Missing capability [%1$s]", MISSING_CAPABILITY_MSG, Category.CATEGORY_PERMISSION_DENIED, 11),
    /**
     * <li>This client cannot be used with an application-specific password.</li>
     * <li>Client blacklisted for app-specific passwords [client: $1%s]</li>
     */
    UNSUPPORTED_CLIENT("Client blacklisted for app-specific passwords [client: $1%s]", AppPasswordExceptionMessages.UNSUPPORTED_CLIENT_MSG, Category.CATEGORY_PERMISSION_DENIED, 12),
    ;

    private static final String PREFIX = "ASP";
    private final String message;
    private final String displayMessage;
    private final Category category;
    private final int number;

    /**
     * Default constructor.
     *
     * @param message message.
     * @param category category.fffff
     * @param number detail number.
     */
    private AppPasswordExceptionCodes(String message, Category category, int number) {
        this(message, null, category, number);
    }

    private AppPasswordExceptionCodes(String message, String displayMessage, Category category, int number) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.category = category;
        this.number = number;
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

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

}
