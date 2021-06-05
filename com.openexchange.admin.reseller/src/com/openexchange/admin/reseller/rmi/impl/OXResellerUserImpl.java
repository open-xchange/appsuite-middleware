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

package com.openexchange.admin.reseller.rmi.impl;

import java.util.Set;
import com.openexchange.admin.plugins.OXUserPluginInterface;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.storage.interfaces.OXResellerStorageInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 *
 */
public class OXResellerUserImpl implements OXUserPluginInterface {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXResellerUserImpl.class);

    private OXResellerStorageInterface oxresell = null;

    /**
     * @throws StorageException
     *
     */
    public OXResellerUserImpl() throws StorageException {
        oxresell = OXResellerStorageInterface.getInstance();
    }

    @Override
    public boolean canHandleContextAdmin() {
        return true;
    }

    @Override
    public void change(Context ctx, User usrdata, Credentials auth) throws PluginException {
        // Nothing to do
    }

    @Override
    public void changeCapabilities(Context ctx, User user, Set<String> capsToAdd, Set<String> capsToRemove, Set<String> capsToDrop, Credentials auth) throws PluginException {
        // Nothing to do
    }

    @Override
    public void changeMailAddressPersonal(Context ctx, User user, String personal, Credentials auth) throws PluginException {
        // Nothing to do
    }

    @Override
    public void changeModuleAccess(Context ctx, User user, String access_combination_name, Credentials auth) throws PluginException {
        // Nothing to do
    }

    @Override
    public void changeModuleAccess(Context ctx, User user, UserModuleAccess moduleAccess, Credentials auth) throws PluginException {
        // Nothing to do
    }

    @Override
    public void create(Context ctx, User usr, UserModuleAccess access, Credentials cred) throws PluginException {
        try {
            final ResellerAdmin owner = oxresell.getContextOwner(ctx);
            if ( 0 == owner.getId().intValue() ) {
                // if context has no owner, restriction checks cannot be done and
                // context has been created by master admin
                return;
            }
            //long tstart = System.currentTimeMillis();
            oxresell.checkPerContextRestrictions(ctx, access, false,
                Restriction.MAX_USER_PER_CONTEXT,
                Restriction.MAX_OVERALL_USER_PER_SUBADMIN,
                Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX,
                Restriction.MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX);
            //long tend = System.currentTimeMillis();
            //System.out.println("Time: " + (tend - tstart) + " ms");
        } catch (StorageException e) {
            log.error("",e);
            throw new PluginException(e);
        }
    }

    @Override
    public void delete(Context ctx, User[] user, Credentials cred) throws PluginException {
        // Nothing to do
    }

    @Override
    public User[] getData(Context ctx, User[] users, Credentials cred) {
        // pass-through
        return users;
    }

}
