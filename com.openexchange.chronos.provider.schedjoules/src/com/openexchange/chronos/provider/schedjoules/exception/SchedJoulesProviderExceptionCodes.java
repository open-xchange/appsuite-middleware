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

package com.openexchange.chronos.provider.schedjoules.exception;

import static com.openexchange.exception.OXExceptionStrings.MESSAGE;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link SchedJoulesProviderExceptionCodes}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum SchedJoulesProviderExceptionCodes implements DisplayableOXExceptionCode {
    /**
     * <li>The requested page does not denote a calendar</li>
     * <li>The page with the id '%$1s' does not denote a calendar</li>
     */
    NO_CALENDAR("The page with the id '%1$s' does not denote a calendar", SchedJoulesProviderExceptionMessages.NO_CALENDAR_MSG, CATEGORY_USER_INPUT, 1),
    /**
     * <li>You have no access to this calendar.</li>
     * <li>No access to calendar with id '%1$s'</li>
     */
    NO_ACCESS("No access to calendar with id '%1$s'", SchedJoulesProviderExceptionMessages.NO_ACCESS_MSG, CATEGORY_PERMISSION_DENIED, 2),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>A JSON error occurred: %1$s</li>
     */
    JSON_ERROR("A JSON error occurred: %1$s", CATEGORY_ERROR, 3),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>The account with id '%1$s' does not contain a valid URL.</li>
     */
    INVALID_URL("The account with id '%1$s' does not contain a valid URL.", CATEGORY_ERROR, 4),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>No user configuration found [account %1$s, user %2$s, context %3$s]</li>
     */
    NO_USER_CONFIGURATION("No user configuration found [account %1$s, user %2$s, context %3$s]", CATEGORY_ERROR, 5),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>No folders metadata found [account %1$s, user %2$s, context %3$s]</li>
     */
    NO_FOLDERS_METADATA("No folder metadata found [account %1$s, user %2$s, context %3$s]", CATEGORY_ERROR, 6),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>No folder metadata found [folderId %1$s, account %2$s, user %3$s, context %4$s]</li>
     */
    NO_FOLDER_METADATA("No folder metadata found [folderId %1$s, account %2$s, user %3$s, context %4$s]", CATEGORY_ERROR, 6),
    /**
     * <li>The requested calendar does not exist.</li>
     * <li>The calendar with the id '%1$s' does not exist.</li>
     */
    CALENDAR_DOES_NOT_EXIST("The calendar with the id '%1$s' does not exist", SchedJoulesProviderExceptionMessages.CALENDAR_DOES_NOT_EXIST_MSG, CATEGORY_USER_INPUT, 7),
    /**
     * <li>Your SchedJoules account is malformed. Please re-create it.</li>
     * <li>The user key is missing [account %1$s, user %2$s, context %3$s]</li>
     */
    MISSING_USER_KEY("The user key is missing [account %1$s, user %2$s, context %3$s]", SchedJoulesProviderExceptionMessages.MALFORMED_ACCOUNT_MSG, CATEGORY_ERROR, 8),
    /**
     * <li>Your SchedJoules account is malformed. Please re-create it.</li>
     * <li>The user key is malformed [account %1$s, user %2$s, context %3$s]</li>
     */
    MALFORMED_USER_KEY("The user key is malformed [account %1$s, user %2$s, context %3$s]", SchedJoulesProviderExceptionMessages.MALFORMED_ACCOUNT_MSG, CATEGORY_ERROR, 9),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>No internal configuration found [account %1$s, user %2$s, context %3$s]</li>
     */
    NO_INTERNAL_CONFIGURATION("No user configuration found [account %1$s, user %2$s, context %3$s]", CATEGORY_ERROR, 10),
    /**
     * <li>You have specified an invalid refresh minimum interval for the calendar subscription</li>
     * <li>Invalid refresh minimum interval was specified [account %1$s, user %2$s, context %3$s]</li>
     */
    INVALID_REFRESH_MINIMUM_INTERVAL("Invalid refresh minimum interval was specified [account %1$s, user %2$s, context %3$s]", SchedJoulesProviderExceptionMessages.INVALID_MINIMUM_REFRESH_INTERVAL_MSG, CATEGORY_ERROR, 11),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>The item identifier is missing from the configuration [account %1$s, user %2$s, context %3$s]</li>
     */
    MISSING_ITEM_ID_FROM_CONFIG("The item identifier is missing from the configuration [account %1$s, user %2$s, context %3$s]", CATEGORY_ERROR, 12),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>The requested item with identifier '{}' does not denote to a JSON page</li>
     */
    PAGE_DOES_NOT_DENOTE_TO_JSON("The requested item with identifier '%1$s' does not denote to a JSON page", CATEGORY_ERROR, 13),
    /**
     * <li>You have specified an invalid alarm value for the calendar subscription</li>
     * <li>Invalid alarm value '%4$s' was specified [account %1$s, user %2$s, context %3$s]</li>
     */
    INVALID_ALARM_VALUE("Invalid alarm value '%4$s' was specified [account %1$s, user %2$s, context %3$s]", SchedJoulesProviderExceptionMessages.INVALID_ALARM_VALUE_MSG, CATEGORY_ERROR, 14),
    ;

    public static final String PREFIX = "SCHEDJOULES";

    private String message;
    private String displayMessage;
    private Category category;
    private int number;

    /**
     * Initialises a new {@link SchedJoulesProviderExceptionCodes}.
     *
     * @param message The exception message
     * @param displayMessage The display message
     * @param category The {@link Category}
     * @param number The error number
     */
    private SchedJoulesProviderExceptionCodes(String message, Category category, int number) {
        this(message, null, category, number);
    }

    /**
     * Initialises a new {@link SchedJoulesProviderExceptionCodes}.
     *
     * @param message The exception message
     * @param displayMessage The display message
     * @param category The {@link Category}
     * @param number The error number
     */
    private SchedJoulesProviderExceptionCodes(String message, String displayMessage, Category category, int number) {
        this.message = message;
        this.displayMessage = null != displayMessage ? displayMessage : MESSAGE;
        this.category = category;
        this.number = number;
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
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public Category getCategory() {
        return category;
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
