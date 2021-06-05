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

package com.openexchange.config.cascade;

import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link ConfigView} - A configuration view.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added JavaDoc
 */
public interface ConfigView {

    /**
     * Sets denoted property
     *
     * @param scope The property's scope
     * @param propertyName The property's name
     * @param value The property's value
     * @throws OXException If setting property fails for any reason
     */
    <T> void set(String scope, String propertyName, T value) throws OXException;

    /**
     * Gets coerced property value.
     *
     * @param propertyName The property name
     * @param coerceTo The type to coerce to
     * @return The coerced value or <code>null</code>
     * @throws OXException If such a property does not exist
     */
    <T> T get(String propertyName, Class<T> coerceTo) throws OXException;

    /**
     * (Optionally) Gets coerced property value.
     *
     * @param propertyName The property name
     * @param coerceTo The type to coerce to
     * @param defaultValue The default value
     * @return The coerced value or <code>defaultValue</code> if absent
     * @throws OXException If returning property fails
     */
    <T> T opt(String propertyName, Class<T> coerceTo, T defaultValue) throws OXException;

    /**
     * Gets coerced property.
     *
     * @param scope The property's scope
     * @param propertyName The property's name
     * @param coerceTo The type to coerce to
     * @return The coerced property
     * @throws OXException If returning property fails
     */
    <T> ConfigProperty<T> property(String scope, String propertyName, Class<T> coerceTo) throws OXException;

    /**
     * Gets coerced composed property (all scopes combined).
     *
     * @param propertyName The property's name
     * @param coerceTo The type to coerce to
     * @return The coerced composed property
     * @throws OXException If returning composed property fails
     */
    <T> ComposedConfigProperty<T> property(String propertyName, Class<T> coerceTo) throws OXException;

    /**
     * Gets all available properties.
     *
     * @return All available properties
     * @throws OXException If operation fails
     */
    Map<String, ComposedConfigProperty<String>> all() throws OXException;

}
