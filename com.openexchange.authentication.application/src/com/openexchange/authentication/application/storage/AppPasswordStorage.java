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

package com.openexchange.authentication.application.storage;

import java.util.List;
import com.openexchange.authentication.application.AppLoginRequest;
import com.openexchange.authentication.application.ApplicationPassword;
import com.openexchange.authentication.application.storage.history.AppPasswordLoginHistoryStorage;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link AppPasswordStorage} handles storage of application specific passwords
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public interface AppPasswordStorage {

    /**
     * Checks if this storage handles this login string.
     *
     * @param loginRequest The login request
     * @return True if the storage handles this application password
     * @throws OXException if an invalid login request object is supplied
     */
    boolean handles(AppLoginRequest loginRequest) throws OXException;

    /**
     * Checks if this storage handles this type of application, prior adding a new password.
     *
     * @param session The session
     * @param appType The application type identifier to check
     * @return <code>true</code> if the storage is handling app-specific passwords for this application type, <code>false</code>, otherwise
     */
    boolean handles(Session session, String appType) throws OXException;

    /**
     * Remove an application specific password from the database
     *
     * @param session The session
     * @param passwordid The identifier of the ApplicationPassword to remove
     * @return The removed password, or <code>null</code> if not found
     * @throws OXException if an error is occurred
     */
    boolean removePassword(Session session, String passwordId) throws OXException;

    /**
     * Add a password to the database.
     * 
     * @param session The session
     * @param appName Application name, user chosen
     * @param appType The application type
     * @throws OXException if an error is occurred
     */
    ApplicationPassword addPassword(Session session, String appName, String appType) throws OXException;

    /**
     * Authenticates and resolves the login and password against the database.
     * Returns the ApplicationPassword if found.
     *
     * @param login The login string
     * @param password The users password
     * @return An ApplicationPassword populated with the user information and permissions
     * @throws OXException If bad authentication or error
     */
    AuthenticatedApplicationPassword doAuth(String login, String password) throws OXException;

    /**
     * Get a list of the passwords for a users account
     *
     * @param session The session
     * @return Collection of ApplicationPasswords
     * @throws OXException if an error is occurred
     */
    List<ApplicationPassword> getList(Session session) throws OXException;

    /**
     * Gets the accompanying login history storage.
     * 
     * @return The login history storage
     */
    AppPasswordLoginHistoryStorage getLoginHistoryStorage();

}
