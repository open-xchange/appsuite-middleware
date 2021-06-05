
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

package com.openexchange.file.storage;

import com.openexchange.annotation.NonNull;

import com.openexchange.annotation.Nullable;
import com.openexchange.exception.OXException;
import java.util.Date;
import java.util.Objects;

/**
 * {@link FileStorageAccountError} -represents an error occurred while accessing a {@link FileStorageAccount}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class FileStorageAccountError {

    private OXException exception;
    private Date timeStamp;

    /**
     * Initializes a new {@link FileStorageAccountError}.
     */
    public FileStorageAccountError() {
        this(null, null);
    }

    /**
     * Initializes a new {@link FileStorageAccountError} with the current time as time stamp
     *
     * @param exception The exception
     */
    public FileStorageAccountError(@NonNull OXException exception) {
        this(Objects.requireNonNull(exception, "exception must not be null"), null);
    }

    /**
     * Initializes a new {@link FileStorageAccountError}.
     *
     * @param exception The error code, might be <code>null</code>
     * @param timeStamp The time stamp of when the error occurred, might be <code>null</code>
     */
    public FileStorageAccountError(@Nullable OXException exception, @Nullable Date timeStamp) {
        this.exception = exception;
        this.timeStamp = timeStamp != null ? timeStamp : new Date();
    }

    /**
     * Gets the error code
     *
     * @return The errorCode
     */
    public @Nullable OXException getException() {
        return exception;
    }

    /**
     * Sets the error code
     *
     * @param exception The exception to set, might be <code>null</code>
     * @return this
     */
    public FileStorageAccountError setException(@Nullable OXException exception) {
        this.exception = exception;
        return this;
    }

    /**
     * Gets the time stamp
     *
     * @return The time stamp of when the error occurred, or <code>null</code>
     */
    public @Nullable Date getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the time stamp
     *
     * @param timeStamp The timeStamp to set, might be <code>null</code>
     * @return this
     */
    public FileStorageAccountError setTimeStamp(@Nullable Date timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }
}
