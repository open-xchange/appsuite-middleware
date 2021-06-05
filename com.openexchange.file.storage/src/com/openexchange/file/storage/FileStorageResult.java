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

import java.util.Collection;
import java.util.Collections;
import com.openexchange.exception.OXException;

/**
 * 
 * {@link FileStorageResult}
 * Makes it possible to return a list of warnings in addition to any response.
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.5
 * @param <R> The type of the response.
 */
public class FileStorageResult<R> {

    /**
     * 
     * Creates a new {@link FileStorageResult} object.
     *
     * @param <R> The type of the response.
     * @param response The response.
     * @param warnings The warnings.
     * @return The {@link FileStorageResult} object.
     */
    public static <R> FileStorageResult<R> newFileStorageResult(final R response, final Collection<OXException> warnings) {
        return new FileStorageResult<R>(response, warnings);
    }

    private final R response;

    private final Collection<OXException> warnings;

    /**
     * 
     * Initializes a new {@link FileStorageResult}.
     * 
     * @param response The response.
     * @param warnings The warnings.
     */
    private FileStorageResult(final R response, final Collection<OXException> warnings) {
        this.response = response;
        this.warnings = null == warnings ? Collections.emptySet() : warnings;
    }

    /**
     * 
     * Gets the response.
     *
     * @return The response.
     */
    public R getResponse() {
        return response;
    }

    /**
     * 
     * Gets the warnings.
     *
     * @return The warnings.
     */
    public Collection<OXException> getWarnings() {
        return warnings;
    }

}