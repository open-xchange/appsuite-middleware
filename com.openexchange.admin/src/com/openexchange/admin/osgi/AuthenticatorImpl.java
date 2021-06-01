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

package com.openexchange.admin.osgi;

import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.BasicAuthenticator;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.auth.Authenticator;
import com.openexchange.auth.Credentials;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;


/**
 * {@link AuthenticatorImpl} - The authenticator wrapping {@code BasicAuthenticator}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class AuthenticatorImpl implements Authenticator, Reloadable {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AuthenticatorImpl.class);

    /**
     * Initializes a new {@link AuthenticatorImpl}.
     */
    public AuthenticatorImpl() {
        super();
    }

    @Override
    public boolean isMasterAuthenticationDisabled() throws OXException {
        ConfigurationService configService = AdminServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null == configService) {
            return false;
        }
        return configService.getBoolProperty("MASTER_AUTHENTICATION_DISABLED", false);
    }

    private com.openexchange.admin.rmi.dataobjects.Credentials toCreds(Credentials authdata) {
        return new com.openexchange.admin.rmi.dataobjects.Credentials(authdata.getLogin(), authdata.getPassword());
    }

    @Override
    public boolean isContextAuthenticationDisabled() throws OXException {
        ConfigurationService configService = AdminServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null == configService) {
            return false;
        }
        return configService.getBoolProperty("CONTEXT_AUTHENTICATION_DISABLED", false);
    }

    @Override
    public void doAuthentication(final Credentials authdata) throws OXException {
        try {
            BasicAuthenticator.createNonPluginAwareAuthenticator().doAuthentication(toCreds(authdata));
        } catch (InvalidCredentialsException e) {
            final OXException oxe = OXException.general(e.getMessage());
            oxe.setStackTrace(e.getStackTrace());
            throw oxe;
        } catch (StorageException e) {
            final OXException oxe = OXException.general(e.getMessage());
            oxe.setStackTrace(e.getStackTrace());
            throw oxe;
        }
    }

    @Override
    public void doAuthentication(final Credentials authdata, final int contextId) throws OXException {
        doAuthentication(authdata, contextId, false);
    }

    @Override
    public void doAuthentication(final Credentials authdata, final int contextId, boolean plugInAware) throws OXException {
        try {
            if (plugInAware) {
                BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(toCreds(authdata), new Context(Integer.valueOf(contextId)));
            } else {
                BasicAuthenticator.createNonPluginAwareAuthenticator().doAuthentication(toCreds(authdata), new Context(Integer.valueOf(contextId)));
            }
        } catch (InvalidCredentialsException e) {
            final OXException oxe = OXException.general(e.getMessage());
            oxe.setStackTrace(e.getStackTrace());
            throw oxe;
        } catch (StorageException e) {
            final OXException oxe = OXException.general(e.getMessage());
            oxe.setStackTrace(e.getStackTrace());
            throw oxe;
        }
    }

    @Override
    public void doUserAuthentication(final Credentials authdata, final int contextId) throws OXException {
        try {
            BasicAuthenticator.createNonPluginAwareAuthenticator().doUserAuthentication(toCreds(authdata), new Context(Integer.valueOf(contextId)));
        } catch (InvalidCredentialsException e) {
            final OXException oxe = OXException.general(e.getMessage());
            oxe.setStackTrace(e.getStackTrace());
            throw oxe;
        } catch (StorageException e) {
            final OXException oxe = OXException.general(e.getMessage());
            oxe.setStackTrace(e.getStackTrace());
            throw oxe;
        }
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            AdminCache cache = AdminDaemon.getCache();
            cache.reloadMasterCredentials(configService);
            cache.reinitAccessCombinations();
        } catch (Exception e) {
            LOGGER.error("Error reloading admin configuration", e);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().configFileNames("mpasswd", "ModuleAccessDefinitions.properties").build();
    }

}
