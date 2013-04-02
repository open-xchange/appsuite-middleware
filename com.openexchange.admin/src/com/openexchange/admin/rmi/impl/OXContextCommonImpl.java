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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.tools.GenericChecks;


public abstract class OXContextCommonImpl extends OXCommonImpl {

    protected BundleContext context;

    public OXContextCommonImpl() throws StorageException {
        super();
    }

    private final static Log log = LogFactory.getLog(OXContextCommonImpl.class);

    protected void createchecks(final Context ctx, final User admin_user, final OXToolStorageInterface tool) throws StorageException, ContextExistsException, InvalidDataException {

        try {
            final Boolean ret = (Boolean)callPluginMethod("checkMandatoryMembersContextCreate", ctx);
            if( ret == null || ( ret != null && ret.booleanValue())  ) {
                if (!ctx.mandatoryCreateMembersSet()) {
                    throw new InvalidDataException("Mandatory fields in context not set: " + ctx.getUnsetMembers());
                }
            }
        } catch (final EnforceableDataObjectException e) {
            throw new InvalidDataException(e.getMessage());
        }

        if (tool.existsContext(ctx)) {
            throw new ContextExistsException("Context already exists!");
        }

        if(ctx.getName()!=null && tool.existsContextName(ctx.getName())){
            throw new InvalidDataException("Context " + ctx.getName() + " already exists!");
        }

        try {
            if (!admin_user.mandatoryCreateMembersSet()) {
                throw new InvalidDataException("Mandatory fields in admin user not set: " + admin_user.getUnsetMembers());
            }
        } catch (final EnforceableDataObjectException e) {
            throw new InvalidDataException(e.getMessage());
        }

        GenericChecks.checkValidMailAddress(admin_user.getPrimaryEmail());
    }

    protected abstract Context createmaincall(final Context ctx, final User admin_user, Database db, UserModuleAccess access, final Credentials auth) throws StorageException, InvalidDataException;

    protected Context createcommon(final Context ctx, final User admin_user, final Database db, final UserModuleAccess access, final Credentials auth) throws InvalidCredentialsException, ContextExistsException, InvalidDataException, StorageException {
        try{
            doNullCheck(ctx,admin_user);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context or user not correct");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }

        new BasicAuthenticator(context).doAuthentication(auth);

        if (log.isDebugEnabled()) {
            log.debug(ctx + " - " + admin_user);
        }

        try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            Context ret = ctx;
            if( isAnyPluginLoaded() ) {
                try {
                    ret = (Context)callPluginMethod("preCreate", ret, admin_user, auth);
                } catch(final StorageException e) {
                    log.error(e.getMessage(),e);
                    throw e;
                }
            }

            createchecks(ret, admin_user, tool);

            final String name = ret.getName();
            final HashSet<String> loginMappings = ret.getLoginMappings();
            if (null == loginMappings || loginMappings.isEmpty()) {
                ret.addLoginMapping(ret.getIdAsString());
            }
            if (null != name) {
                // Add the name of the context to the login mappings and the id
                ret.addLoginMapping(name);
            }

            return createmaincall(ret, admin_user, db, access,auth);
        } catch (final ContextExistsException e) {
            log.error(e.getMessage(),e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (StorageException e) {
            log.error(e.getMessage(), e);
            // Eliminate nested root cause exceptions. These are mostly unknown to clients.
            throw new StorageException(e.getMessage());
        }
    }

    /**
     * Call method <code>method</code> of all bundles registered to the OXContext Service
     * <b>Important:</b> No argument of any args here must be null!
     * Arguments, that are null will cause a {@link StorageException}
     *
     * @param method Name of the method to call
     * @param args All required args of that method
     * @throws StorageException
     */
    protected Object callPluginMethod(final String method, final Object... args) throws StorageException {
        Object ret = null;
        final java.util.List<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE == bundle.getState()) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase("oxcontext")) {
                            final OXContextPluginInterface oxctx = (OXContextPluginInterface) this.context.getService(servicereference);
                            if (log.isDebugEnabled()) {
                                log.debug("Calling " + method + " for plugin: " + bundlename);
                            }
                            try {
                                final Class[] classes = new Class[args.length];
                                for(int i=0; i<args.length; i++) {
                                    if( args[i] == null ) {
                                        final String errtxt = "Error calling method " + method + "() for plugin: " + bundlename + ": argument " + (i+1) + " is null";
                                        final StorageException e = new StorageException(errtxt);
                                        log.error(errtxt);
                                        throw e;
                                    }
                                    classes[i] = args[i].getClass();
                                }
                                final Method pmethod = OXContextPluginInterface.class.getDeclaredMethod(method, classes);
                                ret = pmethod.invoke(oxctx, args);
                                if( args[0] instanceof Context && ret instanceof Context ) {
                                    args[0] = ret;
                                }
                            } catch (final SecurityException e) {
                                log.error("Error while calling method " + method + " of plugin " + bundlename,e);
                                throw new StorageException(e.getCause());
                            } catch (final NoSuchMethodException e) {
                                log.error("Error while calling method " + method + " of plugin " + bundlename,e);
                                throw new StorageException(e.getCause());
                            } catch (final IllegalArgumentException e) {
                                log.error("Error while calling method " + method + " of plugin " + bundlename,e);
                                throw new StorageException(e.getCause());
                            } catch (final IllegalAccessException e) {
                                log.error("Error while calling method " + method + " of plugin " + bundlename,e);
                                throw new StorageException(e.getCause());
                            } catch (final InvocationTargetException e) {
                                log.error("Error while calling method " + method + " of plugin " + bundlename,e);
                                throw new StorageException(e.getCause());
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * @return
     * @throws StorageException
     */
    protected boolean isAnyPluginLoaded() throws StorageException {
        final java.util.List<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            if (Bundle.ACTIVE == bundle.getState()) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase("oxcontext")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
