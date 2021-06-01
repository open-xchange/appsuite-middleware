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

package com.openexchange.dovecot.doveadm.client;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link DoveAdmClientExceptionCodes} - Enumeration of all DoveAdm errors.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum DoveAdmClientExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", Category.CATEGORY_ERROR, 1),
    /**
     * A DoveAdm error occurred: %1$s
     */
    DOVEADM_ERROR("A DoveAdm error occurred: %1$s", DoveAdmClientExceptionMessages.DOVECOT_ERROR_MSG, Category.CATEGORY_ERROR, 2),
    /**
     * A DoveAdm error occurred: %1$s
     */
    DOVEADM_SERVER_ERROR("A DoveAdm error occurred: %1$s", DoveAdmClientExceptionMessages.DOVECOT_SERVER_ERROR_MSG, Category.CATEGORY_ERROR, 3),
    /**
     * Invalid DoveAdm URL: %1$s
     */
    INVALID_DOVECOT_URL("Invalid DoveAdm URL: %1$s", DoveAdmClientExceptionMessages.INVALID_DOVECOT_URL_MSG, Category.CATEGORY_ERROR, 4),
    /**
     * The DoveAdm resource does not exist: %1$s
     */
    NOT_FOUND("The DoveAdm resource does not exist: %1$s", DoveAdmClientExceptionMessages.NOT_FOUND_MSG, Category.CATEGORY_ERROR, 5),
    /**
     * Doveadm HTTP API communication error: 404 Not Found
     */
    NOT_FOUND_SIMPLE("Doveadm HTTP API communication error: 404 Not Found", DoveAdmClientExceptionMessages.NOT_FOUND_SIMPLE_MSG, NOT_FOUND.getCategory(), NOT_FOUND.getNumber()),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", DoveAdmClientExceptionMessages.IO_ERROR_MSG, Category.CATEGORY_ERROR, 6),
    /**
     * Authentication failed: %1$s
     */
    AUTH_ERROR("Authentication failed: %1$s", DoveAdmClientExceptionMessages.AUTH_ERROR_MSG, Category.CATEGORY_ERROR, 7),
    /**
     * A JSON error occurred: %1$s
     */
    JSON_ERROR("A JSON error occurred: %1$s", Category.CATEGORY_ERROR, 8),
    /**
     * Unknown DoveAdm call: %1$s
     */
    UNKNOWN_CALL("Unknown DoveAdm call: %1$s", Category.CATEGORY_ERROR, 9),
    /**
     * DoveAdm not reachable to serve call %1$s
     */
    DOVEADM_NOT_REACHABLE("DoveAdm not reachable to serve call %1$s", DoveAdmClientExceptionMessages.DOVEADM_NOT_REACHABLE_MSG, Category.CATEGORY_SERVICE_DOWN, 10),
    /**
     * DoveAdm not reachable.
     */
    DOVEADM_NOT_REACHABLE_GENERIC("DoveAdm not reachable", DoveAdmClientExceptionMessages.DOVEADM_NOT_REACHABLE_MSG, Category.CATEGORY_SERVICE_DOWN, 10), // Yepp, same error code
    /**
     * Duplicate optional identifier specified: %1$s
     */
    DUPLICATE_OPTIONAL_IDENTIFIER("Duplicate optional identifier specified: %1$s", Category.CATEGORY_ERROR, 11),
    /**
     * Received unknown response type: %1$s
     */
    UNKNOWN_RESPONSE_TYPE("Received unknown response type: %1$s", Category.CATEGORY_ERROR, 12),

    ;

    private static final String PREFIX = "DOVEADM_CLIENT";

    /**
     * Gets the <code>"DC-ACC"</code> prefix for this error code class.
     *
     * @return The <code>"DC-ACC"</code> prefix
     */
    public static String prefix() {
        return PREFIX;
    }

    private final Category category;
    private final int detailNumber;
    private final String message;
    private final String displayMessage;

    private DoveAdmClientExceptionCodes(final String message, final Category category, final int detailNumber) {
        this(message, null, category, detailNumber);
    }

    private DoveAdmClientExceptionCodes(final String message, final String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
        this.displayMessage = (displayMessage == null) ? OXExceptionStrings.MESSAGE : displayMessage;
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

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }
}
