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

import static com.openexchange.java.Autoboxing.I2i;
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
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.services.PluginInterfaces;
import com.openexchange.admin.storage.interfaces.OXGroupStorageInterface;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;

/**
 * Implementation for the RMI interface of group
 *
 * @author d7
 *
 */
public class OXGroup extends OXCommonImpl implements OXGroupInterface {

	private static final long serialVersionUID = -8949889293005549513L;

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
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        }
        cache = ClientAdminThread.cache;
        prop = cache.getProperties();
        basicauth = new BasicAuthenticator();
        LOGGER.info("Class loaded: {}", this.getClass().getName());
    }

    @Override
    public void addMember(final Context ctx, final Group grp, final User[] members, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, NoSuchGroupException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(grp, members);
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the arguments for addMember is null", e3);
            throw e3;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        }

        try {
            setIdOrGetIDFromNameAndIdObject(ctx, grp);
        } catch (NoSuchObjectException e) {
            throw new NoSuchGroupException(e);
        }

            LOGGER.debug("{} - {} - {} - {}", ctx, grp, Arrays.toString(members), auth);

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
            int userId = user.getId();
            if (tool.isGuestUser(ctx, userId)) {
                throw new NoSuchUserException("Cannot add guest user to group");
            }
        }

        final int grp_id = grp.getId();
        try {
            if (tool.existsGroupMember(ctx, grp_id, members)) {
                throw new InvalidDataException("Member already exists in group");
            }
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        }

        oxGroup.addMember(ctx, grp_id, members);

        // JCS
        final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
		if (null != cacheService) {
	        try {
	        	final Cache cache = cacheService.getCache("User");
	        	final int contextId = ctx.getId().intValue();
	        	for (final User user : members) {
                    cache.remove(cacheService.newCacheKey(contextId, user.getId()));
	            }
	        } catch (final OXException e) {
                LOGGER.error("", e);
            }
        }
        // END OF JCS
    }

    @Override
    public void change(final Context ctx, final Group grp, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, NoSuchUserException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(grp);
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for change is null", e3);
            throw e3;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e1) {
            LOGGER.error("", e1);
            throw e1;
        }

            LOGGER.debug("{} - {} - {}", ctx, grp, auth);

        try {
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
                    tmp_mems[i] = mems[i];
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
            if (!tool.existsGroup(ctx, grp.getId())) {
                throw new NoSuchGroupException("No such group");
            }

            if (grp.getName() != null && tool.existsGroupName(ctx, grp)) {
                throw new InvalidDataException("Group " + grp.getName() + " already exists in this context");
            }

            oxGroup.change(ctx, grp);

            //JCS
            final User[] new_members = oxGroup.getMembers(ctx, grp.getId());
            final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
    		if (null != cacheService) {
    	        try {
    	        	final Cache cache = cacheService.getCache("User");
    	        	if(new_members!=null){
                        for (final User user : new_members) {
                            cache.remove(cacheService.newCacheKey(ctx.getId().intValue(), user.getId()));
                        }
                    }

                    if (grp.getMembers() != null && grp.getMembers().length > 0) {
                        for (final Integer old_user_id : grp.getMembers()) {
                            cache.remove(cacheService.newCacheKey(ctx.getId().intValue(), old_user_id));
                        }
                    }
    	        } catch (final OXException e) {
    	            LOGGER.error("", e);
    	        }
            }


        } catch (final EnforceableDataObjectException e2) {
            final InvalidDataException invalidDataException = new InvalidDataException(e2.getMessage());
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e1) {
            LOGGER.error("", e1);
            throw e1;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchGroupException e) {
            LOGGER.error("", e);
            throw e;
        }

        // Trigger plugin extensions
        {
            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (final OXGroupPluginInterface oxgroup : pluginInterfaces.getGroupPlugins().getServiceList()) {
                    final String bundlename = oxgroup.getClass().getName();
                    try {
                        LOGGER.debug("Calling change for plugin: {}", bundlename);
                        oxgroup.change(ctx, grp, auth);
                    } catch (final PluginException e) {
                        LOGGER.error("Error while calling change for plugin: {}", bundlename, e);
                        throw StorageException.wrapForRMI(e);
                    }
                }
            }
        }
    }

    @Override
    public Group create(final Context ctx, final Group grp, final Credentials credentials) throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(grp);
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for create is null", e3);
            throw e3;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e2) {
            LOGGER.error("", e2);
            throw e2;
        }

        LOGGER.debug("{} - {} - {}", ctx, grp, auth);

        checkContextAndSchema(ctx);

        try {
            if (!grp.mandatoryCreateMembersSet()) {
                throw new InvalidDataException("Mandatory fields not set: "
                        + grp.getUnsetMembers());
            }

            if (prop.getGroupProp(AdminProperties.Group.AUTO_LOWERCASE, true)) {
                grp.setName(grp.getName().toLowerCase());
            }

            if (prop.getGroupProp(
                    AdminProperties.Group.CHECK_NOT_ALLOWED_CHARS, true)) {
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
                for (int userId : grp.getMembers()) {
                    if (tool.isGuestUser(ctx, userId)) {
                        throw new NoSuchUserException("Cannot add guest user to group");
                    }
                }
            }
        } catch (final InvalidDataException e2) {
            LOGGER.error("", e2);
            throw e2;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        } catch (EnforceableDataObjectException e) {
            throw new InvalidDataException(e.getMessage());
        }

        final int retval = oxGroup.create(ctx, grp);
        grp.setId(retval);
        final ArrayList<OXGroupPluginInterface> interfacelist = new ArrayList<OXGroupPluginInterface>();

        // Trigger plugin extensions
        {
            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (final OXGroupPluginInterface oxgroup : pluginInterfaces.getGroupPlugins().getServiceList()) {
                    final String bundlename = oxgroup.getClass().getName();
                    try {
                        LOGGER.debug("Calling create for plugin: {}", bundlename);
                        oxgroup.create(ctx, grp, auth);
                        interfacelist.add(oxgroup);
                    } catch (final PluginException e) {
                        LOGGER.error("Error while calling create for plugin: {}", bundlename, e);
                        LOGGER.info("Now doing rollback for everything until now...");
                        for (final OXGroupPluginInterface oxgroupinterface : interfacelist) {
                            try {
                                oxgroupinterface.delete(ctx, new Group[] { grp }, auth);
                            } catch (final PluginException e1) {
                                LOGGER.error("Error doing rollback for plugin: {}", bundlename, e1);
                            }
                        }
                        try {
                            oxGroup.delete(ctx, new Group[] { grp });
                        } catch (final StorageException e1) {
                            LOGGER.error("Error doing rollback for creating resource in database", e1);
                        }
                        throw StorageException.wrapForRMI(e);
                    }
                }
            }
        }

        // JCS
        // If members sent, remove each from cache
        if (grp.getMembers() != null && grp.getMembers().length > 0) {
            final Integer[] mems = grp.getMembers();
            final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);;
    		if (null != cacheService) {
    	        try {
    	        	final Cache cache = cacheService.getCache("User");
    	        	for (final Integer member_id : mems) {
                        cache.remove(cacheService.newCacheKey(ctx.getId().intValue(), member_id));
                    }
    	        } catch (final OXException e) {
    	            LOGGER.error("", e);
    	        }
            }
        }
        // END OF JCS

        return grp;
        // MonitoringInfos.incrementNumberOfCreateGroupCalled();
    }

    @Override
    public void delete(final Context ctx, final Group grp,
            final Credentials credentials) throws RemoteException,
            InvalidCredentialsException, NoSuchContextException,
            StorageException, InvalidDataException, DatabaseUpdateException,
            NoSuchGroupException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(grp);
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for delete is null", e3);
            throw e3;
        }

        delete(ctx, new Group[] { grp }, auth);
    }

    @Override
    public void delete(final Context ctx, final Group[] grp,
            final Credentials credentials) throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck((Object[]) grp);
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for delete is null", e3);
            throw e3;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        try {
            basicauth.doAuthentication(auth, ctx);

            for (final Group elem : grp) {
                // should we allow of deleting the users group?
                try {
                    setIdOrGetIDFromNameAndIdObject(ctx, elem);
                } catch (NoSuchObjectException e) {
                    throw new NoSuchGroupException(e);
                }
                final int grp_id = elem.getId();
                if (1 == grp_id) {
                    throw new InvalidDataException("Group with id " + grp_id
                            + " cannot be deleted");
                }
            }

            LOGGER.debug("{} - {} - {}", ctx, Arrays.toString(grp), auth);

            checkContextAndSchema(ctx);

            if (!tool.existsGroup(ctx, grp)) {
                throw new NoSuchGroupException("No such group");
            }
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchGroupException e) {
            LOGGER.error("", e);
            throw e;
        }

        final ArrayList<OXGroupPluginInterface> interfacelist = new ArrayList<OXGroupPluginInterface>();

        // Trigger plugin extensions
        {
            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (final OXGroupPluginInterface oxgroup : pluginInterfaces.getGroupPlugins().getServiceList()) {
                    final String bundlename = oxgroup.getClass().getName();
                    try {
                        LOGGER.debug("Calling delete for plugin: {}", bundlename);
                        oxgroup.delete(ctx, grp, auth);
                        interfacelist.add(oxgroup);
                    } catch (final PluginException e) {
                        LOGGER.error("Error while calling delete for plugin: {}", bundlename, e);
                        throw StorageException.wrapForRMI(e);
                    }
                }
            }
        }

        try {

            // remember the old members for later cache invalidation
            List<User[]> del_groups_members = new ArrayList<User[]>();
            for (Group del_group : grp) {
                del_groups_members.add(oxGroup.getMembers(ctx, del_group.getId()));
            }

            oxGroup.delete(ctx, grp);

            //JCS
            final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);;
    		if (null != cacheService) {
    	        try {
    	        	final Cache cache = cacheService.getCache("User");
    	        	for(final User[] membaz : del_groups_members){
                        for (final User user : membaz) {
                            cache.remove(cacheService.newCacheKey(ctx.getId().intValue(), user.getId()));
                        }
                    }
    	        } catch (final OXException e) {
    	            LOGGER.error("", e);
    	        }
            }
            // END OF JCS

        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        }

    }

    @Override
    public Group getData(final Context ctx, final Group grp,
            final Credentials credentials) throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(grp);
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for get is null", e3);
            throw e3;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        }

        try {
            setIdOrGetIDFromNameAndIdObject(ctx, grp);
        } catch (NoSuchObjectException e) {
            throw new NoSuchGroupException(e);
        }

        final int grp_id = grp.getId().intValue();

        LOGGER.debug("{} - {} - {}", ctx, grp_id, auth);
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
                    LOGGER.debug("Calling getData for plugin: {}", bundlename);
                    retgrp = oxgroup.get(ctx, retgrp, auth);
                }
            }
        }

        return retgrp;
    }

    @Override
    public Group[] getData(final Context ctx, final Group[] groups,
            final Credentials credentials) throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, NoSuchGroupException, DatabaseUpdateException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck((Object[]) groups);
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for getData is null", e3);
            throw e3;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        try {
            basicauth.doAuthentication(auth, ctx);
            LOGGER.debug("{} - {} - {}", ctx, Arrays.toString(groups), auth);
            checkContextAndSchema(ctx);

            // resolv group id/username
            for (final Group group : groups) {
                // FIXME: cleanup this if constructions for better performance
                if (group.getId() != null
                        && !tool.existsGroup(ctx, group.getId().intValue())) {
                    throw new NoSuchGroupException("No such group "
                            + group.getId().intValue());
                }
                if (group.getName() != null
                        && !tool.existsGroupName(ctx, group.getName())) {
                    throw new NoSuchGroupException("No such group "
                            + group.getName());
                }
                if (group.getName() == null && group.getId() == null) {
                    throw new InvalidDataException(
                            "Groupname and groupid missing! Cannot resolve group data");
                } else {
                    if (group.getName() == null) {
                        // resolv name by id
                        group.setName(tool.getGroupnameByGroupID(ctx, group
                                .getId().intValue()));
                    }
                    if (group.getId() == null) {
                        group.setId(tool.getGroupIDByGroupname(ctx, group
                                .getName()));
                    }
                }
            }
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        }

        final ArrayList<Group> retval = new ArrayList<Group>();

        for (final Group group : groups) {
            retval.add(oxGroup.get(ctx, group));
        }

        // Trigger plugin extensions
        {
            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (final OXGroupPluginInterface oxgroup : pluginInterfaces.getGroupPlugins().getServiceList()) {
                    final String bundlename = oxgroup.getClass().getName();
                    LOGGER.debug("Calling get for plugin: {}", bundlename);
                    for (Group group : retval) {
                        group = oxgroup.get(ctx, group, auth);
                    }
                }
            }
        }

        return retval.toArray(new Group[retval.size()]);
    }

    @Override
    public Group getDefaultGroup(final Context ctx, final Credentials credentials)
            throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }
        basicauth.doAuthentication(auth, ctx);
        LOGGER.debug("{} - {}", ctx, auth);
        checkContextAndSchema(ctx);

        try {
            return new Group(tool
                    .getDefaultGroupForContextWithOutConnection(ctx));
        } catch (final StorageException e) {
            LOGGER.error("Error resolving default group for context", e);
            throw e;
        }
    }

    @Override
    public User[] getMembers(final Context ctx, final Group grp,
            final Credentials credentials) throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(grp);
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for getMembers is null", e3);
            throw e3;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        }

        try {
            setIdOrGetIDFromNameAndIdObject(ctx, grp);
        } catch (NoSuchObjectException e) {
            throw new NoSuchGroupException(e);
        }
        final int grp_id = grp.getId().intValue();

        LOGGER.debug("{} - {} - {}", ctx, grp_id, auth);

        checkContextAndSchema(ctx);

        if (!tool.existsGroup(ctx, grp_id)) {
            throw new NoSuchGroupException("No such group");

        }

        return oxGroup.getMembers(ctx, grp_id);
    }

    @Override
    public Group[] list(final Context ctx, final String pattern,
            final Credentials credentials) throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(pattern);
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for list is null", e3);
            throw e3;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        }

        LOGGER.debug("{} - {} - {}", ctx, pattern, auth);

        checkContextAndSchema(ctx);

        return oxGroup.list(ctx, pattern);
    }

    @Override
    public Group[] listAll(final Context ctx, final Credentials auth)
            throws RemoteException, InvalidCredentialsException,
            NoSuchContextException, StorageException, InvalidDataException,
            DatabaseUpdateException {
        return list(ctx, "*", auth);
    }

    @Override
    public Group[] listGroupsForUser(final Context ctx, final User usr,
            final Credentials credentials) throws RemoteException,
            InvalidCredentialsException, NoSuchContextException,
            StorageException, InvalidDataException, DatabaseUpdateException,
            NoSuchUserException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(usr);
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for getMembers is null", e3);
            throw e3;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        }

        LOGGER.debug("{} - {} - {}", ctx, usr, auth);

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
    }

    @Override
    public void removeMember(final Context ctx, final Group grp, final User[] members, final Credentials credentials)
            throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException,
            NoSuchGroupException, NoSuchUserException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(grp, members);
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for removeMember is null", e3);
            throw e3;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        }

        try {
            setIdOrGetIDFromNameAndIdObject(ctx, grp);
        } catch (NoSuchObjectException e) {
            throw new NoSuchGroupException(e);
        }
        final int grp_id = grp.getId().intValue();
        LOGGER.debug("{} - {} - {} - {}", ctx, grp_id, Arrays.toString(members), auth);

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
        final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);;
		if (null != cacheService) {
	        try {
	        	final Cache cache = cacheService.getCache("User");
	        	for (final User user : members) {
	                cache.remove(cacheService.newCacheKey(ctx.getId().intValue(), user.getId()));
	            }
	        } catch (final OXException e) {
	            LOGGER.error("", e);
	        }
        }
        // END OF JCS

    }

    private User[] getUsersFromIds(final int[] member_ids) {
        final User[] user_objs = new User[member_ids.length];
        for (int i = 0; i < member_ids.length; i++) {
            user_objs[i] = new User(member_ids[i]);
        }
        return user_objs;
    }

    private void validateGroupName(final String groupName)
            throws InvalidDataException {
        // Check for allowed chars:
        // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789
        // _-+.%$@
        if (groupName == null || groupName.trim().length() == 0) {
            throw new InvalidDataException("Invalid group name");
        }
        final String group_check_regexp = prop.getGroupProp(
                "CHECK_GROUP_UID_REGEXP", "[ $@%\\.+a-zA-Z0-9_-]");
        final String illegal = groupName.replaceAll(group_check_regexp, "");
        if (illegal.length() > 0) {
            throw new InvalidDataException("Illegal chars: \"" + illegal + "\"");
        }
    }

}
