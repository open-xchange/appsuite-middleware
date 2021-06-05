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

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link WarningsAware} - Aware of possible warnings.
 * <p>
 * This is an optional interface that may be implemented by {@link FileStorageAccountAccess} subclasses.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @see DefaultWarningsAware
 */
public interface WarningsAware {

    /**
     * Gets the optional warnings.
     *
     * @return The optional warnings
     */
    List<OXException> getWarnings();

    /**
     * Gets the optional warnings and flushes them; meaning subsequent invocations would return an empty list.
     *
     * @return The optional warnings
     */
    List<OXException> getAndFlushWarnings();

    /**
     * Adds given warning.
     *
     * @param warning The warning to add
     */
    void addWarning(OXException warning);

    /**
     * Removes given warning.
     *
     * @param warning The warning to remove
     */
    void removeWarning(OXException warning);
}
