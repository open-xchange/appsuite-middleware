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

package com.openexchange.admin.plugin.hosting.rmi.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.java.Autoboxing.i2I;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.plugin.hosting.services.AdminServiceRegistry;
import com.openexchange.admin.plugin.hosting.services.PluginInterfaces;
import com.openexchange.admin.plugin.hosting.storage.interfaces.OXContextStorageInterface;
import com.openexchange.admin.plugin.hosting.tools.DatabaseDataMover;
import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.admin.plugins.OXContextPluginInterfaceExtended;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.NameAndIdObject;
import com.openexchange.admin.rmi.dataobjects.Quota;
import com.openexchange.admin.rmi.dataobjects.SchemaSelectStrategy;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.AbstractAdminRmiException;
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
import com.openexchange.admin.rmi.exceptions.RemoteExceptionUtils;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;
import com.openexchange.admin.rmi.impl.BasicAuthenticator;
import com.openexchange.admin.rmi.impl.OXContextCommonImpl;
import com.openexchange.admin.storage.interfaces.OXUserStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.taskmanagement.TaskManager;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.filestore.FilestoreDataMover;
import com.openexchange.admin.tools.filestore.PostProcessTask;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorages;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.java.Strings;
import com.openexchange.tools.pipesnfilters.Filter;

public class OXContext extends OXContextCommonImpl implements OXContextInterface {

