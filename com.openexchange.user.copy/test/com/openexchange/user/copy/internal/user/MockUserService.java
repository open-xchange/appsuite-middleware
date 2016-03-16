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

package com.openexchange.user.copy.internal.user;

import java.sql.Connection;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.RdbUserStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.user.UserService;


/**
 * {@link MockUserService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MockUserService implements UserService {
    
    private final UserStorage storage;
    
    
    public MockUserService() {
        super();
        storage = new RdbUserStorage();
    }

    /**
     * @see com.openexchange.user.UserService#getUserAttribute(java.lang.String, int, com.openexchange.groupware.contexts.Context)
     */
    public String getUserAttribute(final String name, final int userId, final Context context) throws OXException {
        return null;
    }

    /**
     * @see com.openexchange.user.UserService#setUserAttribute(java.lang.String, java.lang.String, int, com.openexchange.groupware.contexts.Context)
     */
    public void setUserAttribute(final String name, final String value, final int userId, final Context context) throws OXException {
    }

    /**
     * @see com.openexchange.user.UserService#setAttribute(java.lang.String, java.lang.String, int, com.openexchange.groupware.contexts.Context)
     */
    public void setAttribute(final String name, final String value, final int userId, final Context context) throws OXException {
    }

    /**
     * @see com.openexchange.user.UserService#getUserId(java.lang.String, com.openexchange.groupware.contexts.Context)
     */
    public int getUserId(final String loginInfo, final Context context) throws OXException {
        return 0;
    }

    /**
     * @see com.openexchange.user.UserService#getUser(int, com.openexchange.groupware.contexts.Context)
     */
    public User getUser(final int uid, final Context context) throws OXException {
        return null;
    }

    /**
     * @see com.openexchange.user.UserService#getUser(java.sql.Connection, int, com.openexchange.groupware.contexts.Context)
     */
    public User getUser(final Connection con, final int uid, final Context context) throws OXException {
        return storage.getUser(context, uid, con);
    }

    /**
     * @see com.openexchange.user.UserService#createUser(java.sql.Connection, com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User)
     */
    public int createUser(final Connection con, final Context context, final User user) throws OXException {
        return storage.createUser(con, context, user);
    }

    /**
     * @see com.openexchange.user.UserService#createUser(com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User)
     */
    public int createUser(final Context context, final User user) throws OXException {
        return 0;
    }

    /**
     * @see com.openexchange.user.UserService#getUser(com.openexchange.groupware.contexts.Context, int[])
     */
    public User[] getUser(final Context context, final int[] userIds) throws OXException {
        return null;
    }

    /**
     * @see com.openexchange.user.UserService#getUser(com.openexchange.groupware.contexts.Context)
     */
    public User[] getUser(final Context ctx) throws OXException {
        return null;
    }

    /**
     * @see com.openexchange.user.UserService#updateUser(com.openexchange.groupware.ldap.User, com.openexchange.groupware.contexts.Context)
     */
    public void updateUser(final User user, final Context context) throws OXException {
    }

    /**
     * @see com.openexchange.user.UserService#searchUser(java.lang.String, com.openexchange.groupware.contexts.Context)
     */
    public User searchUser(final String email, final Context context) throws OXException {
        return null;
    }

    /**
     * @see com.openexchange.user.UserService#searchUserByName(java.lang.String, com.openexchange.groupware.contexts.Context, int)
     */
    public User[] searchUserByName(final String name, final Context context, final int searchType) throws OXException {
        return null;
    }

    /**
     * @see com.openexchange.user.UserService#listAllUser(com.openexchange.groupware.contexts.Context)
     */
    public int[] listAllUser(final Context context) throws OXException {
        return null;
    }

    /**
     * @see com.openexchange.user.UserService#resolveIMAPLogin(java.lang.String, com.openexchange.groupware.contexts.Context)
     */
    public int[] resolveIMAPLogin(final String imapLogin, final Context context) throws OXException {
        return null;
    }

    /**
     * @see com.openexchange.user.UserService#listModifiedUser(java.util.Date, com.openexchange.groupware.contexts.Context)
     */
    public int[] listModifiedUser(final Date modifiedSince, final Context context) throws OXException {
        return null;
    }

    /**
     * @see com.openexchange.user.UserService#invalidateUser(com.openexchange.groupware.contexts.Context, int)
     */
    public void invalidateUser(final Context ctx, final int userId) throws OXException {
    }

    /**
     * @see com.openexchange.user.UserService#authenticate(com.openexchange.groupware.ldap.User, java.lang.String)
     */
    public boolean authenticate(final User user, final String password) throws OXException {
        return false;
    }

}
