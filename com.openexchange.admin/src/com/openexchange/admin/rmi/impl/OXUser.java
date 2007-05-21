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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.exceptions.OXUserException;
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
/**
 * @author cutmasta
 */

public class OXUser extends BasicAuthenticator implements OXUserInterface {

    private final AdminCache cache;

    private final static Log log = LogFactory.getLog(OXUser.class);

    private final PropertyHandler prop;
    private BundleContext context = null;
    
    private static final String FALLBACK_LANGUAGE_CREATE = "en";   
    private static final String FALLBACK_COUNTRY_CREATE = "US";
    
    /**
     * Prevent calling the default constructor
     *
     */
    private OXUser() {
        super();
        this.cache = ClientAdminThread.cache;
        this.prop = this.cache.getProperties();
    }

    public OXUser(final BundleContext context) throws RemoteException {
        super();
        this.context = context;
        this.cache = ClientAdminThread.cache;
        this.prop = this.cache.getProperties();
        if (log.isInfoEnabled()) {
            log.info("Class loaded: " + this.getClass().getName());
        }
    }
    
    public static Locale getLanguage(final User usr){
        if(usr.getLanguage()==null){
            usr.setLanguage(new Locale(FALLBACK_LANGUAGE_CREATE,FALLBACK_COUNTRY_CREATE));
        }
        return usr.getLanguage();
    }

