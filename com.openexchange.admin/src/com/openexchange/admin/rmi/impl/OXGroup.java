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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.openexchange.admin.daemons.AdminDaemon;
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
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXGroupStorageInterface;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.cache.CacheKey;

/**
 * Implementation for the RMI interface of group
 * 
 * @author d7
 * 
 */
public class OXGroup extends OXCommonImpl implements OXGroupInterface {

    private static final long serialVersionUID = -8949889293005549513L;

    private AdminCache cache = null;

    private final Log log = LogFactory.getLog(this.getClass());

    private PropertyHandler prop = null;

    private BundleContext context = null;

    private final BasicAuthenticator basicauth;

    private final OXGroupStorageInterface oxGroup;

    public OXGroup(final BundleContext context) throws RemoteException,
            StorageException {
        super();
        try {
            oxGroup = OXGroupStorageInterface.getInstance();
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        this.context = context;
        cache = ClientAdminThread.cache;
        prop = cache.getProperties();
        if (log.isInfoEnabled()) {
            log.info("Class loaded: " + this.getClass().getName());
        }
        basicauth = new BasicAuthenticator();
    }

    @Deprecated
    public void addMember(final Context ctx, final Group grp,
            final int[] member_ids, final Credentials auth)
            throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException, NoSuchUserException,
            NoSuchGroupException {
        try {
            doNullCheck(grp, member_ids, grp.getId());
        } catch (final InvalidDataException e3) {
            log.error("One of the arguments for addMember is null", e3);
            throw e3;
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        final int grp_id = grp.getId();

        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + grp_id + " - "
                    + Arrays.toString(member_ids) + " - " + auth.toString());
        }

        checkSchemaBeingLocked(ctx, tool);

        if (!tool.existsGroup(ctx, grp_id)) {
            throw new NoSuchGroupException("No such group");
        }

        if (!tool.existsUser(ctx, member_ids)) {
            throw new NoSuchUserException("No such user");
        }

        try {
            if (tool.existsGroupMember(ctx, grp_id, member_ids)) {
                throw new InvalidDataException("Member already exists in group");
            }
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        final User[] user_objs = getUsersFromIds(member_ids);
        oxGroup.addMember(ctx, grp_id, user_objs);

        // JCS
        try {
            JCS cache = JCS.getInstance("User");
            for (int user_id : member_ids) {
                cache.remove(new CacheKey(ctx.getIdAsInt(), user_id));
            }
        } catch (final CacheException e) {
            log.error(e.getMessage(), e);
        }
        // OXFolderAdminHelper.propagateGroupModification()
        // END OF JCS
    }

    public void addMember(final Context ctx, final Group grp,
            final User[] members, final Credentials auth)
            throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException, NoSuchUserException,
            NoSuchGroupException {
        try {
            doNullCheck(grp, members);
        } catch (final InvalidDataException e3) {
            log.error("One of the arguments for addMember is null", e3);
            throw e3;
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        setIdOrGetIDFromGroupname(ctx, grp);

        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + grp + " - "
                    + Arrays.toString(members) + " - " + auth.toString());
        }

        checkSchemaBeingLocked(ctx, tool);

        if (!tool.existsGroup(ctx, grp)) {
            throw new NoSuchGroupException("No such group");
        }

        setUserIdInArrayOfUsers(ctx, members);
        if (!tool.existsUser(ctx, members)) {
            throw new NoSuchUserException("No such user");
        }

        final int grp_id = grp.getId();
        try {
            if (tool.existsGroupMember(ctx, grp_id, members)) {
                throw new InvalidDataException("Member already exists in group");
            }
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        oxGroup.addMember(ctx, grp_id, members);

        // JCS
        try {
            JCS cache = JCS.getInstance("User");
            for (User user : members) {
                cache.remove(new CacheKey(ctx.getIdAsInt(), user.getId()));
            }
        } catch (final CacheException e) {
            log.error(e.getMessage(), e);
        }
        // END OF JCS
    }

    public void change(final Context ctx, final Group grp,
            final Credentials auth) throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException,
            NoSuchGroupException, NoSuchUserException {
        try {
            doNullCheck(grp);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for change is null", e3);
            throw e3;
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e1) {
            log.error(e1.getMessage(), e1);
            throw e1;
        }

        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + grp.toString() + " - "
                    + auth.toString());
        }

