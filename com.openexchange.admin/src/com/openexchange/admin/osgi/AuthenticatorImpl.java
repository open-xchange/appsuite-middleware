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

package com.openexchange.admin.osgi;

import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthenticatorImpl.class);

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
        } catch (final InvalidCredentialsException e) {
            final OXException oxe = OXException.general(e.getMessage());
            oxe.setStackTrace(e.getStackTrace());
            throw oxe;
        } catch (final StorageException e) {
            final OXException oxe = OXException.general(e.getMessage());
            oxe.setStackTrace(e.getStackTrace());
            throw oxe;
        }
    }

    @Override
    public void doAuthentication(final Credentials authdata, final int contextId) throws OXException {
        try {
            BasicAuthenticator.createNonPluginAwareAuthenticator().doAuthentication(toCreds(authdata), new Context(Integer.valueOf(contextId)));
        } catch (final InvalidCredentialsException e) {
            final OXException oxe = OXException.general(e.getMessage());
            oxe.setStackTrace(e.getStackTrace());
            throw oxe;
        } catch (final StorageException e) {
            final OXException oxe = OXException.general(e.getMessage());
            oxe.setStackTrace(e.getStackTrace());
            throw oxe;
        } catch (final InvalidDataException e) {
            final OXException oxe = OXException.general(e.getMessage());
            oxe.setStackTrace(e.getStackTrace());
            throw oxe;
        }
    }

    @Override
    public void doUserAuthentication(final Credentials authdata, final int contextId) throws OXException {
        try {
            BasicAuthenticator.createNonPluginAwareAuthenticator().doUserAuthentication(toCreds(authdata), new Context(Integer.valueOf(contextId)));
        } catch (final InvalidCredentialsException e) {
            final OXException oxe = OXException.general(e.getMessage());
            oxe.setStackTrace(e.getStackTrace());
            throw oxe;
        } catch (final StorageException e) {
            final OXException oxe = OXException.general(e.getMessage());
            oxe.setStackTrace(e.getStackTrace());
            throw oxe;
        } catch (final InvalidDataException e) {
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
            log.error("Error reloading admin configuration", e);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().configFileNames("mpasswd", "ModuleAccessDefinitions.properties").build();
    }

}
