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
import com.openexchange.admin.plugins.OXResourcePluginInterface;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXResourceStorageInterface;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.GenericChecks;
import com.openexchange.admin.tools.PropertyHandler;

public class OXResource extends OXCommonImpl implements OXResourceInterface{
    
    private static final long serialVersionUID = -7012370962672596682L;

    private static final Log log = LogFactory.getLog(OXResource.class);    
    
    private final OXToolStorageInterface tool;
    
    private final BasicAuthenticator basicauth;
    
    private final OXResourceStorageInterface oxRes;
    
    private AdminCache cache = null;

    private BundleContext context = null;
    
    private PropertyHandler prop = null;
    
    public OXResource(final BundleContext context) throws RemoteException {
        super();
        this.context = context;
        cache = ClientAdminThread.cache;
        prop = cache.getProperties();     
        if (log.isInfoEnabled()) {
            log.info("Class loaded: " + this.getClass().getName());
        }
        try {
            tool = OXToolStorageInterface.getInstance();
            oxRes = OXResourceStorageInterface.getInstance();
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage());
        }
        basicauth = new BasicAuthenticator();
    }

    public void change(final Context ctx, final Resource res, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchResourceException {
        try {
            doNullCheck(res);
        } catch (final InvalidDataException e3) {
            final InvalidDataException invalidDataException = new InvalidDataException("One of the given arguments for change is null");
            log.error(invalidDataException.getMessage(), invalidDataException);
            throw invalidDataException;
        }        
        
        try {           
            basicauth.doAuthentication(auth,ctx);
        } catch (final InvalidDataException e1) {
            log.error(e1.getMessage(), e1);
            throw e1;
        }
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + res.toString() + " - " + auth.toString());
        }
        
        try {
            setIdOrGetIDFromResourcename(ctx, res);
            final int resource_ID = res.getId();        
            
            checkSchemaBeingLocked(ctx, tool);
            
            if (!tool.existsResource(ctx, resource_ID)) {
                throw new NoSuchResourceException("Resource with this id does not exists");
            }
            
            if (res.getEmail() != null && tool.existsResourceAddress(ctx, res.getEmail(), res.getId())) {
                throw new InvalidDataException("Resource with this email address already exists");
            }

            if ((null != res.getName()) && prop.getResourceProp(AdminProperties.Resource.AUTO_LOWERCASE, true)) {
                final String rid = res.getName().toLowerCase();
                res.setName(rid);
            }

            if ((null != res.getName()) && prop.getResourceProp(AdminProperties.Resource.CHECK_NOT_ALLOWED_CHARS, true)) {
                validateResourceName(res.getName());
            }

            final String resmail = res.getEmail();
            if (resmail != null && resmail.trim().length() > 0 && !GenericChecks.isValidMailAddress(resmail)) {
                throw new InvalidDataException("Invalid email address");
            }
        } catch (final InvalidDataException e1) {
            log.error(e1.getMessage(), e1);
            throw e1;
        } catch (final StorageException e1) {
            log.error(e1.getMessage(), e1);
            throw e1;
        } catch (final DatabaseUpdateException e1) {
            log.error(e1.getMessage(), e1);
            throw e1;
        } catch (final NoSuchContextException e1) {
            log.error(e1.getMessage(), e1);
            throw e1;
        } catch (final NoSuchResourceException e1) {
            log.error(e1.getMessage(), e1);
            throw e1;
        }

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
    
    public Resource create(final Context ctx, final Resource res, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException {        
       try {
           doNullCheck(res);
       } catch (final InvalidDataException e3) {
           log.error("One of the given arguments for create is null", e3);
           throw e3;
       }        
       
       try {
           basicauth.doAuthentication(auth,ctx);
       } catch( final InvalidDataException e) {
           log.error(e.getMessage(), e);
           throw e;
       }
      
       if (log.isDebugEnabled()) {
           log.debug(ctx.toString() + " - " + res.toString() + " - " + auth.toString()); 
       }

       checkSchemaBeingLocked(ctx, tool);

       try {
           if (tool.existsResource(ctx, res.getName())) {
                throw new InvalidDataException("Resource with this name already exists");
            }

            if (res.getEmail() != null && tool.existsResourceAddress(ctx, res.getEmail())) {
                throw new InvalidDataException("Resource with this email address already exists");
            }

            if (!res.mandatoryCreateMembersSet()) {
                throw new InvalidDataException("Mandatory fields not set: " + res.getUnsetMembers());
                // TODO: cutmasta look here
            }

            if (prop.getResourceProp(AdminProperties.Resource.AUTO_LOWERCASE, true)) {
                final String uid = res.getName().toLowerCase();
                res.setName(uid);
            }

            if (prop.getResourceProp(AdminProperties.Resource.CHECK_NOT_ALLOWED_CHARS, true)) {
                validateResourceName(res.getName());
            }

            final String resmail = res.getEmail();
            if (resmail != null && !GenericChecks.isValidMailAddress(resmail)) {
                throw new InvalidDataException("Invalid email address");
            }
        } catch (final InvalidDataException e2) {
            log.error(e2.getMessage(), e2);
            throw e2;
        } catch (EnforceableDataObjectException e) {
            throw new InvalidDataException(e.getMessage());
        }
       
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
                               log.info("Now doing rollback for everything until now...");
                               for (final OXResourcePluginInterface oxresourceinterface : interfacelist) {
                                   try {
                                       oxresourceinterface.delete(ctx, res, auth);
                                   } catch (final PluginException e1) {
                                       log.error("Error doing rollback for plugin: " + bundlename, e1);
                                   }
                               }
                               try {
                                   oxRes.delete(ctx, res);
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

       return res;
    }
    
    public void delete(final Context ctx, final Resource res, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchResourceException {
        try {
            doNullCheck(res);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for delete is null", e3);
            throw e3;
        }        
        
        try {
            basicauth.doAuthentication(auth,ctx);
            
            setIdOrGetIDFromResourcename(ctx, res);
            if (log.isDebugEnabled()) {
                log.debug(ctx.toString() + " - " + res + " - " + auth.toString());
            }
            checkSchemaBeingLocked(ctx, tool);
            if (!tool.existsResource(ctx, res.getId())) {
                throw new NoSuchResourceException("Resource with this id does not exist");
            }
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
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
        } catch (final NoSuchResourceException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
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

        oxRes.delete(ctx, res);
    }
    
    public Resource get(final Context ctx, final Resource res, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchResourceException {
        return getData(ctx, res, auth);
    }

    public Resource getData(final Context ctx, final Resource res, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchResourceException {
        try {
            doNullCheck(res,res.getId());
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for get is null", e3);
            throw e3;
        }        
        
        try {
            basicauth.doAuthentication(auth,ctx);
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }        
        
        final int resource_id = res.getId();
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + resource_id + " - " + auth.toString());
        }
        
        checkSchemaBeingLocked(ctx, tool);
        
        if (!tool.existsResource(ctx, resource_id)) {
            throw new NoSuchResourceException("resource with with this id does not exist");           
        }
        
        Resource retres = oxRes.getData(ctx, res);

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
    
    public Resource[] getData(final Context ctx, final Resource[] resources, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchResourceException, DatabaseUpdateException {
        try {
            doNullCheck((Object[])resources);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for getData is null", e3);
            throw e3;
        }        
        
        try {
            basicauth.doAuthentication(auth,ctx);
        
            if (log.isDebugEnabled()) {
                log.debug(ctx.toString() + " - " + Arrays.toString(resources) + " - " + auth.toString());
            }

            checkSchemaBeingLocked(ctx, tool);

            // check if all resources exists
            for (final Resource resource : resources) {
                if (resource.getId() != null && !tool.existsResource(ctx, resource.getId().intValue())) {
                    throw new NoSuchResourceException("No such resource " + resource.getId().intValue());
                }
                if (resource.getName() != null && !tool.existsResource(ctx, resource.getName())) {
                    throw new NoSuchResourceException("No such resource " + resource.getName());
                }
                if (resource.getName() == null && resource.getId() == null) {
                    throw new InvalidDataException("Resourcename and resourceid missing!Cannot resolve resource data");
                } else {
                    if (resource.getName() == null) {
                        // resolv name by id
                        resource.setName(tool.getResourcenameByResourceID(ctx, resource.getId().intValue()));
                    }
                    if (resource.getId() == null) {
                        resource.setId(tool.getResourceIDByResourcename(ctx, resource.getName()));
                    }
                }
            }
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        
        final ArrayList<Resource> retval = new ArrayList<Resource>();
        
        for (final Resource resource : resources) {
            // not nice, but works ;)
            final Resource tmp = oxRes.getData(ctx, resource);
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
        return retval.toArray(new Resource[retval.size()]);
    }
    
    public Resource[] list(final Context ctx, final String pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException {
        try {
            doNullCheck(pattern);
        } catch (final InvalidDataException e3) {
            log.error("One of the given arguments for list is null", e3);
            throw e3;
        }        
        
        try {
            if(pattern.length()==0){
                throw new InvalidDataException("Invalid pattern!");
            }        
            
            basicauth.doAuthentication(auth,ctx);
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        
        if (log.isDebugEnabled()) {
            log.debug(ctx.toString() + " - " + pattern + " - " + auth.toString());
        }
        
        checkSchemaBeingLocked(ctx, tool);

        return oxRes.list(ctx,pattern);
    }
    
    public Resource[] listAll(final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException {
        return list(ctx, "*", auth);
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
            final DatabaseUpdateException databaseUpdateException = new DatabaseUpdateException(
                    "Database must be updated or currently is beeing updated");
            log.error(databaseUpdateException.getMessage(), databaseUpdateException);
            throw databaseUpdateException;
        }
    }
    
    private void validateResourceName(final String resName) throws InvalidDataException {
        if(resName==null || resName.trim().length()==0){
            throw new InvalidDataException("Invalid resource name");
        }
        // Check for allowed chars:
        // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 _-+.%$@
        final String resource_check_regexp = prop.getResourceProp("CHECK_RES_UID_REGEXP", "[ $@%\\.+a-zA-Z0-9_-]");        
        final String illegal = resName.replaceAll(resource_check_regexp, "");
        if( illegal.length() > 0 ) {
            throw new InvalidDataException( "Illegal chars: \""+illegal+"\"");
        }
    }
    
    private void setIdOrGetIDFromResourcename(final Context ctx, final Resource res) throws StorageException, InvalidDataException {
        final Integer id = res.getId();
        if (null == id) {
            final String resourcename = res.getName();
            if (null != resourcename) {
                res.setId(tool.getResourceIDByResourcename(ctx, resourcename));
            } else {
                throw new InvalidDataException("One resource object has no id or username");
            }
        }
    }
}
