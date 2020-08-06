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

package com.openexchange.external.account;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * {@link ExternalAccountRMIService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public interface ExternalAccountRMIService extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "ExternalAccountRMIService";

    /**
     * Lists all {@link ExternalAccount}s in the specified context.
     *
     * @param contextId The context identifier
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId) throws RemoteException;

    /**
     * Lists all {@link ExternalAccount}s of the specified user
     * in the specified context.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId, int userId) throws RemoteException;

    /**
     * Lists all {@link ExternalAccount}s of the specified provider and
     * in the specified context
     *
     * @param contextId The context identifier
     * @param providerId The provider identifier
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId, String providerId) throws RemoteException;

    /**
     * Lists all {@link ExternalAccount}s of the specified module and
     * in the specified context
     *
     * @param contextId The context identifier
     * @param module The module
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId, ExternalAccountModule module) throws RemoteException;

    /**
     * Lists all {@link ExternalAccount}s of the specified module and
     * in the specified context and for the specified user
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param module The module
     * @param providerId The provider identifier
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId, int userId, ExternalAccountModule module) throws RemoteException;

    /**
     * Lists all {@link ExternalAccount}s of the specified user
     * in the specified context and of the specified provider.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param providerId The provider identifier
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId, int userId, String providerId) throws RemoteException;

    /**
     * Lists all {@link ExternalAccount}s of the specified context the specified provider
     * and the specified module.
     *
     * @param contextId The context identifier
     * @param providerId The provider identifier
     * @param module The module
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId, String providerId, ExternalAccountModule module) throws RemoteException;

    /**
     * Lists all {@link ExternalAccount}s of the specified user
     * in the specified context of the specified provider and the specified module.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param providerId The provider identifier
     * @param module The module
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId, int userId, String providerId, ExternalAccountModule module) throws RemoteException;

    /**
     * Deletes the external account with the specified identifier
     * of the specified user in the specified context
     *
     * @param id The account's identifier
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param module The module
     * @return <code>true</code> if the account was successfully deleted; <code>false</code> otherwise
     * @throws RemoteException
     */
    boolean delete(int id, int contextId, int userId, ExternalAccountModule module) throws RemoteException;
}
