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

package com.openexchange.user.internal;

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.user.UserService;

/**
 * {@link UserServiceImpl} - The {@link UserService} implementation
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserServiceImpl implements UserService {

    /**
     * Initializes a new {@link UserServiceImpl}
     */
    public UserServiceImpl() {
        super();
    }

    @Override
    public String getUserAttribute(String name, int userId, Context context) throws OXException {
        try {
            return UserStorage.getInstance().getUserAttribute(name, userId, context);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public void setUserAttribute(String name, String value, int userId, Context context) throws OXException {
        try {
            UserStorage.getInstance().setUserAttribute(name, value, userId, context);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public void setAttribute(String name, String value, int userId, Context context) throws OXException {
        try {
            UserStorage.getInstance().setAttribute(name, value, userId, context);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public User getUser(int uid, Context context) throws OXException {
        try {
            return UserStorage.getInstance().getUser(uid, context);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public User[] getUser(Context context, int[] userIds) throws OXException {
        return UserStorage.getInstance().getUser(context, userIds);
    }

    @Override
    public User[] getUser(Context context) throws OXException {
        return UserStorage.getInstance().getUser(context);
    }

    @Override
    public int getUserId(final String loginInfo, final Context context) throws OXException {
        try {
            return UserStorage.getInstance().getUserId(loginInfo, context);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public void invalidateUser(final Context ctx, final int userId) throws OXException {
        UserStorage.getInstance().invalidateUser(ctx, userId);
    }

    @Override
    public int[] listAllUser(final Context context) throws OXException {
        return UserStorage.getInstance().listAllUser(context);
    }

    @Override
    public int[] listModifiedUser(final Date modifiedSince, final Context context) throws OXException {
        try {
            return UserStorage.getInstance().listModifiedUser(modifiedSince, context);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public int[] resolveIMAPLogin(final String imapLogin, final Context context) throws OXException {
        return UserStorage.getInstance().resolveIMAPLogin(imapLogin, context);
    }

    @Override
    public User searchUser(final String email, final Context context) throws OXException {
        try {
            return UserStorage.getInstance().searchUser(email, context);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    @Override
    public void updateUser(final User user, final Context context) throws OXException {
        try {
            UserStorage.getInstance().updateUser(user, context);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean authenticate(final User user, final String password) throws OXException {
        return UserStorage.authenticate(user, password);
    }

}
