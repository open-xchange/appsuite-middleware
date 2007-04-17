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

import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.BasicAuthenticator;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUserStorageInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.Arrays;
import com.openexchange.admin.exceptions.OXUserException;
import com.openexchange.admin.plugins.OXUserPluginInterface;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.FileUtils;
import com.openexchange.admin.tools.PropertyHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
/**
 * @author cutmasta
 */

public class OXUser extends BasicAuthenticator implements OXUserInterface {

    private AdminCache cache = null;

    private final static Log log = LogFactory.getLog(OXUser.class);

    private PropertyHandler prop = null;
    private BundleContext context = null;
    
    private static final String FALLBACK_LANGUAGE_CREATE = "en";   
    private static final String FALLBACK_COUNTRY_CREATE = "US";
    
    /**
     * Prevent calling the default constructor
     *
     */
    private OXUser() {
        
    }

    public OXUser(final BundleContext context) throws RemoteException {
        super();
        this.context = context;
        this.cache = ClientAdminThread.cache;
        this.prop = this.cache.getProperties();
        log.info("Class loaded: " + this.getClass().getName());
    }
    
    public static Locale getLanguage(final User usr){
        if(usr.getLanguage()==null){
            usr.setLanguage(new Locale(FALLBACK_LANGUAGE_CREATE,FALLBACK_COUNTRY_CREATE));
        }
        return usr.getLanguage();
    }

