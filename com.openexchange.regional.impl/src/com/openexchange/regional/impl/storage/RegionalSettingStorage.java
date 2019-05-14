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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.regional.impl.storage;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.regional.RegionalSettings;
import com.openexchange.regional.impl.service.RegionalSettingsImpl;

/**
 * {@link RegionalSettingStorage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public interface RegionalSettingStorage {

    /**
     * Gets the stored regional settings for the given user
     *
     * @param contextId The context id
     * @param userId The user id
     * @return The stored {@link RegionalSettingsImpl} for this user or null
     * @throws OXException if an error is occurred
     */
    RegionalSettings get(int contextId, int userId) throws OXException;

    /**
     * Creates or updates a regional setting for the given user
     *
     * @param contextId The context id
     * @param userId The user id
     * @param settings The settings to store
     * @throws OXException if an error is occurred
     */
    void upsert(int contextId, int userId, RegionalSettings settings) throws OXException;

    /**
     * Deletes the entries for a given user
     *
     * @param contextId The context id
     * @param userId The user id
     * @throws OXException if an error is occurred
     */
    void delete(int contextId, int userId) throws OXException;

    /**
     * Deletes the regional settings for the specified user with in the specified context
     * by using the specified writeable connection.
     * 
     * @param contextId The context identifier
     * @param userId the user identifier
     * @param writeCon The writeable connection
     * @throws OXException if an error is occurred
     */
    void delete(int contextId, int userId, Connection writeCon) throws OXException;

    /**
     * Deletes the entries for a given context
     *
     * @param contextId The context id
     * @throws OXException if an error is occurred
     */
    void delete(int contextId) throws OXException;

    /**
     * Delets the regional settings for the specified context
     * by using the specified writeable connection.
     * 
     * @param contextId The context identifier
     * @param writeCon The writable connection
     * @throws OXException if an error is occurred
     */
    void delete(int contextId, Connection writeCon) throws OXException;

}
