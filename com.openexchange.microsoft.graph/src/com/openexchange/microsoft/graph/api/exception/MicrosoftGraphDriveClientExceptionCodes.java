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
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link MicrosoftGraphDriveClientExceptionCodes} - Defines the client exceptions codes
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public enum MicrosoftGraphDriveClientExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * <li>The copying of file %1$s failed. There is nothing we can do about it. Try again later</li>
     * <li>The asynchronous copying of item '%1$s' failed with status code '%2$s'.</li>
     */
    ASYNC_COPY_FAILED("The asynchronous copying of item '%1$s' failed with status code '%2$s'.", MicrosoftGraphDriveClientExceptionMessages.ASYNC_COPY_FAILED, CATEGORY_ERROR, 1),
    /**
     * <li>Uploading the file '%1$s' in folder '%2$s' failed. There is nothing we can do about it. Try again later.</li>
     * <li>Uploading the file '%1$s' in folder '%2$s' failed. The API returned an empty 'uploadUrl'.</li>
     */
    UPLOAD_FAILED_EMPTY_URL("Uploading the file '%1$s' in folder '%2$s' failed. The API returned an empty 'uploadUrl'.", MicrosoftGraphDriveClientExceptionMessages.UPLOAD_FAILED, CATEGORY_ERROR, 2),
    /**
     * <li>Uploading the file '%1$s' in folder '%2$s' failed. There is nothing we can do about it. Try again later.</li>
     * <li>Uploading the file '%1$s' in folder '%2$s' failed with status code '%3$s'.</li>
     */
    UPLOAD_FAILED_STATUS_CODE("Uploading the file '%1$s' in folder '%2$s' failed with status code '%3$s'.", MicrosoftGraphDriveClientExceptionMessages.UPLOAD_FAILED, CATEGORY_ERROR, 3),
    /**
     * <li>Uploading the file '%1$s' in folder '%2$s' failed. There is nothing we can do about it. Try again later.</li>
     * <li>Uploading the file '%1$s' in folder '%2$s' failed. Reached end of stream.</li>
     */
    UPLOAD_FAILED("Uploading the file '%1$s' in folder '%2$s' failed. Reached end of stream.", MicrosoftGraphDriveClientExceptionMessages.UPLOAD_FAILED, CATEGORY_ERROR, 3),
    ;

    public static final String PREFIX = "MICROSOFT-GRAPH-DRIVE-CLIENT";

    private String message;
    private String displayMessage;
    private Category category;
    private int number;

    /**
     * Initialises a new {@link MicrosoftGraphDriveClientExceptionCodes}.
     * 
     * @param message The exception message
     * @param displayMessage The display message
     * @param category The {@link Category}
     * @param number The error number
     */
    private MicrosoftGraphDriveClientExceptionCodes(String message, Category category, int number) {
        this(message, null, category, number);
    }

    /**
     * Initialises a new {@link MicrosoftGraphDriveClientExceptionCodes}.
     * 
     * @param message The exception message
     * @param displayMessage The display message
     * @param category The {@link Category}
     * @param number The error number
     */
    private MicrosoftGraphDriveClientExceptionCodes(String message, String displayMessage, Category category, int number) {
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
