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

import java.util.UUID;

/**
 * {@link UUIDService} to encode/decode the tuple contextID, moduleID, objectID into a {@link java.util.UUID}
 * <p>
 * <u><b>Warning</b></u>: In MySQL contextID and objectID are defined as unsigned ints, thus their maximum
 * value would be 4.294.967.295. Java on the other hand does not support unsigned ints, resulting
 * in a range value from -2.147.483.648 to 2.147.483.647. This means that, if at some point the contextID
 * and/or the objectID surpass the the positive upper bound of signed int, the return value of the get methods
 * will be negative.
 * </p>
 * <p>
 * For more information see <a href="http://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">Java Primitive Data Types</a>
 * and <a href="http://dev.mysql.com/doc/refman/5.0/en/integer-types.html">MySQL Integer Types</a>
 * </p>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface UUIDService {

	/**
	 * Generate a UUID for the tuple contextID, moduleID, objectID
	 * @param contextID
	 * @param moduleID
	 * @param objectID
	 * @return the encoded UUID
	 */
	public UUID generateUUID(int contextID, int moduleID, int objectID);

	/**
	 * Get the contextID from the given UUID which was encoded via the {@link UUIDService}
	 *
	 * @param u encoded UUID
	 * @return contextID
	 */
	public int getContextID(UUID u);

	/**
	 * Get the moduleID from the given UUID which was encoded via the {@link UUIDService}
	 * @param u encoded UUID
	 * @return moduleID
	 */
	public int getModuleID(UUID u);

	/**
	 * Get the objectID from the given UUID which was encoded via the {@link UUIDService}
	 * @param u encoded UUID
	 * @return objectID
	 */
	public int getObjectID(UUID u);
}
