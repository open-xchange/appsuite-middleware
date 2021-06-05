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

package com.openexchange.chronos.provider.ical.exception;

import static com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionMessages.BAD_FEED_URI_MSG;
import static com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionMessages.BAD_PARAMETER_MSG;
import static com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionMessages.CREDENTIALS_CHANGED_MSG;
import static com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionMessages.CREDENTIALS_REQUIRED_MSG;
import static com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionMessages.CREDENTIALS_WRONG_MSG;
import static com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionMessages.FEED_SIZE_EXCEEDED_MSG;
import static com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionMessages.FEED_URI_NOT_ALLOWED_MSG;
import static com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionMessages.MISSING_FEED_URI_MSG;
import static com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionMessages.NOT_ALLOWED_CHANGE_MSG;
import static com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionMessages.NO_FEED_MSG;
import static com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionMessages.PASSWORD_REQUIRED_MSG;
import static com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionMessages.PASSWORD_WRONG_MSG;
import static com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionMessages.UNEXPECTED_FEED_ERROR_MSG;
import static com.openexchange.exception.OXExceptionStrings.MESSAGE;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 *
 * {@link ICalProviderExceptionCodes}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public enum ICalProviderExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * <li>The feed URI is missing.</li>
     * <li>The feed URI is missing.</li>
     */
    MISSING_FEED_URI("The feed URI is missing.", MISSING_FEED_URI_MSG, Category.CATEGORY_USER_INPUT, 4040),
    /**
     * <li>The given feed URI is invalid. Please change it and try again.</li>
     * <li>The requested feed with URI %1$s does not match the standard.</li>
     */
    BAD_FEED_URI("The requested feed with URI %1$s does not match the standard.", BAD_FEED_URI_MSG, Category.CATEGORY_USER_INPUT, 4041),
    /**
     * <li>Cannot connect to feed with URI: %1$s. Please change it and try again.</li>
     * <li>The feed URI %1$s is not allowed due to configuration</li>
     */
    FEED_URI_NOT_ALLOWED("The feed URI %1$s is not allowed due to configuration.", FEED_URI_NOT_ALLOWED_MSG, Category.CATEGORY_USER_INPUT, 4042),
    /**
     * <li>The provided URI %1$s does not contain content as expected. Please change it and try again.</li>
     * <li>The provided URI %1$s does not contain an ICal feed</li>
     */
    NO_FEED("The provided URI %1$s does not contain an ICal feed.", NO_FEED_MSG, Category.CATEGORY_USER_INPUT, 4043),
    /**
     * <li>The field %1$s cannot be changed.</li>
     * <li>The field %1$s cannot be changed.</li>
     */
    NOT_ALLOWED_CHANGE("The field %1$s cannot be changed.", NOT_ALLOWED_CHANGE_MSG, Category.CATEGORY_USER_INPUT, 4044),
    /**
     * <li>The field '%1$s' contains an unexpected value '%2$s'.</li>
     * <li>The field '%1$s' contains an unexpected value '%2$s'.</li>
     */
    BAD_PARAMETER("The field '%1$s' contains an unexpected value '%2$s'.", BAD_PARAMETER_MSG, Category.CATEGORY_USER_INPUT, 4045),
    /**
     * <li>Unfortunately your requested feed cannot be used due to size limitations.</li>
     * <li>The requested feed with URI %1$s does exceed the configured maximum size. Allowed %2$s bytes but was %3$s bytes.</li>
     */
    FEED_SIZE_EXCEEDED("The requested feed with URI %1$s does exceed the configured maximum size. Allowed %2$s bytes but was %3$s bytes.", FEED_SIZE_EXCEEDED_MSG, Category.CATEGORY_CONFIGURATION, 4001),
    /**
     * <li>Unfortunately the given feed URI cannot be processed as expected.</li>
     * <li>An error occurred while retrieving the desired feed URI '%1$s': %2$s</li>
     */
    UNEXPECTED_FEED_ERROR("An error occurred while retrieving the desired feed URI '%1$s': %2$s", UNEXPECTED_FEED_ERROR_MSG, Category.CATEGORY_ERROR, 5001),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>An HTTP client protocol error occurred: %1$s</li>
     */
    CLIENT_PROTOCOL_ERROR("An HTTP client protocol error occurred: %1$s", CATEGORY_ERROR, 5002),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>An I/O error occurred: %1$s</li>
     */
    IO_ERROR("An I/O error occurred: %1$s", CATEGORY_ERROR, 5003),
    /**
     * <li>The remote service is unavailable at the moment. There is nothing we can do about it. Please try again later.</li>
     * <li>The remote service is unavailable at the moment: %1$s. Please try again later.</li>
     */
    REMOTE_SERVICE_UNAVAILABLE("The remote service is unavailable at the moment: %1$s. Please try again later.", ICalProviderExceptionMessages.REMOTE_SERVICE_UNAVAILABLE_MSG, CATEGORY_SERVICE_DOWN, 5031),
    /**
     * <li>An internal server error occurred on the feed provider side. There is nothing we can do about it.</li>
     * <li>A remote internal server error occurred: %1$s</li>
     */
    REMOTE_INTERNAL_SERVER_ERROR("A remote internal server error occurred: %1$s", ICalProviderExceptionMessages.REMOTE_INTERNAL_SERVER_ERROR_MSG, CATEGORY_SERVICE_DOWN, 5032),
    /**
     * <li>A remote server error occurred on the feed provider side. There is nothing we can do about it.</li>
     * <li>A remote server error occurred: %1$s</li>
     */
    REMOTE_SERVER_ERROR("A remote server error occurred: %1$s", ICalProviderExceptionMessages.REMOTE_SERVER_ERROR_MSG, CATEGORY_ERROR, 5033),
    /**
     * <li>Access to this calendar is restricted. Please enter your credentials and try again.</li>
     * <li>Credentials required [url %1$s, status %2$s, realm %3$s]</li>
     */
    CREDENTIALS_REQUIRED("Credentials required [url %1$s, status %2$s, realm %3$s]", CREDENTIALS_REQUIRED_MSG, Category.CATEGORY_USER_INPUT, 4010),
    /**
     * <li>Authentication failed. Please enter your credentials and try again.</li>
     * <li>Credentials required [url %1$s, status %2$s, realm %3$s]</li>
     */
    CREDENTIALS_WRONG("Credentials wrong [url %1$s, status %2$s, realm %3$s]", CREDENTIALS_WRONG_MSG, Category.CATEGORY_USER_INPUT, 4011),
    /**
     * <li>Access to this calendar is restricted. Please enter your password and try again.</li>
     * <li>Password required [url %1$s, status %2$s, realm %3$s]</li>
     */
    PASSWORD_REQUIRED("Password required [url %1$s, status %2$s, realm %3$s]", PASSWORD_REQUIRED_MSG, Category.CATEGORY_USER_INPUT, 4012),
    /**
     * <li>Authentication failed. Please enter your password and try again.</li>
     * <li>Password required [url %1$s, status %2$s, realm %3$s]</li>
     */
    PASSWORD_WRONG("Password wrong [url %1$s, status %2$s, realm %3$s]", PASSWORD_WRONG_MSG, Category.CATEGORY_USER_INPUT, 4013),
    /**
     * <li>Authentication failed due to a recent credentials change. Please remove the account and add it again with correct credentials.</li>
     * <li>Credentials changed [url %1$s, status %2$s, realm %3$s]</li>
     */
    CREDENTIALS_CHANGED("Credentials changed [url %1$s, status %2$s, realm %3$s]", CREDENTIALS_CHANGED_MSG, Category.CATEGORY_USER_INPUT, 4014),

    ;

    private static final String PREFIX = "ICAL-PROV".intern();

    private String message;
    private String displayMessage;
    private Category category;
    private int number;

    private ICalProviderExceptionCodes(String message, Category category, int number) {
        this(message, null, category, number);
    }

    private ICalProviderExceptionCodes(String message, String displayMessage, Category category, int number) {
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
