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

package com.openexchange.messaging.generic.internal;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.session.Session;

/**
 * {@link MessagingAccountStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface MessagingAccountStorage {

    /**
     * Gets the denoted account.
     *
     * @param serviceId The service identifier
     * @param id The account ID
     * @param session The session
     * @return The account
     * @throws OXException If returning account fails
     */
    public MessagingAccount getAccount(String serviceId, int id, Session session, Modifier modifier) throws OXException;

    /**
     * Gets all accounts associated with specified service and given user.
     *
     * @param serviceId The service ID
     * @param session The session
     * @return All accounts associated with specified service and given user
     * @throws OXException If accounts cannot be returned
     */
    public List<MessagingAccount> getAccounts(String serviceId, Session session, Modifier modifier) throws OXException;

    /**
     * Adds given account.
     *
     * @param serviceId The service identifier
     * @param account The account
     * @param session The session
     * @return The identifier of the newly created account
     * @throws OXException If insertion fails
     */
    public int addAccount(String serviceId, MessagingAccount account, Session session, Modifier modifier) throws OXException;

    /**
     * Deletes denoted account.
     *
     * @param serviceId The service identifier
     * @param account The account
     * @param session The session
     * @throws OXException If deletion fails
     */
    public void deleteAccount(String serviceId, MessagingAccount account, Session session, Modifier modifier) throws OXException;

    /**
     * Updates given account.
     *
     * @param serviceId The service identifier
     * @param account The account
     * @param session The session
     * @throws OXException If update fails
     */
    public void updateAccount(String serviceId, MessagingAccount account, Session session, Modifier modifier) throws OXException;

}
