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

package org.json.helpers;

import java.util.concurrent.atomic.AtomicReference;
import org.json.FileBackedJSONStringProvider;

/**
 * {@link FileBackedJSON}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class FileBackedJSON {

    /**
     * Initializes a new {@link FileBackedJSON}.
     */
    private FileBackedJSON() {
        super();
    }

    private static final AtomicReference<FileBackedJSONStringProvider> PROVIDER_REF = new AtomicReference<>(null);

    /**
     * Sets the {@link FileBackedJSONStringProvider} instance
     *
     * @param provider The provider
     */
    public static void setFileBackedJSONStringProvider(FileBackedJSONStringProvider provider) {
        PROVIDER_REF.set(provider);
    }

    /**
     * Gets the {@link FileBackedJSONStringProvider} instance
     *
     * @return The provider
     */
    public static FileBackedJSONStringProvider getFileBackedJSONStringProvider() {
        return PROVIDER_REF.get();
    }

}