    public int create(final Context ctx, final User usr, final UserModuleAccess access, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        
        doNullCheck(ctx,usr,access,auth);        
        
        doAuthentication(auth,ctx);        
        
        Locale langus = OXUser.getLanguage(usr);
        if (langus.getLanguage().indexOf('_') != -1 || langus.getCountry().indexOf('_') != -1) {
            if (log.isDebugEnabled()) {
                log.debug("Client sent invalid locale data(" + langus + ") in users language!");
            }
            throw new InvalidDataException("The specified locale data (Language:" + langus.getLanguage() + " - Country:" + langus.getCountry() + ") for users language is invalid!");
        }
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + usr.toString() + " - " + access.toString() + " - " + auth.toString());
        }
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();

        if (!tools.existsContext(ctx)) {
            throw new NoSuchContextException();
        }

        if (tools.schemaBeingLockedOrNeedsUpdate(ctx)) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        checkCreateUserData(ctx, usr, this.prop);

        if (tools.existsUser(ctx, usr.getUsername())) {
            throw new InvalidDataException("User " + usr.getUsername() + " already exists in this context");
        }

        // validate email adresss
        tools.checkPrimaryMail(ctx, usr.getPrimaryEmail());

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        final int retval = oxu.create(ctx, usr, access);
        usr.setId(retval);
        final ArrayList<OXUserPluginInterface> interfacelist = new ArrayList<OXUserPluginInterface>();

        // homedirectory
        String homedir = this.prop.getUserProp(AdminProperties.User.HOME_DIR_ROOT, "/home");
        homedir += "/" + usr.getUsername();
        if (this.prop.getUserProp(AdminProperties.User.CREATE_HOMEDIRECTORY, false) && !tools.isContextAdmin(ctx, usr.getId())) {
            if (!new File(homedir).mkdir()) {
                log.error("unable to create directory: " + homedir);
            }
            final String CHOWN = "/bin/chown";
            Process p;
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
                log.error("Unable to chown homedirectory: " + homedir);
                log.error(e);
            } catch (final InterruptedException e) {
                log.error("Unable to chown homedirectory: " + homedir);
                log.error(e);
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
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("Calling create for plugin: " + bundlename);
                                }                                
                                oxuser.create(ctx, usr, access, auth);
                                interfacelist.add(oxuser);
                            } catch (final PluginException e) {
                                log.error("Error while calling create for plugin: " + bundlename, e);
                                log.error("Now doing rollback for everything until now...");
                                for (final OXUserPluginInterface oxuserinterface : interfacelist) {
                                    try {
                                        oxuserinterface.delete(ctx, new User[]{usr}, auth);
                                    } catch (final PluginException e1) {
                                        log.error("Error doing rollback for plugin: "+ bundlename, e1);
                                    }
                                }
                                try {
                                    oxu.delete(ctx, new int[]{usr.getId()});
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
        return retval;
    }

    private void validMailAddress(final String address) throws InvalidDataException {
        if( address != null && ! GenericChecks.isValidMailAddress(address)) {  
            throw new InvalidDataException("Invalid email address");
        }
    }
    
    public void checkCreateUserData(final Context ctx, final User usr, final PropertyHandler prop) throws InvalidDataException {
        if (!usr.attributesforcreateset()) {
            throw new InvalidDataException("Mandatory fields not set");
        }

        if (prop.getUserProp(AdminProperties.User.CHECK_NOT_ALLOWED_CHARS, true)) {
            try {
                validateUserName(usr.getUsername());
            } catch (final OXUserException oxu) {
                throw new InvalidDataException("Invalid username");
            }
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, true)) {
            usr.setUsername(usr.getUsername().toLowerCase());
        }


        try {
            // checks below throw InvalidDataException
            validMailAddress(usr.getPrimaryEmail());
            validMailAddress(usr.getEmail1());
            validMailAddress(usr.getEmail2());
            validMailAddress(usr.getEmail3());
            if( usr.getAliases() != null ) {
                for(final String addr : usr.getAliases() ) {
                    validMailAddress(addr);
                }
            }
        } catch( final InvalidDataException e ) {
            log.error(e);
            throw e;
        }
            
        // ### Do some mail attribute checks cause of bug 5444
        // check if primaryemail address is also set in I_OXUser.EMAIL1,
        if( !usr.getPrimaryEmail().equals(usr.getEmail1() )) {
        	 throw new InvalidDataException("Primary mail must have the same value as email1");
        }

        // put primary mail in the aliases,
        usr.addAlias(usr.getPrimaryEmail());

        // check if privateemail1(before refacotring EMAIL1) set in aliases
        if (usr.getAliases() != null) {
            if (!usr.getAliases().contains(usr.getEmail1())) {
                throw new InvalidDataException("email1 must also be set in the aliases");
            }
        }

        // ############################################
    }
    

    public void change(final Context ctx, final User usrdata, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        
        doNullCheck(ctx,usrdata,auth);
        
        if(usrdata.getId()==null|| auth.getLogin()==null||auth.getPassword()==null){
            throw new InvalidDataException();
        }
        
        
        // SPECIAL USER AUTH CHECK FOR THIS METHOD!
        // check if credentials are from oxadmin or from an user
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        final int auth_user_id = tools.getUserIDByUsername(ctx, auth.getLogin());
        // check if given user is admin
        if (tools.isContextAdmin(ctx, auth_user_id)) {
            doAuthentication(auth, ctx);
        } else {
            doUserAuthentication(auth, ctx);
            // now check if user which authed has the same id as the user he
            // wants to change,else fail,
            // cause then he/she wants to change not his own data!
            if (usrdata.getId().intValue() != auth_user_id) {
                throw new InvalidCredentialsException("Authenticated User`s Id does not match User.getId()");
            }
        } 
        
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + usrdata.toString() + " - " + auth.toString());
        }        

        if (tools.schemaBeingLockedOrNeedsUpdate(ctx)) {
            throw new DatabaseUpdateException("Database must be updated or is currently beeing updated");
        }
        
        if (!tools.existsUser(ctx, usrdata.getId())) {
            throw new NoSuchUserException("No such user " + usrdata.getId() + " in context " + ctx.getIdAsInt());
        }

        checkChangeUserData(ctx, usrdata, this.prop);

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
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("Calling change for plugin: " + bundlename);
                                }                                
                                oxuser.change(ctx, usrdata, auth);
                            } catch (final PluginException e) {
                                log.error("Error while calling change for plugin: "+ bundlename, e);
                                throw new StorageException(e);
                            }
                        }
                    }
                    
                }
            }
        }

    }

    public void delete(final Context ctx, final User user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        delete(ctx, new User[]{user}, auth);
    }

    public void delete(final Context ctx, final User[] users, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        
        doNullCheck(ctx,users,auth);
        
        doAuthentication(auth,ctx);       
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + Arrays.toString(users) + " - " + auth.toString());
        }        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();       

        if( tools.schemaBeingLockedOrNeedsUpdate(ctx) ) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        final int[] user_ids = getUserIdArrayFromUsers(users);
        // FIXME: Change function form int to user object
        if (!tools.existsUser(ctx, user_ids)) {
            throw new NoSuchUserException("No such user " + Arrays.toString(users) + " in context " + ctx.getIdAsInt());
            
        }
        for (final User user : users) {
            if (tools.isContextAdmin(ctx, user.getId())) {
                throw new InvalidDataException("Admin delete not supported");              
            }
        }

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        final User[] retusers = oxu.getData(ctx, users);
        
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
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("Calling delete for plugin: " + bundlename);
                                }                                
                                oxuser.delete(ctx, retusers, auth);
                                interfacelist.add(oxuser);
                            } catch (final PluginException e) {
                                log.error("Error while calling delete for plugin: "+ bundlename, e);
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
        
        // FIXME: Change function from int to user object
        oxu.delete(ctx, user_ids);
    }

    private int[] getUserIdArrayFromUsers(final User[] users) {
        final int[] retval = new int[users.length];
        for (int i = 0; i < users.length; i++) {
            retval[i] = users[i].getId();
        }
        return retval;
    }

    public UserModuleAccess getModuleAccess(final Context ctx, final int user_id, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        
        doNullCheck(ctx,auth);
        
        doAuthentication(auth,ctx);
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + user_id + " - " + auth.toString());
        }        
        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();

        if( tools.schemaBeingLockedOrNeedsUpdate(ctx) ) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        if (!tools.existsUser(ctx, user_id)) {
            throw new NoSuchUserException("No such user " + user_id + " in context " + ctx.getIdAsInt());
           
        }

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        return oxu.getModuleAccess(ctx, user_id);

    }

    public void changeModuleAccess(final Context ctx, final int user_id, final UserModuleAccess moduleAccess, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchUserException {
    
       doNullCheck(ctx,moduleAccess,auth);
       
       doAuthentication(auth,ctx);
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + user_id + " - "+ moduleAccess.toString() + " - " + auth.toString());
        }        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();


        if( tools.schemaBeingLockedOrNeedsUpdate(ctx) ) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        if (!tools.existsUser(ctx, user_id)) {
            throw new NoSuchUserException("No such user " + user_id + " in context " + ctx.getIdAsInt());
            
        }

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        oxu.changeModuleAccess(ctx, user_id, moduleAccess);

    }

    public int[] getAll(final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        
        doNullCheck(ctx,auth);
        
        doAuthentication(auth,ctx);
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + auth.toString());
        }
        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();      

        if( tools.schemaBeingLockedOrNeedsUpdate(ctx) ) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        return oxu.getAll(ctx);

    }

    public User getData(final Context ctx, final int user_id, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException {
        return getData(ctx, new int[]{user_id}, auth)[0];
    }

    public User[] getData(final Context ctx, final int[] user_ids, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException {
        if (user_ids==null) {
            throw new InvalidDataException();
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
        
        doNullCheck(ctx,users,auth);
        
        if (ctx.getIdAsInt() == null|| auth.getLogin() == null) {
            throw new InvalidDataException();
        }
        
        if (users.length <= 0) {
            throw new InvalidDataException();
        }
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + Arrays.toString(users) + " - " + auth.toString());
        }
        
        // ok here its possible that a user wants to get his own data
        //  SPECIAL USER AUTH CHECK FOR THIS METHOD!
        // check if credentials are from oxadmin or from an user
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        final int auth_user_id = tools.getUserIDByUsername(ctx, auth.getLogin());
        // check if given user is not admin, if he is admin, the
        if (!tools.isContextAdmin(ctx, auth_user_id)) {
            if (users.length > 1) {
                log.debug("User sent more than >1 users to get data for!Thats not permitted for normal users!");
                throw new InvalidCredentialsException("Authenticated User`s Id does not match!");
                // one user cannot edit more than his own data
            } else {
                doUserAuthentication(auth, ctx);
                // its possible that he wants his own data
                if (users[0].getId() != null) {
                    if (users[0].getId().intValue() != auth_user_id) {
                        throw new InvalidCredentialsException("Authenticated User`s Id does not match User.getId()");
                    }
                } else {
                    // id not set, try to resolv id by username and then check
                    // again
                    if (users[0].getUsername() != null) {
                        final int check_user_id = tools.getUserIDByUsername(ctx, users[0].getUsername());
                        if (check_user_id != auth_user_id) {
                            log.debug("user[0].getId() does not match id from Credentials.getLogin()");
                            throw new InvalidCredentialsException("Authenticated User`s Id does not match User.getId()");
                        }
                    } else {
                        log.debug("Cannot resolv user[0]`s internal id because the username is not set!");
                        throw new InvalidDataException("Username and userid missing!Cannot resolve user data");
                    }
                }
            }
        } else {
            doAuthentication(auth, ctx);
        }
         
              
       
        if (tools.schemaBeingLockedOrNeedsUpdate(ctx)) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }
        
        for (final User usr : users) {
            if (usr.getId()!=null && !tools.existsUser(ctx, usr.getId())) {
                throw new NoSuchUserException("No such user "+usr);
            }
            if (usr.getUsername() != null && !tools.existsUser(ctx, usr.getUsername())) {
                throw new NoSuchUserException("No such user " + usr);
            }
            final String username = usr.getUsername();
            if (username == null && usr.getId() == null) {
                throw new InvalidDataException("Username and userid missing!Cannot resolve user data");
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

    private void validateUserName( final String userName ) throws OXUserException {
        // Check for allowed chars:
        // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@
        final String usr_uid_regexp = this.prop.getUserProp("CHECK_USER_UID_REGEXP", "[$@%\\.+a-zA-Z0-9_-]");        
        final String illegal = userName.replaceAll(usr_uid_regexp,"");
        if( illegal.length() > 0 ) {
            throw new OXUserException( OXUserException.ILLEGAL_CHARS + ": \""+illegal+"\"");
        }
    }

    private void checkChangeUserData(final Context ctx, final User usrdata, final PropertyHandler prop) throws StorageException,InvalidDataException {
    
        // MAIL ATTRIBUTE CHANGE SUPPORTED?? - currently disabled cause of a
        // discussion
        // if( usrdata.getPrimaryEmail()==null) {
        // //throw USER_EXCEPTIONS.create(23,"Changing mail attribute not
        // allowed");
        // }
    
        // Do some mail attribute checks cause of bug
        // http://www.open-xchange.org/bugzilla/show_bug.cgi?id=5444
    
        // 1. If user sends only Aliases but not primarymail and email2 field.
        // show error.
        // cause he must set which adress is primarymail and email2 from the new
        // aliases
        if (OXUser.getLanguage(usrdata) != null) {
            Locale langus = OXUser.getLanguage(usrdata);
            if (langus.getLanguage().indexOf('_') != -1 || langus.getCountry().indexOf('_') != -1) {
                if (log.isDebugEnabled()) {
                    log.debug("Client sent invalid locale data(" + langus + ") in users language!");
                }
                throw new InvalidDataException("The specified locale data (Language:" + langus.getLanguage() + " - Country:" + langus.getCountry() + ") for users language is invalid!");
            }
        }

        try {
            // checks below throw InvalidDataException
            validMailAddress(usrdata.getPrimaryEmail());
            validMailAddress(usrdata.getEmail1());
            validMailAddress(usrdata.getEmail2());
            validMailAddress(usrdata.getEmail3());
            if( usrdata.getAliases() != null ) {
                for(final String addr : usrdata.getAliases() ) {
                    validMailAddress(addr);
                }
            }
        } catch (final InvalidDataException e) {
            log.error(e);
            throw e;
        }

        if (usrdata.getAliases() != null) {
            if (usrdata.getPrimaryEmail() == null || usrdata.getPrimaryEmail().length() < 1) {
                throw new InvalidDataException("If ALIAS sent you need to send also primarymail!");
            }
            if (usrdata.getEmail1() == null || usrdata.getEmail1().length() < 1) {
                throw new InvalidDataException("If ALIAS sent you need to send also mail1!");
            }
        }
    
        HashSet<String> aliases = new HashSet<String>();
        if (usrdata.getAliases() != null) {
            aliases = usrdata.getAliases();
        } else {
            final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
            final User[] usr = oxu.getData(ctx, new User[]{usrdata});
            aliases = usr[0].getAliases();
        }
    
        if (usrdata.getPrimaryEmail() != null) {
            if (aliases != null && !aliases.contains(usrdata.getPrimaryEmail())) {
                throw new InvalidDataException("primarymail sent but does not exists in aliases of the user!");
            }
        }
    
        // added "usrdata.getPrimaryEmail() != null" for this check, else we cannot update user data without mail data
        // which is not very good when just changing the displayname for example
        if ((usrdata.getPrimaryEmail() != null && usrdata.getEmail1() == null)) {
            throw new InvalidDataException("email1 not sent but required!");
           
        }
    
        if (usrdata.getPrimaryEmail() != null && usrdata.getEmail1() != null) {
            // primary mail value must be same with email1
            if (!usrdata.getPrimaryEmail().equals(usrdata.getEmail1())) {
                throw new InvalidDataException("email1 not equal with primarymail!");
                
            }
        }
    }
    
}
