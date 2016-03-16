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

package com.openexchange.admin.plugins;

import java.util.Set;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;

public interface OXUserPluginInterface {

    public void create(final Context ctx, final User usr, final UserModuleAccess access, final Credentials cred) throws PluginException;

    public void delete(final Context ctx, final User[] user, final Credentials cred) throws PluginException;

    public void change(final Context ctx, final User usrdata, final Credentials auth) throws PluginException;

    public User[] getData(final Context ctx, final User[] users, final Credentials cred);

    /**
     * This Method is used for each plugin to check if it makes sense to run as administrator.
     *
     * @return
     */
    public boolean canHandleContextAdmin();

    /**
     * Changes the personal part of specified user's E-Mail address.
     *
     * @param ctx The context
     * @param user The user
     * @param personal The personal to set or <code>null</code> to drop the personal information (if any)
     * @param auth The credentials
     * @throws PluginException If operation fails
     */
    public void changeMailAddressPersonal(Context ctx, User user, String personal, Credentials auth) throws PluginException;

    /**
     * Changes specified user's capabilities.
     *
     * @param ctx The context
     * @param user The user
     * @param capsToAdd The capabilities to add
     * @param capsToRemove The capabilities to remove
     * @param capsToDrop The capabilities to drop; e.g. clean from storage
     * @param auth The credentials
     * @throws PluginException If changing capabilities fails
     */
    public void changeCapabilities(Context ctx, User user, Set<String> capsToAdd, Set<String> capsToRemove, Set<String> capsToDrop, Credentials auth) throws PluginException;

    /**
     * Manipulate user module access within the given context.
     *
     * @param ctx Context object.
     * @param user_id int containing the user id.
     * @param access String containing access combination name.
     * @param auth Credentials for authenticating against server.
     * @throws PluginException If change operation fails
     */
    public void changeModuleAccess(Context ctx, User user, String access_combination_name, Credentials auth) throws PluginException;

    /**
     * Manipulate user module access within the given context.
     *
     * @param ctx Context object.
     * @param userId int[] containing the user id.
     * @param moduleAccess UserModuleAccess containing module access.
     * @param auth Credentials for authenticating against server.
     * @throws PluginException If change operation fails
     */
    public void changeModuleAccess(Context ctx, User user, UserModuleAccess moduleAccess, Credentials auth) throws PluginException;

}
