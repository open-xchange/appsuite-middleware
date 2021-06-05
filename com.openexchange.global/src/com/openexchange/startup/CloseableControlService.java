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

package com.openexchange.startup;

import java.io.Closeable;
import java.util.Collection;
import java.util.Collections;

/**
 * {@link CloseableControlService} - The closeable control to register {@link Closeable} instances that are supposed to be closed on thread termination.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface CloseableControlService {

    /**
     * The dummy <code>Closeable</code> control that does nothing at all.
     */
    public static final CloseableControlService DUMMY_CONTROL = new CloseableControlService() {

        @Override
        public boolean removeCloseable(Closeable closeable) {
            return false;
        }

        @Override
        public void closeAll() {
            // Nothing to do
        }

        @Override
        public Collection<Closeable> getCurrentCloseables() {
            return Collections.emptyList();
        }

        @Override
        public boolean addCloseable(Closeable closeable) {
            return false;
        }
    };

    // -------------------------------------------------------------------------------------------------------------------------------

    /**
     * Adds specified <code>Closeable</code> to this control
     *
     * @param closeable The <code>Closeable</code> to add
     * @return <code>true</code> if successfully added; otherwise <code>false</code>
     */
    boolean addCloseable(Closeable closeable);

    /**
     * Removes specified <code>Closeable</code> from this control
     *
     * @param closeable The <code>Closeable</code> to remove
     * @return <code>true</code> if successfully removed; otherwise <code>false</code> if no such <code>Closeable</code> is present in this control
     */
    boolean removeCloseable(Closeable closeable);

    /**
     * Gets the (unmodifiable) {@link Collection} view on currently managed <code>Closeable</code> instances.
     *
     * @return The currently managed <code>Closeable</code> instances
     */
    Collection<Closeable> getCurrentCloseables();

    /**
     * Closes & removes all currently managed <code>Closeable</code> instances.
     */
    void closeAll();

}
