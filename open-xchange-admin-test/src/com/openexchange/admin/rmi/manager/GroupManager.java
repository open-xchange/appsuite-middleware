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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.admin.rmi.manager;

import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.groupware.i18n.Groups;

/**
 * {@link GroupManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class GroupManager extends AbstractManager {

    private static GroupManager INSTANCE;

    /**
     * Gets the instance of the {@link GroupManager}
     * 
     * @param host
     * @param masterCredentials
     * @return
     */
    public static GroupManager getInstance(String host, Credentials masterCredentials) {
        if (INSTANCE == null) {
            INSTANCE = new GroupManager(host, masterCredentials);
        }
        return INSTANCE;
    }

    /**
     * Initialises a new {@link GroupManager}.
     * 
     * @param rmiEndPointURL
     * @param masterCredentials
     */
    private GroupManager(String rmiEndPointURL, Credentials masterCredentials) {
        super(rmiEndPointURL, masterCredentials);
    }

    /**
     * Creates the specified {@link Group} in the specified {@link Context}
     * 
     * @param group The {@link Group}
     * @param context The {@link Context}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @return The newly created {@link Group}
     * @throws Exception if an error is occurred
     */
    public Group createGroup(Group group, Context context, Credentials contextAdminCredentials) throws Exception {
        OXGroupInterface groupInterface = getGroupInterface();
        return groupInterface.create(context, group, contextAdminCredentials);
    }

    /**
     * Retrieves all data of the specified {@link Group} in the specified {@link Context}
     * 
     * @param Group The {@link Group}
     * @param context The {@link Context}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @return The {@link Group} with all its data loaded
     * @throws Exception if an error is occurred
     */
    public Group getData(Group Group, Context context, Credentials contextAdminCredentials) throws Exception {
        OXGroupInterface groupInterface = getGroupInterface();
        return groupInterface.getData(context, Group, contextAdminCredentials);
    }

    /**
     * Retrieves an array with all found {@link Group} in the specified {@link Context}
     * that match the specified search pattern.
     * 
     * @param context The {@link Context}
     * @param searchPattern The search pattern
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @return An array with all found {@link Group}s
     * @throws Exception if an error is occurred
     */
    public Group[] listGroups(Context context, String searchPattern, Credentials contextAdminCredentials) throws Exception {
        OXGroupInterface groupInterface = getGroupInterface();
        return groupInterface.list(context, searchPattern, contextAdminCredentials);
    }

    /**
     * Retrieves an array with all {@link Group}s in the specified {@link Context}
     * 
     * @param context The {@link Context}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @return An array with all found {@link Group}s
     * @throws Exception if an error is occurred
     */
    public Group[] listAllGroups(Context context, Credentials contextAdminCredentials) throws Exception {
        OXGroupInterface groupInterface = getGroupInterface();
        return groupInterface.listAll(context, contextAdminCredentials);
    }

    /**
     * Changes the specified {@link Group} in the specified {@link Context}
     * 
     * @param Group The {@link Group} to change
     * @param context The {@link Context}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @throws Exception if an error is occurred
     */
    public void changeGroup(Group Group, Context context, Credentials contextAdminCredentials) throws Exception {
        OXGroupInterface groupInterface = getGroupInterface();
        groupInterface.change(context, Group, contextAdminCredentials);
    }

    /**
     * Returns an array with all members of the specified {@link Group} in the specified {@link Context}
     * 
     * @param group The {@link Group}
     * @param context The {@link Context}
     * @param contextAdminCredentials The {@link Credentials}
     * @return An array with all members of the specified group
     * @throws Exception if an error is occurred
     */
    public User[] getMembers(Group group, Context context, Credentials contextAdminCredentials) throws Exception {
        OXGroupInterface groupInterface = getGroupInterface();
        return groupInterface.getMembers(context, group, contextAdminCredentials);
    }

    /**
     * Adds the specified {@link User}s as members to the specified {@link Group} in the
     * specified {@link Context}
     * 
     * @param group The {@link Group}
     * @param context The {@link Context}
     * @param members The members
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @throws Exception if an error is occurred
     */
    public void addMember(Group group, Context context, User[] members, Credentials contextAdminCredentials) throws Exception {
        OXGroupInterface groupInterface = getGroupInterface();
        groupInterface.addMember(context, group, members, contextAdminCredentials);
    }

    /**
     * Removes the specified {@link User}s from the specified {@link Group} in the
     * specified {@link Context}
     * 
     * @param group The {@link Group}
     * @param context The {@link Context}
     * @param members The members
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @throws Exception if an error is occurred
     */
    public void removeMember(Group group, Context context, User[] members, Credentials contextAdminCredentials) throws Exception {
        OXGroupInterface groupInterface = getGroupInterface();
        groupInterface.removeMember(context, group, members, contextAdminCredentials);
    }

    /**
     * Returns an array with all {@link Group}s that the specified {@link User} is member of
     * 
     * @param user The {@link User}
     * @param context The {@link Context}
     * @param contextAdminCredentials The context admin's credentials
     * @return An array with all {@link Groups} that the user is member
     * @throws Exception if an error is occurred
     */
    public Group[] listUserGroups(User user, Context context, Credentials contextAdminCredentials) throws Exception {
        OXGroupInterface groupInterface = getGroupInterface();
        return groupInterface.listGroupsForUser(context, user, contextAdminCredentials);
    }

    /**
     * Deletes the specified {@link Group} from the specified {@link Context}
     * 
     * @param Group The {@link Group} to delete
     * @param context The {@link Context}
     * @param contextAdminCredentials The context's admin {@link Credentials}
     * @throws Exception if an error is occurred
     */
    public void deleteGroup(Group Group, Context context, Credentials contextAdminCredentials) throws Exception {
        OXGroupInterface groupInterface = getGroupInterface();
        groupInterface.delete(context, Group, contextAdminCredentials);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.manager.AbstractManager#clean(java.lang.Object)
     */
    @Override
    boolean clean(Object object) {
        // Nothing to do, the group will be implicitly deleted when the context is deleted.
        return true;
    }

    /**
     * Retrieves the remote {@link OXGroupInterface}
     * 
     * @return the remote {@link OXGroupInterface}
     * @throws Exception if the remote interface cannot be retrieved
     */
    private OXGroupInterface getGroupInterface() throws Exception {
        return getRemoteInterface(OXGroupInterface.RMI_NAME, OXGroupInterface.class);
    }
}