    public int create(final Context ctx, final User usr, final UserModuleAccess access, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException {

        if(ctx==null || usr==null || access==null){
            throw new InvalidDataException();            
        }        
        
        doAuthentication(auth,ctx);
        
        log.debug(ctx.toString()+" - "+usr.toString()+" - "+access.toString()+" - "+auth.toString());
        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();

        if (!tools.existsContext(ctx)) {
            throw new NoSuchContextException();
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
        String homedir = prop.getUserProp(AdminProperties.User.HOME_DIR_ROOT, "/home");
        homedir += "/" + usr.getUsername();
        if( prop.getUserProp(AdminProperties.User.CREATE_HOMEDIRECTORY, false) &&
                ! tools.isContextAdmin(ctx, usr.getId()) ) {
            if( ! new File(homedir).mkdir() ) {
                log.error("unable to create directory: " + homedir);
            }
            final String CHOWN = "/bin/chown";
            Process p;
            try {
                p = Runtime.getRuntime().exec(
                        new String[] {CHOWN,
                                usr.getUsername() + ":",
                                homedir});
                p.waitFor();
                if( p.exitValue() != 0 ) {
                    log.error(CHOWN + " exited abnormally");
                    final BufferedReader prerr = new BufferedReader(new InputStreamReader(
                            p.getErrorStream()));
                    String line = null;
                    while( (line = prerr.readLine()) != null ) {
                        log.error(line);
                    }
                    log.error("Unable to chown homedirectory: " + homedir);
                }
            } catch (IOException e) {
                log.error("Unable to chown homedirectory: " + homedir);
                log.error(e);
            } catch (InterruptedException e) {
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
                                log.info("Calling create for plugin: " + bundlename);
                                oxuser.create(ctx, usr, access, auth);
                                interfacelist.add(oxuser);
                            } catch (final PluginException e) {
                                log.error("Error while calling create for plugin: " + bundlename, e);
                                log.info("Now doing rollback for everything until now...");
                                for (final OXUserPluginInterface oxuserinterface : interfacelist) {
                                    try {
                                        oxuserinterface.delete(ctx, new User[]{usr}, auth);
                                    } catch (final PluginException e1) {
                                        log.error("Error doing rollback for plugin: " + bundlename, e1);
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
    

    public void change(final Context ctx, final User usrdata, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {

        if(ctx==null || usrdata==null|| usrdata.getId()==null){
            throw new InvalidDataException();
        }
        
        
        doAuthentication(auth,ctx);
        
        log.debug(ctx.toString()+" - "+usrdata.toString()+" - "+auth.toString());
        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();

        if (!tools.existsContext(ctx)) {
            throw new NoSuchContextException();
        }

        if (!tools.existsUser(ctx, usrdata.getId())) {
            throw new InvalidDataException("No such user " + usrdata.getId() + " in context " + ctx.getIdAsInt());
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
                                log.info("Calling change for plugin: " + bundlename);
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

    public void delete(final Context ctx, final User user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException {
        delete(ctx, new User[]{user}, auth);
    }

    public void delete(final Context ctx, final User[] users, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {

        if(ctx==null || users ==null){
            throw new InvalidDataException();            
        }
        
        doAuthentication(auth,ctx);
        
        log.debug(ctx.toString() + " - " + Arrays.toString(users)+" - "+auth.toString());

        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();

        if (!tools.existsContext(ctx)) {
            throw new NoSuchContextException();
            
        }

        final int[] user_ids = getUserIdArrayFromUsers(users);
        // FIXME: Change function form int to user object
        if (!tools.existsUser(ctx, user_ids)) {
            throw new InvalidDataException("No such user " + Arrays.toString(users) + " in context " + ctx.getIdAsInt());
            
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
                                log.info("Calling delete for plugin: " + bundlename);
                                oxuser.delete(ctx, retusers, auth);
                                interfacelist.add(oxuser);
                            } catch (final PluginException e) {
                                log.error("Error while calling delete for plugin: " + bundlename, e);
                                throw new StorageException(e);
                            }
                        }
                    }
                    
                }
            }
        }

        if( prop.getUserProp(AdminProperties.User.CREATE_HOMEDIRECTORY, false) ) {
            for(User usr : users) {
                // homedirectory
                String homedir = prop.getUserProp(AdminProperties.User.HOME_DIR_ROOT, "/home");
                homedir += "/" + usr.getUsername();
                // FIXME: if(! tools.isContextAdmin(ctx, usr.getId()) ) {} ??
                FileUtils.deleteDirectory(homedir);
            }
        }
        
        // FIXME: Change function from int to user object
        oxu.delete(ctx, user_ids);
    }

    private int[] getUserIdArrayFromUsers(User[] users) {
        final int[] retval = new int[users.length];
        for (int i = 0; i < users.length; i++) {
            retval[i] = users[i].getId();
        }
        return retval;
    }

    public UserModuleAccess getModuleAccess(final Context ctx, final int user_id, final Credentials auth)
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {

        if(ctx==null){
            throw new InvalidDataException();            
        }
        
        doAuthentication(auth,ctx);
        
        log.debug(ctx.toString()+ " - " + user_id+" - "+auth.toString());

        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        if (!tools.existsContext(ctx)) {
            throw new NoSuchContextException();
            
        }

        if (!tools.existsUser(ctx, user_id)) {
            throw new InvalidDataException("No such user " + user_id + " in context " + ctx.getIdAsInt());
           
        }

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        return oxu.getModuleAccess(ctx, user_id);

    }

    public void changeModuleAccess(final Context ctx, final int user_id, final UserModuleAccess moduleAccess, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {
    
        if(ctx==null || moduleAccess ==null){
            throw new InvalidDataException();            
        }
        
        doAuthentication(auth,ctx);
        
        log.debug(ctx.toString() + " - " + user_id + " - " + moduleAccess.toString()+" - "+auth.toString());

        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();

        if (!tools.existsContext(ctx)) {
            throw new NoSuchContextException();
           
        }

        if (!tools.existsUser(ctx, user_id)) {
            throw new InvalidDataException("No such user " + user_id + " in context " + ctx.getIdAsInt());
            
        }

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        oxu.changeModuleAccess(ctx, user_id, moduleAccess);

    }

    public int[] getAll(final Context ctx, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException {

        if(ctx==null){
            throw new InvalidDataException();            
        }
       
        doAuthentication(auth,ctx);
        
        log.debug(ctx.toString()+" - "+auth.toString());

        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        if (!tools.existsContext(ctx)) {
            throw new NoSuchContextException();
           
        }

        final OXUserStorageInterface oxu = OXUserStorageInterface.getInstance();
        return oxu.getAll(ctx);

    }

    public User getData(Context ctx, int user_id, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException {
        return getData(ctx, new int[]{user_id}, auth)[0];
    }

    public User[] getData(final Context ctx, final int[] user_ids, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException {
        
        if (ctx==null||user_ids==null) {
            throw new InvalidDataException();
        }
        
        final User[] users = new User[user_ids.length];
        for (int i = 0; i < user_ids.length; i++) {
            users[i] = new User(user_ids[i]);
        }

        return getData(ctx, users, auth);
    }

    public User getData(Context ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException {
        return getData(ctx, new User[]{user}, auth)[0];
    }

    public User[] getData(final Context ctx, final User[] users, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException {        
      
        if (ctx==null || ctx.getIdAsInt()==null || users ==null) {
            throw new InvalidDataException(); 
        }
        
        doAuthentication(auth,ctx);
        
        log.debug(ctx.toString()+" - "+Arrays.toString(users)+" - "+auth.toString());
        
        final OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        
        if (!tools.existsContext(ctx)) {
            throw new NoSuchContextException();            
        }
        
        for (final User usr : users) {
            final String username = usr.getUsername();
            if(username==null && usr.getId()==null){
                throw new InvalidDataException("Username and userid missing!Cannot resolve user data");            
            }else{
                // ok , try to get the username by id or username
                if(username==null){
                    usr.setUsername(tools.getUsernameByUserID(ctx, usr.getId().intValue()));
                }
                
                if(usr.getId()==null ){
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
                            log.info("Calling getData for plugin: " + bundlename);
                            retusers = oxuserplugin.getData(ctx, retusers, auth);
                        }
                    }
                }
            }
        }

        return retusers;
    }

    private void validateUserName( final String userName ) throws OXUserException {
        // Check for allowed chars:
        // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@
        String usr_uid_regexp = prop.getUserProp("CHECK_USER_UID_REGEXP", "[$@%\\.+a-zA-Z0-9_-]");        
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
            if (aliases!=null && !aliases.contains(usrdata.getPrimaryEmail())) {
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
