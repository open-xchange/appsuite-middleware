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
package com.openexchange.uuid.impl;

import java.util.UUID;

import com.openexchange.eav.UUIDService;

/**
 * Implementation of {@link UUIDService}
 *
 * The structure of the generated UUID is as follows:
 * 12 bytes for contextID, 4 bytes for moduleID, 12 bytes for objectID<br/>
 * eg.: 00000000-0002-0007-0000-00000000000a refers to contextID:2, moduleID: 7, objectID: 10<br/><br/>
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class UUIDServiceImpl implements UUIDService {

	private static final int contextShift = 16;

	/* (non-Javadoc)
	 * @see com.openexchange.eav.UUIDService#generateUUID(long, long, long)
	 */
	@Override
	public synchronized UUID generateUUID(int contextID, int moduleID, int objectID) {
		long lsb = objectID;
		long cid = contextID & 0xffffffffL;;
		long msb = cid << contextShift;
		msb += moduleID;

		return new UUID(msb, lsb);
	}

	/* (non-Javadoc)
	 * @see com.openexchange.eav.UUIDService#getContextID(java.util.UUID)
	 */
	@Override
	public synchronized int getContextID(UUID u) {
		long msb = u.getMostSignificantBits();
		return (int)msb >> contextShift;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.eav.UUIDService#getModuleID(java.util.UUID)
	 */
	@Override
	public synchronized int getModuleID(UUID u) {
		long msb = u.getMostSignificantBits();
		long cid = msb >> contextShift;
		return (int)(msb - (cid << contextShift));
	}

	/* (non-Javadoc)
	 * @see com.openexchange.eav.UUIDService#getObjectID(java.util.UUID)
	 */
	@Override
	public synchronized int getObjectID(UUID u) {
		return (int)u.getLeastSignificantBits();
	}
}
