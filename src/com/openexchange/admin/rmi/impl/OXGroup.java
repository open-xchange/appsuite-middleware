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
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.exceptions.OXGroupException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.BasicAuthenticator;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
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

    public OXGroup() throws RemoteException {
        super();
        cache = ClientAdminThread.cache;
        prop = cache.getProperties();
        log.info("Class loaded: " + this.getClass().getName());
    }

   
    public int create(final Context ctx, final Group grp, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {

        if(ctx==null || grp==null){
            throw new InvalidDataException();
        }          
        
        doAuthentication(auth,ctx);
        
        log.debug(""+ctx.toString()+" - "+grp.toString()+" - "+auth.toString());

        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
        }
        if (!grp.attributesforcreateset()) {
            throw new InvalidDataException("Mandatory fields not set");           
        }

        if (prop.getGroupProp(AdminProperties.Group.AUTO_LOWERCASE, true)) {
            grp.setName(grp.getName().toLowerCase());
        }

        if (prop.getGroupProp(AdminProperties.Group.CHECK_NOT_ALLOWED_CHARS, true)) {
            try {
                validateGroupName(grp.getName());
            } catch (final OXGroupException ox) {
                throw new InvalidDataException("Invalid group name!");
            }
        }

        if (tool.existsGroup(ctx, grp.getName())) {
            throw new InvalidDataException("Group already exists!");
        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        return oxGroup.create(ctx, grp);
        // MonitoringInfos.incrementNumberOfCreateGroupCalled();

    }
  
    public Group[] list(final Context ctx, final String pattern, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {
    
        if(ctx==null){
            throw new InvalidDataException();
        }
        
        doAuthentication(auth,ctx);
        
        log.debug(""+ctx.toString()+" - "+pattern+" - "+auth.toString());
        
        
        if(pattern==null){
            throw new InvalidDataException("Invalid search pattern!");
        }
        
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();          
        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        return oxGroup.list(ctx, pattern);

    }

  
    public Group get(final Context ctx, final Group grp, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {

        if(ctx==null){
            throw new InvalidDataException();
        }
        
        doAuthentication(auth,ctx);
        
        final int grp_id = grp.getId();
        log.debug(""+ctx.toString()+" - "+grp_id+" - "+auth.toString());

        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
            
        }

        if (!tool.existsGroup(ctx, grp_id)) {
            throw new InvalidDataException("No such group");
            
        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        return oxGroup.get(ctx, grp);

    }

  
    public void change(final Context ctx, final Group grp, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {
        
        if(ctx==null || grp==null){
            throw new InvalidDataException();
        }
        
        doAuthentication(auth,ctx);
        
        log.debug(""+ctx.toString()+" - "+grp.toString()+" - "+auth.toString());
        
        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();           
        }

        if (!grp.attributesforchangeset()) {
            throw new InvalidDataException("Mandatory fields not set");           
        }

        if (grp.getName() != null && prop.getGroupProp(AdminProperties.Group.AUTO_LOWERCASE, true)) {
            grp.setName(grp.getName().toLowerCase());
        }

        if (grp.getName() != null && prop.getGroupProp(AdminProperties.Group.CHECK_NOT_ALLOWED_CHARS, true)) {
            try {
                validateGroupName(grp.getName());
            } catch (final OXGroupException ox) {
                throw new InvalidDataException("Invalid group name");
            }
        }

        if (!tool.existsGroup(ctx, grp.getId())) {
            throw new InvalidDataException("No such group");           
        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        oxGroup.change(ctx, grp);

    }

   
    public void addMember(final Context ctx, final Group grp, final int[] member_ids, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {

        if(ctx==null || member_ids==null){
            throw new InvalidDataException();
        }
        
        doAuthentication(auth,ctx);
        final int grp_id = grp.getId();
        log.debug(""+ctx.toString()+" - "+grp_id+" - "+Arrays.toString(member_ids)+" - "+auth.toString());
                

        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();           
        }

        if (!tool.existsGroup(ctx, grp_id)) {
            throw new InvalidDataException("No such group");
        }

        if (!tool.existsUser(ctx, member_ids)) {
            throw new InvalidDataException("No such user");
        }

        if (tool.existsGroupMember(ctx, grp_id, member_ids)) {
            throw new InvalidDataException("Member already exists in group");
        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        oxGroup.addMember(ctx, grp_id, member_ids);
       
    }

    
    public void removeMember(final Context ctx, final Group grp, final int[] member_ids, final Credentials auth)
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {

        if(ctx==null || member_ids==null){
            throw new InvalidDataException();
        }
        
        doAuthentication(auth,ctx);
        final int grp_id = grp.getId();
        log.debug(""+ctx.toString()+" - "+grp_id+" - "+Arrays.toString(member_ids)+" - "+auth.toString());

        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();

        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
            
        }

        if (!tool.existsUser(ctx, member_ids)) {
            throw new InvalidDataException("No such user");
            
        }

        if (!tool.existsGroup(ctx, grp_id)) {
            throw new InvalidDataException("No such group");
        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        oxGroup.removeMember(ctx, grp_id, member_ids);

    }

   
    public void delete(final Context ctx, final Group grp, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        if (null == grp) {
            throw new InvalidDataException();
        }
        delete(ctx, new Group[]{grp}, auth);
    }


    public void delete(final Context ctx, final Group[] grp, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {

        if(ctx==null || grp==null){
            throw new InvalidDataException();
        }
        
        doAuthentication(auth,ctx);
        
        log.debug(ctx.toString() + " - " +Arrays.toString(grp)+" - "+auth.toString());

        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
        }

        final int[] grp_ids = new int[grp.length];
        int i = 0;
        for (final Group elem : grp) {
            // should we allow of deleting the users group?
            final int grp_id = elem.getId();
            if (1 == grp_id) {
                throw new InvalidDataException("Group with id " + grp_id + " cannot be deleted");
            }
            grp_ids[i++] = grp_id;
        }

        if (!tool.existsGroup(ctx, grp_ids)) {
            throw new InvalidDataException("No such group");
        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        oxGroup.delete(ctx, grp);

    }
   
    public int[] getMembers(final Context ctx, final Group grp, final Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException {

        if(ctx==null){
            throw new InvalidDataException();
        }
        
        doAuthentication(auth,ctx);
        final int grp_id = grp.getId();
        log.debug(ctx.toString() + " - " + grp_id+" - "+auth.toString());

        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.existsContext(ctx)) {
            throw new NoSuchContextException();
           
        }

        if (!tool.existsGroup(ctx, grp_id)) {
            throw new InvalidDataException("No such group");
            
        }

        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
        return oxGroup.getMembers(ctx, grp_id);

    }

    private void validateGroupName( final String groupName ) throws OXGroupException {
        // Check for allowed chars:
        // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 _-+.%$@
        final String illegal = groupName.replaceAll("[ $@%\\.+a-zA-Z0-9_-]", "");
        if( illegal.length() > 0 ) {
            throw new OXGroupException( OXGroupException.ILLEGAL_CHARS + ": \""+illegal+"\"");
        }
    }


	public Group[] getGroupsForUser(Context ctx, User usr, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
		 	
			if(ctx==null){
	            throw new InvalidDataException();
		 	}
	        
	        doAuthentication(auth,ctx);
	       
	        log.debug(ctx.toString() + " - " + usr.getId().intValue()+" - "+auth.toString());
	        
	        final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
	        if (!tool.existsContext(ctx)) {
	            throw new NoSuchContextException();
	           
	        }
	        
	        if(!tool.existsUser(ctx, usr.getId().intValue())){
	        	throw new InvalidDataException("No such user");
	        }
	        
	        final OXGroupStorageInterface oxGroup = OXGroupStorageInterface.getInstance();
	        return oxGroup.getGroupsForUser(ctx, usr);
	        
	}
}
