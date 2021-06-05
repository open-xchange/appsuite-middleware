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

package com.openexchange.admin.rmi.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.java.Autoboxing.i;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.plugins.OXGroupPluginInterface;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.AbstractAdminRmiException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.RemoteExceptionUtils;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.services.PluginInterfaces;
import com.openexchange.admin.storage.interfaces.OXGroupStorageInterface;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.group.GroupService;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;

/**
 * Implementation for the RMI interface of group
 *
 * @author d7
 *
 */
public class OXGroup extends OXCommonImpl implements OXGroupInterface {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OXGroup.class);

    // --------------------------------------------------------------------------------------------------------- //

    private final AdminCache cache;
    private final PropertyHandler prop;
    private final BasicAuthenticator basicauth;
    private final OXGroupStorageInterface oxGroup;

    public OXGroup() throws StorageException {
        super();
        try {
            oxGroup = OXGroupStorageInterface.getInstance();
        } catch (StorageException e) {
            log(LogLevel.ERROR, LOGGER, null, e, "");
            throw e;
        }
        cache = ClientAdminThread.cache;
        prop = cache.getProperties();
        basicauth = BasicAuthenticator.createPluginAwareAuthenticator();
        log(LogLevel.INFO, LOGGER, null, null, "Class loaded: {}", this.getClass().getName());
    }

    private void logAndEnhanceException(Throwable t, final Credentials credentials, final Context ctx, final Group grp) {
        logAndEnhanceException(t, credentials, null != ctx ? ctx.getIdAsString() : null, null != grp && null != grp.getId() ? grp.getId().toString() : null);
    }

    private void logAndEnhanceException(Throwable t, final Credentials credentials, final String contextId, final String groupId) {
        if (t instanceof AbstractAdminRmiException) {
            logAndReturnException(LOGGER, ((AbstractAdminRmiException) t), credentials, contextId, groupId);
        } else if (t instanceof RemoteException) {
            RemoteException remoteException = (RemoteException) t;
            String exceptionId = AbstractAdminRmiException.generateExceptionId();
            RemoteExceptionUtils.enhanceRemoteException(remoteException, exceptionId);
            logAndReturnException(LOGGER, remoteException, exceptionId, credentials, contextId, groupId);
        } else if (t instanceof Exception) {
            RemoteException remoteException = RemoteExceptionUtils.convertException((Exception) t);
            String exceptionId = AbstractAdminRmiException.generateExceptionId();
            RemoteExceptionUtils.enhanceRemoteException(remoteException, exceptionId);
            logAndReturnException(LOGGER, remoteException, exceptionId, credentials, contextId, groupId);
        }
    }

    @Override
    public void addMember(final Context ctx, final Group grp, final User[] members, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, NoSuchGroupException {
        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck(grp, members);
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the arguments for addMember is null");
                throw e3;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            basicauth.doAuthentication(auth, ctx);

            try {
                setIdOrGetIDFromNameAndIdObject(ctx, grp);
            } catch (NoSuchObjectException e) {
                throw new NoSuchGroupException(e);
            }

            log(LogLevel.DEBUG, LOGGER, auth, ctx.getIdAsString(), String.valueOf(grp.getId()), null, "{} - {} - {} - {}", ctx, grp, getObjectIds(members), auth);

            checkContextAndSchema(ctx);

            if (!tool.existsGroup(ctx, grp)) {
                throw new NoSuchGroupException("No such group");
            }

            try {
                setUserIdInArrayOfUsers(ctx, members);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }
            if (!tool.existsUser(ctx, members)) {
                throw new NoSuchUserException("No such user");
            }
            for (User user : members) {
                int userId = user.getId().intValue();
                if (tool.isGuestUser(ctx, userId)) {
                    throw new NoSuchUserException("Cannot add guest user to group");
                }
            }

            final int grp_id = grp.getId().intValue();
            if (tool.existsGroupMember(ctx, grp_id, members)) {
                throw new InvalidDataException("Member already exists in group");
            }

            oxGroup.addMember(ctx, grp_id, members);

            // JCS
            final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
            if (null != cacheService) {
                try {
                    Cache cache = cacheService.getCache("User");
                    int contextId = ctx.getId().intValue();
                    for (User user : members) {
                        cache.remove(cacheService.newCacheKey(contextId, user.getId().intValue()));
                        UserConfigurationStorage.getInstance().invalidateCache(user.getId().intValue(), new ContextImpl(ctx.getId().intValue()));
                    }

                    Cache groupCache = cacheService.getCache(GroupService.CACHE_REGION_NAME);
                    groupCache.remove(cacheService.newCacheKey(contextId, grp_id));
                } catch (OXException e) {
                    log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(grp_id), e, "");
                }
            }
            // END OF JCS
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx, grp);
            throw e;
        }
    }

    @Override
    public void change(final Context ctx, final Group grp, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, NoSuchUserException {
        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck(grp);
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for change is null");
                throw e3;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            basicauth.doAuthentication(auth, ctx);

            log(LogLevel.DEBUG, LOGGER, auth, ctx.getIdAsString(), String.valueOf(grp.getId()), null, "{} - {} - {}", ctx, grp, auth);

            checkContextAndSchema(ctx);

            if (!grp.mandatoryChangeMembersSet()) {
                throw new InvalidDataException("Mandatory fields not set: " + grp.getUnsetMembers());
            }

            if (null != grp.getName() && prop.getGroupProp(AdminProperties.Group.AUTO_LOWERCASE, true)) {
                grp.setName(grp.getName().toLowerCase());
            }

            if (null != grp.getName() && prop.getGroupProp(AdminProperties.Group.CHECK_NOT_ALLOWED_CHARS, true)) {
                validateGroupName(grp.getName());
            }

            // if members sent, check existence
            if (grp.getMembers() != null && grp.getMembers().length > 0) {
                final Integer[] mems = grp.getMembers();
                final int[] tmp_mems = new int[mems.length];
                for (int i = 0; i < mems.length; i++) {
                    tmp_mems[i] = mems[i].intValue();
                }
                if (!tool.existsUser(ctx, tmp_mems)) {
                    throw new NoSuchUserException("No such user");
                }
            }

            try {
                setIdOrGetIDFromNameAndIdObject(ctx, grp);
            } catch (NoSuchObjectException e) {
                throw new NoSuchGroupException(e);
            }
            grp.testMandatoryCreateFieldsNull();
            if (!tool.existsGroup(ctx, grp.getId().intValue())) {
                throw new NoSuchGroupException("No such group");
            }

            if (grp.getName() != null && tool.existsGroupName(ctx, grp)) {
                throw new InvalidDataException("Group " + grp.getName() + " already exists in this context");
            }

            oxGroup.change(ctx, grp);

            //JCS
            final User[] new_members = oxGroup.getMembers(ctx, grp.getId().intValue());
            final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
            if (null != cacheService) {
                try {
                    final Cache cache = cacheService.getCache("User");
                    int contextId = ctx.getId().intValue();
                    if (new_members != null) {
                        for (final User user : new_members) {
                            cache.remove(cacheService.newCacheKey(contextId, user.getId().intValue()));
                            UserConfigurationStorage.getInstance().invalidateCache(user.getId().intValue(), new ContextImpl(ctx.getId().intValue()));
                        }
                    }

                    if (grp.getMembers() != null && grp.getMembers().length > 0) {
                        for (final Integer old_user_id : grp.getMembers()) {
                            cache.remove(cacheService.newCacheKey(contextId, old_user_id.intValue()));
                            UserConfigurationStorage.getInstance().invalidateCache(old_user_id.intValue(), new ContextImpl(ctx.getId().intValue()));
                        }
                    }

                    Cache groupCache = cacheService.getCache(GroupService.CACHE_REGION_NAME);
                    groupCache.remove(cacheService.newCacheKey(contextId, grp.getId().intValue()));
                } catch (OXException e) {
                    log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(grp.getId()), e, "");
                }
            }

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXGroupPluginInterface oxgroup : pluginInterfaces.getGroupPlugins().getServiceList()) {
                        final String bundlename = oxgroup.getClass().getName();
                        log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(grp.getId()), null, "Calling change for plugin: {}", bundlename);
                        oxgroup.change(ctx, grp, auth);
                    }
                }
            }
        } catch (PluginException e) {
            throw logAndReturnException(LOGGER, StorageException.wrapForRMI(e), credentials, ctx.getIdAsString(), String.valueOf(grp.getId()));
        } catch (EnforceableDataObjectException e) {
            RemoteException remoteException = RemoteExceptionUtils.convertException(e);
            logAndReturnException(LOGGER, remoteException, e.getExceptionId(), credentials, ctx.getIdAsString(), String.valueOf(grp.getId()));
            throw remoteException;
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx, grp);
            throw e;
        }
    }

    @Override
    public Group create(final Context ctx, final Group grp, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        try {
            final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck(grp);
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for create is null");
                throw e3;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            basicauth.doAuthentication(auth, ctx);

            log(LogLevel.DEBUG, LOGGER, auth, ctx.getIdAsString(), String.valueOf(grp.getId()), null, "{} - {} - {}", ctx, grp, auth);

            checkContextAndSchema(ctx);

            if (!grp.mandatoryCreateMembersSet()) {
                throw new InvalidDataException("Mandatory fields not set: " + grp.getUnsetMembers());
            }

            if (prop.getGroupProp(AdminProperties.Group.AUTO_LOWERCASE, true)) {
                grp.setName(grp.getName().toLowerCase());
            }

            if (prop.getGroupProp(AdminProperties.Group.CHECK_NOT_ALLOWED_CHARS, true)) {
                validateGroupName(grp.getName());
            }

            if (tool.existsGroupName(ctx, grp.getName())) {
                throw new InvalidDataException("Group already exists!");
            }

            // if members sent, check exist
            if (grp.getMembers() != null && grp.getMembers().length > 0) {
                if (!tool.existsUser(ctx, I2i(grp.getMembers()))) {
                    throw new NoSuchUserException("No such user");
                }
                for (Integer userId : grp.getMembers()) {
                    if (tool.isGuestUser(ctx, userId.intValue())) {
                        throw new NoSuchUserException("Cannot add guest user to group");
                    }
                }
            }

            final int retval = oxGroup.create(ctx, grp);
            grp.setId(I(retval));
            final List<OXGroupPluginInterface> interfacelist = new ArrayList<>();

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXGroupPluginInterface oxgroup : pluginInterfaces.getGroupPlugins().getServiceList()) {
                        final String bundlename = oxgroup.getClass().getName();
                        try {
                            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(grp.getId()), null, "Calling create for plugin: {}", bundlename);
                            oxgroup.create(ctx, grp, auth);
                            interfacelist.add(oxgroup);
                        } catch (PluginException e) {
                            log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(grp.getId()), e, "Error while calling create for plugin: {}", bundlename);
                            log(LogLevel.INFO, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(grp.getId()), null, "Now doing rollback for everything until now...");
                            for (final OXGroupPluginInterface oxgroupinterface : interfacelist) {
                                try {
                                    oxgroupinterface.delete(ctx, new Group[] { grp }, auth);
                                } catch (PluginException e1) {
                                    log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(grp.getId()), e1, "Error doing rollback for plugin: {}", bundlename);
                                }
                            }
                            try {
                                oxGroup.delete(ctx, new Group[] { grp });
                            } catch (StorageException e1) {
                                log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(grp.getId()), e1, "Error doing rollback for creating resource in database");
                            }
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }

            // JCS
            // If members sent, remove each from cache
            if (grp.getMembers() != null && grp.getMembers().length > 0) {
                Integer[] mems = grp.getMembers();
                CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
                if (null != cacheService) {
                    try {
                        Cache cache = cacheService.getCache("User");
                        int contextId = ctx.getId().intValue();
                        for (final Integer member_id : mems) {
                            cache.remove(cacheService.newCacheKey(contextId, member_id.intValue()));
                            UserConfigurationStorage.getInstance().invalidateCache(i(member_id), new ContextImpl(ctx.getId().intValue()));
                        }

                        Cache groupCache = cacheService.getCache(GroupService.CACHE_REGION_NAME);
                        groupCache.remove(cacheService.newCacheKey(contextId, GroupStorage.SPECIAL_FOR_ALL_GROUP_IDS));
                    } catch (OXException e) {
                        log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(grp.getId()), e, "");
                    }
                }
            } else {
                CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
                if (null != cacheService) {
                    try {
                        Cache groupCache = cacheService.getCache(GroupService.CACHE_REGION_NAME);
                        groupCache.remove(cacheService.newCacheKey(ctx.getId().intValue(), GroupStorage.SPECIAL_FOR_ALL_GROUP_IDS));
                    } catch (OXException e) {
                        log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(grp.getId()), e, "");
                    }
                }
            }
            // END OF JCS

            return grp;
            // MonitoringInfos.incrementNumberOfCreateGroupCalled();
        } catch (EnforceableDataObjectException e) {
            RemoteException remoteException = RemoteExceptionUtils.convertException(e);
            logAndReturnException(LOGGER, remoteException, e.getExceptionId(), credentials, ctx.getIdAsString(), String.valueOf(grp.getId()));
            throw remoteException;
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx, grp);
            throw e;
        }
    }

    @Override
    public void delete(final Context ctx, final Group grp, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            doNullCheck(grp);
        } catch (InvalidDataException e3) {
            log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for delete is null");
            throw e3;
        }

        try {
            delete(ctx, new Group[] { grp }, auth);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx, grp);
            throw e;
        }
    }

    @Override
    public void delete(final Context ctx, final Group[] grp, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        try {
            final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck((Object[]) grp);
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for delete is null");
                throw e3;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            basicauth.doAuthentication(auth, ctx);

            for (final Group elem : grp) {
                // should we allow of deleting the users group?
                try {
                    setIdOrGetIDFromNameAndIdObject(ctx, elem);
                } catch (NoSuchObjectException e) {
                    throw new NoSuchGroupException(e);
                }
                final int grp_id = elem.getId().intValue();
                if (1 == grp_id) {
                    throw new InvalidDataException("Group with id " + grp_id + " cannot be deleted");
                }
            }

            log(LogLevel.DEBUG, LOGGER, auth, ctx.getIdAsString(), getObjectIds(grp), null, "{} - {} - {}", ctx, Arrays.toString(grp), auth);

            checkContextAndSchema(ctx);

            if (!tool.existsGroup(ctx, grp)) {
                throw new NoSuchGroupException("No such group");
            }

            final ArrayList<OXGroupPluginInterface> interfacelist = new ArrayList<>();

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXGroupPluginInterface oxgroup : pluginInterfaces.getGroupPlugins().getServiceList()) {
                        final String bundlename = oxgroup.getClass().getName();
                        try {
                            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(getObjectIds(grp)), null, "Calling delete for plugin: {}", bundlename);
                            oxgroup.delete(ctx, grp, auth);
                            interfacelist.add(oxgroup);
                        } catch (PluginException e) {
                            log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(getObjectIds(grp)), e, "Error while calling delete for plugin: {}", bundlename);
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }

            // remember the old members for later cache invalidation
            List<User[]> del_groups_members = new ArrayList<>();
            for (Group del_group : grp) {
                del_groups_members.add(oxGroup.getMembers(ctx, del_group.getId().intValue()));
            }

            oxGroup.delete(ctx, grp);

            //JCS
            final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
            if (null != cacheService) {
                try {
                    final Cache cache = cacheService.getCache("User");
                    int contextId = ctx.getId().intValue();
                    for (final User[] membaz : del_groups_members) {
                        for (final User user : membaz) {
                            cache.remove(cacheService.newCacheKey(contextId, user.getId().intValue()));
                            UserConfigurationStorage.getInstance().invalidateCache(user.getId().intValue(), new ContextImpl(ctx.getId().intValue()));
                        }
                    }

                    Cache groupCache = cacheService.getCache(GroupService.CACHE_REGION_NAME);
                    for (final Group elem : grp) {
                        groupCache.remove(cacheService.newCacheKey(contextId, elem.getId().intValue()));
                    }
                    groupCache.remove(cacheService.newCacheKey(contextId, GroupStorage.SPECIAL_FOR_ALL_GROUP_IDS));
                } catch (OXException e) {
                    log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(getObjectIds(grp)), e, "");
                }
            }
            // END OF JCS

        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx.getIdAsString(), getObjectIds(grp));
            throw e;
        }
    }

    @Override
    public Group getData(final Context ctx, final Group grp, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        try {
            final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck(grp);
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for get is null");
                throw e3;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            basicauth.doAuthentication(auth, ctx);

            try {
                setIdOrGetIDFromNameAndIdObject(ctx, grp);
            } catch (NoSuchObjectException e) {
                throw new NoSuchGroupException(e);
            }

            final int grp_id = grp.getId().intValue();

            log(LogLevel.DEBUG, LOGGER, auth, ctx.getIdAsString(), String.valueOf(grp.getId()), null, "{} - {} - {}", ctx, grp, auth);
            checkContextAndSchema(ctx);

            if (!tool.existsGroup(ctx, grp_id)) {
                throw new NoSuchGroupException("No such group");
            }

            Group retgrp = oxGroup.get(ctx, grp);

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXGroupPluginInterface oxgroup : pluginInterfaces.getGroupPlugins().getServiceList()) {
                        final String bundlename = oxgroup.getClass().getName();
                        log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(grp_id), null, "Calling getData for plugin: {}", bundlename);
                        retgrp = oxgroup.get(ctx, retgrp, auth);
                    }
                }
            }

            return retgrp;
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx, grp);
            throw e;
        }
    }

    @Override
    public Group[] getData(final Context ctx, final Group[] groups, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchGroupException, DatabaseUpdateException {
        try {
            final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck((Object[]) groups);
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for getData is null");
                throw e3;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            try {
                basicauth.doAuthentication(auth, ctx);
                log(LogLevel.DEBUG, LOGGER, auth, ctx.getIdAsString(), getObjectIds(groups), null, "{} - {} - {}", ctx, Arrays.toString(groups), auth);
                checkContextAndSchema(ctx);

                // resolve group id/username
                for (final Group group : groups) {
                    // FIXME: cleanup this if constructions for better performance
                    if (group.getId() != null && !tool.existsGroup(ctx, group.getId().intValue())) {
                        throw new NoSuchGroupException("No such group " + group.getId().intValue());
                    }
                    if (group.getName() != null && !tool.existsGroupName(ctx, group.getName())) {
                        throw new NoSuchGroupException("No such group " + group.getName());
                    }
                    if (group.getName() == null && group.getId() == null) {
                        throw new InvalidDataException("Groupname and groupid missing! Cannot resolve group data");
                    }

                    if (group.getName() == null) {
                        // resolve name by id
                        group.setName(tool.getGroupnameByGroupID(ctx, group.getId().intValue()));
                    }
                    if (group.getId() == null) {
                        group.setId(I(tool.getGroupIDByGroupname(ctx, group.getName())));
                    }
                }
            } catch (InvalidDataException e) {
                log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), getObjectIds(groups), e, "");
                throw e;
            }

            List<Group> retval = new ArrayList<>(groups.length);
            for (final Group group : groups) {
                retval.add(oxGroup.get(ctx, group));
            }

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXGroupPluginInterface oxgroup : pluginInterfaces.getGroupPlugins().getServiceList()) {
                        final String bundlename = oxgroup.getClass().getName();
                        log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), getObjectIds(groups), null, "Calling get for plugin: {}", bundlename);
                        for (Group group : retval) {
                            group = oxgroup.get(ctx, group, auth);
                        }
                    }
                }
            }

            return retval.toArray(new Group[retval.size()]);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx.getIdAsString(), getObjectIds(groups));
            throw e;
        }
    }

    @Override
    public Group getDefaultGroup(final Context ctx, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        try {
            final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }
            basicauth.doAuthentication(auth, ctx);
            log(LogLevel.DEBUG, LOGGER, auth, ctx.getIdAsString(), null, "{} - {}", ctx, auth);
            checkContextAndSchema(ctx);

            return new Group(I(tool.getDefaultGroupForContextWithOutConnection(ctx)));
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx, null);
            throw e;
        }
    }

    @Override
    public User[] getMembers(final Context ctx, final Group grp, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        try {
            final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck(grp);
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for getMembers is null");
                throw e3;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            basicauth.doAuthentication(auth, ctx);

            try {
                setIdOrGetIDFromNameAndIdObject(ctx, grp);
            } catch (NoSuchObjectException e) {
                throw new NoSuchGroupException(e);
            }
            final int grp_id = grp.getId().intValue();

            log(LogLevel.DEBUG, LOGGER, auth, ctx.getIdAsString(), String.valueOf(grp.getId()), null, "{} - {} - {}", ctx, grp, auth);

            checkContextAndSchema(ctx);

            if (!tool.existsGroup(ctx, grp_id)) {
                throw new NoSuchGroupException("No such group");
            }

            return oxGroup.getMembers(ctx, grp_id);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx, grp);
            throw e;
        }
    }

    @Override
    public Group[] list(final Context ctx, final String pattern, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        try {
            final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck(pattern);
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for list is null");
                throw e3;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            basicauth.doAuthentication(auth, ctx);

            log(LogLevel.DEBUG, LOGGER, auth, ctx.getIdAsString(), null, "{} - {} - {}", ctx, pattern, auth);

            checkContextAndSchema(ctx);

            return oxGroup.list(ctx, pattern);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx, null);
            throw e;
        }
    }

    @Override
    public Group[] listAll(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException {
        return list(ctx, "*", auth);
    }

    @Override
    public Group[] listGroupsForUser(final Context ctx, final User usr, final Credentials credentials) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        try {
            final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck(usr);
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for getMembers is null");
                throw e3;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            basicauth.doAuthentication(auth, ctx);

            log(LogLevel.DEBUG, LOGGER, auth, ctx.getIdAsString(), String.valueOf(usr.getId()), null, "{} - {} - {}", ctx, usr, auth);

            checkContextAndSchema(ctx);

            try {
                setIdOrGetIDFromNameAndIdObject(ctx, usr);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }
            if (!tool.existsUser(ctx, usr)) {
                throw new NoSuchUserException("No such user");
            }

            return oxGroup.getGroupsForUser(ctx, usr);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx.getIdAsString(), String.valueOf(usr.getId()));
            throw e;
        }
    }

    @Override
    public void removeMember(final Context ctx, final Group grp, final User[] members, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, NoSuchUserException {
        try {
            final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck(grp, members);
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for removeMember is null");
                throw e3;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            basicauth.doAuthentication(auth, ctx);

            try {
                setIdOrGetIDFromNameAndIdObject(ctx, grp);
            } catch (NoSuchObjectException e) {
                throw new NoSuchGroupException(e);
            }
            final int grp_id = grp.getId().intValue();
            log(LogLevel.DEBUG, LOGGER, auth, ctx.getIdAsString(), String.valueOf(grp.getId()), null, "{} - {} - {} - {}", ctx, grp, Arrays.toString(members), auth);

            checkContextAndSchema(ctx);

            try {
                setUserIdInArrayOfUsers(ctx, members);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }
            if (!tool.existsUser(ctx, members)) {
                throw new NoSuchUserException("No such user");
            }

            if (!tool.existsGroup(ctx, grp_id)) {
                throw new NoSuchGroupException("No such group");
            }

            oxGroup.removeMember(ctx, grp_id, members);

            // JCS
            CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
            if (null != cacheService) {
                try {
                    Cache cache = cacheService.getCache("User");
                    int contextId = ctx.getId().intValue();
                    for (User user : members) {
                        cache.remove(cacheService.newCacheKey(contextId, user.getId().intValue()));
                        UserConfigurationStorage.getInstance().invalidateCache(user.getId().intValue(), new ContextImpl(ctx.getId().intValue()));
                    }

                    Cache groupCache = cacheService.getCache(GroupService.CACHE_REGION_NAME);
                    groupCache.remove(cacheService.newCacheKey(contextId, grp.getId().intValue()));
                } catch (OXException e) {
                    log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(grp.getId()), e, "");
                }
            }
            // END OF JCS
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx.getIdAsString(), String.valueOf(grp.getId()));
            throw e;
        }
    }

    private void validateGroupName(final String groupName) throws InvalidDataException {
        // Check for allowed chars:
        // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789
        // _-+.%$@
        if (groupName == null || groupName.trim().length() == 0) {
            throw new InvalidDataException("Invalid group name");
        }
        final String group_check_regexp = prop.getGroupProp("CHECK_GROUP_UID_REGEXP", "[ $@%\\.+a-zA-Z0-9_-]");
        final String illegal = groupName.replaceAll(group_check_regexp, "");
        if (illegal.length() > 0) {
            throw new InvalidDataException("Illegal chars: \"" + illegal + "\"");
        }
    }

}
