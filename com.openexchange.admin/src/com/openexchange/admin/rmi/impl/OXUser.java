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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.plugins.OXUserPluginInterface;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.BasicAuthenticator;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUserStorageInterface;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.GenericChecks;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.admin.tools.SHACrypt;
import com.openexchange.admin.tools.UnixCrypt;
import com.openexchange.cache.CacheKey;
/**
 * @author d7
 * @author cutmasta
 */

public class OXUser extends OXCommonImpl implements OXUserInterface {

    private final static Log log = LogFactory.getLog(OXUser.class);

    private static final String FALLBACK_LANGUAGE_CREATE = "en";

    private static final String FALLBACK_COUNTRY_CREATE = "US";
    public static Locale getLanguage(final User usr){
        if (usr.getLanguage() == null) {
            usr.setLanguage(new Locale(FALLBACK_LANGUAGE_CREATE, FALLBACK_COUNTRY_CREATE));
        }
        return usr.getLanguage();
    }
    
    private final AdminCache cache;   
    private final PropertyHandler prop;
    
    private BundleContext context = null;

    public OXUser(final BundleContext context) throws RemoteException {
        super();
        this.context = context;
        this.cache = ClientAdminThread.cache;
        this.prop = this.cache.getProperties();
        if (log.isInfoEnabled()) {
            log.info("Class loaded: " + this.getClass().getName());
        }
    }
    
    /**
     * Prevent calling the default constructor
     *
     */
    @SuppressWarnings("unused")
    private OXUser() {
        super();
        this.cache = ClientAdminThread.cache;
        this.prop = this.cache.getProperties();
    }

