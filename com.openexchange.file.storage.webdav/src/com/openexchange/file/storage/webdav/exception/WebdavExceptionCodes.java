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

package com.openexchange.file.storage.webdav.exception;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link WebdavExceptionCodes}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public enum WebdavExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * Missing capability for file storage %1$s
     */
    MISSING_CAPABILITY("Missing capability for file storage %1$s", Category.CATEGORY_PERMISSION_DENIED, 1, WebdavExceptionMessages.MISSING_CAP_MSG),

    /**
     * Invalid configuration: %1$s
     */
    INVALID_CONFIG("Invalid configuration: %1$s", Category.CATEGORY_CONFIGURATION, 2, WebdavExceptionMessages.INVALID_CONFIG_MSG),

    /**
     * The document could not be updated because it was modified. Reload the view.
     */
    MODIFIED_CONCURRENTLY("The document could not be updated because it was modified. Reload the view.", CATEGORY_CONFLICT, 3, WebdavExceptionMessages.MODIFIED_CONCURRENTLY_MSG_DISPLAY),

    /**
     * The connection check failed
     */
    PING_FAILED("The connection check failed.", CATEGORY_CONNECTIVITY, 4, WebdavExceptionMessages.PING_FAILED),

    /**
     * Cannot connect to URL: %1$s. Please change and try again.
     */
    URL_NOT_ALLOWED("The feed URL %1$s is not allowed due to configuration.", CATEGORY_USER_INPUT, 5, WebdavExceptionMessages.URL_NOT_ALLOWED_MSG),

    /**
     * The requested URI %1$s does not match the standard.
     */
    BAD_URL("The requested URL %1$s does not match the standard.", Category.CATEGORY_USER_INPUT, 6, WebdavExceptionMessages.BAD_URL_MSG),

    ;

    /**
     * The prefix constant.
     */
    public static final String PREFIX = "FILE_STORAGE_WEBDAV";

    private final Category category;

    private final int detailNumber;

    private final String message;

    private final String displayMessage;

    private WebdavExceptionCodes(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    private WebdavExceptionCodes(final String message, final Category category, final int detailNumber, final String displayMessage) {
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
