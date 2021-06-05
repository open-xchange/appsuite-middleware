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

package com.openexchange.pop3.storage;

import com.openexchange.exception.OXException;

/**
 * {@link POP3StorageProperties} - Properties for a {@link POP3Storage}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface POP3StorageProperties {

    /**
     * Gets the property value associated with specified property name.
     *
     * @param propertyName The property name
     * @return The property value associated with specified property name or <code>null</code>
     * @throws OXException If property retrieval fails
     */
    public String getProperty(String propertyName) throws OXException;

    /**
     * Maps given property name to given property value.
     *
     * @param propertyName The property name
     * @param propertyValue The property value
     * @throws OXException If property mapping cannot be added
     */
    public void addProperty(String propertyName, String propertyValue) throws OXException;

    /**
     * Removes the property value associated with specified property name.
     *
     * @param propertyName The property name
     * @throws OXException If property removal fails
     */
    public void removeProperty(String propertyName) throws OXException;
}