    /** The logger */
    static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OXContext.class);

    private final AdminCache cache;

    /**
     * Initializes a new {@link OXContext}.
     */
    public OXContext() {
        super();
        cache = ClientAdminThread.cache;
        log(LogLevel.DEBUG, LOGGER, null, null, "Class loaded: {}", this.getClass().getName());
    }

    private void logAndEnhanceException(Throwable t, final Credentials credentials) {
        logAndEnhanceException(t, credentials, (String) null);
    }

    private void logAndEnhanceException(Throwable t, final Credentials credentials, final Context ctx) {
        logAndEnhanceException(t, credentials, null != ctx ? ctx.getIdAsString() : null);
    }

    private void logAndEnhanceException(Throwable t, final Credentials credentials, final String contextId) {
        if (t instanceof AbstractAdminRmiException) {
            logAndReturnException(LOGGER, ((AbstractAdminRmiException) t), credentials, contextId);
        } else if (t instanceof RemoteException) {
            RemoteException remoteException = (RemoteException) t;
            String exceptionId = AbstractAdminRmiException.generateExceptionId();
            RemoteExceptionUtils.enhanceRemoteException(remoteException, exceptionId);
            logAndReturnException(LOGGER, remoteException, exceptionId, credentials, contextId);
        }
    }

    @Override
    public Quota[] listQuotas(Context ctx, Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;

            BasicAuthenticator.createNonPluginAwareAuthenticator().doAuthentication(auth);

            callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

            try {
                setIdOrGetIDFromNameAndIdObject(null, ctx);
            } catch (NoSuchObjectException e) {
                throw new NoSuchContextException(e);
            }
            checkExistence(ctx);
            OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.listQuotas(ctx);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public void changeQuota(final Context ctx, final String sModule, final long quotaValue, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        try {
            final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            if (com.openexchange.java.Strings.isEmpty(sModule)) {
                throw new InvalidDataException("No valid module specified.");
            }
            final String[] mods = sModule.split(" *, *");
            final Set<String> modules = new LinkedHashSet<>(mods.length);
            for (final String mod : mods) {
                modules.add(mod);
            }

            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

            callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

            try {
                setIdOrGetIDFromNameAndIdObject(null, ctx);
            } catch (NoSuchObjectException e) {
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

            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), null, "{} - {} - {}", ctx, modules, Long.valueOf(quota));
            checkExistence(ctx);

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
            oxcox.changeQuota(ctx, new ArrayList<>(modules), quota, auth);
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, null != ctx ? ctx.getIdAsString() : null);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public Set<String> getCapabilities(final Context ctx, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        if (null == ctx) {
            throw new InvalidDataException("Missing context.");
        }

        try {
            final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);
            callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);
            try {
                setIdOrGetIDFromNameAndIdObject(null, ctx);
            } catch (NoSuchObjectException e) {
                throw new NoSuchContextException(e.getMessage());
            }
            checkExistence(ctx);
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.getCapabilities(ctx);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public void changeCapabilities(final Context ctx, Set<String> capsToAdd, Set<String> capsToRemove, Set<String> capsToDrop, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        if ((null == capsToAdd || capsToAdd.isEmpty()) && (null == capsToRemove || capsToRemove.isEmpty()) && (null == capsToDrop || capsToDrop.isEmpty())) {
            throw new InvalidDataException("No capabilities specified.");
        }

        try {
            Set<String> capasToAdd = capsToAdd;
            if (capasToAdd == null) {
                capasToAdd = Collections.emptySet();
            }
            Set<String> capasToRemove = capsToRemove;
            if (capasToRemove == null) {
                capasToRemove = Collections.emptySet();
            }
            Set<String> capasToDrop = capsToDrop;
            if (capasToDrop == null) {
                capasToDrop = Collections.emptySet();
            }
            if (null == ctx) {
                throw new InvalidDataException("Missing context.");
            }
            final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

            callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

            try {
                setIdOrGetIDFromNameAndIdObject(null, ctx);
            } catch (NoSuchObjectException e) {
                throw new NoSuchContextException(e);
            }
            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), null, "{} - {} | {}", ctx, capasToAdd.toString(), capasToRemove.toString());
            checkExistence(ctx);

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        oxContextPlugin.changeCapabilities(ctx, capasToAdd, capasToRemove, capasToDrop, auth);
                    }
                }
            }

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.changeCapabilities(ctx, capasToAdd, capasToRemove, capasToDrop, auth);
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, null != ctx ? ctx.getIdAsString() : null);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public void change(final Context ctx, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        if (null == ctx) {
            InvalidDataException x = new InvalidDataException("context is null");
            log(LogLevel.ERROR, LOGGER, credentials, x, "Context is invalid");
            throw x;
        }

        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);
            validateloginmapping(ctx);

            callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

            try {
                setIdOrGetIDFromNameAndIdObject(null, ctx);
            } catch (NoSuchObjectException e) {
                throw new NoSuchContextException(e);
            }
            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), null, ctx.toString());
            Set<String> loginMappings = null; // used for invalidating old login mappings in the cache
            if (ctx.getName() == null) {
                checkExistence(ctx);
            } else if (Strings.isEmpty(ctx.getName())) {
                ctx.setName(null);
                checkExistence(ctx);
            } else {
                if (false == tool.checkContextName(ctx)) {
                    // Holds the same name
                    ctx.setName(null);
                }
            }

            // check if he wants to change the filestore id, if yes, make sure filestore with this id exists in the system
            if (ctx.getFilestoreId() != null) {
                if (!tool.existsStore(ctx.getFilestoreId().intValue())) {
                    final InvalidDataException inde = new InvalidDataException("No such filestore with id " + ctx.getFilestoreId());
                    throw logAndReturnException(LOGGER, inde, credentials, ctx.getIdAsString());
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
            try {
                final ContextStorage cs = ContextStorage.getInstance();
                cs.invalidateContext(ctx.getId().intValue());
                if (loginMappings != null && !loginMappings.isEmpty()) {
                    for (String loginMapping : loginMappings) {
                        cs.invalidateLoginInfo(loginMapping);
                    }
                }
            } catch (OXException e) {
                log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), null, e, "Error invalidating cached infos of context {} in context storage", ctx.getId());
            }
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, ctx.getIdAsString());
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public Context create(final Context ctx, final User admin_user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        try {
            return createcommon(ctx, admin_user, null, null, auth, getDefaultSchemaSelectStrategy());
        } catch (Throwable e) {
            logAndEnhanceException(e, auth, ctx);
            throw e;
        }
    }

    @Override
    public Context create(final Context ctx, final User admin_user, final Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        try {
            return createcommon(ctx, admin_user, null, null, auth, schemaSelectStrategy);
        } catch (Throwable e) {
            logAndEnhanceException(e, auth, ctx);
            throw e;
        }
    }

    @Override
    public Context create(final Context ctx, final User admin_user, final String access_combination_name, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        return create(ctx, admin_user, access_combination_name, credentials, getDefaultSchemaSelectStrategy());
    }

    @Override
    public Context create(final Context ctx, final User admin_user, final String access_combination_name, final Credentials credentials, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        try {
            final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            // Resolve access rights by name
            try {
                doNullCheck(admin_user, access_combination_name);
                if (access_combination_name.trim().length() == 0) {
                    throw new InvalidDataException("Invalid access combination name");
                }
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for create is null");
                throw e3;
            }

            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), "", null, "{} - {} - {} - {}", ctx, admin_user, access_combination_name, auth);

            UserModuleAccess access = cache.getNamedAccessCombination(access_combination_name.trim(), true);
            if (access == null) {
                // no such access combination name defined in configuration
                // throw error!
                throw new InvalidDataException("No such access combination name \"" + access_combination_name.trim() + "\"");
            }
            access = access.clone();

            return createcommon(ctx, admin_user, null, access, auth, schemaSelectStrategy);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public Context create(final Context ctx, final User admin_user, final UserModuleAccess access, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        return create(ctx, admin_user, access, credentials, getDefaultSchemaSelectStrategy());
    }

    @Override
    public Context create(final Context ctx, final User admin_user, final UserModuleAccess access, final Credentials credentials, SchemaSelectStrategy schemaSelectStrategy) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        try {
            final Credentials auth = credentials == null ? new Credentials("", "") : credentials;

            try {
                doNullCheck(admin_user, access);
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for create is null");
                throw e3;
            }

            return createcommon(ctx, admin_user, null, access, auth, schemaSelectStrategy);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public void delete(final Context ctx, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException {
        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            BasicAuthenticator basicAuthenticator = BasicAuthenticator.createPluginAwareAuthenticator();
            basicAuthenticator.doAuthentication(auth);

            callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

            try {
                setIdOrGetIDFromNameAndIdObject(null, ctx);
            } catch (NoSuchObjectException e) {
                throw new NoSuchContextException(e);
            }
            log(LogLevel.DEBUG, LOGGER, credentials, null, ctx.toString());
            checkExistence(ctx);

            try {
                if (tool.checkAndUpdateSchemaIfRequired(ctx)) {
                    throw tool.generateDatabaseUpdateException(ctx.getId().intValue());
                }
            } catch (StorageException e) {
                // Context deletion should be a robust process. Therefore not failing if the schema is not up
                log(LogLevel.DEBUG, LOGGER, credentials, e, "Error while checking/updating schema");
            }

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();

            // Trigger plug-in extensions for pre-deletion
            Map<OXContextPluginInterfaceExtended, Map<String, Object>> undeleteInfos = null;
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    List<OXContextPluginInterface> plugins = pluginInterfaces.getContextPlugins().getServiceList();
                    for (final OXContextPluginInterface oxContextPlugin : plugins) {
                        if (oxContextPlugin instanceof OXContextPluginInterfaceExtended) {
                            OXContextPluginInterfaceExtended extended = (OXContextPluginInterfaceExtended) oxContextPlugin;
                            Map<String, Object> undoInfo = extended.undoableDelete(ctx, auth);
                            if (undoInfo != null) {
                                if (undeleteInfos == null) {
                                    undeleteInfos = new LinkedHashMap<OXContextPluginInterfaceExtended, Map<String, Object>>(plugins.size());
                                }
                                undeleteInfos.put(extended, undoInfo);
                            }
                        } else {
                            oxContextPlugin.delete(ctx, auth);
                        }
                    }
                }
            }

            if (undeleteInfos == null) {
                oxcox.delete(ctx);
            } else {
                boolean deleted = false;
                try {
                    oxcox.delete(ctx);
                    deleted = true;
                } finally {
                    if (!deleted) {
                        for (Map.Entry<OXContextPluginInterfaceExtended, Map<String, Object>> undeleteInfo : undeleteInfos.entrySet()) {
                            try {
                                Map<String, Object> undoInfo = undeleteInfo.getValue();
                                undeleteInfo.getKey().undelete(ctx, undoInfo);
                            } catch (PluginException x) {
                                log(LogLevel.WARNING, LOGGER, credentials, ctx.getIdAsString(), x, "Undeletion failed");
                            }
                        }
                    }
                }
            }

            basicAuthenticator.removeFromAuthCache(ctx);
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, ctx.getIdAsString());
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
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
        } catch (InvalidDataException e1) {
            log(LogLevel.ERROR, LOGGER, credentials, e1, "Invalid data sent by client!");
            throw e1;
        }

        try {
            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

            callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

            try {
                setIdOrGetIDFromNameAndIdObject(null, ctx);
            } catch (NoSuchObjectException e) {
                throw new NoSuchContextException(e);
            }
            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), null, "{} - {}", ctx, reason);
            checkExistence(ctx);

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        oxContextPlugin.disable(ctx, auth);
                    }
                }
            }

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.disable(ctx, reason);
            log(LogLevel.INFO, LOGGER, credentials, ctx.getIdAsString(), null, "Context {} successfully disabled", ctx.getId());

            try {
                ContextStorage.getInstance().invalidateContext(ctx.getId().intValue());
                log(LogLevel.INFO, LOGGER, credentials, ctx.getIdAsString(), null, "Context {} successfully invalidated", ctx.getId());
            } catch (OXException e) {
                log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), e, "Error invalidating context {} in ox context storage", ctx.getId());
            }
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, ctx.getIdAsString());
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
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
        } catch (InvalidDataException e1) {
            log(LogLevel.ERROR, LOGGER, credentials, e1, "Invalid data sent by client!");
            throw e1;
        }
        try {
            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);
            Integer reason_id = reason.getId();
            log(LogLevel.DEBUG, LOGGER, credentials, null, "{}", reason_id);
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            if (ClientAdminThreadExtended.cache.isMasterAdmin(auth)) {
                oxcox.disableAll(reason);
            } else {
                // Trigger plugin extensions
                {
                    final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                    if (null != pluginInterfaces) {
                        for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                            oxContextPlugin.disableAll(auth);
                        }
                    }
                }
            }
            // Clear context cache
            // CACHE
            final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
            if (null != cacheService) {
                try {
                    final Cache cache = cacheService.getCache("Context");
                    cache.clear();
                } catch (OXException e) {
                    log(LogLevel.ERROR, LOGGER, credentials, null, "{}", reason_id);
                }
            }
            // END OF CACHE
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials);
            throw e;
        }
    }

    @Override
    public void enable(final Context ctx, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        if (null == ctx) {
            InvalidDataException invalidDataException = new InvalidDataException("Context is null");
            log(LogLevel.ERROR, LOGGER, credentials, invalidDataException, "");
            throw invalidDataException;
        }

        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

            callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

            try {
                setIdOrGetIDFromNameAndIdObject(null, ctx);
            } catch (NoSuchObjectException e) {
                throw new NoSuchContextException(e);
            }
            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), null, ctx.toString());
            checkExistence(ctx);

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        oxContextPlugin.enable(ctx, auth);
                    }
                }
            }

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.enable(ctx);

            try {
                ContextStorage.getInstance().invalidateContext(ctx.getId().intValue());
            } catch (OXException e) {
                log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), e, "Error invalidating context {} in ox context storage", ctx.getId());
            }
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, ctx.getIdAsString());
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public void enableAll(final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException {
        try {
            final Credentials auth = credentials == null ? new Credentials("", "") : credentials;

            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            if (ClientAdminThreadExtended.cache.isMasterAdmin(auth)) {
                oxcox.enableAll();
            } else {
                // Trigger plugin extensions
                {
                    final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                    if (null != pluginInterfaces) {
                        for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                            oxContextPlugin.enableAll(auth);
                        }
                    }
                }
            }

            // Clear context cache
            // CACHE
            final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
            if (null != cacheService) {
                try {
                    final Cache cache = cacheService.getCache("Context");
                    cache.clear();
                } catch (OXException e) {
                    log(LogLevel.ERROR, LOGGER, credentials, e, "");
                }
            }
            // END OF CACHE
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials);
            throw e;
        }
    }

    @Override
    public Context getOwnData(Context ctx, Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        if (null == ctx) {
            InvalidDataException invalidDataException = new InvalidDataException("Context is null");
            log(LogLevel.ERROR, LOGGER, credentials, invalidDataException, "");
            throw invalidDataException;
        }

        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;

            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth, ctx);

            OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            return oxcox.getData(new Context[] { ctx })[0];
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public Context[] getData(final Context[] ctxs, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            try {
                doNullCheck((Object[]) ctxs);
            } catch (InvalidDataException e1) {
                log(LogLevel.ERROR, LOGGER, credentials, e1, "One of the given arguments for getData is null");
                throw e1;
            }

            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

            callBeforeDbLookupPluginMethods(ctxs, credentials);

            final List<Context> retval = new ArrayList<>();
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
                log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), null, ctx.toString());
                try {
                    checkExistence(ctx);
                } catch (NoSuchContextException e) {
                    log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), e, "");
                    throw e;
                }
            }
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
            Context[] newRetval = retval.toArray(new Context[retval.size()]);
            callAfterDbLookupPluginMethods(newRetval, auth);
            return newRetval;
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, getObjectIds(ctxs));
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, getObjectIds(ctxs));
            throw e;
        }
    }

    @Override
    public Context getData(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        return getData(new Context[] { ctx }, auth)[0];
    }

    @Override
    public Context[] list(String search_pattern, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return list(search_pattern, -1, -1, credentials);
    }

    @Override
    public Context[] list(String search_pattern, int offset, int length, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        if (null == search_pattern) {
            InvalidDataException invalidDataException = new InvalidDataException("Search pattern is null");
            log(LogLevel.ERROR, LOGGER, credentials, invalidDataException, "");
            throw invalidDataException;
        }

        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;

            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

            log(LogLevel.DEBUG, LOGGER, credentials, null, "{}", search_pattern);

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            Filter<Context, Context> loader = null;
            Filter<Integer, Integer> filter = null;
            final ArrayList<Filter<Context, Context>> loaderFilter = new ArrayList<>();
            final ArrayList<Filter<Integer, Integer>> contextFilter = new ArrayList<>();

            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (OXContextPluginInterface oxctx : pluginInterfaces.getContextPlugins().getServiceList()) {
                    log(LogLevel.DEBUG, LOGGER, credentials, null, "Calling list for plugin: {}", oxctx.getClass().getName());
                    filter = oxctx.filter(auth);
                    if (null != filter) {
                        contextFilter.add(filter);
                    }
                    loader = oxctx.list(search_pattern, auth);
                    if (null != loader) {
                        loaderFilter.add(loader);
                    }
                }
                search_pattern = callSearchDbLookupPluginMethods(search_pattern, auth);
            }

            Context[] newRetval = oxcox.listContext(search_pattern, contextFilter, loaderFilter, offset, length);
            callAfterDbLookupPluginMethods(newRetval, auth);
            return newRetval;
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials);
            throw e;
        }
    }

    @Override
    public Context[] listAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return listAll(-1, -1, auth);
    }

    @Override
    public Context[] listAll(int offset, int length, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        return list("*", offset, length, auth);
    }

    @Override
    public Context[] listByDatabase(Database db, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchDatabaseException {
        return listByDatabase(db, -1, -1, credentials);
    }

    @Override
    public Context[] listByDatabase(Database db, int offset, int length, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchDatabaseException {
        if (null == db) {
            InvalidDataException invalidDataException = new InvalidDataException("Database is null");
            log(LogLevel.ERROR, LOGGER, credentials, invalidDataException, "");
            throw invalidDataException;
        }

        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;

            BasicAuthenticator.createNonPluginAwareAuthenticator().doAuthentication(auth);

            try {
                setIdOrGetIDFromNameAndIdObject(null, db);
            } catch (NoSuchObjectException e) {
                throw new NoSuchDatabaseException(e);
            }
            log(LogLevel.DEBUG, LOGGER, credentials, null, db.toString());
            if (!tool.existsDatabase(db.getId().intValue())) {
                throw new NoSuchDatabaseException();
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();

            final List<Context> retval = new ArrayList<>();
            final Context[] ret = oxcox.searchContextByDatabase(db, offset, length);
            final List<Context> callGetDataPlugins = callGetDataPlugins(Arrays.asList(ret), auth, oxcox);
            if (null != callGetDataPlugins) {
                retval.addAll(callGetDataPlugins);
            } else {
                retval.addAll(Arrays.asList(ret));
            }

            Context[] newRetval = retval.toArray(new Context[retval.size()]);
            callAfterDbLookupPluginMethods(newRetval, auth);
            return newRetval;
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials);
            throw e;
        }
    }

    @Override
    public Context[] listByFilestore(Filestore filestore, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchFilestoreException {
        return listByFilestore(filestore, -1, -1, credentials);
    }

    @Override
    public Context[] listByFilestore(Filestore filestore, int offset, int length, Credentials credentials) throws RemoteException, StorageException, InvalidDataException, InvalidCredentialsException, NoSuchFilestoreException {
        if (null == filestore) {
            InvalidDataException invalidDataException = new InvalidDataException("Filestore is null");
            log(LogLevel.ERROR, LOGGER, credentials, invalidDataException, "");
            throw invalidDataException;
        }
        if (null == filestore.getId()) {
            InvalidDataException invalidDataException = new InvalidDataException("Filestore ID is null");
            log(LogLevel.ERROR, LOGGER, credentials, invalidDataException, "");
            throw invalidDataException;
        }

        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;

            BasicAuthenticator.createNonPluginAwareAuthenticator().doAuthentication(auth);

            log(LogLevel.DEBUG, LOGGER, credentials, null, filestore.toString());
            if (!tool.existsStore(filestore.getId().intValue())) {
                throw new NoSuchFilestoreException();
            }
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            final List<Context> retval = new ArrayList<>();
            final Context[] ret = oxcox.searchContextByFilestore(filestore, offset, length);
            final List<Context> callGetDataPlugins = callGetDataPlugins(Arrays.asList(ret), auth, oxcox);
            if (null != callGetDataPlugins) {
                retval.addAll(callGetDataPlugins);
            } else {
                retval.addAll(Arrays.asList(ret));
            }

            Context[] newRetval = retval.toArray(new Context[retval.size()]);
            callAfterDbLookupPluginMethods(newRetval, auth);
            return newRetval;
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials);
            throw e;
        }
    }

    /**
     * @see com.openexchange.admin.plugin.hosting.rmi.OXContextInterface#moveContextDatabase(com.openexchange.admin.plugin.hosting.rmi.dataobjects.Context, com.openexchange.admin.plugin.hosting.rmi.dataobjects.Database,
     *      com.openexchange.admin.plugin.hosting.rmi.dataobjects.Credentials)
     */
    @Override
    public int moveContextDatabase(final Context ctx, final Database db, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, OXContextException {
        return moveContextDatabase(ctx, db, new MaintenanceReason(Integer.valueOf(42)), auth);
    }

    private int moveContextDatabase(final Context ctx, final Database db, final MaintenanceReason reason, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, OXContextException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            doNullCheck(ctx, db, reason);
            doNullCheck(reason.getId());
        } catch (InvalidDataException e1) {
            log(LogLevel.ERROR, LOGGER, credentials, e1, "Invalid data sent by client!");
            throw e1;
        }

        try {
            BasicAuthenticator.createNonPluginAwareAuthenticator().doAuthentication(auth);

            callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

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
            Integer reason_id = reason.getId();
            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), null, "{} - {} - {}", ctx, db, reason_id);
            checkExistence(ctx);
            if (tool.checkAndUpdateSchemaIfRequired(ctx)) {
                throw tool.generateDatabaseUpdateException(ctx.getId().intValue());
            }
            if (!tool.isContextEnabled(ctx)) {
                throw new OXContextException(OXContextException.CONTEXT_DISABLED);
            }
            final Integer dbid = db.getId();
            if (!tool.isMasterDatabase(dbid.intValue())) {
                throw new OXContextException("Database with id " + dbid + " is NOT a master!");
            }
            {
                // Check if target database is already source database
                OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
                Context storageVersion = oxcox.getData(new Context(ctx.getId()));
                if (storageVersion.getWriteDatabase().getId().intValue() == dbid.intValue()) {
                    throw new OXContextException("Context with id " + ctx.getId() + " already exists in database with id " + dbid);
                }
            }
            final DatabaseDataMover ddm = new DatabaseDataMover(ctx, db, reason);

            return TaskManager.getInstance().addJob(ddm, "movedatabase", "move context " + ctx.getIdAsString() + " to database " + dbid, ctx.getId().intValue());
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public int moveContextFilestore(final Context ctx, final Filestore dst_filestore, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, NoSuchReasonException, OXContextException {
        final MaintenanceReason reason = new MaintenanceReason(I(42));
        return moveContextFilestore(ctx, dst_filestore, reason, auth);
    }

    private int moveContextFilestore(final Context ctx, final Filestore dst_filestore, final MaintenanceReason reason, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, NoSuchReasonException, OXContextException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            doNullCheck(ctx, dst_filestore, reason);
            doNullCheck(dst_filestore.getId(), reason.getId());
        } catch (InvalidDataException e) {
            log(LogLevel.ERROR, LOGGER, credentials, e, "Invalid data sent by client!");
            throw e;
        }

        try {
            BasicAuthenticator.createNonPluginAwareAuthenticator().doAuthentication(auth);

            Context retval = null;

            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), null, "{} - {}", ctx, dst_filestore);

            final OXContextStorageInterface oxcox;
            try {
                oxcox = OXContextStorageInterface.getInstance();
            } catch (StorageException e) {
                log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), e, "");
                throw new StorageException(e.getMessage());
            }
            try {
                try {
                    setIdOrGetIDFromNameAndIdObject(null, ctx);
                } catch (NoSuchObjectException e) {
                    throw new NoSuchContextException(e);
                }
                checkExistence(ctx);
                if (!tool.existsStore(dst_filestore.getId().intValue())) {
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
                Filestore destFilestore = oxu.getFilestore(dst_filestore.getId().intValue(), false);

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
                    throw StorageException.wrapForRMI(e);
                } catch (URISyntaxException e) {
                    throw new StorageException("Invalid file storage URI: " + baseUri, e);
                }

                try {
                    // Initialize mover instance
                    FilestoreDataMover fsdm = FilestoreDataMover.newContextMover(oxu.getFilestore(srcStore_id, false), destFilestore, ctx);

                    // Enable context after processing
                    fsdm.addPostProcessTask(new PostProcessTask() {

                        @Override
                        public void perform(ExecutionException executionError) throws StorageException {
                            if (null == executionError) {
                                oxcox.enable(ctx);
                            } else {
                                log(LogLevel.WARNING, LOGGER, credentials, ctx.getIdAsString(), null, "An execution error occurred during \"movefilestore\" for context {}. Context will stay disabled.", ctx.getId(), executionError.getCause());
                            }
                        }
                    });

                    // Schedule task
                    oxcox.disable(ctx, reason);
                    return TaskManager.getInstance().addJob(fsdm, "movefilestore", "move context " + ctx.getIdAsString() + " to filestore " + dst_filestore.getId(), ctx.getId().intValue());
                } catch (StorageException e) {
                    throw new StorageException(e.getMessage());
                }
            } finally {
                oxcox.enable(ctx);
            }
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
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
        final ConfigViewFactory viewFactory = AdminServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (viewFactory != null) {
            ConfigView view;
            try {
                view = viewFactory.getView(admin_user.getId().intValue(), ctx.getId().intValue());
                Boolean check = view.opt("com.openexchange.imap.initWithSpecialUse", Boolean.class, Boolean.TRUE);
                if (check != null && check.booleanValue()) {
                    ConfigProperty<Boolean> prop = view.property("user", "com.openexchange.mail.specialuse.check", Boolean.class);
                    prop.set(Boolean.TRUE);
                }
            } catch (OXException e) {
                log(LogLevel.ERROR, LOGGER, auth, ctx.getIdAsString(), e, "Unable to set special use check property!");
            }
        }

        if (isAnyPluginLoaded()) {
            PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                PluginException pe = null;
                for (OXContextPluginInterface contextInterface : pluginInterfaces.getContextPlugins().getServiceList()) {
                    try {
                        ret = contextInterface.postCreate(ret, admin_user, createaccess, auth);
                    } catch (PluginException e) {
                        pe = e;
                    }
                }
                if (null != pe) {
                    // cleanup
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        try {
                            oxContextPlugin.delete(ctx, auth);
                        } catch (PluginException e) {
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                    oxcox.delete(ret);
                    throw StorageException.wrapForRMI(pe);
                }
            }
        }

        return ret;
    }

    private void validateloginmapping(final Context ctx) throws InvalidDataException {
        Set<String> loginMappings = ctx.getLoginMappings();
        if (null != loginMappings) {
            String login_regexp = ClientAdminThreadExtended.cache.getProperties().getProp("CHECK_CONTEXT_LOGIN_MAPPING_REGEXP", "[$%\\.+a-zA-Z0-9_-]");
            Pattern pattern = Pattern.compile(login_regexp);
            for (String mapping : loginMappings) {
                String illegal = pattern.matcher(mapping).replaceAll("");
                if (illegal.length() > 0) {
                    throw new InvalidDataException("Illegal chars: \"" + illegal + "\"" + " in login mapping");
                }
            }
        }
    }

    @Override
    public void changeModuleAccess(final Context ctx, final UserModuleAccess access, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        try {
            doNullCheck(access);
        } catch (InvalidDataException e3) {
            log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for create is null");
            throw e3;
        }

        try {
            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

            callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

            try {
                setIdOrGetIDFromNameAndIdObject(null, ctx);
            } catch (NoSuchObjectException e) {
                throw new NoSuchContextException(e);
            }

            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), null, "{} - {}", ctx, access);
            checkExistence(ctx);

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
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, ctx.getIdAsString());
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public void changeModuleAccess(final Context ctx, final String access_combination_name, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {

        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        try {
            doNullCheck(access_combination_name);
            if (access_combination_name.trim().length() == 0) {
                throw new InvalidDataException("Invalid access combination name");
            }
        } catch (InvalidDataException e3) {
            log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), e3, "One of the given arguments for create is null");
            throw e3;
        }

        try {
            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

            callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

            try {
                setIdOrGetIDFromNameAndIdObject(null, ctx);
            } catch (NoSuchObjectException e) {
                throw new NoSuchContextException(e);
            }

            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), null, "{} - {}", ctx, access_combination_name);

            checkExistence(ctx);

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
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, ctx.getIdAsString());
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void downgrade(final Context ctx, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException {
        if (null == ctx) {
            InvalidDataException invalidDataException = new InvalidDataException("Context is null");
            log(LogLevel.ERROR, LOGGER, credentials, invalidDataException, "");
            throw invalidDataException;
        }

        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;

            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

            callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

            try {
                setIdOrGetIDFromNameAndIdObject(null, ctx);
            } catch (NoSuchObjectException e) {
                throw new NoSuchContextException(e);
            }

            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), null, ctx.toString());

            checkExistence(ctx);

            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        try {
                            oxContextPlugin.downgrade(ctx, auth);
                        } catch (PluginException e) {
                            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, ctx.getIdAsString());
                        }
                    }
                }
            }
            oxcox.downgrade(ctx);

            try {
                ContextStorage.getInstance().invalidateContext(ctx.getId().intValue());
            } catch (OXException e) {
                log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), e, "Error invalidating context {} in ox context storage", ctx.getId());
            }
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public String getAccessCombinationName(final Context ctx, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        if (null == ctx) {
            InvalidDataException invalidDataException = new InvalidDataException("Context is null");
            log(LogLevel.ERROR, LOGGER, credentials, invalidDataException, "");
            throw invalidDataException;
        }

        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;

            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

            callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

            try {
                setIdOrGetIDFromNameAndIdObject(null, ctx);
            } catch (NoSuchObjectException e) {
                throw new NoSuchContextException(e);
            }

            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), null, ctx.toString());

            // Resolve admin user and get the module access from db and query cache for access combination name
            checkExistence(ctx);

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        oxContextPlugin.getAccessCombinationName(ctx, auth);
                    }
                }
            }

            // Get admin id and fetch current access object and query cache for its name!
            final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();

            return cache.getNameForAccessCombination(oxu.getModuleAccess(ctx, tool.getAdminForContext(ctx)));
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, ctx.getIdAsString());
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public UserModuleAccess getModuleAccess(final Context ctx, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        if (null == ctx) {
            InvalidDataException invalidDataException = new InvalidDataException("Context is null");
            log(LogLevel.ERROR, LOGGER, credentials, invalidDataException, "");
            throw invalidDataException;
        }

        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;

            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

            callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

            try {
                setIdOrGetIDFromNameAndIdObject(null, ctx);
            } catch (NoSuchObjectException e) {
                throw new NoSuchContextException(e);
            }

            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), null, ctx.toString());

            checkExistence(ctx);

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        oxContextPlugin.getModuleAccess(ctx, auth);
                    }
                }
            }

            // Get admin id and fetch current access object and return it to the client!
            final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
            return oxu.getModuleAccess(ctx, tool.getAdminForContext(ctx));
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, ctx.getIdAsString());
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
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
    private List<Context> callGetDataPlugins(final List<Context> ctxs, final Credentials auth, @SuppressWarnings("unused") final OXContextStorageInterface oxcox) throws PluginException {
        List<OXCommonExtension> retval = null;
        boolean extensionsFound = false;

        PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
        if (null != pluginInterfaces) {
            for (final OXContextPluginInterface oxctx : pluginInterfaces.getContextPlugins().getServiceList()) {
                extensionsFound = true;
                final String bundlename = oxctx.getClass().getName();
                log(LogLevel.DEBUG, LOGGER, auth, getObjectIds(ctxs.toArray(new NameAndIdObject[ctxs.size()])), null, "Calling getData for plugin: {}", bundlename);
                retval = oxctx.getData(ctxs, auth);
                addExtensionToContext(ctxs, retval, bundlename);
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
                } catch (DuplicateExtensionException e) {
                    throw new PluginException(e);
                }
            }
        }
    }

    @Override
    public int getAdminId(final Context ctx, final Credentials credentials) throws RemoteException, InvalidCredentialsException, StorageException, NoSuchContextException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        try {
            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

            callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        oxContextPlugin.getAdminId(ctx, auth);
                    }
                }
            }

            checkExistence(ctx);

            return tool.getAdminForContext(ctx);
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, ctx.getIdAsString());
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public boolean exists(final Context ctx, final Credentials credentials) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

        if (ctx == null) {
            throw new InvalidDataException("Given context is invalid");
        }

        callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

        try {
            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        oxContextPlugin.exists(ctx, auth);
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
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, ctx.getIdAsString());
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public boolean existsInServer(Context ctx, Credentials credentials) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

        if (ctx == null) {
            throw new InvalidDataException("Given context is invalid");
        }

        callBeforeDbLookupPluginMethods(new Context[] { ctx }, credentials);

        try {
            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        oxContextPlugin.existsInServer(ctx, auth);
                    }
                }
            }

            if (null != ctx.getId()) {
                return tool.existsContextInServer(ctx);
            } else if (null != ctx.getName()) {
                return tool.existsContextNameInServer(ctx.getName());
            } else {
                throw new InvalidDataException("neither id or name is set in supplied context object");
            }
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, ctx.getIdAsString());
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx);
            throw e;
        }
    }

    @Override
    public boolean checkExists(final Context ctx, final Credentials credentials) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException {
        return exists(ctx, credentials);
    }

    @Override
    public void checkCountsConsistency(boolean checkDatabaseCounts, boolean checkFilestoreCounts, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException {
        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            BasicAuthenticator.createPluginAwareAuthenticator().doAuthentication(auth);

            log(LogLevel.DEBUG, LOGGER, credentials, null, "Checking consistency for counters");

            OXContextStorageInterface.getInstance().checkCountsConsistency(checkDatabaseCounts, checkFilestoreCounts);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials);
            throw e;
        }
    }

}
