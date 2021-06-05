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
 * {@link CASPOP3StorageProperties} - Enhances {@link POP3StorageProperties} by a method to atomically compare-and-set a property.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface CASPOP3StorageProperties extends POP3StorageProperties {

    /**
     * Atomically sets the property value to the given updated value if the current property value == the expected property value.
     *
     * @param propertyName The property name
     * @param expectedPropertyValue The expected property value
     * @param newPropertyValue The new property value (if current property value == the expected property value)
     * @return <code>true</code> if successful; otherwise <code>false</code> if the actual value was not equal to the expected value.
     * @throws OXException If adding mapping fails
     */
    public boolean compareAndSetProperty(final String propertyName, final String expectedPropertyValue, final String newPropertyValue) throws OXException;

}
