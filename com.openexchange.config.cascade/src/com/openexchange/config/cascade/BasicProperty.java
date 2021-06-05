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

import java.util.List;
import com.openexchange.exception.OXException;


/**
 * {@link BasicProperty}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added JavaDoc
 */
public interface BasicProperty {

    /**
     * Sets this property's value
     *
     * @param value The value
     * @throws OXException If setting value fails
     */
    void set(String value) throws OXException;

    /**
     * Gets this property's value
     *
     * @return The value
     * @throws OXException If retrieving value fails
     */
    String get() throws OXException;

    /**
     * Sets specified meta data.
     *
     * @param metadataName The meta data's name
     * @param value The meta data's value
     * @throws OXException If setting meta data fails
     */
    void set(String metadataName, String value) throws OXException;

    /**
     * Gets specified meta data.
     *
     * @param metadataName The meta data's name
     * @return The meta data's value or <code>null</code> if absent
     * @throws OXException
     */
    String get(String metadataName) throws OXException;

    /**
     * Indicates whether this property is defined.
     *
     * @return <code>true</code> if defined; otherwise <code>false</code>
     * @throws OXException If checking defined state fails
     */
    boolean isDefined() throws OXException;

    /**
     * Gets the listing of all available meta data names.
     *
     * @return The meta data names
     * @throws OXException If listing cannot be returned
     */
    List<String> getMetadataNames() throws OXException;
}
