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

package com.openexchange.authentication.application;

import java.util.List;
import java.util.Map;
import com.openexchange.authentication.application.storage.history.AppPasswordLogin;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link AppPasswordService} entry point for handling the storage of application specific passwords
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
@SingletonService
public interface AppPasswordService {

    /**
     * Remove an application specific password from the database
     *
     * @param session The session
     * @param passwordId The identifier of the ApplicationPassword to remove
     * @throws OXException if an error is occurred
     */
    void removePassword(Session session, String passwordId) throws OXException;

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
     * Get a list of the passwords for a users account
     *
     * @param session The session
     * @return Collection of ApplicationPasswords
     * @throws OXException if an error is occurred
     */
    List<ApplicationPassword> getList(Session session) throws OXException;

    /**
     * Gets the last logins for all application-specific passwords of the session's user.
     * 
     * @param session The session
     * @return The last logins, associated to the corresponding password's identifier, or an empty map if there are none
     */
    Map<String, AppPasswordLogin> getLastLogins(Session session) throws OXException;

    /**
     * Returns list of applications the can be configured for application passwords
     * getApplications
     *
     * @param session The session
     * @return A list of applications that can be configured
     * @throws OXException if an error is occurred
     */
    List<AppPasswordApplication> getApplications(Session session) throws OXException;

}
