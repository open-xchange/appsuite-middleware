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

package com.openexchange.file.storage;

import java.sql.Connection;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link AdministrativeFileStorageAccountStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public interface AdministrativeFileStorageAccountStorage {

    /**
     * Returns all file storage accounts in the specified context
     *
     * @param contextId The context identifier
     * @return all file storage accounts in the specified context
     * @throws OXException if an error is occurred
     */
    List<AdministrativeFileStorageAccount> getAccounts(int contextId) throws OXException;

    /**
     * Returns all file storage accounts in the specified context for the
     * specified provider
     *
     * @param contextId The context identifier
     * @param providerId The provider identifier
     * @return all file storage accounts in the specified context
     * @throws OXException if an error is occurred
     */
    List<AdministrativeFileStorageAccount> getAccounts(int contextId, String providerId) throws OXException;

    /**
     * Returns all file storage accounts in the specified context for the
     * specified user
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return all file storage accounts in the specified context
     * @throws OXException if an error is occurred
     */
    List<AdministrativeFileStorageAccount> getAccounts(int contextId, int userId) throws OXException;

    /**
     * Returns all file storage accounts in the specified context for the
     * specified user and the specified provider
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param providerId The provider identifier
     * @return all file storage accounts in the specified context
     * @throws OXException if an error is occurred
     */
    List<AdministrativeFileStorageAccount> getAccounts(int contextId, int userId, String providerId) throws OXException;

    /**
     * Deletes the specified file storage account
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return <code>true</code> if the account was successfully deleted; <code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    boolean deleteAccount(int contextId, int userId, int accountId) throws OXException;

    /**
     * Deletes the specified file storage account
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @param connection The writeable connection
     * @return <code>true</code> if the account was successfully deleted; <code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    boolean deleteAccount(int contextId, int userId, int accountId, Connection connection) throws OXException;
}
