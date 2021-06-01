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

package com.openexchange.jsieve.export.utils;

import java.io.IOException;

/** An I/O result */
public class IOResult<V> {

    private static final IOResult<?> EMPTY = new IOResult<>(null, null);

    /**
     * Creates a success result for result instance.
     *
     * @param <V> The result type
     * @param result The result to propagate
     * @return The success result
     */
    static <V> IOResult<V> resultFor(V result) {
        if (result == null) {
            @SuppressWarnings("unchecked")
            IOResult<V> ior = (IOResult<V>) EMPTY;
            return ior;
        }
        return new IOResult<V>(result, null);
    }

    /**
     * Creates an error result for given I/O error.
     *
     * @param <V> The result type
     * @param ioError The I/O error
     * @return The error result
     */
    static <V> IOResult<V> errorFor(IOException ioError) {
        if (ioError == null) {
            throw new IllegalArgumentException("I/O error must not be null");
        }
        return new IOResult<V>(null, ioError);
    }

    // -------------------------------------------------------------------------------------------------------

    private final V result;
    private final IOException ioError;

    /**
     * Initializes a new {@link IOResult}.
     *
     * @param result The result or <code>null</code>
     * @param ioError The I/O error or <code>null</code>
     */
    private IOResult(V result, IOException ioError) {
        super();
        this.result = result;
        this.ioError = ioError;
    }

    /**
     * Gets the result or <code>null</code> if an I/O error is available.
     *
     * @return The result or <code>null</code>
     */
    public V getResult() {
        return result;
    }

    /**
     * Gets the checked result.
     *
     * @return The result
     * @throws IOException If there is {@link #getIoError() an I/O error available}
     */
    public V getCheckedResult() throws IOException {
        if (ioError != null) {
            throw ioError;
        }
        return result;
    }

    /**
     * Gets the optional I/O error
     *
     * @return The I/O error or <code>null</code>
     */
    public IOException getIoError() {
        return ioError;
    }
}