    public void change(final Context ctx, final User usrdata, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        try {
            doNullCheck(usrdata,usrdata.getId());
        } catch (final InvalidDataException e2) {
            log.error("One of the given arguments for change is null", e2);
            throw e2;
        }
        
        // SPECIAL USER AUTH CHECK FOR THIS METHOD!
                // check if credentials are from oxadmin or from an user
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        
        try {   
            contextcheck(ctx);
                
            if(!tools.existsContext(ctx)){           
                 final InvalidCredentialsException invalidCredentialsException = new InvalidCredentialsException("Authentication failed for user " + auth.getLogin());
                 log.error("Requested context "+ctx.getIdAsInt()+" does not exist!",invalidCredentialsException);
                 throw invalidCredentialsException;
            }
           
            if (!tools.existsUser(ctx, usrdata.getId())) {
                final NoSuchUserException noSuchUserException = new NoSuchUserException("No such user " + usrdata.getId() + " in context " + ctx.getIdAsInt());
                log.error(noSuchUserException.getMessage(), noSuchUserException);
                throw noSuchUserException;
            }

            final int auth_user_id = tools.getUserIDByUsername(ctx, auth.getLogin());
            // check if given user is admin
            final BasicAuthenticator basicAuthenticator = new BasicAuthenticator();
            if (tools.isContextAdmin(ctx, auth_user_id)) {
                basicAuthenticator.doAuthentication(auth, ctx);
            } else {
                basicAuthenticator.doUserAuthentication(auth, ctx);
                // now check if user which authed has the same id as the user he
                // wants to change,else fail,
                // cause then he/she wants to change not his own data!
                if (usrdata.getId().intValue() != auth_user_id) {
                    throw new InvalidCredentialsException("Permission denied");
                }
            } 
            
            if (tools.schemaBeingLockedOrNeedsUpdate(ctx)) {
                final DatabaseUpdateException databaseUpdateException = new DatabaseUpdateException("Database must be updated or currently is beeing updated");
                log.error(databaseUpdateException.getMessage(), databaseUpdateException);
                throw databaseUpdateException;
            }
            
            if (log.isDebugEnabled()) {
                log.debug(ctx.toString() + " - " + usrdata.toString() + " - " + auth.toString());
            }

            if( usrdata.getUsername() != null ) {
                throw new InvalidDataException("Username cannot be changed");
            }
            
            final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
            final User[] dbuser = oxu.getData(ctx, new User[]{usrdata});
            
            checkChangeUserData(ctx, usrdata, dbuser[0], this.prop);
        } catch (final InvalidDataException e1) {
            log.error(e1.getMessage(), e1);
            throw e1;
        }
        
        boolean isContextAdmin = tools.isContextAdmin(ctx, usrdata.getId());

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        oxu.change(ctx, usrdata);

        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE==bundle.getState()) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase("oxuser")) {
                            final OXUserPluginInterface oxuser = (OXUserPluginInterface) this.context.getService(servicereference);
                            if (oxuser.canHandleContextAdmin() || (!oxuser.canHandleContextAdmin() && !isContextAdmin)) {
                                try {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Calling change for plugin: " + bundlename);
                                    }
                                    oxuser.change(ctx, usrdata, auth);
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
        // change cached admin credentials if neccessary
        if (isContextAdmin && usrdata.getPassword() != null) {
            final Credentials cauth = ClientAdminThread.cache.getAdminCredentials(ctx);
            final String mech = ClientAdminThread.cache.getAdminAuthMech(ctx);
            if ("{CRYPT}".equals(mech)) {
                cauth.setPassword(UnixCrypt.crypt(usrdata.getPassword()));
            } else if ("{SHA}".equals(mech)) {
                try {
                    cauth.setPassword(SHACrypt.makeSHAPasswd(usrdata.getPassword()));
                } catch (NoSuchAlgorithmException e) {
                    log.error("Error encrypting password for credential cache ", e);
                    throw new StorageException(e);
                } catch (UnsupportedEncodingException e) {
                    log.error("Error encrypting password for credential cache ", e);
                    throw new StorageException(e);
                }
            }
            ClientAdminThread.cache.setAdminCredentials(ctx,mech,cauth);
        }
        try {
            JCS cache = JCS.getInstance("User");
            cache.remove(new CacheKey(ctx.getIdAsInt(), usrdata.getId()));
        } catch (final CacheException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void changeModuleAccess(final Context ctx, final int user_id, final UserModuleAccess moduleAccess, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        try {
            doNullCheck(moduleAccess);
        } catch (final InvalidDataException e1) {
            log.error("One of the given arguments for changeModuleAccess is null", e1);
            throw e1;
        }
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + user_id + " - "+ moduleAccess.toString() + " - " + auth.toString());
        }

        new BasicAuthenticator().doAuthentication(auth,ctx);
        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();       
        
        checkSchemaBeingLocked(ctx, tools);

        if (!tools.existsUser(ctx, user_id)) {
            final NoSuchUserException noSuchUserException = new NoSuchUserException("No such user " + user_id + " in context " + ctx.getIdAsInt());
            log.error(noSuchUserException.getMessage(), noSuchUserException);
            throw noSuchUserException;
        }

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        oxu.changeModuleAccess(ctx, user_id, moduleAccess);
    }

    public void changeModuleAccess(final Context ctx, final User user, final UserModuleAccess moduleAccess, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        try {
            doNullCheck(user,moduleAccess);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("User or UserModuleAccess is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + user + " - "+ moduleAccess.toString() + " - " + auth.toString());
        }

        new BasicAuthenticator().doAuthentication(auth,ctx);
        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();       
        
        checkSchemaBeingLocked(ctx, tools);

        final int user_id = user.getId();
        if (!tools.existsUser(ctx, user_id)) {
            final NoSuchUserException noSuchUserException = new NoSuchUserException("No such user " + user_id + " in context " + ctx.getIdAsInt());
            log.error(noSuchUserException.getMessage(), noSuchUserException);
            throw noSuchUserException;
        }

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        oxu.changeModuleAccess(ctx, user_id, moduleAccess);
    }

    public User create(final Context ctx, final User usr, final UserModuleAccess access, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        try {
            doNullCheck(usr,access);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for create is null", e3);
            throw e3;
        }        
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + usr.toString() + " - " + access.toString() + " - " + auth.toString());
        }
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();

        try {
            new BasicAuthenticator().doAuthentication(auth,ctx);        
            
            checkSchemaBeingLocked(ctx, tools);
            
            checkCreateUserData(ctx, usr, this.prop);

            if (tools.existsUser(ctx, usr.getUsername())) {
                throw new InvalidDataException("User " + usr.getUsername() + " already exists in this context");
            }

            // validate email adresss
            tools.primaryMailExists(ctx, usr.getPrimaryEmail());
        } catch (final InvalidDataException e2) {
            log.error(e2.getMessage(), e2);
            throw e2;
        }

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        final int retval = oxu.create(ctx, usr, access);
        usr.setId(retval);
        final ArrayList<OXUserPluginInterface> interfacelist = new ArrayList<OXUserPluginInterface>();

        // homedirectory
        final String homedir = this.prop.getUserProp(AdminProperties.User.HOME_DIR_ROOT, "/home") + "/" + usr.getUsername();
        if (this.prop.getUserProp(AdminProperties.User.CREATE_HOMEDIRECTORY, false) && !tools.isContextAdmin(ctx, usr.getId())) {
            if (!new File(homedir).mkdir()) {
                log.error("unable to create directory: " + homedir);
            }
            final String CHOWN = "/bin/chown";
            final Process p;
            try {
                p = Runtime.getRuntime().exec(new String[] { CHOWN, usr.getUsername() + ":", homedir });
                p.waitFor();
                if (p.exitValue() != 0) {
                    log.error(CHOWN + " exited abnormally");
                    final BufferedReader prerr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    String line = null;
                    while ((line = prerr.readLine()) != null) {
                        log.error(line);
                    }
                    log.error("Unable to chown homedirectory: " + homedir);
                }
            } catch (final IOException e) {
                log.error("Unable to chown homedirectory: " + homedir, e);
            } catch (final InterruptedException e) {
                log.error("Unable to chown homedirectory: " + homedir, e);
            }
        }

        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE==bundle.getState()) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase("oxuser")) {
                            final OXUserPluginInterface oxuser = (OXUserPluginInterface) this.context.getService(servicereference);
                            
                            if (oxuser.canHandleContextAdmin() || (!oxuser.canHandleContextAdmin() && !tools.isContextAdmin(ctx, usr.getId()))) {
                                try {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Calling create for plugin: " + bundlename);
                                    }
                                    oxuser.create(ctx, usr, access, auth);
                                    interfacelist.add(oxuser);
                                } catch (final PluginException e) {
                                    log.error("Error while calling create for plugin: " + bundlename, e);
                                    log.info("Now doing rollback for everything until now...");
                                    for (final OXUserPluginInterface oxuserinterface : interfacelist) {
                                        try {
                                            oxuserinterface.delete(ctx, new User[] { usr }, auth);
                                        } catch (final PluginException e1) {
                                            log.error("Error doing rollback for plugin: " + bundlename, e1);
                                        }
                                    }
                                    try {
                                        oxu.delete(ctx, usr);
                                    } catch (final StorageException e1) {
                                        log.error("Error doing rollback for creating user in database", e1);
                                    }
                                    throw new StorageException(e);
                                }
                            }                            
                        }
                    }
                }
            }
        }
        return usr;
    }

    public void delete(final Context ctx, final User user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        delete(ctx, new User[]{user}, auth);
    }

    public void delete(final Context ctx, final User[] users, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        try {
            doNullCheck((Object[])users);
        } catch (final InvalidDataException e1) {
            log.error("One of the given arguments for delete is null", e1);
            throw e1;
        }
        
        if (users.length == 0) {
            final InvalidDataException e = new InvalidDataException("User array is empty");
            log.error(e.getMessage(), e);
            throw e;
        }
        
        new BasicAuthenticator().doAuthentication(auth,ctx);       
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + Arrays.toString(users) + " - " + auth.toString());
        }        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();       
        
        checkSchemaBeingLocked(ctx, tools);
        
        int[] user_ids;
        try {
            user_ids = getUserIdArrayFromUsers(users);
            // FIXME: Change function form int to user object
            if (!tools.existsUser(ctx, user_ids)) {
                final NoSuchUserException noSuchUserException = new NoSuchUserException("No such user " + Arrays.toString(users) + " in context " + ctx.getIdAsInt());
                log.error(noSuchUserException.getMessage(), noSuchUserException);
                throw noSuchUserException;
            }
            for (final User user : users) {
                if (tools.isContextAdmin(ctx, user.getId())) {
                    throw new InvalidDataException("Admin delete not supported");              
                }
            }
        } catch (final InvalidDataException e1) {
            log.error(e1.getMessage(), e1);
            throw e1;
        }

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        User[] retusers = oxu.getData(ctx, users);
        
        final ArrayList<OXUserPluginInterface> interfacelist = new ArrayList<OXUserPluginInterface>();

        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        final ArrayList<Bundle> revbundles = new ArrayList<Bundle>();
        for (int i = bundles.size() - 1; i >= 0; i--) {
            revbundles.add(bundles.get(i));
        }
        for (final Bundle bundle : revbundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE==bundle.getState()) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase("oxuser")) {
                            final OXUserPluginInterface oxuser = (OXUserPluginInterface) this.context.getService(servicereference);
                            if (!oxuser.canHandleContextAdmin()) {
                                retusers = removeContextAdmin(ctx, retusers);
                                if (retusers.length > 0) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Calling delete for plugin: " + bundlename);
                                    }
                                    callDeleteForPlugin(ctx, auth, retusers, interfacelist, bundlename, oxuser);
                                }
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("Calling delete for plugin: " + bundlename);
                                }
                                callDeleteForPlugin(ctx, auth, retusers, interfacelist, bundlename, oxuser);
                            }
                        }
                    }
                }
            }
        }

        if( this.prop.getUserProp(AdminProperties.User.CREATE_HOMEDIRECTORY, false) ) {
            for(final User usr : users) {
                // homedirectory
                String homedir = this.prop.getUserProp(AdminProperties.User.HOME_DIR_ROOT, "/home");
                homedir += "/" + usr.getUsername();
                // FIXME: if(! tools.isContextAdmin(ctx, usr.getId()) ) {} ??
                try {
                    FileUtils.deleteDirectory(new File(homedir));
                } catch (final IOException e) {
                    log.error("Could not delete homedir for user: " + usr);
                }
            }
        }
        
        oxu.delete(ctx, users);

        for(int id : user_ids) {
            try {
                JCS cache = JCS.getInstance("User");
                cache.remove(new CacheKey(ctx.getIdAsInt(), id));
            } catch (final CacheException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public int[] getAll(final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + auth.toString());
        }
        
        new BasicAuthenticator().doAuthentication(auth,ctx);
        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();      
        
        checkSchemaBeingLocked(ctx, tools);

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        return oxu.getAll(ctx);
    }

    public User getData(final Context ctx, final int user_id, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException {
        return getData(ctx, new int[]{user_id}, auth)[0];
    }

    public User[] getData(final Context ctx, final int[] user_ids, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException {
        if (null == user_ids) {
            final InvalidDataException invalidDataException = new InvalidDataException("Array of user ids is empty");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }
        
        final User[] users = new User[user_ids.length];
        for (int i = 0; i < user_ids.length; i++) {
            users[i] = new User(user_ids[i]);
        }

        return getData(ctx, users, auth);
    }

    public User getData(final Context ctx, final User user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException {
        return getData(ctx, new User[]{user}, auth)[0];
    }

    public User[] getData(final Context ctx, final User[] users, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException {        
        try {
            doNullCheck((Object[])users);
        } catch (final InvalidDataException e1) {
            log.error("One of the given arguments for getAll is null", e1);
            throw e1;
        }
        
        try {
            checkContext(ctx);
            
            if (users.length <= 0) {
                throw new InvalidDataException();
            }
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + Arrays.toString(users) + " - " + auth.toString());
        }
                
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        
        try {
            // enable check who wants to get data if authentcaition is enabled 
            final BasicAuthenticator basicAuthenticator = new BasicAuthenticator();
            if (!cache.contextAuthenticationDisabled()) {
                // ok here its possible that a user wants to get his own data
                // SPECIAL USER AUTH CHECK FOR THIS METHOD!
                // check if credentials are from oxadmin or from an user
                final int auth_user_id = tools.getUserIDByUsername(ctx, auth.getLogin());
                // check if given user is not admin, if he is admin, the
                if (!tools.isContextAdmin(ctx, auth_user_id)) {
                    final InvalidCredentialsException invalidCredentialsException = new InvalidCredentialsException("Permission denied");
                    if (users.length > 1) {
                        log.error("User sent more than 1 users to get data for. Only context admin is allowed to do that", invalidCredentialsException);
                        throw invalidCredentialsException;
                        // one user cannot edit more than his own data
                    } else {
                        basicAuthenticator.doUserAuthentication(auth, ctx);
                        // its possible that he wants his own data
                        if (users[0].getId() != null) {
                            if (users[0].getId().intValue() != auth_user_id) {
                                throw invalidCredentialsException;
                            }
                        } else {
                            // id not set, try to resolv id by username and then
                            // check
                            // again
                            if (users[0].getUsername() != null) {
                                final int check_user_id = tools.getUserIDByUsername(ctx, users[0].getUsername());
                                if (check_user_id != auth_user_id) {
                                    log.debug("user[0].getId() does not match id from Credentials.getLogin()");
                                    throw invalidCredentialsException;
                                }
                            } else {
                                log.debug("Cannot resolv user[0]`s internal id because the username is not set!");
                                throw new InvalidDataException("Username and userid missing.");
                            }
                        }
                    }
                } else {
                    basicAuthenticator.doAuthentication(auth, ctx);
                }

            } else {
                basicAuthenticator.doAuthentication(auth, ctx);
            }
        
            checkSchemaBeingLocked(ctx, tools);
            
            for (final User usr : users) {
                if (usr.getId()!=null && !tools.existsUser(ctx, usr.getId())) {
                    throw new NoSuchUserException("No such user "+usr);
                }
                if (usr.getUsername() != null && !tools.existsUser(ctx, usr.getUsername())) {
                    throw new NoSuchUserException("No such user " + usr);
                }
                final String username = usr.getUsername();
                if (username == null && usr.getId() == null) {
                    throw new InvalidDataException("Username and userid missing.");
                } else {
                    // ok , try to get the username by id or username
                    if (username == null) {
                        usr.setUsername(tools.getUsernameByUserID(ctx, usr.getId().intValue()));
                    }

                    if (usr.getId() == null) {
                        usr.setId(new Integer(tools.getUserIDByUsername(ctx, username)));
                    }
                }
            }
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw(e);
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw(e);
        }
         
        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        
        User[] retusers = oxu.getData(ctx, users);
        
        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE==bundle.getState()) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase("oxuser")) {
                            final OXUserPluginInterface oxuserplugin = (OXUserPluginInterface) this.context.getService(servicereference);
                            //TODO: Implement check for contextadmin here
                            if (log.isDebugEnabled()) {
                                log.debug("Calling getData for plugin: " + bundlename);
                            }
                            retusers = oxuserplugin.getData(ctx, retusers, auth);
                        }
                    }
                }
            }
        }
        log.debug(Arrays.toString(retusers));
        return retusers;
    }
    
    public UserModuleAccess getModuleAccess(final Context ctx, final int user_id, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + user_id + " - " + auth.toString());
        }        
        
        new BasicAuthenticator().doAuthentication(auth,ctx);
        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        
        checkSchemaBeingLocked(ctx, tools);

        if (!tools.existsUser(ctx, user_id)) {
            final NoSuchUserException noSuchUserException = new NoSuchUserException("No such user " + user_id + " in context " + ctx.getIdAsInt());
            log.error(noSuchUserException.getMessage(), noSuchUserException);
            throw noSuchUserException;
        }

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        return oxu.getModuleAccess(ctx, user_id);
    }

    public UserModuleAccess getModuleAccess(final Context ctx, final User user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        try {
            doNullCheck(user);
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("User object is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + user + " - " + auth.toString());
        }        
        new BasicAuthenticator().doAuthentication(auth,ctx);
        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        
        checkSchemaBeingLocked(ctx, tools);

        final int user_id = user.getId();
        if (!tools.existsUser(ctx, user_id)) {
            final NoSuchUserException noSuchUserException = new NoSuchUserException("No such user " + user + " in context " + ctx.getIdAsInt());
            log.error(noSuchUserException.getMessage(), noSuchUserException);
            throw noSuchUserException;
        }

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        return oxu.getModuleAccess(ctx, user_id);
    }

    public boolean isContextAdmin(Context ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException {
        try {
            doNullCheck(user);
        } catch (final InvalidDataException e1) {
            log.error("One of the given arguments is null", e1);
            throw e1;
        }
                
        new BasicAuthenticator().doUserAuthentication(auth,ctx);
        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        
        checkSchemaBeingLocked(ctx, tools);
        
        if (user.getId()!=null && !tools.existsUser(ctx, user.getId().intValue())) {
            final NoSuchUserException noSuchUserException = new NoSuchUserException("No such user " + user.getId().intValue() + " in context " + ctx.getIdAsInt());
            log.error(noSuchUserException.getMessage(), noSuchUserException);
            throw noSuchUserException;           
        }
        
        return tools.isContextAdmin(ctx, user.getId().intValue());
    }

    public User[] list(final Context ctx, final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + auth.toString());
        }
        
        new BasicAuthenticator().doAuthentication(auth,ctx);
        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();      
        
        checkSchemaBeingLocked(ctx, tools);

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        
        final User[] retval =  oxu.list(ctx, search_pattern);
        
        return retval;
    }

    public User[] listAll(final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        return list(ctx, "*", auth);
    }

    private void callDeleteForPlugin(final Context ctx, final Credentials auth, User[] retusers, final ArrayList<OXUserPluginInterface> interfacelist, final String bundlename, final OXUserPluginInterface oxuser) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Calling delete for plugin: " + bundlename);
            }
            oxuser.delete(ctx, retusers, auth);
            interfacelist.add(oxuser);
        } catch (final PluginException e) {
            log.error("Error while calling delete for plugin: " + bundlename, e);
        }
    }

    private void checkChangeUserData(final Context ctx, final User newuser, final User dbuser, final PropertyHandler prop) throws StorageException, InvalidDataException {
    
        // Do some mail attribute checks cause of bug
        // http://www.open-xchange.org/bugzilla/show_bug.cgi?id=5444
    
        // 1. If user sends only Aliases but not primarymail and email2 field.
        // show error.
        // cause he must set which adress is primarymail and email2 from the new
        // aliases
        
        if (newuser.getPassword() != null && newuser.getPassword().trim().length() == 0) {
            throw new InvalidDataException("Empty password is not allowed");
        }
        
        final OXToolStorageInterface oxtool = OXToolStorageInterface.getInstance();
        
        if (!oxtool.isContextAdmin(ctx, newuser.getId())) {
            // checks below throw InvalidDataException
            checkValidEmailsInUserObject(newuser);
            HashSet<String> useraliases = newuser.getAliases();
            final String primaryEmail = newuser.getPrimaryEmail();
            final String email1 = newuser.getEmail1();
            if (primaryEmail != null && email1 != null) {
                // primary mail value must be same with email1
                if (!primaryEmail.equals(email1)) {
                    throw new InvalidDataException("email1 not equal with primarymail!");
                }
            }
            if (useraliases == null) {
                useraliases = dbuser.getAliases();
            }
            String check_primary_mail;
            String check_email1;
            if (primaryEmail != null) {
                check_primary_mail = primaryEmail;
            } else {
                check_primary_mail = dbuser.getPrimaryEmail();
            }
            if (email1 != null) {
                check_email1 = email1;
            } else {
                check_email1 = dbuser.getPrimaryEmail();
            }
            boolean found_primary_mail = false;
            boolean found_email1 = false;
            if (null != useraliases) {
                for (final String addr : useraliases) {
                    GenericChecks.checkValidMailAddress(addr);
                    if (check_primary_mail.equals(addr)) {
                        found_primary_mail = true;
                    }
                    if (check_email1.equals(addr)) {
                        found_email1 = true;
                    }
                }
            }
            if (!found_primary_mail || !found_email1) {
                throw new InvalidDataException("primarymail and mail1 must be present in set of aliases.");
            }
            // added "usrdata.getPrimaryEmail() != null" for this check, else we cannot update user data without mail data
            // which is not very good when just changing the displayname for example
            if (primaryEmail != null && email1 == null) {
                throw new InvalidDataException("email1 not sent but required!");

            }
        }        
    
    }

    private void checkContext(final Context ctx) throws InvalidDataException {
        if (null == ctx.getIdAsInt()) {
            throw new InvalidDataException("Context invalid");
        }
    }

    private void checkCreateUserData(final Context ctx, final User usr, final PropertyHandler prop) throws InvalidDataException {
        final Locale langus = OXUser.getLanguage(usr);
        if (langus.getLanguage().indexOf('_') != -1 || langus.getCountry().indexOf('_') != -1) {
            throw new InvalidDataException("The specified locale data (Language:" + langus.getLanguage() + " - Country:" + langus.getCountry() + ") for users language is invalid!");
        }
        
        if(usr.getPassword()==null || usr.getPassword().trim().length()==0){
            throw new InvalidDataException("Empty password is not allowed");
        }
    
        if (!usr.attributesforcreateset()) {
            throw new InvalidDataException("Mandatory fields not set");
        }
    
        if (prop.getUserProp(AdminProperties.User.CHECK_NOT_ALLOWED_CHARS, true)) {
            validateUserName(usr.getUsername());
        }
    
        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, true)) {
            usr.setUsername(usr.getUsername().toLowerCase());
        }
    
        // checks below throw InvalidDataException
        checkValidEmailsInUserObject(usr);
        final HashSet<String> aliases = usr.getAliases();
        if (aliases != null) {
            for (final String addr : aliases) {
                GenericChecks.checkValidMailAddress(addr);
            }
        }
            
        // ### Do some mail attribute checks cause of bug 5444
        // check if primaryemail address is also set in I_OXUser.EMAIL1,
        if (!usr.getPrimaryEmail().equals(usr.getEmail1())) {
        	 throw new InvalidDataException("primarymail must have the same value as email1");
        }
    
        // put primary mail in the aliases,
        usr.addAlias(usr.getPrimaryEmail());
    
        // check if privateemail1(before refacotring EMAIL1) set in aliases
        if (aliases != null) {
            if (!aliases.contains(usr.getEmail1())) {
                throw new InvalidDataException("email1 must also be set in the aliases");
            }
        }
        // ############################################
    }

    /**
     * @param ctx
     * @param tools
     * @throws StorageException
     * @throws DatabaseUpdateException
     * @throws NoSuchContextException
     */
    private void checkSchemaBeingLocked(final Context ctx, final OXToolStorageInterface tools) throws StorageException, DatabaseUpdateException, NoSuchContextException {
        if (tools.schemaBeingLockedOrNeedsUpdate(ctx)) {
            final DatabaseUpdateException databaseUpdateException = new DatabaseUpdateException("Database must be updated or currently is beeing updated");
            log.error(databaseUpdateException.getMessage(), databaseUpdateException);
            throw databaseUpdateException;
        }
    }

    private void checkValidEmailsInUserObject(final User usr) throws InvalidDataException {
        GenericChecks.checkValidMailAddress(usr.getPrimaryEmail());
        GenericChecks.checkValidMailAddress(usr.getEmail1());
        GenericChecks.checkValidMailAddress(usr.getEmail2());
        GenericChecks.checkValidMailAddress(usr.getEmail3());
    }

    private int[] getUserIdArrayFromUsers(final User[] users) throws InvalidDataException {
        final int[] retval = new int[users.length];
        for (int i = 0; i < users.length; i++) {
            final Integer id = users[i].getId();
            if (null != id) {
                retval[i] = id;
            } else {
                throw new InvalidDataException("One user object has no id");
            }
        }
        return retval;
    }
    
    private User[] removeContextAdmin(final Context ctx, final User[] retusers) throws StorageException {
        final OXToolStorageInterface oxtool = OXToolStorageInterface.getInstance();
        final ArrayList<User> list = new ArrayList<User>();
        for (final User user : retusers) {
            if (!oxtool.isContextAdmin(ctx, user.getId())) {
                list.add(user);
            }
        }
        return list.toArray(new User[list.size()]);
    }

    private void validateUserName(final String userName) throws InvalidDataException {
        // Check for allowed chars:
        // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@
        final String usr_uid_regexp = this.prop.getUserProp("CHECK_USER_UID_REGEXP", "[$@%\\.+a-zA-Z0-9_-]");        
        final String illegal = userName.replaceAll(usr_uid_regexp,"");
        if( illegal.length() > 0 ) {
            throw new InvalidDataException("Illegal chars: \""+illegal+"\"");
        }
    }
}
