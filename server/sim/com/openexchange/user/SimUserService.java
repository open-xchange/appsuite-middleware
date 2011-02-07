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

package com.openexchange.user;

import java.util.Date;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;


/**
 * {@link SimUserService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimUserService implements UserService {

    public String getUserAttribute(String name, int userId, Context context) throws UserException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setUserAttribute(String name, String value, int userId, Context context) throws UserException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#authenticate(com.openexchange.groupware.ldap.User, java.lang.String)
     */
    public boolean authenticate(User user, String password) throws UserException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#getUser(int, com.openexchange.groupware.contexts.Context)
     */
    public User getUser(int uid, Context context) throws UserException {
        // TODO Auto-generated method stub
        return null;
    }

    public User[] getUser(Context context, int[] userIds) throws UserException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#getUser(com.openexchange.groupware.contexts.Context)
     */
    public User[] getUser(Context ctx) throws UserException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#getUserId(java.lang.String, com.openexchange.groupware.contexts.Context)
     */
    public int getUserId(String loginInfo, Context context) throws UserException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#invalidateUser(com.openexchange.groupware.contexts.Context, int)
     */
    public void invalidateUser(Context ctx, int userId) throws UserException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#listAllUser(com.openexchange.groupware.contexts.Context)
     */
    public int[] listAllUser(Context context) throws UserException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#listModifiedUser(java.util.Date, com.openexchange.groupware.contexts.Context)
     */
    public int[] listModifiedUser(Date modifiedSince, Context context) throws UserException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#resolveIMAPLogin(java.lang.String, com.openexchange.groupware.contexts.Context)
     */
    public int[] resolveIMAPLogin(String imapLogin, Context context) throws UserException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#searchUser(java.lang.String, com.openexchange.groupware.contexts.Context)
     */
    public User searchUser(String email, Context context) throws UserException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#updateUser(com.openexchange.groupware.ldap.User, com.openexchange.groupware.contexts.Context)
     */
    public void updateUser(User user, Context context) throws UserException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.user.UserService#setAttribute(java.lang.String, java.lang.String, int, com.openexchange.groupware.contexts.Context)
     */
    public void setAttribute(String name, String value, int userId, Context context) throws UserException {
        // TODO Auto-generated method stub
        
    }

}
