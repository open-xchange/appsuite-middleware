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

package com.openexchange.preview;

import static com.openexchange.conversion.DataExceptionMessages.TRUNCATED_MSG;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * Enumeration about all {@link OXException}s.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum PreviewExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    ERROR("An error occurred: %1$s", CATEGORY_ERROR, 1),
    /**
     * The following field(s) are too long: %1$s
     */
    TRUNCATED("The following field(s) are too long: %1$s", TRUNCATED_MSG, CATEGORY_TRUNCATED, 2),
    /**
     * Unable to change data. (%1$s)
     */
    UNABLE_TO_CHANGE_DATA("Unable to change data. (%1$s)", PreviewExceptionMessages.UNABLE_TO_CHANGE_DATA_MSG, CATEGORY_USER_INPUT, 3),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", CATEGORY_ERROR, 4),
    /**
     * Thumbnail image not available. Reason: %1$s
     */
    THUMBNAIL_NOT_AVAILABLE("Thumbnail image not available. Reason: %1$s", PreviewExceptionMessages.THUMBNAIL_NOT_AVAILABLE, CATEGORY_ERROR, 5),
    /**
     * No preview service for MIME type %1$s
     */
    NO_PREVIEW_SERVICE("No preview service for MIME type %1$s", PreviewExceptionMessages.NO_PREVIEW_SERVICE, CATEGORY_ERROR, 6),
    /**
     * No preview service for MIME type %1$s (file name %2$s)
     */
    NO_PREVIEW_SERVICE2("No preview service for MIME type %1$s (file name %2$s)", PreviewExceptionMessages.NO_PREVIEW_SERVICE, NO_PREVIEW_SERVICE.getCategory(), NO_PREVIEW_SERVICE.getNumber()),

    ;

    private final Category category;

    private final int number;

    private final String message;

    private final String displayMessage;


    private PreviewExceptionCodes(final String message, final Category category, final int detailNumber) {
        this(message, null, category, detailNumber);
    }

    private PreviewExceptionCodes(final String message, final String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        number = detailNumber;
        this.category = category;
        this.displayMessage = (displayMessage == null) ? OXExceptionStrings.MESSAGE : displayMessage;
    }

    @Override
    public String getPrefix() {
        return "PREVIEW";
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
        return number;
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
