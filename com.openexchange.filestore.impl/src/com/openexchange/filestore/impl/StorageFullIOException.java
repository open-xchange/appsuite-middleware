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

package com.openexchange.filestore.impl;

import java.io.IOException;

/**
 * {@link StorageFullIOException} - The I/O exception to advertise that storage limit is exceeded.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class StorageFullIOException extends IOException {

    private static final long serialVersionUID = 5590995590704481202L;

    /** The actual size of the request. */
    private final long actual;

    /** The maximum permitted size of the request. */
    private final long permitted;

    /**
     * Initializes a new {@link StorageFullIOException}.
     */
    public StorageFullIOException(long actual, long permitted) {
        super();
        this.actual = actual;
        this.permitted = permitted;
    }

    /**
     * Gets the actual size of the file, which was about to be stored.
     *
     * @return The actual size of the file.
     */
    public long getActualSize() {
        return actual;
    }

    /**
     * Gets the permitted storage size.
     *
     * @return The permitted size.
     */
    public long getPermittedSize() {
        return permitted;
    }

}
