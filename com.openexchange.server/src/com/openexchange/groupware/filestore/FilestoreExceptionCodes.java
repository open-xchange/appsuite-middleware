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

package com.openexchange.groupware.filestore;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.filestore.FileStorageCodes;

/**
 * {@link FilestoreExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum FilestoreExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * "Wrong filestore %1$d for context %2$d needing filestore %3$d.
     */
    FILESTORE_MIXUP(FileStorageCodes.FILESTORE_MIXUP),
    /**
     * Cannot find filestore with id %1$s.
     */
    NO_SUCH_FILESTORE(FileStorageCodes.NO_SUCH_FILE_STORAGE),
    /**
     * Cannot create URI from "%1$s".
     */
    URI_CREATION_FAILED(FileStorageCodes.URI_CREATION_FAILED),
    /**
     * SQL Problem: "%s".
     */
    SQL_PROBLEM(FileStorageCodes.SQL_PROBLEM),
    ;

    private final String message;
    private final String displayMessage;
    private final Category category;
    private final int number;
    private final String prefix;

    private FilestoreExceptionCodes(DisplayableOXExceptionCode origin) {
        this.message = origin.getMessage();
        this.displayMessage = origin.getDisplayMessage();
        this.category = origin.getCategory();
        this.number = origin.getNumber();
        this.prefix = origin.getPrefix();
    }

    @Override
    public String getPrefix() {
        return prefix;
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
    public Category getCategory() {
        return category;
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
