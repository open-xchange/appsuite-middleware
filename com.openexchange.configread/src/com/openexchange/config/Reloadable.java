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

package com.openexchange.config;

/**
 * {@link Reloadable} - Marks services that perform necessary actions in order to apply (possibly) new configuration.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added some JavaDoc
 * @since 7.6.0
 */
public interface Reloadable {

    /**
     * Gets the interests in property names and/or configuration files.
     *
     * @return The interests
     */
    Interests getInterests();

    /**
     * Signals that the configuration has been reloaded.
     * <p>
     * This {@link Reloadable} instance should perform necessary actions in order to apply (possibly) new configuration.
     *
     * @param configService The configuration service providing newly initialized properties
     */
    void reloadConfiguration(ConfigurationService configService);
}
