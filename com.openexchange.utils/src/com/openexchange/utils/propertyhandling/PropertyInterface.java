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

package com.openexchange.utils.propertyhandling;

/**
 * Interface which defines how a property should look like
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface PropertyInterface {

    public Class<? extends Object> getClazz();

    /**
     * Defines if the corresponding property is required, might be a clear true or false, or a dependency on a condition
     * 
     * @return a {@link Required} object
     */
    public Required getRequired();

    /**
     * Get the name of the property in the property file
     * 
     * @return the name
     */
    public String getName();

    /**
     * If the property should be logged or not, might be usefull for password properties
     * 
     * @return true or false
     */
    public boolean isLog();

}
