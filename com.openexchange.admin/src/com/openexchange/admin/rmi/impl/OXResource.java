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
/*
 * $Id$
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
import com.openexchange.admin.exceptions.OXGroupException;
import com.openexchange.admin.exceptions.OXResourceException;
import com.openexchange.admin.plugins.OXResourcePluginInterface;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.BasicAuthenticator;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.extensions.OXResourceExtensionInterface;
import com.openexchange.admin.storage.interfaces.OXResourceStorageInterface;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;

public class OXResource extends BasicAuthenticator implements OXResourceInterface{
    
    private static final long serialVersionUID = -7012370962672596682L;
    
    private AdminCache cache = null;
    
    private PropertyHandler prop = null;
    
    private final Log log = LogFactory.getLog(this.getClass());    
    private BundleContext context = null;
    
    public OXResource(final BundleContext context) throws RemoteException {
        super();
        this.context = context;
        cache = ClientAdminThread.cache;
        prop = cache.getProperties();     
        if (log.isInfoEnabled()) {
            log.info("Class loaded: " + this.getClass().getName());
        }
        
    }
    
   public int create(final Context ctx, final Resource res, final Credentials auth)
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException {        
       
       doNullCheck(ctx,res,auth);
       
       doAuthentication(auth,ctx);    
      
        
       if (log.isDebugEnabled()) {
           log.debug(ctx.toString() + " - " + res.toString() + " - " + auth.toString()); 
       }

       final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();


       if( tool.schemaBeingLockedOrNeedsUpdate(ctx) ) {
           throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
       }

       if (tool.existsResource(ctx, res.getName())) {
           throw new InvalidDataException("Resource with this name already exist");

       }

       if (!res.attributesforcreateset()) {
           throw new InvalidDataException("Mandatory fields not set");
           // TODO: cutmasta look here

       }

       if (prop.getResourceProp(AdminProperties.Resource.AUTO_LOWERCASE, true)) {
           final String uid = res.getName().toLowerCase();
           res.setName(uid);
       }

       if (prop.getResourceProp(AdminProperties.Resource.CHECK_NOT_ALLOWED_CHARS, true)) {
           try {
               validateResourceName(res.getName());
           } catch (final OXResourceException oxres) {
               throw new InvalidDataException("Invalid resource name");

           }
       }

       final OXResourceStorageInterface oxRes = OXResourceStorageInterface.getInstance();
       final int retval = oxRes.create(ctx, res);
       res.setId(retval);
       final ArrayList<OXResourcePluginInterface> interfacelist = new ArrayList<OXResourcePluginInterface>();

       final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
       for (final Bundle bundle : bundles) {
           final String bundlename = bundle.getSymbolicName();
           if (Bundle.ACTIVE==bundle.getState()) {
               final ServiceReference[] servicereferences = bundle.getRegisteredServices();
               if (null != servicereferences) {
                   for (final ServiceReference servicereference : servicereferences) {
                       final Object property = servicereference.getProperty("name");
                       if (null != property && property.toString().equalsIgnoreCase("oxresource")) {
                           final OXResourcePluginInterface oxresource = (OXResourcePluginInterface) this.context.getService(servicereference);
                           try {
                               if (log.isDebugEnabled()) {
                                   log.debug("Calling create for plugin: " + bundlename);
                               }
                               oxresource.create(ctx, res, auth);
                               interfacelist.add(oxresource);
                           } catch (final PluginException e) {
                               log.error("Error while calling create for plugin: " + bundlename, e);
                               log.error("Now doing rollback for everything until now...");
                               for (final OXResourcePluginInterface oxresourceinterface : interfacelist) {
                                   try {
                                       oxresourceinterface.delete(ctx, res, auth);
                                   } catch (final PluginException e1) {
                                       log.error("Error doing rollback for plugin: " + bundlename, e1);
                                   }
                               }
                               try {
                                   oxRes.delete(ctx, retval);
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
    }
    
    
    public void change(final Context ctx, final Resource res, final Credentials auth)
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchResourceException {
        
        doNullCheck(ctx,res,auth);
        
        if(res.getId()==null){
            throw new InvalidDataException();
        }
        
        doAuthentication(auth,ctx);
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + res.toString() + " - " + auth.toString());
        }
        
        final int resource_ID = res.getId();        
        
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();        

        if( tool.schemaBeingLockedOrNeedsUpdate(ctx) ) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }
        
        if (!tool.existsResource(ctx, resource_ID)) {
            throw new NoSuchResourceException("Resource with this id does not exists");
            
        }
        
        if ((null != res.getName()) && prop.getResourceProp(AdminProperties.Resource.AUTO_LOWERCASE, true)) {
            final String rid = res.getName().toLowerCase();
            res.setName(rid);
        }
        
        if ((null != res.getName()) && prop.getResourceProp(AdminProperties.Resource.CHECK_NOT_ALLOWED_CHARS, true)) {
            try {
                validateResourceName(res.getName());
            } catch (final OXResourceException xres) {
                throw new InvalidDataException("Invalid resource name");
                
            }
        }
        
        final OXResourceStorageInterface oxRes = OXResourceStorageInterface.getInstance();
        oxRes.change(ctx, res);
        
        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE==bundle.getState()) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase("oxresource")) {
                            final OXResourcePluginInterface oxresource = (OXResourcePluginInterface) this.context.getService(servicereference);
                            try {
                                if (log.isInfoEnabled()) {
                                    log.info("Calling change for plugin: " + bundlename);
                                }
                                oxresource.change(ctx, res, auth);
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
    
    public void delete(final Context ctx, final Resource res, final Credentials auth)
        throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchResourceException {
        
        doNullCheck(ctx,res,auth);
        
        if(res.getId()==null){
            throw new InvalidDataException();
        }
        
        doAuthentication(auth,ctx);        
        
        final int resource_id = res.getId();
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + resource_id + " - " + auth.toString());
        }
        
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        
        if( tool.schemaBeingLockedOrNeedsUpdate(ctx) ) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        if (!tool.existsResource(ctx, resource_id)) {
            throw new NoSuchResourceException("Resource with this id does not exist");
           
        }
        
        final OXResourceStorageInterface oxRes = OXResourceStorageInterface.getInstance();
        final ArrayList<OXResourcePluginInterface> interfacelist = new ArrayList<OXResourcePluginInterface>();

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
                        if (null != property && property.toString().equalsIgnoreCase("oxresource")) {
                            final OXResourcePluginInterface oxresource = (OXResourcePluginInterface) this.context.getService(servicereference);
                            try {
                                if (log.isInfoEnabled()) {
                                    log.info("Calling delete for plugin: " + bundlename);
                                }
                                oxresource.delete(ctx, res, auth);
                                interfacelist.add(oxresource);
                            } catch (final PluginException e) {
                                log.error("Error while calling delete for plugin: " + bundlename, e);
                                throw new StorageException(e);
                            }
                        }
                    }
                    
                }
            }
        }

        oxRes.delete(ctx, resource_id);
        
    }
    
    public Resource get(final Context ctx, final Resource res, final Credentials auth)
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchResourceException {
        
        doNullCheck(ctx,res,auth);
        
        if(res.getId()==null){
            throw new InvalidDataException();
        }
        
        doAuthentication(auth,ctx);        
        
        final int resource_id = res.getId();
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + resource_id + " - " + auth.toString());
        }
        
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        
        if( tool.schemaBeingLockedOrNeedsUpdate(ctx) ) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        if (!tool.existsResource(ctx, resource_id)) {
            throw new NoSuchResourceException("resource with with this id does not exist");           
        }
        
        final OXResourceStorageInterface oxRes = OXResourceStorageInterface.getInstance();
        Resource retres = oxRes.get(ctx, resource_id);

        // TODO: this code is superflous as soon as get takes resource as arg:
        for(OXResourceExtensionInterface or : res.getExtensions() ) {
            retres.addExtension(or);
        }
            
        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE==bundle.getState()) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase("oxresource")) {
                            final OXResourcePluginInterface oxresourceplugin = (OXResourcePluginInterface) this.context.getService(servicereference);
                            if (log.isInfoEnabled()) {
                                log.info("Calling getData for plugin: " + bundlename);
                            }
                            retres = oxresourceplugin.get(ctx, retres, auth);
                        }
                    }
                }
            }
        }

        return retres;
        
    }
    
    public Resource[] list(final Context ctx, final String pattern, final Credentials auth)
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException {
        
        doNullCheck(ctx,pattern,auth);
        
        if(pattern.length()==0){
            throw new InvalidDataException("Invalid pattern!");
        }        
        
        doAuthentication(auth,ctx);
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + pattern + " - " + auth.toString());
        }
        
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        
        if( tool.schemaBeingLockedOrNeedsUpdate(ctx) ) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

        final OXResourceStorageInterface oxRes = OXResourceStorageInterface.getInstance();
        return oxRes.list(ctx,pattern);
        
    }
    
    private void validateResourceName( String resName ) throws OXResourceException {
        if(resName==null || resName.trim().length()==0){
            throw new OXResourceException("Invalid resource name");
        }
        // Check for allowed chars:
        // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 _-+.%$@
        String resource_check_regexp = prop.getResourceProp("CHECK_RES_UID_REGEXP", "[ $@%\\.+a-zA-Z0-9_-]");        
        String illegal = resName.replaceAll(resource_check_regexp, "");
        if( illegal.length() > 0 ) {
            throw new OXResourceException( OXResourceException.ILLEGAL_CHARS + ": \""+illegal+"\"");
        }
    }

    public Resource[] getData(Context ctx, Resource[] resources, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchResourceException, DatabaseUpdateException {
        
        doNullCheck(ctx,resources,auth);
        
        doAuthentication(auth,ctx);
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + Arrays.toString(resources) + " - " + auth.toString());
        }
        
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        
        if( tool.schemaBeingLockedOrNeedsUpdate(ctx) ) {
            throw new DatabaseUpdateException("Database must be updated or currently is beeing updated");
        }

       // check if all resources exists
        for (Resource resource : resources) {
            if(resource.getId()!=null && !tool.existsResource(ctx, resource.getId().intValue())){
                throw new NoSuchResourceException("No such resource "+resource.getId().intValue());
            }
            if(resource.getName()!=null && !tool.existsResource(ctx, resource.getName())){
                throw new NoSuchResourceException("No such resource "+resource.getName());
            }
            if(resource.getName()==null && resource.getId()==null){
                throw new InvalidDataException("Resourcename and resourceid missing!Cannot resolve resource data");  
            }else{
                if(resource.getName()==null){
                    // resolv name by id
                    resource.setName(tool.getResourcenameByResourceID(ctx, resource.getId().intValue()));
                }
                if(resource.getId()==null){
                    resource.setId(tool.getResourceIDByResourcename(ctx, resource.getName()));
                }
            }
        }
        
        
        ArrayList<Resource> retval = new ArrayList<Resource>();
        
        final OXResourceStorageInterface oxRes = OXResourceStorageInterface.getInstance();
        
        for (Resource resource : resources) {
            // not nice, but works ;)
            Resource tmp = oxRes.get(ctx, resource.getId().intValue());
            for(OXResourceExtensionInterface or : resource.getExtensions() ) {
                tmp.addExtension(or);
            }
            retval.add(tmp);
        }        
            
        final ArrayList<Bundle> bundles = AdminDaemon.getBundlelist();
        for (final Bundle bundle : bundles) {
            final String bundlename = bundle.getSymbolicName();
            if (Bundle.ACTIVE==bundle.getState()) {
                final ServiceReference[] servicereferences = bundle.getRegisteredServices();
                if (null != servicereferences) {
                    for (final ServiceReference servicereference : servicereferences) {
                        final Object property = servicereference.getProperty("name");
                        if (null != property && property.toString().equalsIgnoreCase("oxresource")) {
                            final OXResourcePluginInterface oxresourceplugin = (OXResourcePluginInterface) this.context.getService(servicereference);
                            if (log.isInfoEnabled()) {
                                log.info("Calling get for plugin: " + bundlename);
                            }
                            for (Resource resource : retval) {
                                resource = oxresourceplugin.get(ctx, resource, auth);
                            }
                        }
                    }
                }
            }
        }
        return (Resource[])retval.toArray(new Resource[retval.size()]);
       
    }
}