        try {
            checkSchemaBeingLocked(ctx, tool);

            if (!grp.mandatoryChangeMembersSet()) {
                throw new InvalidDataException("Mandatory fields not set: "
                        + grp.getUnsetMembers());
            }

            if (grp.getName() != null
                    && prop.getGroupProp(AdminProperties.Group.AUTO_LOWERCASE,
                            true)) {
                grp.setName(grp.getName().toLowerCase());
            }

            if (grp.getName() != null
                    && prop
                            .getGroupProp(
                                    AdminProperties.Group.CHECK_NOT_ALLOWED_CHARS,
                                    true)) {
                validateGroupName(grp.getName());
            }

            // if members sent, check exist
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

            setIdOrGetIDFromGroupname(ctx, grp);

            if (!tool.existsGroup(ctx, grp.getId())) {
                throw new NoSuchGroupException("No such group");
            }

            oxGroup.change(ctx, grp);
        } catch (final EnforceableDataObjectException e2) {
            final InvalidDataException invalidDataException = new InvalidDataException(
                    e2.getMessage());
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e1) {
            log.error(e1.getMessage(), e1);
            throw e1;
        } catch (final NoSuchUserException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE == bundle.getState()) {
                final ServiceReference[] servicereferences = bundle
                        .getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference
                                .getProperty("name");
                        if (null != property
                                && property.toString().equalsIgnoreCase(
                                        "oxgroup")) {
                            final OXGroupPluginInterface oxgroup = (OXGroupPluginInterface) this.context
                                    .getService(servicereference);
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("Calling change for plugin: "
                                            + bundlename);
                                }
                                oxgroup.change(ctx, grp, auth);
                            } catch (final PluginException e) {
                                log.error(
                                        "Error while calling change for plugin: "
                                                + bundlename, e);
                                throw new StorageException(e);
                            }
                        }
                    }
                }
            }
        }
    }

    public Group create(final Context ctx, final Group grp,
            final Credentials auth) throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        try {
            doNullCheck(grp);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for create is null", e3);
            throw e3;
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e2) {
            log.error(e2.getMessage(), e2);
            throw e2;
        }

        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + grp.toString() + " - "
                    + auth.toString());
        }

        checkSchemaBeingLocked(ctx, tool);

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

            if (tool.existsGroup(ctx, grp.getName())) {
                throw new InvalidDataException("Group already exists!");
            }

            // if members sent, check exist
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
        } catch (final InvalidDataException e2) {
            log.error(e2.getMessage(), e2);
            throw e2;
        } catch (final NoSuchUserException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (EnforceableDataObjectException e) {
            throw new InvalidDataException(e.getMessage());
        }

        final int retval = oxGroup.create(ctx, grp);
        grp.setId(retval);
        final ArrayList<OXGroupPluginInterface> interfacelist = new ArrayList<OXGroupPluginInterface>();

        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE == bundle.getState()) {
                final ServiceReference[] servicereferences = bundle
                        .getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference
                                .getProperty("name");
                        if (null != property
                                && property.toString().equalsIgnoreCase(
                                        "oxgroup")) {
                            final OXGroupPluginInterface oxgroup = (OXGroupPluginInterface) this.context
                                    .getService(servicereference);
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("Calling create for plugin: "
                                            + bundlename);
                                }
                                oxgroup.create(ctx, grp, auth);
                                interfacelist.add(oxgroup);
                            } catch (final PluginException e) {
                                log.error(
                                        "Error while calling create for plugin: "
                                                + bundlename, e);
                                log
                                        .info("Now doing rollback for everything until now...");
                                for (final OXGroupPluginInterface oxgroupinterface : interfacelist) {
                                    try {
                                        oxgroupinterface.delete(ctx,
                                                new Group[] { grp }, auth);
                                    } catch (final PluginException e1) {
                                        log.error(
                                                "Error doing rollback for plugin: "
                                                        + bundlename, e1);
                                    }
                                }
                                try {
                                    oxGroup.delete(ctx, new Group[] { grp });
                                } catch (final StorageException e1) {
                                    log
                                            .error(
                                                    "Error doing rollback for creating resource in database",
                                                    e1);
                                }
                                throw new StorageException(e);
                            }
                        }
                    }
                }
            }
        }

        // JCS
        // If members sent, remove each from cache
        if (grp.getMembers() != null && grp.getMembers().length > 0) {
            final Integer[] mems = grp.getMembers();

            try {
                JCS cache = JCS.getInstance("User");
                for (Integer member_id : mems) {
                    cache.remove(new CacheKey(ctx.getIdAsInt(), member_id));
                }
            } catch (final CacheException e) {
                log.error(e.getMessage(), e);
            }
        }
        // END OF JCS

        return grp;
        // MonitoringInfos.incrementNumberOfCreateGroupCalled();
    }

    public void delete(final Context ctx, final Group grp,
            final Credentials auth) throws RemoteException,
            InvalidCredentialsException, NoSuchContextException,
            StorageException, InvalidDataException, DatabaseUpdateException,
            NoSuchGroupException {
        try {
            doNullCheck(grp);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for delete is null", e3);
            throw e3;
        }

        delete(ctx, new Group[] { grp }, auth);
    }

    public void delete(final Context ctx, final Group[] grp,
            final Credentials auth) throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        try {
            doNullCheck((Object[]) grp);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for delete is null", e3);
            throw e3;
        }

        try {
            basicauth.doAuthentication(auth, ctx);

            for (final Group elem : grp) {
                // should we allow of deleting the users group?
                setIdOrGetIDFromGroupname(ctx, elem);
                final int grp_id = elem.getId();
                if (1 == grp_id) {
                    throw new InvalidDataException("Group with id " + grp_id
                            + " cannot be deleted");
                }
            }

            if (log.isDebugEnabled()) {
                log.debug(ctx.toString() + " - " + Arrays.toString(grp) + " - "
                        + auth.toString());
            }
            checkSchemaBeingLocked(ctx, tool);

            if (!tool.existsGroup(ctx, grp)) {
                throw new NoSuchGroupException("No such group");
            }
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchContextException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        final ArrayList<OXGroupPluginInterface> interfacelist = new ArrayList<OXGroupPluginInterface>();

        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        final ArrayList<Bundle> revbundles = new ArrayList<Bundle>();
        for (int n = bundles.size() - 1; n >= 0; n--) {
            revbundles.add(bundles.get(n));
        }
        for (final Bundle bundle : revbundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE == bundle.getState()) {
                final ServiceReference[] servicereferences = bundle
                        .getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference
                                .getProperty("name");
                        if (null != property
                                && property.toString().equalsIgnoreCase(
                                        "oxgroup")) {
                            final OXGroupPluginInterface oxgroup = (OXGroupPluginInterface) this.context
                                    .getService(servicereference);
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("Calling delete for plugin: "
                                            + bundlename);
                                }
                                oxgroup.delete(ctx, grp, auth);
                                interfacelist.add(oxgroup);
                            } catch (final PluginException e) {
                                log.error(
                                        "Error while calling delete for plugin: "
                                                + bundlename, e);
                                throw new StorageException(e);
                            }
                        }
                    }

                }
            }
        }

        try {
            oxGroup.delete(ctx, grp);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

    }

    @Deprecated
    public Group get(final Context ctx, final Group grp, final Credentials auth)
            throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        return getData(ctx, grp, auth);
    }

    public Group getData(final Context ctx, final Group grp,
            final Credentials auth) throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        try {
            doNullCheck(grp, grp.getId());
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for get is null", e3);
            throw e3;
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        final int grp_id = grp.getId();

        if (log.isDebugEnabled()) {
            log
                    .debug(ctx.toString() + " - " + grp_id + " - "
                            + auth.toString());
        }
        checkSchemaBeingLocked(ctx, tool);

        if (!tool.existsGroup(ctx, grp_id)) {
            throw new NoSuchGroupException("No such group");
        }

        Group retgrp = oxGroup.get(ctx, grp);

        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE == bundle.getState()) {
                final ServiceReference[] servicereferences = bundle
                        .getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference
                                .getProperty("name");
                        if (null != property
                                && property.toString().equalsIgnoreCase(
                                        "oxgroup")) {
                            final OXGroupPluginInterface oxgroupplugin = (OXGroupPluginInterface) this.context
                                    .getService(servicereference);
                            if (log.isDebugEnabled()) {
                                log.debug("Calling getData for plugin: "
                                        + bundlename);
                            }
                            retgrp = oxgroupplugin.get(ctx, retgrp, auth);
                        }
                    }
                }
            }
        }

        return retgrp;
    }

    public Group[] getData(final Context ctx, final Group[] groups,
            final Credentials auth) throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, NoSuchGroupException, DatabaseUpdateException {
        try {
            doNullCheck((Object[]) groups);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for getData is null", e3);
            throw e3;
        }

        try {
            basicauth.doAuthentication(auth, ctx);
            if (log.isDebugEnabled()) {
                log.debug(ctx.toString() + " - " + groups + " - "
                        + auth.toString());
            }
            checkSchemaBeingLocked(ctx, tool);

            // resolv group id/username
            for (final Group group : groups) {
                // FIXME: cleanup this if constructions for better performance
                if (group.getId() != null
                        && !tool.existsGroup(ctx, group.getId().intValue())) {
                    throw new NoSuchGroupException("No such group "
                            + group.getId().intValue());
                }
                if (group.getName() != null
                        && !tool.existsGroup(ctx, group.getName())) {
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
            log.error(e.getMessage(), e);
            throw e;
        }

        final ArrayList<Group> retval = new ArrayList<Group>();

        for (final Group group : groups) {
            retval.add(oxGroup.get(ctx, group));
        }

        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE == bundle.getState()) {
                final ServiceReference[] servicereferences = bundle
                        .getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference
                                .getProperty("name");
                        if (null != property
                                && property.toString().equalsIgnoreCase(
                                        "oxgroup")) {
                            final OXGroupPluginInterface oxgroupplugin = (OXGroupPluginInterface) this.context
                                    .getService(servicereference);
                            if (log.isDebugEnabled()) {
                                log.debug("Calling get for plugin: "
                                        + bundlename);
                            }
                            for (Group group : retval) {
                                group = oxgroupplugin.get(ctx, group, auth);
                            }
                        }
                    }
                }
            }
        }
        return retval.toArray(new Group[retval.size()]);
    }

    public Group getDefaultGroup(final Context ctx, final Credentials auth)
            throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException {
        basicauth.doAuthentication(auth, ctx);
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + auth.toString());
        }
        checkSchemaBeingLocked(ctx, tool);

        try {
            return new Group(tool
                    .getDefaultGroupForContextWithOutConnection(ctx));
        } catch (final StorageException e) {
            log.error("Error resolving default group for context", e);
            throw e;
        }
    }

    @Deprecated
    public Group[] getGroupsForUser(final Context ctx, final User usr,
            final Credentials auth) throws RemoteException,
            InvalidCredentialsException, NoSuchContextException,
            StorageException, InvalidDataException, DatabaseUpdateException,
            NoSuchUserException {
        try {
            doNullCheck(usr, usr.getId());
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for getMembers is null", e3);
            throw e3;
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + usr.getId().intValue() + " - "
                    + auth.toString());
        }

        checkSchemaBeingLocked(ctx, tool);

        if (!tool.existsUser(ctx, usr.getId().intValue())) {
            throw new NoSuchUserException("No such user");
        }

        return oxGroup.getGroupsForUser(ctx, usr);
    }

    public User[] getMembers(final Context ctx, final Group grp,
            final Credentials auth) throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        try {
            doNullCheck(grp);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for getMembers is null", e3);
            throw e3;
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        setIdOrGetIDFromGroupname(ctx, grp);
        final int grp_id = grp.getId();

        if (log.isDebugEnabled()) {
            log
                    .debug(ctx.toString() + " - " + grp_id + " - "
                            + auth.toString());
        }

        checkSchemaBeingLocked(ctx, tool);

        if (!tool.existsGroup(ctx, grp_id)) {
            throw new NoSuchGroupException("No such group");

        }

        return oxGroup.getMembers(ctx, grp_id);
    }

    public Group[] list(final Context ctx, final String pattern,
            final Credentials auth) throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException {
        try {
            doNullCheck(pattern);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for list is null", e3);
            throw e3;
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + pattern + " - "
                    + auth.toString());
        }

        checkSchemaBeingLocked(ctx, tool);

        return oxGroup.list(ctx, pattern);
    }

    public Group[] listAll(final Context ctx, final Credentials auth)
            throws RemoteException, InvalidCredentialsException,
            NoSuchContextException, StorageException, InvalidDataException,
            DatabaseUpdateException {
        return list(ctx, "*", auth);
    }

    public Group[] listGroupsForUser(final Context ctx, final User usr,
            final Credentials auth) throws RemoteException,
            InvalidCredentialsException, NoSuchContextException,
            StorageException, InvalidDataException, DatabaseUpdateException,
            NoSuchUserException {
        try {
            doNullCheck(usr);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for getMembers is null", e3);
            throw e3;
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + usr + " - " + auth.toString());
        }

        checkSchemaBeingLocked(ctx, tool);

        setIdOrGetIDFromUsername(ctx, usr);
        if (!tool.existsUser(ctx, usr)) {
            throw new NoSuchUserException("No such user");
        }

        return oxGroup.getGroupsForUser(ctx, usr);
    }

    @Deprecated
    public void removeMember(final Context ctx, final Group grp,
            final int[] member_ids, final Credentials auth)
            throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException,
            NoSuchGroupException, NoSuchUserException {
        try {
            doNullCheck(grp, member_ids, grp.getId());
        } catch (final InvalidDataException e3) {
            log
                    .error(
                            "One of the given arguments for removeMember is null",
                            e3);
            throw e3;
        }

        try {
            basicauth.doAuthentication(auth, ctx);

        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        final int grp_id = grp.getId();
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + grp_id + " - "
                    + Arrays.toString(member_ids) + " - " + auth.toString());
        }

        checkSchemaBeingLocked(ctx, tool);

        if (!tool.existsUser(ctx, member_ids)) {
            throw new NoSuchUserException("No such user");
        }

        if (!tool.existsGroup(ctx, grp_id)) {
            throw new NoSuchGroupException("No such group");
        }

        final User[] user_objs = getUsersFromIds(member_ids);
        oxGroup.removeMember(ctx, grp_id, user_objs);
    }

    public void removeMember(final Context ctx, final Group grp,
            final User[] members, final Credentials auth)
            throws RemoteException, StorageException,
            InvalidCredentialsException, NoSuchContextException,
            InvalidDataException, DatabaseUpdateException,
            NoSuchGroupException, NoSuchUserException {
        try {
            doNullCheck(grp, members);
        } catch (final InvalidDataException e3) {
            log
                    .error(
                            "One of the given arguments for removeMember is null",
                            e3);
            throw e3;
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        setIdOrGetIDFromGroupname(ctx, grp);
        final int grp_id = grp.getId();
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + grp_id + " - "
                    + Arrays.toString(members) + " - " + auth.toString());
        }

        checkSchemaBeingLocked(ctx, tool);

        setUserIdInArrayOfUsers(ctx, members);
        if (!tool.existsUser(ctx, members)) {
            throw new NoSuchUserException("No such user");
        }

        if (!tool.existsGroup(ctx, grp_id)) {
            throw new NoSuchGroupException("No such group");
        }

        oxGroup.removeMember(ctx, grp_id, members);
    }

    /**
     * @param ctx
     * @param tools
     * @throws StorageException
     * @throws DatabaseUpdateException
     * @throws NoSuchContextException
     */
    private void checkSchemaBeingLocked(final Context ctx,
            final OXToolStorageInterface tools) throws StorageException,
            DatabaseUpdateException, NoSuchContextException {

        if (tools.schemaBeingLockedOrNeedsUpdate(ctx)) {
            final DatabaseUpdateException databaseUpdateException = new DatabaseUpdateException(
                    "Database must be updated or currently is beeing updated");
            log.error(databaseUpdateException.getMessage(),
                    databaseUpdateException);
            throw databaseUpdateException;
        }
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

    private void setIdOrGetIDFromGroupname(final Context ctx, final Group grp)
            throws StorageException, InvalidDataException {
        final Integer id = grp.getId();
        if (null == id) {
            final String groupname = grp.getName();
            if (null != groupname) {
                grp.setId(tool.getGroupIDByGroupname(ctx, groupname));
            } else {
                throw new InvalidDataException(
                        "One group object has no id or groupname");
            }
        }
    }
}