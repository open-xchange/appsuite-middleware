/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */
package com.openexchange.admin.plugins;

import java.util.List;
import java.util.Set;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;
import com.openexchange.tools.pipesnfilters.Filter;

/**
 * @author choeger
 *
 */
public interface OXContextPluginInterface {

    /**
     * Define the operations which should be done before the real change process.
     *
     * @param ctx
     * @param auth
     * @throws PluginException
     */
    public void change(final Context ctx, final Credentials auth) throws PluginException;

    /**
     * Changes specified context's capabilities.
     *
     * @param ctx The context
     * @param capsToAdd The capabilities to add
     * @param capsToRemove The capabilities to remove
     * @param capsToDrop The capabilities to drop; e.g. clean from storage
     * @param auth The credentials
     * @throws PluginException If change operation fails
     */
    public void changeCapabilities(Context ctx, Set<String> capsToAdd, Set<String> capsToRemove, Set<String> capsToDrop, Credentials auth) throws PluginException;

    /**
     * Changes specified context's capabilities.
     *
     * @param ctx The context
     * @param module The module to apply quota to
     * @param quotaValue The quota value to set
     * @param auth The credentials
     * @throws PluginException If change operation fails
     */
    public void changeQuota(Context ctx, String module, long quotaValue, Credentials auth) throws PluginException;

    /**
     * Define the operations which should be done before the real context creation process.
     *
     * @param ctx
     * @param admin_user
     * @param auth
     * @return An {@link Context} object must be returned. If nothing is done
     *         in the implementing method, the context object given in
     *         <code>ctx</code> should be returned.
     * @throws PluginException
     */
    public Context preCreate(final Context ctx, final User admin_user, final Credentials auth) throws PluginException;

    /**
     * Define the operations which should be done after the real context creation process.
     *
     * @param ctx
     * @param admin_user
     * @param access
     * @param auth
     * @return An {@link Context} object must be returned. If nothing is done
     *         in the implementing method, the context object given in
     *         <code>ctx</code> should be returned.
     * @throws PluginException
     */
    public Context postCreate(final Context ctx, final User admin_user, final UserModuleAccess access, final Credentials auth) throws PluginException;

    /**
     * Define the operations which should be done before the real context delete process.
     *
     * @param ctx
     * @param auth
     * @throws PluginException
     */
    public void delete(final Context ctx, final Credentials auth) throws PluginException;

    /**
     * Define the operations which should be done before the real context disable process.
     *
     * @param ctx
     * @param auth
     * @throws PluginException
     */
    public void disable(final Context ctx, final Credentials auth) throws PluginException;

    /**
     * Define the operations which should be done before the real context disableAll process.
     *
     * @param auth
     * @throws PluginException
     */
    public void disableAll(final Credentials auth) throws PluginException;

    /**
     * Define the operations which should be done before the real context enable process.
     *
     * @param ctx
     * @param auth
     * @throws PluginException
     */
    public void enable(final Context ctx, final Credentials auth) throws PluginException;

    /**
     * Define the operations which should be done instead of the enableAll
     * process if the credentials given are those from the master admin
     *
     * @param auth
     * @throws PluginException
     */
    public void enableAll(final Credentials auth) throws PluginException;

    /**
     * Define the operations which should be done after the real context
     * getData process.
     * Note that the real context getData process will only be called if
     * the context object wasn't filled by a list operation before.
     *
     * @param ctx
     * @param auth
     * @return A {@link List} containing {@link OXCommonExtension} objects,
     *         this can be null.
     * @throws PluginException
     */
    public List<OXCommonExtension> getData(final List<Context> ctx, final Credentials auth) throws PluginException;

    /**
     * Return a {@link Filter} to load plugin specific data or null in case no specific data available
     *
     * @param search_pattern
     * @param auth
     * @return
     * @throws PluginException
     */
    public Filter<Context, Context> list(final String search_pattern, final Credentials auth) throws PluginException;

    /**
     * Return a {@link Filter} to optionally filter out contexts
     *
     * @param auth
     * @return
     * @throws PluginException
     */
    public Filter<Integer, Integer> filter(final Credentials auth) throws PluginException;

    /**
     * Define the operations which should be done before the real context
     * changeModuleAccess process.
     *
     * @param ctx
     * @param access
     * @param auth
     * @throws PluginException
     */
    public void changeModuleAccess(final Context ctx, final UserModuleAccess access,final Credentials auth) throws PluginException;

    /**
     * Define the operations which should be done before the real context
     * changeModuleAccess process.
     *
     * @param ctx
     * @param access_combination_name
     * @param auth
     * @throws PluginException
     */
    public void changeModuleAccess(final Context ctx, final String access_combination_name,final Credentials auth) throws PluginException;

    /**
     * Define the operations which should be done before the real context
     * downgrade process.
     *
     * @param ctx
     * @param auth
     * @throws PluginException
     */
    public void downgrade(final Context ctx, final Credentials auth) throws PluginException;

    /**
     * Define the operations which should be done before the real context
     * getAccessCombinationName process.
     *
     * @param ctx
     * @param auth
     * @return Currently not used, so can be null
     * @throws PluginException
     */
    public String getAccessCombinationName(final Context ctx, final Credentials auth) throws PluginException;

    /**
     * Define the operations which should be done before the real context
     * getModuleAccess process.
     *
     * @param ctx
     * @param auth
     * @return Currently not used, so can be null
     * @throws PluginException
     */
    public UserModuleAccess getModuleAccess(final Context ctx, final Credentials auth) throws PluginException;

    /**
     * Define if the mandatory members for a context create should be checked or not
     *
     * @param ctx
     * @return If null or false are set, the check is ommitted otherwise the check is made
     * @throws PluginException
     */
    public Boolean checkMandatoryMembersContextCreate(final Context ctx) throws PluginException;

    /**
     * @param ctx
     * @param auth
     * @return
     * @throws PluginException
     */
    public void exists(Context ctx, Credentials auth) throws PluginException;

    /**
     * @param ctx
     * @param auth
     * @return
     * @throws PluginException
     */
    public void existsInServer(Context ctx, Credentials auth) throws PluginException;

    /**
     * @param ctx
     * @param auth
     * @throws PluginException
     */
    public void getAdminId(Context ctx, Credentials auth) throws PluginException;
}
