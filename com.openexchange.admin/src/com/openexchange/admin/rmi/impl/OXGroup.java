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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.plugins.OXGroupPluginInterface;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.BasicAuthenticator;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
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

public class OXGroup extends BasicAuthenticator implements OXGroupInterface {

    private static final long serialVersionUID = -8949889293005549513L;

    private AdminCache cache = null;

    private final Log log = LogFactory.getLog(this.getClass());

    private PropertyHandler prop = null;

    private BundleContext context = null;

    public OXGroup(final BundleContext context) throws RemoteException {
        super();
        this.context = context;
        cache = ClientAdminThread.cache;
        prop = cache.getProperties();
        if (log.isInfoEnabled()) {
            log.info("Class loaded: " + this.getClass().getName());
        }
    }

    private void checkContext(final Context ctx) throws InvalidDataException {
        if (null == ctx.getIdAsInt()) {
            throw new InvalidDataException("Context invalid");
        }
    }

    public int create(final Context ctx, final Group grp, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        
        try {
            doNullCheck(ctx,grp,auth);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for create is null", e3);
            throw e3;
        }        

        try {
            checkContext(ctx);
            
            doAuthentication(auth, ctx);
        } catch (InvalidDataException e2) {
            log.error(e2);
            throw e2;
        }        

        if (log.isDebugEnabled()) {
            log.debug("" + ctx.toString() + " - " + grp.toString() + " - " + auth.toString());
        }

        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();

        if (tool.schemaBeingLockedOrNeedsUpdate(ctx)) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }
        try {
            if (!grp.attributesforcreateset()) {
                throw new InvalidDataException("Mandatory fields not set");
            }

            if (prop.getGroupProp(AdminProperties.Group.AUTO_LOWERCASE, true)) {
                grp.setName(grp.getName().toLowerCase());
            }

            if (prop.getGroupProp(AdminProperties.Group.CHECK_NOT_ALLOWED_CHARS, true)) {
                validateGroupName(grp.getName());
            }

            if (tool.existsGroup(ctx, grp.getName())) {
                throw new InvalidDataException("Group already exists!");
            }
            
            // if members sent, check exist
            if(grp.getMembers()!=null && grp.getMembers().length>0){
                Integer[] mems = grp.getMembers();
                int[] tmp_mems = new int[mems.length];
                for (int i = 0; i < mems.length; i++) {
                    tmp_mems[i] = mems[i];
                } 
                if (!tool.existsUser(ctx, tmp_mems)) {
                    throw new NoSuchUserException("No such user");
                }
            }
        } catch (InvalidDataException e2) {
            log.error(e2);
            throw e2;
        } catch (NoSuchUserException e) {
            log.error(e);
            throw e;
        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        final int retval = oxGroup.create(ctx, grp);
        grp.setId(retval);
        final ArrayList<OXGroupPluginInterface> interfacelist = new ArrayList<OXGroupPluginInterface>();

        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE == bundle.getState()) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase("oxgroup")) {
                            final OXGroupPluginInterface oxgroup = (OXGroupPluginInterface) this.context.getService(servicereference);
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("Calling create for plugin: " + bundlename);
                                }
                                oxgroup.create(ctx, grp, auth);
                                interfacelist.add(oxgroup);
                            } catch (final PluginException e) {
                                log.error("Error while calling create for plugin: " + bundlename, e);
                                log.error("Now doing rollback for everything until now...");
                                for (final OXGroupPluginInterface oxgroupinterface : interfacelist) {
                                    try {
                                        oxgroupinterface.delete(ctx, new Group[] { grp }, auth);
                                    } catch (final PluginException e1) {
                                        log.error("Error doing rollback for plugin: " + bundlename, e1);
                                    }
                                }
                                try {
                                    oxGroup.delete(ctx, new Group[] { grp });
                                } catch (final StorageException e1) {
                                    log.error("Error doing rollback for creating resource in database", e1);
                                }
                                throw new StorageException(e);
                            }
                        }
                    }

                }
            }
        }

        return retval;
        // MonitoringInfos.incrementNumberOfCreateGroupCalled();
    }

    public Group[] list(final Context ctx, final String pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        
        try {
            doNullCheck(ctx,pattern,auth);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for list is null", e3);
            throw e3;
        }        
        
        try {
            checkContext(ctx);
            
            doAuthentication(auth, ctx);
        } catch (InvalidDataException e) {
            log.error(e);
            throw e;
        }        
        
        if (log.isDebugEnabled()) {
            log.debug("" + ctx.toString() + " - " + pattern + " - " + auth.toString());
        }        

        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();

        if (tool.schemaBeingLockedOrNeedsUpdate(ctx)) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        return oxGroup.list(ctx, pattern);

    }

    public Group get(final Context ctx, final Group grp, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        
        try {
            doNullCheck(ctx,grp,auth);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for get is null", e3);
            throw e3;
        }        

        try {
            checkContext(ctx);

            if(grp.getId()==null){
                throw new InvalidDataException();
            }
            
            doAuthentication(auth, ctx);
        } catch (InvalidDataException e) {
            log.error(e);
            throw e;
        }


        final int grp_id = grp.getId();

        
        if (log.isDebugEnabled()) {
            log.debug("" + ctx.toString() + " - " + grp_id + " - " + auth.toString());
        }
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();

        if (tool.schemaBeingLockedOrNeedsUpdate(ctx)) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        if (!tool.existsGroup(ctx, grp_id)) {
            throw new NoSuchGroupException("No such group");

        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        Group retgrp = oxGroup.get(ctx, grp);

        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE == bundle.getState()) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase("oxgroup")) {
                            final OXGroupPluginInterface oxgroupplugin = (OXGroupPluginInterface) this.context.getService(servicereference);
                            if (log.isDebugEnabled()) {
                                log.debug("Calling getData for plugin: " + bundlename);
                            }
                            retgrp = oxgroupplugin.get(ctx, retgrp, auth);
                        }
                    }
                }
            }
        }

        return retgrp;
    }

    public void change(final Context ctx, final Group grp, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, NoSuchUserException {
        
        try {
            doNullCheck(ctx,grp,auth);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for change is null", e3);
            throw e3;
        }        
        
        try {
            checkContext(ctx);
            
            if(grp.getId()==null){
                throw new InvalidDataException();
            }

            doAuthentication(auth, ctx);
        } catch (InvalidDataException e1) {
            log.error(e1);
            throw e1;
        }
        
        if (log.isDebugEnabled()) {
            log.debug("" + ctx.toString() + " - " + grp.toString() + " - " + auth.toString());
        }
        
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();

        if (tool.schemaBeingLockedOrNeedsUpdate(ctx)) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        if (!grp.attributesforchangeset()) {
            throw new InvalidDataException("Mandatory fields not set");
        }

        if (grp.getName() != null && prop.getGroupProp(AdminProperties.Group.AUTO_LOWERCASE, true)) {
            grp.setName(grp.getName().toLowerCase());
        }

        try {
            if (grp.getName() != null && prop.getGroupProp(AdminProperties.Group.CHECK_NOT_ALLOWED_CHARS, true)) {
                validateGroupName(grp.getName());
            }
            
//          if members sent, check exist
            if(grp.getMembers()!=null && grp.getMembers().length>0){
                Integer[] mems = grp.getMembers();
                int[] tmp_mems = new int[mems.length];
                for (int i = 0; i < mems.length; i++) {
                    tmp_mems[i] = mems[i];
                } 
                if (!tool.existsUser(ctx, tmp_mems)) {
                    throw new NoSuchUserException("No such user");
                }
            }
            
        } catch (InvalidDataException e1) {
            log.error(e1);
            throw e1;
        } catch (NoSuchUserException e) {
            log.error(e);
            throw e;
        }

        if (!tool.existsGroup(ctx, grp.getId())) {
            throw new NoSuchGroupException("No such group");
        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        oxGroup.change(ctx, grp);

        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE == bundle.getState()) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase("oxgroup")) {
                            final OXGroupPluginInterface oxgroup = (OXGroupPluginInterface) this.context.getService(servicereference);
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("Calling change for plugin: " + bundlename);
                                }
                                oxgroup.change(ctx, grp, auth);
                            } catch (final PluginException e) {
                                log.error("Error while calling change for plugin: " + bundlename, e);
                                throw new StorageException(e);
                            }
                        }
                    }

                }
            }
        }

    }

    public void addMember(final Context ctx, final Group grp, final int[] member_ids, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, NoSuchGroupException {
        
        try {
            doNullCheck(ctx,grp,member_ids,auth);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for addMember is null", e3);
            throw e3;
        }        

        try {
            if(grp.getId()==null){
                throw new InvalidDataException();
            }

            checkContext(ctx);
            
            doAuthentication(auth, ctx);       

        } catch (InvalidDataException e) {
            log.error(e);
            throw e;
        }
        
        int grp_id = grp.getId();

        if (log.isDebugEnabled()) {
            log.debug("" + ctx.toString() + " - " + grp_id + " - " + Arrays.toString(member_ids) + " - " + auth.toString());
        }

        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();

        if (tool.schemaBeingLockedOrNeedsUpdate(ctx)) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        if (!tool.existsGroup(ctx, grp_id)) {
            throw new NoSuchGroupException("No such group");
        }

        if (!tool.existsUser(ctx, member_ids)) {
            throw new NoSuchUserException("No such user");
        }

        try {
            if (tool.getDefaultGroupForContextWithOutConnection(ctx) != grp_id) {
                if (tool.existsGroupMember(ctx, grp_id, member_ids)) {
                    throw new InvalidDataException("Member already exists in group");
                }
            }
        } catch (final InvalidDataException e) {
            log.error(e);
            throw e;
        }
        
        // TODO: This is a workaround for the problem that a new created user is per default in the default group
        // If you afterwards try to add the member to the default group (what the GUI and the servlet do ATM) you
        // get an error here. This is fixed. But it should be handled in the servlet or the GUI somehow. Also
        // see #7816
        if (tool.getDefaultGroupForContextWithOutConnection(ctx) != grp_id) {
            final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
            oxGroup.addMember(ctx, grp_id, member_ids);
        }

    }

    public void removeMember(final Context ctx, final Group grp, final int[] member_ids, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, NoSuchUserException {

        try {
            doNullCheck(ctx,grp,member_ids,auth);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for removeMember is null", e3);
            throw e3;
        }        

        try {
            if(grp.getId()==null){
                throw new InvalidDataException();
            }

            checkContext(ctx);
            
            doAuthentication(auth, ctx);       

        } catch (InvalidDataException e) {
            log.error(e);
            throw e;
        }

        final int grp_id = grp.getId();
        if (log.isDebugEnabled()) {
            log.debug("" + ctx.toString() + " - " + grp_id + " - " + Arrays.toString(member_ids) + " - " + auth.toString());
        }

        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();

        if (tool.schemaBeingLockedOrNeedsUpdate(ctx)) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        if (!tool.existsUser(ctx, member_ids)) {
            throw new NoSuchUserException("No such user");
        }

        if (!tool.existsGroup(ctx, grp_id)) {
            throw new NoSuchGroupException("No such group");
        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        oxGroup.removeMember(ctx, grp_id, member_ids);

    }

    public void delete(final Context ctx, final Group grp, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        
        try {
            doNullCheck(ctx,grp,auth);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for delete is null", e3);
            throw e3;
        }        
        
        delete(ctx, new Group[] { grp }, auth);
    }

    public void delete(final Context ctx, final Group[] grp, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        
        try {
            doNullCheck(ctx,grp,auth);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for delete is null", e3);
            throw e3;
        }        
        
        int[] grp_ids;
        try {
            checkContext(ctx);

            doAuthentication(auth, ctx);

            grp_ids = new int[grp.length];
            int i = 0;
            for (final Group elem : grp) {
                // should we allow of deleting the users group?
                if(elem.getId()==null){
                    throw new InvalidDataException();
                }
                final int grp_id = elem.getId();
                if (1 == grp_id) {
                    throw new InvalidDataException("Group with id " + grp_id + " cannot be deleted");
                }
                grp_ids[i++] = grp_id;
            }
        } catch (InvalidDataException e1) {
            log.error(e1);
            throw e1;
        }


        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + Arrays.toString(grp) + " - " + auth.toString());
        }
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();

        if (tool.schemaBeingLockedOrNeedsUpdate(ctx)) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        if (!tool.existsGroup(ctx, grp_ids)) {
            throw new NoSuchGroupException("No such group");
        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();

        final ArrayList<OXGroupPluginInterface> interfacelist = new ArrayList<OXGroupPluginInterface>();

        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        final ArrayList<Bundle> revbundles = new ArrayList<Bundle>();
        for (int n = bundles.size() - 1; n >= 0; n--) {
            revbundles.add(bundles.get(n));
        }
        for (final Bundle bundle : revbundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE == bundle.getState()) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase("oxgroup")) {
                            final OXGroupPluginInterface oxgroup = (OXGroupPluginInterface) this.context.getService(servicereference);
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("Calling delete for plugin: " + bundlename);
                                }
                                oxgroup.delete(ctx, grp, auth);
                                interfacelist.add(oxgroup);
                            } catch (final PluginException e) {
                                log.error("Error while calling delete for plugin: " + bundlename, e);
                                throw new StorageException(e);
                            }
                        }
                    }

                }
            }
        }

        oxGroup.delete(ctx, grp);
    }

    public int[] getMembers(final Context ctx, final Group grp, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        
        try {
            doNullCheck(ctx,grp,auth);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for getMembers is null", e3);
            throw e3;
        }        

        try {
            if(grp.getId()==null){
                throw new InvalidDataException();
            }

            checkContext(ctx);
            
            doAuthentication(auth, ctx);       

        } catch (InvalidDataException e) {
            log.error(e);
            throw e;
        }

        final int grp_id = grp.getId();

        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + grp_id + " - " + auth.toString());
        }

        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();

        if (tool.schemaBeingLockedOrNeedsUpdate(ctx)) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        if (!tool.existsGroup(ctx, grp_id)) {
            throw new NoSuchGroupException("No such group");

        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        return oxGroup.getMembers(ctx, grp_id);

    }

    private void validateGroupName(final String groupName) throws InvalidDataException {
        // Check for allowed chars:
        // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789
        // _-+.%$@
        if(groupName==null || groupName.trim().length()==0){
            throw new InvalidDataException("Invalid group name");
        }
        String group_check_regexp = prop.getGroupProp("CHECK_GROUP_UID_REGEXP", "[ $@%\\.+a-zA-Z0-9_-]");
        final String illegal = groupName.replaceAll(group_check_regexp, "");
        if (illegal.length() > 0) {
            throw new InvalidDataException("Illegal chars: \"" + illegal + "\"");
        }
    }

    public Group[] getGroupsForUser(Context ctx, User usr, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        
        try {
            doNullCheck(ctx,usr,auth);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for getMembers is null", e3);
            throw e3;
        }        

        try {
            if(usr.getId()==null){
                throw new InvalidDataException();
            }

            checkContext(ctx);
            
            doAuthentication(auth, ctx);       

        } catch (InvalidDataException e) {
            log.error(e);
            throw e;
        }
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + usr.getId().intValue() + " - " + auth.toString());
        }
        
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();

        if (tool.schemaBeingLockedOrNeedsUpdate(ctx)) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        if (!tool.existsUser(ctx, usr.getId().intValue())) {
            throw new NoSuchUserException("No such user");
        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        return oxGroup.getGroupsForUser(ctx, usr);

    }

    public Group[] getData(Context ctx, Group[] groups, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchGroupException, DatabaseUpdateException {
        
        try {
            doNullCheck(ctx,groups,auth);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for getData is null", e3);
            throw e3;
        }        

        try {
            checkContext(ctx);
            
            doAuthentication(auth, ctx);       
            if (log.isDebugEnabled()) {
                log.debug("" + ctx.toString() + " - " + groups + " - " + auth.toString());
            }
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();

            if (tool.schemaBeingLockedOrNeedsUpdate(ctx)) {
                throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
            }

            // resolv group id/username
            for (Group group : groups) {
                if (group.getId() != null && !tool.existsGroup(ctx, group.getId().intValue())) {
                    throw new NoSuchGroupException("No such group " + group.getId().intValue());
                }
                if (group.getName() != null && !tool.existsGroup(ctx, group.getName())) {
                    throw new NoSuchGroupException("No such group " + group.getName());
                }
                if (group.getName() == null && group.getId() == null) {
                    throw new InvalidDataException("Groupname and groupid missing!Cannot resolve group data");
                } else {
                    if (group.getName() == null) {
                        // resolv name by id
                        group.setName(tool.getGroupnameByGroupID(ctx, group.getId().intValue()));
                    }
                    if (group.getId() == null) {
                        group.setId(tool.getGroupIDByGroupname(ctx, group.getName()));
                    }
                }
            }

        } catch (InvalidDataException e) {
            log.error(e);
            throw e;
        }


        ArrayList<Group> retval = new ArrayList<Group>();

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        for (Group group : groups) {
            retval.add(oxGroup.get(ctx, group));
        }

        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE == bundle.getState()) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase("oxgroup")) {
                            final OXGroupPluginInterface oxgroupplugin = (OXGroupPluginInterface) this.context.getService(servicereference);
                            if (log.isDebugEnabled()) {
                                log.debug("Calling get for plugin: " + bundlename);
                            }
                            for (Group group : retval) {
                                group = oxgroupplugin.get(ctx, group, auth);
                            }
                        }
                    }
                }
            }
        }
        return (Group[]) retval.toArray(new Group[retval.size()]);

    }

    public int getDefaultGroup(Context ctx, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        try {
            doNullCheck(ctx,auth);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for getDefaultGroup is null", e3);
            throw e3;
        }        
        
        checkContext(ctx);
        
        doAuthentication(auth, ctx);       
        if (log.isDebugEnabled()) {
            log.debug("" + ctx.toString() + " -  " + auth.toString());
        }
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        try{
           return  tool.getDefaultGroupForContextWithOutConnection(ctx);
        }catch (StorageException e) {
            log.error("Error resolving default group for context",e);
            throw e;
        }
        
    }
}