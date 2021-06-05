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
package com.openexchange.eav;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.exception.OXException;

/**
 * {@link EAVStorage}
 *
 * Attribute-value-based storage for extended properties of groupware objects.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface EAVStorage {

	/**
     * Gets all extended attributes for an object.
	 *
	 * @param uuid of the object encoded via the {@link UUIDService}
     *
     * @return a map containing all attribute-values, or <code>null</code> if
     * no extended properties are stored
     * @throws OXException
     */
    Map<String, Object> getAttributes(UUID uuid) throws OXException;

    /**
     * Gets the specified extended attributes for an object.
     * @param uuid of the object encoded via the {@link UUIDService}
     * @param attributes the attribute names
     *
     * @return A map containing the requested attribute-values, or
     * <code>null</code> if none of the requested extended properties are
     * stored at all. Requested attributes missing from the result are treated
     * as not set.
     * @throws OXException
     */
    Map<String, Object> getAttributes(UUID uuid, String...attributes) throws OXException;

    /**
     * Gets all extended attributes for a list of objects in a folder.
     *
     * @param uuid List with objects' UUIDs which are encoded via the {@link UUIDService}
     *
     * @return A map representing all attribute-values for all objects in the
     * queried objects in the folder that have stored attributes. Each objects'
     * ID is mapped to the corresponding extended properties.
     *
     * @throws OXException
     */
    Map<UUID, Map<String, Object>> getAttributes(List<UUID> uuids) throws OXException;

    /**
     * Gets the specified extended attributes for a list of objects in a folder.
     *
     * @param uuid List with objects' UUIDs which are encoded via the {@link UUIDService}
     *
     * @return A map representing the specified attribute-values for the
     * queried objects in the folder that have at least one of the requested
     * attributes. Each objects' ID is mapped to the corresponding extended
     * properties. Requested attributes missing from an objects' result are
     * treated as not set.
     * @throws OXException
     */
    Map<UUID, Map<String, Object>> getAttributes(List<UUID> uuids, String...attributes) throws OXException;

    /**
     * Gets a value indicating whether the storage contains extended properties
     * for an object or not.
     *
     * @param uuid of the object encoded via the {@link UUIDService}
     *
     * @return <code>true</code>, if there are any extended properties,
     * <code>false</code>, otherwise
     * @throws OXException
     */
    boolean hasAttributes(UUID uuid) throws OXException;

    /**
     * Deletes all extended properties for an object.
     *
     * @param uuid of the object encoded via the {@link UUIDService}
     *
     * @throws OXException
     */
    void deleteAttributes(UUID uuid) throws OXException;

    /**
     * Sets extended properties for an object. For each mapped attribute, this
     * will either lead to adding a new attribute-value-pair, an update of an
     * existing attribute-value, or the deletion of the attribute if it is
     * mapped to <code>null</code>.
     *
     * @param uuid of the object encoded via the {@link UUIDService}
     *
     * @param attributes the attributes to set
     *
     * @throws OXException
     */
    void setAttributes(UUID uuid, Map<String, Object> attributes) throws OXException;
}
