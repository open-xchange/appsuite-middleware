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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.exceptions.OXResourceException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.BasicAuthenticator;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXResourceStorageInterface;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;

public class OXResource extends BasicAuthenticator implements OXResourceInterface{
    
    
    private static final long serialVersionUID = -7012370962672596682L;
    
    private AdminCache cache = null;
    
    private PropertyHandler prop = null;
    
    private final Log log = LogFactory.getLog(this.getClass());    
    
    
    public OXResource() throws RemoteException {
        super();
        cache = ClientAdminThread.cache;
        prop = cache.getProperties();        
        log.info("Class loaded: " + this.getClass().getName());
        
    }
    
   public int create(final Context ctx, final Resource res, final Credentials auth)
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {        
        
        if(ctx==null||res==null){
            throw new InvalidDataException();
        }
        
        doAuthentication(auth,ctx);
        
        log.debug(ctx.toString() + " - " + res.toString()+" - "+auth.toString()); 
        
        //try {
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
            
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
        return oxRes.create(ctx, res);
        
    }
    
    
    public void change(final Context ctx, final Resource res, final Credentials auth)
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {
                
        if(ctx==null||res==null || res.getId()==null){
            throw new InvalidDataException();
        }
        
        doAuthentication(auth,ctx);
        
        log.debug(ctx.toString() + " - " + res.toString()+" - "+auth.toString());
                
        final int resource_ID = res.getId();        
        
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
           
        }
        
        if (!tool.existsResource(ctx, resource_ID)) {
            throw new InvalidDataException("Resource with this id does not exists");
            
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
        
    }
    
    public void delete(final Context ctx, final Resource res, final Credentials auth)
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {
        
        if(ctx==null||res==null||res.getId()==null){
            throw new InvalidDataException();
        }
        
        doAuthentication(auth,ctx);
        final int resource_id = res.getId();
        log.debug(ctx.toString() + " - " + resource_id+" - "+auth.toString());
        
        
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
           
        }
        
        if (!tool.existsResource(ctx, resource_id)) {
            throw new InvalidDataException("Resource with this id does not exist");
           
        }
        
        final OXResourceStorageInterface oxRes = OXResourceStorageInterface.getInstance();
        oxRes.delete(ctx, resource_id);
        
    }
    
    public Resource get(final Context ctx, final Resource res, final Credentials auth)
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {
        
        if(ctx==null||res==null||res.getId()==null){
            throw new InvalidDataException();
        }
        
        doAuthentication(auth,ctx);
        final int resource_id = res.getId();
        log.debug(ctx.toString() + " - " + resource_id+" - "+auth.toString());
        
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();           
        }
        
        if (!tool.existsResource(ctx, resource_id)) {
            throw new InvalidDataException("resource with with this id does not exist");           
        }
        
        final OXResourceStorageInterface oxRes = OXResourceStorageInterface.getInstance();
        return oxRes.get(ctx, resource_id);
        
    }
    
    public Resource[] list(final Context ctx, final String pattern, final Credentials auth)
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {
        
        if(ctx==null){
            throw new InvalidDataException();
        }
        
        doAuthentication(auth,ctx);
        
        log.debug(ctx.toString() + " - " + pattern+" - "+auth.toString());
        
        if(pattern==null || pattern.length()==0){
            throw new InvalidDataException("Invalid pattern!");
        }
        
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();            
        }
        
        final OXResourceStorageInterface oxRes = OXResourceStorageInterface.getInstance();
        return oxRes.list(ctx,pattern);
        
    }
    
    private void validateResourceName( String resName ) throws OXResourceException {
        // Check for allowed chars:
        // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 _-+.%$@
        String illegal = resName.replaceAll("[ $@%\\.+a-zA-Z0-9_-]", "");
        if( illegal.length() > 0 ) {
            throw new OXResourceException( OXResourceException.ILLEGAL_CHARS + ": \""+illegal+"\"");
        }
    }
}
