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

import org.osgi.framework.BundleContext;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.plugins.BasicAuthenticatorPluginInterface;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.PluginInterfaces;
import com.openexchange.admin.storage.interfaces.OXAuthStorageInterface;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.tools.AdminCache;

/**
 *
 * @author cutmasta
 */
public class BasicAuthenticator extends OXCommonImpl {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BasicAuthenticator.class);

    /**
     * Creates an authenticator that <b>ignores</b> possibly registered {@link BasicAuthenticatorPluginInterface} instances.
     *
     * @return The authenticator instance
     * @throws StorageException If instantiation fails
     */
    public static BasicAuthenticator createNonPluginAwareAuthenticator() throws StorageException {
        return new BasicAuthenticator(false);
    }

    /**
     * Creates an authenticator that <b>respects</b> possibly registered {@link BasicAuthenticatorPluginInterface} instances.
     *
     * @return The authenticator instance
     * @throws StorageException If instantiation fails
     */
    public static BasicAuthenticator createPluginAwareAuthenticator() throws StorageException {
        return new BasicAuthenticator(true);
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private final OXAuthStorageInterface sqlAuth;
    private final OXAuthStorageInterface fileAuth;
    private final OXToolStorageInterface oxtool;
    private final AdminCache cache;
    private final boolean plugInAware;

    /**
     * Use this constructor in case additional bundles should be able to override login
     *
     * @param context The bundle context; needed to mark the authenticator as plugIn-aware
     * @throws StorageException If instantiation fails
     * @deprecated Use {@link #createPluginAwareAuthenticator(BundleContext)}
     */
    @Deprecated
    public BasicAuthenticator(BundleContext context) throws StorageException {
        this(context != null);
    }

    /**
     * Use this constructor in case no additional bundles should be able to override login!
     *
     * @throws StorageException If instantiation fails
     * @deprecated Use {@link #createNonPluginAwareAuthenticator()}
     */
    @Deprecated
    public BasicAuthenticator() throws StorageException {
        this(false);
    }

    /**
     * Initializes a new {@link BasicAuthenticator}.
     *
     * @param plugInAware Whether the authenticator should respect possibly registered {@link BasicAuthenticatorPluginInterface} instances.
     */
    private BasicAuthenticator(boolean plugInAware) throws StorageException {
        super();
        this.plugInAware = plugInAware;
        sqlAuth  = OXAuthStorageInterface.getInstanceSQL();
        fileAuth = OXAuthStorageInterface.getInstanceFile();
        oxtool = OXToolStorageInterface.getInstance();
        cache = ClientAdminThread.cache;
        if (null == cache) {
            // Obviously not properly initialized.
            StorageException e = new StorageException("Open-Xchange Admin not properly initialized.");
            LOG.error("Probably start-up of bundle \"com.openexchange.admin\" failed. Please check log files and/or bundle status via \"/opt/open-xchange/sbin/listbundles\" command-line tool.", e);
            throw e;
        }
    }

    /**
     * Authenticates the master admin. Other bundles can register an OSGi
     * service here and take care about authentication themself
     *
     * @param authdata
     * @throws InvalidCredentialsException
     */
    public void doAuthentication(final Credentials authdata) throws InvalidCredentialsException{
        boolean autoLowerCase = cache.getProperties().getUserProp(AdminProperties.User.AUTO_LOWERCASE, false);
        if (autoLowerCase && null != authdata.getLogin()) {
            authdata.setLogin(authdata.getLogin().toLowerCase());
        }

        final Credentials master = cache.getMasterCredentials();
        if (autoLowerCase && null != master.getLogin()) {
            master.setLogin(master.getLogin().toLowerCase());
        }

        boolean doPluginAuth = false;
        if (cache.masterAuthenticationDisabled() || (authdata != null && master != null && !master.getLogin().equals(authdata.getLogin()))) {
            doPluginAuth = true;
        }
        // only let other plugins authenticate, when we have the BundleContext
        // AND when
        if( plugInAware && doPluginAuth) {
            // Trigger plugin extensions
            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (final BasicAuthenticatorPluginInterface authplug : pluginInterfaces.getBasicAuthenticatorPlugins().getServiceList()) {
                    final String bundlename = authplug.getClass().getName();
                    LOG.debug("Calling doAuthentication for plugin: {}", bundlename);
                    authplug.doAuthentication(authdata);
                    // leave
                    return;
                }
            }
        }
        // first check if whole authentication mech is disabled
        if(!cache.masterAuthenticationDisabled()){
            if( authdata == null ) {
                throw new InvalidCredentialsException("credential object is null");
            }
            if(!fileAuth.authenticate(authdata)){
                final InvalidCredentialsException invalidCredentialsException = new InvalidCredentialsException("Authentication failed");
                LOG.error("Master authentication for user: {}", authdata.getLogin(), invalidCredentialsException);
                throw invalidCredentialsException;
            }
        }
    }

    /**
     * Remove cached admin authdata from auth cache
     *
     * @param ctx
     */
    public void removeFromAuthCache(final Context ctx) {
       cache.removeAdminCredentials(ctx);
    }

    public boolean isMasterOfContext(final Credentials creds, final Context ctx) throws InvalidCredentialsException {
        if (!cache.isAllowMasterOverride() ) {
            return false;
        }
        if (cache.isMasterAdmin(creds) ) {
            return true;
        }
        if( plugInAware ) {
            // Trigger plugin extensions
            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (final BasicAuthenticatorPluginInterface authplug : pluginInterfaces.getBasicAuthenticatorPlugins().getServiceList()) {
                    final String bundlename = authplug.getClass().getName();
                    LOG.debug("Calling isMasterOfContext for plugin: {}", bundlename);
                    return authplug.isMasterOfContext(creds, ctx);
                }
            }
        }
        return false;
    }

    /**
     * Authenticates ONLY the context admin!
     * This method also validates the Context object data!
     * @param authdata
     * @param ctx
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public void doAuthentication(final Credentials authdata,final Context ctx) throws InvalidCredentialsException, StorageException, InvalidDataException{
        contextcheck(ctx);

        boolean autoLowerCase = cache.getProperties().getUserProp(AdminProperties.User.AUTO_LOWERCASE, false);
        if (autoLowerCase) {
            authdata.setLogin(authdata.getLogin().toLowerCase());
        }

        // only do context check, if we have not already admin creds in our cache for given context
        // ATTENTION: It is correct that we don't throw a now such context exception here because we won't
        // give an opportunity to indirectly check for contexts here
        if (cache.getAdminCredentials(ctx) == null ) {
            if (!OXToolStorageInterface.getInstance().existsContext(ctx)) {
                final InvalidCredentialsException invalidCredentialsException = new InvalidCredentialsException(
                        "Authentication failed");
                LOG.error("Requested context {} does not exist!", ctx.getId(), invalidCredentialsException);
                throw invalidCredentialsException;
            }
        }

        // first check if whole authentication mechanism is disabled
        if (!cache.contextAuthenticationDisabled()) {
            if( isMasterOfContext(authdata, ctx) ) {
                doAuthentication(authdata);
            } else if (!oxtool.existsUserName(ctx, authdata.getLogin()) || !sqlAuth.authenticate(authdata, ctx)) {
                final InvalidCredentialsException invalidCredentialsException = new InvalidCredentialsException(
                        "Authentication failed");
                LOG.error("Admin authentication for user {}", authdata.getLogin(),invalidCredentialsException);
                throw invalidCredentialsException;
            }
        }
    }

    /**
     * Authenticates all users within a context!
     * This method also validates the Context object data!
     * @param authdata
     * @param ctx
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public void doUserAuthentication(final Credentials authdata,final Context ctx) throws InvalidCredentialsException, StorageException, InvalidDataException{
        contextcheck(ctx);

        boolean autoLowerCase = cache.getProperties().getUserProp(AdminProperties.User.AUTO_LOWERCASE, false);
        if (autoLowerCase) {
            authdata.setLogin(authdata.getLogin().toLowerCase());
        }

        if (!OXToolStorageInterface.getInstance().existsContext(ctx)) {
            final InvalidCredentialsException invalidCredentialsException = new InvalidCredentialsException(
                    "Authentication failed for user " + authdata.getLogin());
            LOG.error("Requested context {} does not exist!", ctx.getId(), invalidCredentialsException);
            throw invalidCredentialsException;
        }

        // first check if whole authentication mech is disabled
        if (!cache.contextAuthenticationDisabled()) {
            if (!sqlAuth.authenticateUser(authdata, ctx)) {
                final InvalidCredentialsException invalidCredentialsException = new InvalidCredentialsException(
                        "Authentication failed for user " + authdata.getLogin());
                LOG.error("User authentication: ", invalidCredentialsException);
                throw invalidCredentialsException;
            }
        }
    }
}
