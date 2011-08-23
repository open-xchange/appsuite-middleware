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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.appstore;

import java.util.List;
import com.openexchange.appstore.internal.ReleaseStatus;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;

/**
 * {@link AppStoreService}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public interface AppStoreService {

    public static final String locationProperty = "com.openexchange.app.descriptions.path";

    /**
     * Lists all available Applications
     *
     * @return
     * @throws OXException
     */
    public List<Application> list(Context context, User user) throws OXException;

    /**
     * Lists Applications installed for the given user.
     *
     * @param user
     * @return
     * @throws OXException
     */
    public List<Application> installed(Context context, User user) throws OXException;

    /**
     * Lists all Applications for the given category.
     *
     * @param category
     * @return
     */
    public List<Application> list(String category);

    /**
     * Installs an Application
     *
     * @param user
     * @param id
     * @throws OXException
     */
    public boolean install(Context context, User user, String id) throws OXException;

    /**
     * Uninstalls an Application
     *
     * @param user
     * @param id
     * @throws OXException
     */
    public boolean uninstall(Context context, User user, String id) throws OXException;

    /**
     * Crawl the file system and stores all available Applications in the database. Also removes no longer existing Applications.
     *
     * @param user
     * @param context
     * @return All available Applications
     * @throws OXException
     */
    public List<Application> crawl(User user, Context context) throws OXException;

    /**
     * Makes a given Application available for installation. If no context or user is given, the availability is global. If a context is
     * given, the availability is restrictied to this context. Same for user (needs both, user AND context).
     *
     * @param contextId
     * @param userId
     * @param application
     * @throws OXException
     */
    public void release(Integer contextId, Integer userId, Application application) throws OXException;

    /**
     * Revokes availability of an Application. If no context or user is given, the availability is global. If a context is given, the
     * availability is restrictied to this context. Same for user (needs both, user AND context).
     *
     * @param contextId
     * @param userId
     * @param application
     * @throws OXException
     */
    public void revoke(Integer contextId, Integer userId, Application application) throws OXException;

    /**
     * Returns all release status of a given application Id.
     *
     * @param applicationId
     * @return
     * @throws OXException
     */
    public List<ReleaseStatus> status(String applicationId) throws OXException;

}
