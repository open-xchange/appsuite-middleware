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

package com.openexchange.admin.autocontextid.rmi.impl;

import java.util.List;
import java.util.Set;
import com.openexchange.admin.autocontextid.storage.interfaces.OXAutoCIDStorageInterface;
import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;
import com.openexchange.tools.pipesnfilters.Filter;

/**
 * @author choeger
 */
public class OXAutoCIDContextImpl implements OXContextPluginInterface {

    private final OXAutoCIDStorageInterface oxautocid;

    public OXAutoCIDContextImpl() throws StorageException {
        super();
        oxautocid = OXAutoCIDStorageInterface.getInstance();
    }

    @Override
    public void change(final Context ctx, final Credentials auth) {
        // Nothing to do.
    }

    @Override
    public void changeModuleAccess(final Context ctx, final UserModuleAccess access, final Credentials auth) {
        // Nothing to do.
    }

    @Override
    public void changeModuleAccess(final Context ctx, final String access_combination_name, final Credentials auth) {
        // Nothing to do.
    }

    @Override
    public void changeCapabilities(Context ctx, Set<String> capsToAdd, Set<String> capsToRemove, Set<String> capsToDrop, Credentials auth) throws PluginException {
        // Nothing to do.
    }

    @Override
    public void changeQuota(Context ctx, String module, long quotaValue, Credentials auth) throws PluginException {
        // Nothing to do.
    }

    @Override
    public Context postCreate(final Context ctx, final User admin_user, final UserModuleAccess access, final Credentials auth) {
        return ctx;
    }

    @Override
    public Context preCreate(final Context ctx, final User admin_user, final Credentials auth) throws PluginException {
        try {
            final int id = oxautocid.generateContextId();
            ctx.setId(Integer.valueOf(id));
            return ctx;
        } catch (StorageException e) {
            throw new PluginException(e);
        }
    }

    @Override
    public void delete(final Context ctx, final Credentials auth) {
        // Nothing to do.
    }

    @Override
    public void disable(final Context ctx, final Credentials auth) {
        // Nothing to do.
    }

    @Override
    public void disableAll(final Credentials auth) {
        // Nothing to do.
    }

    @Override
    public void downgrade(final Context ctx, final Credentials auth) {
        // Nothing to do.
    }

    @Override
    public void enable(final Context ctx, final Credentials auth) {
        // Nothing to do.
    }

    @Override
    public void enableAll(final Credentials auth) {
        // Nothing to do.
    }

    @Override
    public String getAccessCombinationName(final Context ctx, final Credentials auth) {
        return null;
    }

    @Override
    public List<OXCommonExtension> getData(final List<Context> ctxs, final Credentials auth) {
        return null;
    }

    @Override
    public UserModuleAccess getModuleAccess(final Context ctx, final Credentials auth) {
        return null;
    }

    @Override
    public Filter<Context, Context> list(final String search_pattern, final Credentials auth) {
        return null;
    }

    @Override
    public Filter<Integer, Integer> filter(final Credentials auth) {
        return null;
    }

    @Override
    public Boolean checkMandatoryMembersContextCreate(final Context ctx) {
        return new Boolean(true);
    }

    @Override
    public void exists(final Context ctx, final Credentials auth) {
        // Nothing to do.
    }

    @Override
    public void existsInServer(Context ctx, Credentials auth) throws PluginException {
        // Nothing to do.
    }

    @Override
    public void getAdminId(final Context ctx, final Credentials auth) {
        // Nothing to do.
    }
}
