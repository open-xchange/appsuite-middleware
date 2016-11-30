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

package com.openexchange.admin.rmi.impl;

import java.rmi.RemoteException;
import org.osgi.framework.BundleContext;
import com.openexchange.admin.plugins.OXUserPluginInterface;
import com.openexchange.admin.rmi.OXLoginInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.PluginInterfaces;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUserStorageInterface;

/**
 *
 * @author d7
 * @author cutmasta
 */
public class OXLogin extends OXCommonImpl implements OXLoginInterface {

    private final static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OXLogin.class);

    private final BundleContext context;

    public OXLogin(final BundleContext context) throws RemoteException, StorageException {
        super();
        this.context = context;
        LOGGER.info("Class loaded: {}", this.getClass().getName());
    }

    @Override
    public void login(final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException,DatabaseUpdateException {
        BasicAuthenticator.createNonPluginAwareAuthenticator().doUserAuthentication(auth, ctx);
        triggerUpdateProcess(ctx);
    }

    @Override
    public void login(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        doNullCheck(auth);
        BasicAuthenticator.createNonPluginAwareAuthenticator().doAuthentication(auth);
    }

    @Override
    public User login2User(final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        BasicAuthenticator.createNonPluginAwareAuthenticator().doUserAuthentication(auth, ctx);

        triggerUpdateProcess(ctx);

        int user_id;
        try {
            user_id = tool.getUserIDByUsername(ctx, auth.getLogin());
        } catch (NoSuchUserException e) {
            throw new StorageException(e);
        }
        tool.isContextAdmin(ctx, user_id);
        final User retval = new User(user_id);
        retval.setName(auth.getLogin());

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();

        User[] retusers = oxu.getData(ctx, new User[] { retval });

        // Trigger plugin extensions
        {
            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (final OXUserPluginInterface oxuserplugin : pluginInterfaces.getUserPlugins().getServiceList()) {
                    LOGGER.debug("Calling getData for plugin: {}", oxuserplugin.getClass().getName());
                    retusers = oxuserplugin.getData(ctx, retusers, auth);
                }
            }
        }

        return retusers[0];
    }

    private void triggerUpdateProcess(Context ctx) throws DatabaseUpdateException {
        // Check for update.
        try {
            OXToolStorageInterface oxt = OXToolStorageInterface.getInstance();
            if (oxt.checkAndUpdateSchemaIfRequired(ctx)) {
                oxt.generateDatabaseUpdateException(ctx.getId().intValue());
            }
        } catch (StorageException e) {
            LOGGER.error("Error running updateprocess", e);
            throw new DatabaseUpdateException(e.toString());
        }
    }

}
