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
