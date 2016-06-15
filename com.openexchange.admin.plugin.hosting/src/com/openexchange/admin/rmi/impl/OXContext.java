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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.java.Autoboxing.i2I;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.osgi.framework.BundleContext;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Quota;
import com.openexchange.admin.rmi.dataobjects.SchemaSelectStrategy;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchDatabaseException;
import com.openexchange.admin.rmi.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.NoSuchReasonException;
import com.openexchange.admin.rmi.exceptions.OXContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.services.PluginInterfaces;
import com.openexchange.admin.storage.interfaces.OXContextStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUserStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolDBPoolExtension;
import com.openexchange.admin.storage.utils.Filestore2UserUtil;
import com.openexchange.admin.taskmanagement.TaskManager;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.DatabaseDataMover;
import com.openexchange.admin.tools.filestore.FilestoreDataMover;
import com.openexchange.admin.tools.filestore.PostProcessTask;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorages;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.tools.pipesnfilters.Filter;

public class OXContext extends OXContextCommonImpl implements OXContextInterface {

    /** The logger */
    static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OXContext.class);

    private final AdminCache cache;
    private final OXAdminPoolDBPoolExtension pool;

    /**
     * Initializes a new {@link OXContext}.
     *
     * @param context The associated bundle context
     */
    public OXContext(final BundleContext context) {
        super(context);
        cache = ClientAdminThread.cache;
        this.pool = new OXAdminPoolDBPoolExtension();
        LOGGER.debug("Class loaded: {}", this.getClass().getName());
    }

    @Override
    public Quota[] listQuotas(Context ctx, Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        new BasicAuthenticator().doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, ctx);
        } catch (final NoSuchObjectException e) {
            throw new NoSuchContextException(e);
        }

        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }

            OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.listQuotas(ctx);
        } catch (StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public void changeQuota(final Context ctx, final String sModule, final long quotaValue, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        if (com.openexchange.java.Strings.isEmpty(sModule)) {
            throw new InvalidDataException("No valid module specified.");
        }

        final String[] mods = sModule.split(" *, *");
        final Set<String> modules = new LinkedHashSet<String>(mods.length);
        for (final String mod : mods) {
            modules.add(mod);
        }

        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        new BasicAuthenticator(context).doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, ctx);
        } catch (final NoSuchObjectException e) {
            throw new NoSuchContextException(e);
        }

        final long quota;
        if (quotaValue < 0) {
            quota = -1L;
        } else {
            // MySQL int(10) unsigned: the allowable range is from 0 to 4294967295
            if (quotaValue > 4294967295L) {
                throw new InvalidDataException("Quota value is out of range (allowable range is from 0 to 4294967295): " + quotaValue);
            }
            quota = quotaValue;
        }

        LOGGER.debug("{} - {} - {}", ctx, modules, Long.valueOf(quota));

        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        oxContextPlugin.changeQuota(ctx, sModule, quotaValue, auth);
                    }
                }
            }

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.changeQuota(ctx, new ArrayList<String>(modules), quota, auth);
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final PluginException e) {
            LOGGER.error("", e);
            throw StorageException.wrapForRMI(e);
        }
    }

    @Override
    public Set<String> getCapabilities(final Context ctx, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        if (null == ctx) {
            throw new InvalidDataException("Missing context.");
        }

        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        new BasicAuthenticator(context).doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, ctx);
        } catch (final NoSuchObjectException e) {
            throw new NoSuchContextException(e);
        }

        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.getCapabilities(ctx);
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public void changeCapabilities(final Context ctx, final Set<String> capsToAdd, final Set<String> capsToRemove, final Set<String> capsToDrop, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        if ((null == capsToAdd || capsToAdd.isEmpty()) && (null == capsToRemove || capsToRemove.isEmpty()) && (null == capsToDrop || capsToDrop.isEmpty())) {
            throw new InvalidDataException("No capabilities specified.");
        }
        if (null == ctx) {
            throw new InvalidDataException("Missing context.");
        }

        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        new BasicAuthenticator(context).doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, ctx);
        } catch (NoSuchObjectException e) {
            throw new NoSuchContextException(e);
        }

        LOGGER.debug(ctx + " - " + (null == capsToAdd ? "" : capsToAdd.toString()) + " | " + (null == capsToRemove ? "" : capsToRemove.toString()));

        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        oxContextPlugin.changeCapabilities(ctx, capsToAdd, capsToRemove, capsToDrop, auth);
                    }
                }
            }

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.changeCapabilities(ctx, capsToAdd, capsToRemove, capsToDrop, auth);
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final PluginException e) {
            LOGGER.error("", e);
            throw StorageException.wrapForRMI(e);
        }
    }

    @Override
    public void change(final Context ctx, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is invalid");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }
        validateloginmapping(ctx);

        new BasicAuthenticator(context).doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, ctx);
        } catch (NoSuchObjectException e) {
            throw new NoSuchContextException(e);
        }
        LOGGER.debug(ctx.toString());

        Set<String> loginMappings = null; // used for invalidating old login mappings in the cache
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }

            if (ctx.getName() != null && tool.existsContextName(ctx)) {
                throw new InvalidDataException("Context " + ctx.getName() + " already exists!");
            }

            // check if he wants to change the filestore id, if yes, make sure filestore with this id exists in the system
            if (ctx.getFilestoreId() != null) {
                if (!tool.existsStore(ctx.getFilestoreId().intValue())) {
                    final InvalidDataException inde = new InvalidDataException("No such filestore with id " + ctx.getFilestoreId());
                    LOGGER.error("", inde);
                    throw inde;
                }
            }

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        oxContextPlugin.change(ctx, auth);
                    }
                }
            }

            OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();

            // Check if login-mappings are supposed to be changed
            if (null != ctx.getLoginMappings()) {
                // Load old ones for invalidation purpose
                loginMappings = oxcox.getLoginMappings(ctx);
            }

            oxcox.change(ctx);
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final PluginException e) {
            LOGGER.error("", e);
            throw StorageException.wrapForRMI(e);
        }

        try {
            final ContextStorage cs = ContextStorage.getInstance();
            cs.invalidateContext(ctx.getId().intValue());
            if (loginMappings != null && !loginMappings.isEmpty()) {
                for (String loginMapping : loginMappings) {
                    cs.invalidateLoginInfo(loginMapping);
                }
            }
        } catch (final OXException e) {
            LOGGER.error("Error invalidating cached infos of context {} in context storage", ctx.getId(), e);
        }
    }

    @Override
    public Context create(final Context ctx, final User admin_user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        return createcommon(ctx, admin_user, null, null, auth, getDefaultSchemaSelectStrategy());
    }

    @Override
    public Context create(final Context ctx, final User admin_user, final Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        return createcommon(ctx, admin_user, null, null, auth, schemaSelectStrategy);
    }

    @Override
    public Context create(final Context ctx, final User admin_user, final String access_combination_name, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        return create(ctx, admin_user, access_combination_name, credentials, getDefaultSchemaSelectStrategy());
    }

    @Override
    public Context create(final Context ctx, final User admin_user, final String access_combination_name, final Credentials credentials, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {

        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        // Resolve access rights by name
        try {
            doNullCheck(admin_user, access_combination_name);
            if (access_combination_name.trim().length() == 0) {
                throw new InvalidDataException("Invalid access combination name");
            }
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for create is null", e3);
            throw e3;
        }

        LOGGER.debug("{} - {} - {} - {}", ctx, admin_user, access_combination_name, auth);

        UserModuleAccess access = cache.getNamedAccessCombination(access_combination_name.trim(), true);
        if (access == null) {
            // no such access combination name defined in configuration
            // throw error!
            throw new InvalidDataException("No such access combination name \"" + access_combination_name.trim() + "\"");
        }
        access = access.clone();

        return createcommon(ctx, admin_user, null, access, auth, schemaSelectStrategy);
    }

    @Override
    public Context create(final Context ctx, final User admin_user, final UserModuleAccess access, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        return create(ctx, admin_user, access, credentials, getDefaultSchemaSelectStrategy());
    }

    @Override
    public Context create(final Context ctx, final User admin_user, final UserModuleAccess access, final Credentials credentials, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        try {
            doNullCheck(admin_user, access);
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for create is null", e3);
            throw e3;
        }

        return createcommon(ctx, admin_user, null, access, auth, schemaSelectStrategy);

    }

    @Override
    public void delete(final Context ctx, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e) {
            final InvalidDataException e1 = new InvalidDataException("Context is null");
            LOGGER.error("", e1);
            throw e1;
        }

        final BasicAuthenticator basicAuthenticator = new BasicAuthenticator(context);
        basicAuthenticator.doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, ctx);
        } catch (NoSuchObjectException e) {
            throw new NoSuchContextException(e);
        }
        LOGGER.debug(ctx.toString());
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }

            try {
                if (tool.checkAndUpdateSchemaIfRequired(ctx)) {
                    throw new DatabaseUpdateException("Database is locked or is now beeing updated, please try again later");
                }
            } catch (final StorageException e) {
                // Context deletion should be a robust process. Therefore not failing if the schema is not up
            }

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        try {
                            oxContextPlugin.delete(ctx, auth);
                        } catch (final PluginException e) {
                            LOGGER.error("", e);
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }

            oxcox.delete(ctx);
            Filestore2UserUtil.removeFilestore2UserEntries(ctx.getId().intValue(), cache);
            basicAuthenticator.removeFromAuthCache(ctx);
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public void disable(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchReasonException, OXContextException {
        final MaintenanceReason reason = new MaintenanceReason(Integer.valueOf(42));
        disable(ctx, reason, auth);
    }

    private void disable(final Context ctx, final MaintenanceReason reason, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchReasonException, OXContextException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            doNullCheck(ctx, reason);
            doNullCheck(reason.getId());
        } catch (final InvalidDataException e1) {
            LOGGER.error("Invalid data sent by client!", e1);
            throw e1;
        }

        new BasicAuthenticator(context).doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, ctx);
        } catch (NoSuchObjectException e) {
            throw new NoSuchContextException(e);
        }
        LOGGER.debug("{} - {}", ctx, reason);
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            /*
             * if (!tool.existsReason(reason_id)) {
             * throw new NoSuchReasonException();
             * }
             */

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        try {
                            oxContextPlugin.disable(ctx, auth);
                        } catch (final PluginException e) {
                            LOGGER.error("", e);
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }

            if (!tool.isContextEnabled(ctx)) {
                throw new OXContextException(OXContextException.CONTEXT_DISABLED);
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.disable(ctx, reason);
            LOGGER.info("Context {} successfully disabled", ctx.getId());

            try {
                ContextStorage.getInstance().invalidateContext(ctx.getId().intValue());
                LOGGER.info("Context {} successfully invalidated", ctx.getId());
            } catch (final OXException e) {
                LOGGER.error("Error invalidating context {} in ox context storage", ctx.getId(), e);
            }

        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
            /*
             * } catch (final NoSuchReasonException e) {
             * log.error("", e);
             * throw e;
             */
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final OXContextException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public void disableAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchReasonException {
        final MaintenanceReason reason = new MaintenanceReason(Integer.valueOf(42));
        disableAll(reason, auth);
    }

    private void disableAll(final MaintenanceReason reason, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchReasonException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            doNullCheck(reason);
            doNullCheck(reason.getId());
        } catch (final InvalidDataException e1) {
            LOGGER.error("Invalid data sent by client!", e1);
            throw e1;
        }
        new BasicAuthenticator(context).doAuthentication(auth);

        final int reason_id = reason.getId();
        LOGGER.debug("{}", reason_id);
        try {
            //            if (!tool.existsReason(reason_id)) {
            //                throw new NoSuchReasonException();
            //            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            if (ClientAdminThreadExtended.cache.isMasterAdmin(auth)) {
                oxcox.disableAll(reason);
            } else {
                // Trigger plugin extensions
                {
                    final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                    if (null != pluginInterfaces) {
                        for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                            try {
                                oxContextPlugin.disableAll(auth);
                            } catch (final PluginException e) {
                                LOGGER.error("", e);
                                throw StorageException.wrapForRMI(e);
                            }
                        }
                    }
                }
            }
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
            //        } catch (final NoSuchReasonException e) {
            //            log.error("", e);
            //            throw e;
        }

        // Clear context cache
        // CACHE
        final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
        ;
        if (null != cacheService) {
            try {
                final Cache cache = cacheService.getCache("Context");
                cache.clear();
            } catch (final OXException e) {
                LOGGER.error("", e);
            }
        }
        // END OF CACHE
    }

    @Override
    public void enable(final Context ctx, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        new BasicAuthenticator(context).doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, ctx);
        } catch (NoSuchObjectException e) {
            throw new NoSuchContextException(e);
        }
        LOGGER.debug(ctx.toString());
        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        try {
                            oxContextPlugin.enable(ctx, auth);
                        } catch (final PluginException e) {
                            LOGGER.error("", e);
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.enable(ctx);
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        }

        try {
            ContextStorage.getInstance().invalidateContext(ctx.getId().intValue());
        } catch (final OXException e) {
            LOGGER.error("Error invalidating context {} in ox context storage", ctx.getId(), e);
        }
    }

    @Override
    public void enableAll(final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        new BasicAuthenticator(context).doAuthentication(auth);

        try {
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            if (ClientAdminThreadExtended.cache.isMasterAdmin(auth)) {
                oxcox.enableAll();
            } else {
                // Trigger plugin extensions
                {
                    final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                    if (null != pluginInterfaces) {
                        for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                            try {
                                oxContextPlugin.enableAll(auth);
                            } catch (final PluginException e) {
                                LOGGER.error("", e);
                                throw StorageException.wrapForRMI(e);
                            }
                        }
                    }
                }
            }
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        }

        // Clear context cache
        // CACHE
        final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
        ;
        if (null != cacheService) {
            try {
                final Cache cache = cacheService.getCache("Context");
                cache.clear();
            } catch (final OXException e) {
                LOGGER.error("", e);
            }
        }
        // END OF CACHE
    }

    @Override
    public Context getOwnData(Context ctx, Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            try {
                doNullCheck(ctx);
            } catch (final InvalidDataException e1) {
                LOGGER.error("One of the given arguments for getData is null", e1);
                throw e1;
            }

            new BasicAuthenticator(context).doAuthentication(auth, ctx);

            OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.getData(new Context[] { ctx })[0];
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final RuntimeException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public Context[] getData(final Context[] ctxs, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            try {
                doNullCheck((Object[]) ctxs);
            } catch (final InvalidDataException e1) {
                LOGGER.error("One of the given arguments for getData is null", e1);
                throw e1;
            }

            new BasicAuthenticator(context).doAuthentication(auth);

            final List<Context> retval = new ArrayList<Context>();
            boolean filled = true;
            for (final Context ctx : ctxs) {
                if (!ctx.isListrun()) {
                    filled = false;
                }
                try {
                    setIdOrGetIDFromNameAndIdObject(null, ctx);
                } catch (NoSuchObjectException e) {
                    throw new NoSuchContextException(e);
                }
                LOGGER.debug(ctx.toString());
                try {
                    if (!tool.existsContext(ctx)) {
                        throw new NoSuchContextException();
                    }
                } catch (final NoSuchContextException e) {
                    LOGGER.error("", e);
                    throw e;
                }
            }
            try {
                final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();

                if (filled) {
                    final List<Context> callGetDataPlugins = callGetDataPlugins(Arrays.asList(ctxs), auth, oxcox);
                    if (null != callGetDataPlugins) {
                        retval.addAll(callGetDataPlugins);
                    } else {
                        retval.addAll(Arrays.asList(ctxs));
                    }
                } else {
                    final Context[] ret = oxcox.getData(ctxs);
                    final List<Context> callGetDataPlugins = callGetDataPlugins(Arrays.asList(ret), auth, oxcox);
                    if (null != callGetDataPlugins) {
                        retval.addAll(callGetDataPlugins);
                    } else {
                        retval.addAll(Arrays.asList(ret));
                    }
                }
            } catch (final StorageException e) {
                LOGGER.error("", e);
                throw e;
            }
            return retval.toArray(new Context[retval.size()]);
        } catch (final RuntimeException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public Context getData(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        return getData(new Context[] { ctx }, auth)[0];
    }

    @Override
    public Context[] list(final String search_pattern, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("Search pattern is null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }
        new BasicAuthenticator(context).doAuthentication(auth);

        LOGGER.debug("{}", search_pattern);

        try {
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();

            Filter<Context, Context> loader = null;
            Filter<Integer, Integer> filter = null;
            final ArrayList<Filter<Context, Context>> loaderFilter = new ArrayList<Filter<Context, Context>>();
            final ArrayList<Filter<Integer, Integer>> contextFilter = new ArrayList<Filter<Integer, Integer>>();

            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (final OXContextPluginInterface oxctx : pluginInterfaces.getContextPlugins().getServiceList()) {
                    final String bundlename = oxctx.getClass().getName();
                    LOGGER.debug("Calling list for plugin: {}", bundlename);
                    try {
                        filter = oxctx.filter(auth);
                        if (null != filter) {
                            contextFilter.add(filter);
                        }
                        loader = oxctx.list(search_pattern, auth);
                        if (null != loader) {
                            loaderFilter.add(loader);
                        }
                    } catch (final PluginException e) {
                        LOGGER.error("Error while calling method list of plugin {}", bundlename, e);
                        throw StorageException.wrapForRMI(e);
                    }
                }
            }

            return oxcox.listContext(search_pattern, contextFilter, loaderFilter);
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    public Context[] listAll(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return list("*", auth);
    }

    @Override
    public Context[] listAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return list("*", auth);
    }

    @Override
    public Context[] listByDatabase(final Database db, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchDatabaseException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            doNullCheck(db);
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("Database is null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }
        new BasicAuthenticator().doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, db);
        } catch (NoSuchObjectException e) {
            throw new NoSuchDatabaseException(e);
        }
        LOGGER.debug(db.toString());
        try {
            if (!tool.existsDatabase(db.getId())) {
                throw new NoSuchDatabaseException();
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();

            final List<Context> retval = new ArrayList<Context>();
            final Context[] ret = oxcox.searchContextByDatabase(db);
            final List<Context> callGetDataPlugins = callGetDataPlugins(Arrays.asList(ret), auth, oxcox);
            if (null != callGetDataPlugins) {
                retval.addAll(callGetDataPlugins);
            } else {
                retval.addAll(Arrays.asList(ret));
            }
            return retval.toArray(new Context[retval.size()]);
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public Context[] listByFilestore(final Filestore filestore, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchFilestoreException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            doNullCheck(filestore);
            doNullCheck(filestore.getId());
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("Filestore is null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }
        new BasicAuthenticator().doAuthentication(auth);

        LOGGER.debug(filestore.toString());
        try {
            if (!tool.existsStore(filestore.getId())) {
                throw new NoSuchFilestoreException();
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            final List<Context> retval = new ArrayList<Context>();
            final Context[] ret = oxcox.searchContextByFilestore(filestore);
            final List<Context> callGetDataPlugins = callGetDataPlugins(Arrays.asList(ret), auth, oxcox);
            if (null != callGetDataPlugins) {
                retval.addAll(callGetDataPlugins);
            } else {
                retval.addAll(Arrays.asList(ret));
            }
            return retval.toArray(new Context[retval.size()]);
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    /**
     * @see com.openexchange.admin.rmi.OXContextInterface#moveContextDatabase(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Database, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public int moveContextDatabase(final Context ctx, final Database db, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, OXContextException {
        final MaintenanceReason reason = new MaintenanceReason(42);
        return moveContextDatabase(ctx, db, reason, auth);
    }

    private int moveContextDatabase(final Context ctx, final Database db, final MaintenanceReason reason, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, OXContextException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            doNullCheck(ctx, db, reason);
            doNullCheck(reason.getId());
        } catch (final InvalidDataException e1) {
            LOGGER.error("Invalid data sent by client!", e1);
            throw e1;
        }

        new BasicAuthenticator().doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, ctx);
        } catch (NoSuchObjectException e) {
            throw new NoSuchContextException(e);
        }
        try {
            setIdOrGetIDFromNameAndIdObject(null, db);
        } catch (NoSuchObjectException e) {
            // FIXME normally NoSuchDatabaseException needs to be thrown here. Unfortunately it is not already in the throws declaration.
            throw new StorageException(e);
        }
        final int reason_id = reason.getId();
        LOGGER.debug("{} - {} - {}", ctx, db, reason_id);
        try {
            /*
             * if (!tool.existsReason(reason_id)) {
             * // FIXME: Util in context???
             * throw new OXContextException(OXUtilException.NO_SUCH_REASON);
             * }
             */
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }
            if (tool.checkAndUpdateSchemaIfRequired(ctx)) {
                throw new DatabaseUpdateException("Database is locked or is now beeing updated, please try again later");
            }
            if (!tool.isContextEnabled(ctx)) {
                throw new OXContextException(OXContextException.CONTEXT_DISABLED);
            }
            final Integer dbid = db.getId();
            if (!tool.isMasterDatabase(dbid)) {
                throw new OXContextException("Database with id " + dbid + " is NOT a master!");
            }
            {
                // Check if target database is already source database
                final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
                final Context[] results = oxcox.searchContextByDatabase(db);
                for (final Context context : results) {
                    if (context.getId().intValue() == ctx.getId().intValue()) {
                        throw new OXContextException("Context with id " + ctx.getId() + " already exists in database with id " + dbid);
                    }
                }
            }
            final DatabaseDataMover ddm = new DatabaseDataMover(ctx, db, reason);

            return TaskManager.getInstance().addJob(ddm, "movedatabase", "move context " + ctx.getIdAsString() + " to database " + dbid, ctx.getId());
        } catch (final OXContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public int moveContextFilestore(final Context ctx, final Filestore dst_filestore, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, NoSuchReasonException, OXContextException {
        final MaintenanceReason reason = new MaintenanceReason(I(42));
        return moveContextFilestore(ctx, dst_filestore, reason, auth);
    }

    private int moveContextFilestore(final Context ctx, final Filestore dst_filestore, final MaintenanceReason reason, final Credentials credentials) throws InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, OXContextException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            doNullCheck(ctx, dst_filestore, reason);
            doNullCheck(dst_filestore.getId(), reason.getId());
        } catch (final InvalidDataException e) {
            LOGGER.error("Invalid data sent by client!", e);
            throw e;
        }

        new BasicAuthenticator().doAuthentication(auth);

        Context retval = null;

        LOGGER.debug("{} - {}", ctx, dst_filestore);

        final OXContextStorageInterface oxcox;
        try {
            oxcox = OXContextStorageInterface.getInstance();
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw new OXContextException(e);
        }
        try {
            try {
                setIdOrGetIDFromNameAndIdObject(null, ctx);
            } catch (NoSuchObjectException e) {
                throw new NoSuchContextException(e);
            }
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            } else if (!tool.existsStore(dst_filestore.getId().intValue())) {
                throw new NoSuchFilestoreException();
            } else if (!tool.isContextEnabled(ctx)) {
                throw new OXContextException("Unable to disable Context " + ctx.getIdAsString());
            }

            retval = oxcox.getData(ctx);

            // Check equality
            int srcStore_id = retval.getFilestoreId().intValue();
            ctx.setFilestoreId(Integer.valueOf(srcStore_id));
            if (srcStore_id == dst_filestore.getId().intValue()) {
                throw new OXContextException("The identifiers for the source and destination storage are equal: " + dst_filestore);
            }

            // Check storage name
            String ctxdir = retval.getFilestore_name();
            if (ctxdir == null) {
                throw new OXContextException("Unable to get filestore directory " + ctx.getIdAsString());
            }

            OXUtilStorageInterface oxu = OXUtilStorageInterface.getInstance();
            Filestore destFilestore = oxu.getFilestore(dst_filestore.getId().intValue());

            // Check capacity
            if (!oxu.hasSpaceForAnotherContext(destFilestore)) {
                throw new StorageException("Destination filestore does not have enough space for another context.");
            }

            // Load it to ensure validity
            String baseUri = destFilestore.getUrl();
            try {
                URI uri = FileStorages.getFullyQualifyingUriForContext(ctx.getId().intValue(), new java.net.URI(baseUri));
                FileStorages.getFileStorageService().getFileStorage(uri);
            } catch (OXException e) {
                throw new StorageException(e.getMessage(), e);
            } catch (URISyntaxException e) {
                throw new StorageException("Invalid file storage URI: " + baseUri, e);
            }

            try {
                // Initialize mover instance
                FilestoreDataMover fsdm = FilestoreDataMover.newContextMover(oxu.getFilestore(srcStore_id), destFilestore, ctx);

                // Enable context after processing
                fsdm.addPostProcessTask(new PostProcessTask() {

                    @Override
                    public void perform(ExecutionException executionError) throws StorageException {
                        if (null == executionError) {
                            oxcox.enable(ctx);
                        } else {
                            LOGGER.warn("An execution error occurred during \"movefilestore\" for context {}. Context will stay disabled.", ctx.getId(), executionError.getCause());
                        }
                    }
                });

                // Schedule task
                oxcox.disable(ctx, reason);
                return TaskManager.getInstance().addJob(fsdm, "movefilestore", "move context " + ctx.getIdAsString() + " to filestore " + dst_filestore.getId(), ctx.getId());
            } catch (final StorageException e) {
                throw new OXContextException(e);
            }
        } catch (final NoSuchFilestoreException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final OXContextException e) {
            LOGGER.error("", e);
            throw e;
        } finally {
            oxcox.enable(ctx);
        }
    }

    @Override
    protected Context createmaincall(final Context ctx, final User admin_user, final Database db, final UserModuleAccess access, final Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws StorageException, InvalidDataException, ContextExistsException {
        validateloginmapping(ctx);
        OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();

        String DEFAULT_ACCESS_COMBINATION_NAME = ClientAdminThreadExtended.cache.getProperties().getProp("NEW_CONTEXT_DEFAULT_ACCESS_COMBINATION_NAME", "NOT_DEFINED");

        // If not defined or access combination name does NOT exist, use hardcoded fallback!
        UserModuleAccess createaccess;
        if (access == null) {
            if (DEFAULT_ACCESS_COMBINATION_NAME.equals("NOT_DEFINED") || cache.getNamedAccessCombination(DEFAULT_ACCESS_COMBINATION_NAME, true) == null) {
                createaccess = AdminCache.getDefaultUserModuleAccess().clone();
            } else {
                createaccess = cache.getNamedAccessCombination(DEFAULT_ACCESS_COMBINATION_NAME, true).clone();
            }
        } else {
            createaccess = access.clone();
        }

        Context ret = oxcox.create(ctx, admin_user, createaccess, schemaSelectStrategy == null ? getDefaultSchemaSelectStrategy() : schemaSelectStrategy);

        if (isAnyPluginLoaded()) {
            PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (OXContextPluginInterface contextInterface : pluginInterfaces.getContextPlugins().getServiceList()) {
                    try {
                        ret = contextInterface.postCreate(ret, admin_user, createaccess, auth);
                    } catch (PluginException e) {
                        LOGGER.error("", e);
                        // callPluginMethod delete may fail here for what ever reason.
                        // this must not prevent us from cleaning up the rest
                        try {
                            contextInterface.delete(ctx, auth);
                        } catch (Exception e1) {
                            LOGGER.error("", e);
                        }
                        oxcox.delete(ret);
                        throw StorageException.wrapForRMI(e);
                    }
                }
            }
        }

        return ret;
    }

    private void validateloginmapping(final Context ctx) throws InvalidDataException {
        final HashSet<String> loginMappings = ctx.getLoginMappings();
        final String login_regexp = ClientAdminThreadExtended.cache.getProperties().getProp("CHECK_CONTEXT_LOGIN_MAPPING_REGEXP", "[$%\\.+a-zA-Z0-9_-]");
        if (null != loginMappings) {
            for (final String mapping : loginMappings) {
                final String illegal = mapping.replaceAll(login_regexp, "");
                if (illegal.length() > 0) {
                    throw new InvalidDataException("Illegal chars: \"" + illegal + "\"" + " in login mapping");
                }
            }
        }
    }

    private StringBuilder builduppath(final String ctxdir, final URI uri) {
        final StringBuilder src = new StringBuilder(uri.getPath());
        if (src.length() == 0 || src.charAt(src.length() - 1) != '/') {
            src.append('/');
        }
        src.append(ctxdir);
        if (src.charAt(src.length() - 1) == '/') {
            src.deleteCharAt(src.length() - 1);
        }
        return src;
    }

    @Override
    public void changeModuleAccess(final Context ctx, final UserModuleAccess access, final Credentials credentials)
        throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        try {
            doNullCheck(access);
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for create is null", e3);
            throw e3;
        }

        new BasicAuthenticator(context).doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, ctx);
        } catch (NoSuchObjectException e) {
            throw new NoSuchContextException(e);
        }

        LOGGER.debug("{} - {}", ctx, access);

        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        oxContextPlugin.changeModuleAccess(ctx, access, auth);
                    }
                }
            }

            final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();

            // change rights for all users in context to specified one in access
            if (access.isPublicFolderEditable()) {
                // publicFolderEditable can only be applied to the context administrator.
                Integer[] userIds = i2I(oxu.getAll(ctx));
                final int adminId = tool.getAdminForContext(ctx);
                userIds = com.openexchange.tools.arrays.Arrays.remove(userIds, I(adminId));
                oxu.changeModuleAccess(ctx, adminId, access);
                access.setPublicFolderEditable(false);
                oxu.changeModuleAccess(ctx, I2i(userIds), access);
            } else {
                oxu.changeModuleAccess(ctx, oxu.getAll(ctx), access);
            }
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final PluginException e) {
            LOGGER.error("", e);
            throw StorageException.wrapForRMI(e);
        }
    }

    @Override
    public void changeModuleAccess(final Context ctx, final String access_combination_name, final Credentials credentials)
        throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {

        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        try {
            doNullCheck(access_combination_name);
            if (access_combination_name.trim().length() == 0) {
                throw new InvalidDataException("Invalid access combination name");
            }
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for create is null", e3);
            throw e3;
        }

        new BasicAuthenticator(context).doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, ctx);
        } catch (NoSuchObjectException e) {
            throw new NoSuchContextException(e);
        }

        LOGGER.debug("{} - {}", ctx, access_combination_name);

        try {

            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }

            UserModuleAccess accessAdmin = cache.getNamedAccessCombination(access_combination_name.trim(), true);
            UserModuleAccess accessUser = cache.getNamedAccessCombination(access_combination_name.trim(), false);
            if (null == accessAdmin || null == accessUser) {
                // no such access combination name defined in configuration
                // throw error!
                throw new InvalidDataException("No such access combination name \"" + access_combination_name.trim() + "\"");
            }
            accessAdmin = accessAdmin.clone();
            accessUser = accessUser.clone();

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        oxContextPlugin.changeModuleAccess(ctx, access_combination_name, auth);
                    }
                }
            }

            final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();

            // change rights for all users in context to specified one in access combination name
            Integer[] userIds = i2I(oxu.getAll(ctx));
            final int adminId = tool.getAdminForContext(ctx);
            userIds = com.openexchange.tools.arrays.Arrays.remove(userIds, I(adminId));
            oxu.changeModuleAccess(ctx, adminId, accessAdmin);
            oxu.changeModuleAccess(ctx, I2i(userIds), accessUser);
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final PluginException e) {
            LOGGER.error("", e);
            throw StorageException.wrapForRMI(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void downgrade(final Context ctx, final Credentials credentials) throws
        RemoteException, InvalidCredentialsException, NoSuchContextException,
        StorageException, DatabaseUpdateException, InvalidDataException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is invalid");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }
        new BasicAuthenticator(context).doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, ctx);
        } catch (NoSuchObjectException e) {
            throw new NoSuchContextException(e);
        }

        LOGGER.debug(ctx.toString());

        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
        }

        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
        try {
            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        try {
                            oxContextPlugin.downgrade(ctx, auth);
                        } catch (final PluginException e) {
                            LOGGER.error("", e);
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }
            oxcox.downgrade(ctx);
        } catch (final RuntimeException e) {
            LOGGER.error("", e);
            throw e;
        }

        try {
            ContextStorage.getInstance().invalidateContext(ctx.getId().intValue());
        } catch (final OXException e) {
            LOGGER.error("Error invalidating context {} in ox context storage", ctx.getId(), e);
        }
    }

    @Override
    public String getAccessCombinationName(final Context ctx, final Credentials credentials)
        throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        // Resolve admin user and get the module access from db and query cache for access combination name
        try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is invalid");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        new BasicAuthenticator(context).doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, ctx);
        } catch (NoSuchObjectException e) {
            throw new NoSuchContextException(e);
        }

        LOGGER.debug(ctx.toString());

        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        try {
                            oxContextPlugin.getAccessCombinationName(ctx, auth);
                        } catch (final PluginException e) {
                            LOGGER.error("", e);
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }

            // Get admin id and fetch current access object and query cache for its name!
            final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();

            return cache.getNameForAccessCombination(oxu.getModuleAccess(ctx, tool.getAdminForContext(ctx)));
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public UserModuleAccess getModuleAccess(final Context ctx, final Credentials credentials)
        throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        try {
            doNullCheck(ctx);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context is invalid");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        new BasicAuthenticator(context).doAuthentication(auth);

        try {
            setIdOrGetIDFromNameAndIdObject(null, ctx);
        } catch (NoSuchObjectException e) {
            throw new NoSuchContextException(e);
        }

        LOGGER.debug(ctx.toString());

        try {
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            }

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        try {
                            oxContextPlugin.getModuleAccess(ctx, auth);
                        } catch (final PluginException e) {
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }

            // Get admin id and fetch current access object and return it to the client!
            final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
            return oxu.getModuleAccess(ctx, tool.getAdminForContext(ctx));
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    /**
     * @param ctxs
     * @param auth
     * @param oxcox
     * @return null if no extensions available, contexts filled with extensions otherwise
     * @throws StorageException
     */
    private List<Context> callGetDataPlugins(final List<Context> ctxs, final Credentials auth, final OXContextStorageInterface oxcox) throws StorageException {
        List<OXCommonExtension> retval = null;
        boolean extensionsFound = false;

        PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
        if (null != pluginInterfaces) {
            for (final OXContextPluginInterface oxctx : pluginInterfaces.getContextPlugins().getServiceList()) {
                extensionsFound = true;
                final String bundlename = oxctx.getClass().getName();
                LOGGER.debug("Calling getData for plugin: {}", bundlename);
                try {
                    retval = oxctx.getData(ctxs, auth);
                    addExtensionToContext(ctxs, retval, bundlename);
                } catch (final PluginException e) {
                    LOGGER.error("Error while calling method list of plugin {}", bundlename, e);
                    throw StorageException.wrapForRMI(e);
                }
            }
        }

        return extensionsFound ? ctxs : null;
    }

    private void addExtensionToContext(final List<Context> ctxs, final List<OXCommonExtension> retval, final String bundlename) throws PluginException {
        if (null != retval) {
            if (retval.size() != ctxs.size()) {
                throw new PluginException("After the call of plugin: " + bundlename + " the size of the context and the extensions differ");
            }
            for (int i = 0; i < retval.size(); i++) {
                try {
                    ctxs.get(i).addExtension(retval.get(i));
                } catch (final DuplicateExtensionException e) {
                    throw new PluginException(e);
                }
            }
        }
    }

    @Override
    public int getAdminId(final Context ctx, final Credentials credentials) throws RemoteException, InvalidCredentialsException, StorageException, NoSuchContextException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        new BasicAuthenticator(context).doAuthentication(auth);

        // Trigger plugin extensions
        {
            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                    try {
                        oxContextPlugin.getAdminId(ctx, auth);
                    } catch (final PluginException e) {
                        LOGGER.error("", e);
                        throw StorageException.wrapForRMI(e);
                    }
                }
            }
        }

        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
        }

        return tool.getAdminForContext(ctx);
    }

    @Override
    public boolean exists(final Context ctx, final Credentials credentials) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        new BasicAuthenticator(context).doAuthentication(auth);

        if (ctx == null) {
            throw new InvalidDataException("Given context is invalid");
        }

        // Trigger plugin extensions
        {
            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                    try {
                        oxContextPlugin.exists(ctx, auth);
                    } catch (final PluginException e) {
                        LOGGER.error("", e);
                        throw StorageException.wrapForRMI(e);
                    }
                }
            }
        }

        if (null != ctx.getId()) {
            return tool.existsContext(ctx);
        } else if (null != ctx.getName()) {
            return tool.existsContextName(ctx.getName());
        } else {
            throw new InvalidDataException("neither id or name is set in supplied context object");
        }
    }

    @Override
    public boolean checkExists(final Context ctx, final Credentials credentials) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException {
        return exists(ctx, credentials);
    }
}
