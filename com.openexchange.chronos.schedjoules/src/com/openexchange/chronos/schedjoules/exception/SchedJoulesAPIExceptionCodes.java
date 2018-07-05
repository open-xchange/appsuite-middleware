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

package com.openexchange.chronos.schedjoules.exception;

import static com.openexchange.exception.OXExceptionStrings.MESSAGE;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link SchedJoulesAPIExceptionCodes}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum SchedJoulesAPIExceptionCodes implements DisplayableOXExceptionCode {
    /**
     * <li>You have no access to this calendar.</li>
     * <li>No access to calendar with id '%1$s'</li>
     */
    NO_ACCESS("No access to calendar with id '%1$s'", SchedJoulesAPIExceptionMessages.NO_ACCESS_MSG, CATEGORY_PERMISSION_DENIED, 2),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>A JSON error occurred: %1$s</li>
     */
    JSON_ERROR("A JSON error occurred: %1$s", CATEGORY_ERROR, 3),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>The page with id '%1$s' does not contain a valid URL.</li>
     */
    INVALID_URL("The page with id '%1$s' does not contain a valid URL.", CATEGORY_ERROR, 4),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>An I/O error occurred: %1$s</li>
     */
    IO_ERROR("An I/O error occurred: %1$s", CATEGORY_ERROR, 5),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>No stream parser found for the specified content type '%1$s'</li>
     */
    NO_STREAM_PARSER("No stream parser found for the specified content type '%1$s'", CATEGORY_ERROR, 6),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>The configuration is missing from the '%1$s' property. Please configure the service properly.</li>
     */
    CONFIGURATION_MISSING("The configuration is missing from the '%1$s' property. Please configure the service properly.", CATEGORY_ERROR, 7),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>The URI path '%1$s' is not valid.</li>
     */
    INVALID_URI_PATH("The URI path '%1$s' is not valid.", CATEGORY_ERROR, 8),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>An unknown HTTP method '%1$s' was specified.</li>
     */
    UNKNOWN_HTTP_METHOD("An unknown HTTP method '%1$s' was specified.", CATEGORY_ERROR, 9),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>An HTTP client protocol error occurred: %1$s</li>
     */
    CLIENT_PROTOCOL_ERROR("An HTTP client protocol error occurred: %1$s", CATEGORY_ERROR, 10),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>The middleware is not authorized to access the SchedJoules API: %1$s. Check your API key in 'schedjoules.properties'</li>
     */
    NOT_AUTHORIZED("The middleware is not authorized to access the SchedJoules API: %1$s. Check your API key is set in the %2$s property.", CATEGORY_PERMISSION_DENIED, 11),
    /**
     * <li>The requested page was not found.</li>
     * <li>The requested page was not found.</li>
     */
    PAGE_NOT_FOUND("The requested page was not found.", SchedJoulesAPIExceptionMessages.PAGE_NOT_FOUND, CATEGORY_ERROR, 12),
    /**
     * <li>The remote SchedJoules service is unavailable at the moment. There is nothing we can do about it. Please try again later.</li>
     * <li>The remote service is unavailable at the moment: %1$s. Please try again later.</li>
     */
    REMOTE_SERVICE_UNAVAILABLE("The remote service is unavailable at the moment: %1$s. Please try again later.", SchedJoulesAPIExceptionMessages.REMOTE_SERVICE_UNAVAILABLE_MSG, CATEGORY_SERVICE_DOWN, 13),
    /**
     * <li>An internal server error occurred on SchedJoules side. There is nothing we can do about it.</li>
     * <li>A remote internal server error occurred: %1$s</li>
     */
    REMOTE_INTERNAL_SERVER_ERROR("A remote internal server error occurred: %1$s", SchedJoulesAPIExceptionMessages.REMOTE_INTERNAL_SERVER_ERROR_MSG, CATEGORY_SERVICE_DOWN, 14),
    /**
     * <li>A remote server error occurred on SchedJoules side. There is nothing we can do about it.</li>
     * <li>A remote server error occurred: %1$s</li>
     */
    REMOTE_SERVER_ERROR("A remote server error occurred: %1$s", SchedJoulesAPIExceptionMessages.REMOTE_SERVER_ERROR_MSG, CATEGORY_ERROR, 15),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>An unexpected error occurred: %1$s</li>
     */
    UNEXPECTED_ERROR("An unexpected error occurred: %1$s", MESSAGE, CATEGORY_ERROR, 16),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>No content was returned.</li>
     */
    NO_CONTENT("No content was returned.", MESSAGE, CATEGORY_ERROR, 17),
    ;

    public static final String PREFIX = "SCHEDJOULES-API";

    private String message;
    private String displayMessage;
    private Category category;
    private int number;

    /**
     * Initialises a new {@link SchedJoulesAPIExceptionCodes}.
     * 
     * @param message The exception message
     * @param displayMessage The display message
     * @param category The {@link Category}
     * @param number The error number
     */
    private SchedJoulesAPIExceptionCodes(String message, Category category, int number) {
        this(message, null, category, number);
    }

    /**
     * Initialises a new {@link SchedJoulesAPIExceptionCodes}.
     * 
     * @param message The exception message
     * @param displayMessage The display message
     * @param category The {@link Category}
     * @param number The error number
     */
    private SchedJoulesAPIExceptionCodes(String message, String displayMessage, Category category, int number) {
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
