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

package com.openexchange.multiple.internal;

import com.openexchange.multiple.MultipleHandlerFactoryService;

/**
 * {@link MultipleHandlerRegistry} - Registry for multiple handlers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MultipleHandlerRegistry {

    /**
     * Gets the factory service supporting specified module.
     *
     * @param module The module
     * @return The factory service supporting specified module or <code>null</code> if none available
     */
    public MultipleHandlerFactoryService getFactoryService(String module);

    /**
     * Adds specified factory service to this registry. Factory service is associated with module name indicated through
     * {@link MultipleHandlerFactoryService#getSupportedModule()}.
     *
     * @param factoryService The factory service to register
     * @return <code>true</code> if factory service was successfully added; otherwise <code>false</code>
     */
    public boolean addFactoryService(MultipleHandlerFactoryService factoryService);

    /**
     * Removes the factory service associated with specified module.
     *
     * @param module The module
     */
    public void removeFactoryService(String module);

}
