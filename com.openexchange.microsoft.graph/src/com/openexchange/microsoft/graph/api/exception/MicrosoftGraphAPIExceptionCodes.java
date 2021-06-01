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

package com.openexchange.microsoft.graph.api.exception;

import static com.openexchange.exception.OXExceptionStrings.MESSAGE;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.java.Strings;

/**
 * {@link MicrosoftGraphAPIExceptionCodes} - Defines the API exceptions codes that
 * the Microsoft Graph endpoint might return.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/errors#code-property">Basic Error Codes</a>
 */
public enum MicrosoftGraphAPIExceptionCodes implements DisplayableOXExceptionCode {

    ACCESS_DENIED("%1$s", CATEGORY_ERROR, ErrorCode.accessDenied, 1),
    ACTIVITY_LIMIT_REACHED("%1$s", CATEGORY_ERROR, ErrorCode.activityLimitReached, 2),
    GENERAL_EXCEPTION("%1$s", CATEGORY_ERROR, ErrorCode.generalException, 3),
    INVALID_RANGE("%1$s", CATEGORY_ERROR, ErrorCode.invalidRange, 4),
    INVALID_REQUEST("%1$s", CATEGORY_ERROR, ErrorCode.invalidRequest, 5),
    ITEM_NOT_FOUND("%1$s", CATEGORY_ERROR, ErrorCode.itemNotFound, 6),
    MALWARE_DETECTED("%1$s", CATEGORY_ERROR, ErrorCode.malwareDetected, 7),
    NAME_ALREADY_EXISTS("%1$s", CATEGORY_ERROR, ErrorCode.nameAlreadyExists, 8),
    NOT_ALLOWED("%1$s", CATEGORY_ERROR, ErrorCode.notAllowed, 9),
    NOT_SUPPORTED("%1$s", CATEGORY_ERROR, ErrorCode.notSupported, 10),
    RESOURCE_MODIFIED("%1$s", CATEGORY_ERROR, ErrorCode.resourceModified, 11),
    RESYNC_REQUIRED("%1$s", CATEGORY_ERROR, ErrorCode.resyncRequired, 12),
    SERVICE_NOT_AVAILABLE("%1$s", CATEGORY_ERROR, ErrorCode.serviceNotAvailable, 13),
    QUOTA_LIMIT_REACHED("%1$s", CATEGORY_ERROR, ErrorCode.quotaLimitReached, 14),
    UNAUTHENTICATED("%1$s", CATEGORY_ERROR, ErrorCode.unauthenticated, 15),
    ;

    public static final String PREFIX = "MICROSOFT-GRAPH-API";

    private String message;
    private String displayMessage;
    private Category category;
    private int number;
    private final ErrorCode errorCode;

    /**
     * A reverse index to map the {@link ErrorCode}s with exceptions
     */
    private static final Map<ErrorCode, MicrosoftGraphAPIExceptionCodes> reverseIndex;
    static {
        Map<ErrorCode, MicrosoftGraphAPIExceptionCodes> m = new HashMap<>(8);
        for (MicrosoftGraphAPIExceptionCodes c : MicrosoftGraphAPIExceptionCodes.values()) {
            m.put(c.getErrorCode(), c);
        }
        reverseIndex = Collections.unmodifiableMap(m);
    }

    /**
     * Initialises a new {@link MicrosoftGraphAPIExceptionCodes}.
     * 
     * @param message The exception message
     * @param displayMessage The display message
     * @param category The {@link Category}
     * @param number The error number
     */
    private MicrosoftGraphAPIExceptionCodes(String message, Category category, ErrorCode errorCode, int number) {
        this(message, null, category, errorCode, number);
    }

    /**
     * Initialises a new {@link MicrosoftGraphAPIExceptionCodes}.
     * 
     * @param message The exception message
     * @param displayMessage The display message
     * @param category The {@link Category}
     * @param number The error number
     */
    private MicrosoftGraphAPIExceptionCodes(String message, String displayMessage, Category category, ErrorCode errorCode, int number) {
        this.message = message;
        this.errorCode = errorCode;
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
     * Parses the specified 'error' to a {@link MicrosoftGraphAPIExceptionCodes}
     * 
     * @param error The error string
     * @return The {@link MicrosoftGraphAPIExceptionCodes} constant
     */
    @SuppressWarnings("unused")
    public static MicrosoftGraphAPIExceptionCodes parse(String error) {
        if (Strings.isEmpty(error)) {
            return MicrosoftGraphAPIExceptionCodes.GENERAL_EXCEPTION;
        }
        try {
            ErrorCode errorCode = ErrorCode.valueOf(error);
            MicrosoftGraphAPIExceptionCodes ex = reverseIndex.get(errorCode);
            if (ex == null) {
                return MicrosoftGraphAPIExceptionCodes.GENERAL_EXCEPTION;
            }
            return ex;
        } catch (IllegalArgumentException e) {
            return MicrosoftGraphAPIExceptionCodes.GENERAL_EXCEPTION;
        }
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

    /**
     * Gets the errorCode
     *
     * @return The errorCode
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
