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

import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;

/**
 * {@link ResourceManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ResourceManager extends AbstractManager {

    private static ResourceManager INSTANCE;

    /**
     * Gets the instance of the {@link ResourceManager}
     * 
     * @param host
     * @param masterCredentials
     * @return
     */
    public static ResourceManager getInstance(String host, Credentials masterCredentials) {
        if (INSTANCE == null) {
            INSTANCE = new ResourceManager(host, masterCredentials);
        }
        return INSTANCE;
    }

    /**
     * Initialises a new {@link ResourceManager}.
     * 
     * @param rmiEndPointURL
     * @param masterCredentials
     */
    private ResourceManager(String rmiEndPointURL, Credentials masterCredentials) {
        super(rmiEndPointURL, masterCredentials);
    }

    /**
     * Creates the specified {@link Resource} in the specified {@link Context}
     * 
     * @param resource The {@link Resource} to create
     * @param context The {@link Context}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @return The created {@link Resource}
     * @throws Exception if an error is occurred
     */
    public Resource create(Resource resource, Context context, Credentials contextAdminCredentials) throws Exception {
        OXResourceInterface resourceInterface = getResourceInterface();
        return resourceInterface.create(context, resource, contextAdminCredentials);
    }

    /**
     * Retrieves all data of the specified {@link Resource} in the specified {@link Context}
     * 
     * @param resource The {@link Resource}
     * @param context The {@link Context}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @return The {@link Resource} with all its data loaded
     * @throws Exception if an error is occurred
     */
    public Resource getData(Resource resource, Context context, Credentials contextAdminCredentials) throws Exception {
        OXResourceInterface resourceInterface = getResourceInterface();
        return resourceInterface.getData(context, resource, contextAdminCredentials);
    }

    /**
     * Retrieves an array with all found {@link Resource} in the specified {@link Context}
     * that match the specified search pattern.
     * 
     * @param context The {@link Context}
     * @param searchPattern The search pattern
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @return An array with all found {@link Resource}s
     * @throws Exception if an error is occurred
     */
    public Resource[] search(Context context, String searchPattern, Credentials contextAdminCredentials) throws Exception {
        OXResourceInterface resourceInterface = getResourceInterface();
        return resourceInterface.list(context, searchPattern, contextAdminCredentials);
    }

    /**
     * Retrieves an array with all {@link Resource}s in the specified {@link Context}
     * 
     * @param context The {@link Context}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @return An array with all {@link Resource}s
     * @throws Exception if an error is occurred
     */
    public Resource[] listAll(Context context, Credentials contextAdminCredentials) throws Exception {
        OXResourceInterface resourceInterface = getResourceInterface();
        return resourceInterface.listAll(context, contextAdminCredentials);
    }

    /**
     * Changes the specified {@link Resource} in the specified {@link Context}
     * 
     * @param resource The {@link Resource} to change
     * @param context The {@link Context}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @throws Exception if an error is occurred
     */
    public void change(Resource resource, Context context, Credentials contextAdminCredentials) throws Exception {
        OXResourceInterface resourceInterface = getResourceInterface();
        resourceInterface.change(context, resource, contextAdminCredentials);
    }

    /**
     * Deletes the specified {@link Resource} from the specified {@link Context}
     * 
     * @param resource The {@link Resource} to delete
     * @param context The {@link Context}
     * @param contextAdminCredentials The context's admin {@link Credentials}
     * @throws Exception if an error is occurred
     */
    public void delete(Resource resource, Context context, Credentials contextAdminCredentials) throws Exception {
        OXResourceInterface resourceInterface = getResourceInterface();
        resourceInterface.delete(context, resource, contextAdminCredentials);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.manager.AbstractManager#clean(java.lang.Object)
     */
    @Override
    boolean clean(Object object) {
        // Nothing to do, the resource will be implicitly deleted when the context is deleted.
        return true;
    }

    /**
     * Retrieves the remote {@link OXResourceInterface}
     * 
     * @return the remote {@link OXResourceInterface}
     * @throws Exception if the remote interface cannot be retrieved
     */
    private OXResourceInterface getResourceInterface() throws Exception {
        return getRemoteInterface(OXResourceInterface.RMI_NAME, OXResourceInterface.class);
    }
}
