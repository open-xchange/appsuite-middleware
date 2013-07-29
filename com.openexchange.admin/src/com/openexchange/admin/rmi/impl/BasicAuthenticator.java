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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import org.apache.commons.logging.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.plugins.BasicAuthenticatorPluginInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXAuthStorageInterface;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.log.LogFactory;

/**
 *
 * @author cutmasta
 */
public class BasicAuthenticator extends OXCommonImpl {

    private final static Log LOG = LogFactory.getLog (BasicAuthenticator.class);

    private OXAuthStorageInterface sqlAuth = null;
    private OXAuthStorageInterface fileAuth = null;
    private AdminCache cache = null;

    private BundleContext context = null;

    /**
     * Use this constructor when additional bundles should be able to
     * override login
     * @throws StorageException  */
    public BasicAuthenticator(final BundleContext context) throws StorageException {
        super();
        this.context = context;
        sqlAuth  = OXAuthStorageInterface.getInstanceSQL();
        fileAuth = OXAuthStorageInterface.getInstanceFile();
        cache = ClientAdminThread.cache;
    }

    /**
     * @throws StorageException  */
    public BasicAuthenticator() throws StorageException {
        super();
        sqlAuth  = OXAuthStorageInterface.getInstanceSQL();
        fileAuth = OXAuthStorageInterface.getInstanceFile();
        cache = ClientAdminThread.cache;
    }

    /**
     * Authenticates the master admin. Other bundles can register an OSGi
     * service here and take care about authentication themself
     *
     * @param authdata
     * @throws InvalidCredentialsException
     */
    public void doAuthentication(final Credentials authdata) throws InvalidCredentialsException{
        final Credentials master = ClientAdminThread.cache.getMasterCredentials();

        boolean doPluginAuth = false;
        if (cache.masterAuthenticationDisabled() || (authdata != null && master != null && !master.getLogin().equals(authdata.getLogin()))) {
            doPluginAuth = true;
        }
        // only let other plugins authenticate, when we have the BundleContext
        // AND when
        if( this.context != null && doPluginAuth) {
            final java.util.List<Bundle> bundles = AdminDaemon.getBundlelist();
            for (final Bundle bundle : bundles) {
                final String bundlename = bundle.getSymbolicName();
                if (Bundle.ACTIVE == bundle.getState()) {
                    final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                    if (null != servicereferences) {
                        for (final ServiceReference servicereference : servicereferences) {
                            final Object property = servicereference.getProperty("name");
                            if (null != property && property.toString().equalsIgnoreCase("BasicAuthenticator")) {
                                final BasicAuthenticatorPluginInterface authplug = (BasicAuthenticatorPluginInterface) this.context.getService(servicereference);
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Calling doAuthentication for plugin: " + bundlename);
                                }
                                authplug.doAuthentication(authdata);
                                // leave
                                return;
                            }
                        }
                    }
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
                LOG.error("Master authentication for user: " + authdata.getLogin(), invalidCredentialsException);
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
       ClientAdminThread.cache.removeAdminCredentials(ctx);
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

        // only do context check, if we have not already admin creds in our cache for given context
        // ATTENTION: It is correct that we don't throw a now such context exception here because we won't
        // give an opportunity to indirectly check for contexts here
        if( ClientAdminThread.cache.getAdminCredentials(ctx) == null ) {
            if (!OXToolStorageInterface.getInstance().existsContext(ctx)) {
                final InvalidCredentialsException invalidCredentialsException = new InvalidCredentialsException(
                        "Authentication failed");
                LOG.error("Requested context " + ctx.getId()
                        + " does not exist!", invalidCredentialsException);
                throw invalidCredentialsException;
            }
        }

        // first check if whole authentication mechanism is disabled
        if (!cache.contextAuthenticationDisabled()) {
            if (!sqlAuth.authenticate(authdata, ctx)) {
                final InvalidCredentialsException invalidCredentialsException = new InvalidCredentialsException(
                        "Authentication failed");
                LOG.error("Admin authentication for user " + authdata.getLogin(),invalidCredentialsException);
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

        if (!OXToolStorageInterface.getInstance().existsContext(ctx)) {
            final InvalidCredentialsException invalidCredentialsException = new InvalidCredentialsException(
                    "Authentication failed for user " + authdata.getLogin());
            LOG.error("Requested context " + ctx.getId()
                    + " does not exist!", invalidCredentialsException);
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
