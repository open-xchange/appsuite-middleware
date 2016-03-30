/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
