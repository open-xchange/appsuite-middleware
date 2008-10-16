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

package com.openexchange.admin.reseller.rmi.impl;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.reseller.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.reseller.storage.interfaces.OXResellerStorageInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.BasicAuthenticator;
import com.openexchange.admin.rmi.impl.OXCommonImpl;
import com.openexchange.admin.tools.GenericChecks;

/**
 * @author choeger
 *
 */
public class OXReseller extends OXCommonImpl implements OXResellerInterface {

    private final static Log log = LogFactory.getLog(OXReseller.class);
    
    private final BasicAuthenticator basicauth;

    private final ResellerAuth resellerauth;

    private final OXResellerStorageInterface oxresell;

    private HashMap<String, Restriction> validRestrictions = null;
    
    public OXReseller() throws StorageException {
        super();
        if (log.isDebugEnabled()) {
            log.debug("Class loaded: " + this.getClass().getName());
        }
        basicauth = new BasicAuthenticator();
        resellerauth = new ResellerAuth();
        try {
            oxresell = OXResellerStorageInterface.getInstance();
        } catch (StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Checks whether either id or name is specified
     * 
     * @param adm
     * @throws InvalidDataException
     */
    private void checkIdOrName(final ResellerAdmin adm) throws InvalidDataException {
        if( adm.getId() == null && adm.getName() == null ) {
            throw new InvalidDataException("either ID or name must be specified");
        }
    }

    /**
     * load existing {@link Restriction} definitions from database into the 
     * validRestrictions {@link HashMap}
     * 
     * @throws StorageException
     */
    private void initRestrictions() throws StorageException {
        if( this.validRestrictions == null ) {
            validRestrictions = oxresell.listRestrictions("*");
            if( validRestrictions == null ) {
                throw new StorageException("unable to load available restrictions from database");
            }
        }
    }
    
    /**
     * Check whether creator supplied any {@link Restriction} and check if those exist
     * within the database. If, add the corresponding Restriction id.
     * Check whether Restrictions can be applied to context.
     * If not, throw {@link InvalidDataException} or {@link StorageException} if there
     * are no Restrictions defined within the database.
     * 
     * @param restrictions
     * @throws StorageException
     * @throws InvalidDataException 
     */
    private void checkRestrictionsPerContext(final HashSet<Restriction> restrictions) throws StorageException, InvalidDataException {
        initRestrictions();
        
        final Iterator<Restriction> i = restrictions.iterator();
        while( i.hasNext() ) {
            Restriction r = i.next();
            final String rname = r.getName();
            final String rval  = r.getValue();
            if( rname == null ) {
                throw new InvalidDataException("Restriction name must be set");
            }
            if( !( rname.equals(Restriction.MAX_USER_PER_CONTEXT) )) {
                 throw new InvalidDataException("Restriction " + rname + " cannot be applied to context");
             }
            if( ! validRestrictions.containsKey(rname) ) {
                throw new InvalidDataException("No restriction named " + rname + " found in database");
            }
            if( rval == null ) {
                throw new InvalidDataException("Restriction value must be set");
            }
            r.setId(validRestrictions.get(rname).getId());
        }
    }
    
    /**
     * Check whether creator supplied any {@link Restriction} and check if those exist
     * within the database. If, add the corresponding Restriction id.
     * Check whether Restrictions can be applied to subadmin.
     * If not, throw {@link InvalidDataException} or {@link StorageException} if there
     * are no Restrictions defined within the database.
     * 
     * @throws StorageException
     * @throws InvalidDataException 
     */
    private void checkRestrictionsPerSubadmin(ResellerAdmin adm) throws StorageException, InvalidDataException {
        initRestrictions();
        
        HashSet<Restriction> res = adm.getRestrictions();
        if( res != null ) {
            Restriction[] rarr = res.toArray(new Restriction[res.size()]);
            for(int i=0; i<res.size(); i++) {
                final String rname = rarr[i].getName();
                final String rval  = rarr[i].getValue();
                if( rname == null ) {
                    throw new InvalidDataException("Restriction name must be set");
                }
                if( !( rname.equals(Restriction.MAX_CONTEXT_PER_SUBADMIN) ||
                       rname.equals(Restriction.MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN) ||
                       rname.equals(Restriction.MAX_OVERALL_USER_PER_SUBADMIN) ) ) {
                    throw new InvalidDataException("Restriction " + rname + " cannot be applied to subadmin");
                }
                if( rval == null ) {
                    throw new InvalidDataException("Restriction value must be set");
                }
                if( !validRestrictions.containsKey(rname) ) {
                    throw new InvalidDataException("No restriction named " + rname + " found in database");
                }
                rarr[i].setId(validRestrictions.get(rname).getId());
            }
        }
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#change(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void change(ResellerAdmin adm, Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        try {
            doNullCheck(adm);
        } catch (final InvalidDataException e) {            
            log.error("Invalid data sent by client!", e);
            throw e;
        }
        
        try {
            basicauth.doAuthentication(creds);

            checkIdOrName(adm);

            if( adm.getId()!= null && !oxresell.existsAdmin(adm) ) {
                throw new OXResellerException(OXResellerException.RESELLER_ADMIN_NOT_EXIST + ": " + adm.getName());
            }

            GenericChecks.checkChangeValidPasswordMech(adm);

            // if no password mech supplied, use the old one as set in db
            final ResellerAdmin dbadm = oxresell.getData(new ResellerAdmin[]{adm})[0];
            if( adm.getPasswordMech() == null ) {
                adm.setPasswordMech(dbadm.getPasswordMech());
            }

            final String newname = adm.getName();
            final String dbname  = dbadm.getName();

            if( newname != null && adm.getId() != null && !newname.equals(dbname) ) {
                // want to change name?, check whether new name does already
                // exist
                if( oxresell.existsAdmin(new ResellerAdmin(newname)) ) {
                    throw new OXResellerException(OXResellerException.RESELLER_ADMIN_EXISTS + ": " + adm.getName());
                }
            }

            checkRestrictionsPerSubadmin(dbadm);
            oxresell.change(adm);
        } catch (InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        } catch (StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (OXResellerException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#create(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public ResellerAdmin create(ResellerAdmin adm, Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException, OXResellerException {
        try {
            doNullCheck(adm);
        } catch (final InvalidDataException e) {            
            log.error("Invalid data sent by client!", e);
            throw e;
        }
        
        try {
            basicauth.doAuthentication(creds);

            if( oxresell.existsAdmin(adm) ) {
                throw new OXResellerException(OXResellerException.RESELLER_ADMIN_EXISTS + ": " + adm.getName());
            }

            GenericChecks.checkCreateValidPasswordMech(adm);
            if (adm.getPassword() != null && adm.getPassword().trim().length() == 0) {
                throw new InvalidDataException("Empty password is not allowed");
            }

            //TODO: parent id must be the ID of the creator
            adm.setParentId(0);

            checkRestrictionsPerSubadmin(adm);
            
            return oxresell.create(adm);
        } catch(final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (OXResellerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#delete(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void delete(ResellerAdmin adm, Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        try {
            doNullCheck(adm);
        } catch (final InvalidDataException e) {            
            log.error("Invalid data sent by client!", e);
            throw e;
        }
        
        try {
            basicauth.doAuthentication(creds);

            checkIdOrName(adm);
            
            if( !oxresell.existsAdmin(adm) ) {
                throw new OXResellerException(OXResellerException.RESELLER_ADMIN_NOT_EXIST + ": " + adm.getName());
            }
            if( oxresell.ownsContext(null, new Credentials(adm.getName(),null)) ) {
                throw new OXResellerException("Unable to delete " + adm.getName() + ", still owns Context(s)");
            }
            oxresell.delete(adm);
        } catch (InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        } catch (StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (OXResellerException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#list(java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public ResellerAdmin[] list(String search_pattern, Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException {
        try {
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e) {            
            log.error("Invalid data sent by client!", e);
            throw e;
        }
        
        try {
            basicauth.doAuthentication(creds);

            return oxresell.list(search_pattern);
        } catch (StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * throws {@link InvalidDataException} when neither id nor name supplied
     * 
     * @param admins
     * @throws InvalidDataException
     */
    private void checkAdminIdOrName(final ResellerAdmin[] admins) throws InvalidDataException {
        for(final ResellerAdmin adm : admins ) {
            if( adm == null ) {
                throw new InvalidDataException("cannot handle null object");
            }
            final Integer id = adm.getId();
            final String name = adm.getName();
            if( id == null && name == null ) {
                throw new InvalidDataException("either ID or name must be specified: " +
                        id != null ? "id=" + id : "" + name != null ? "name=" + name : "");
            }
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#getData(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin[])
     */
    public ResellerAdmin[] getMultipleData(ResellerAdmin[] admins, Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        try {
            doNullCheck((Object[])admins);
            if (admins.length <= 0) {
                throw new InvalidDataException();
            }
        } catch (final InvalidDataException e) {            
            log.error("Invalid data sent by client!", e);
            throw e;
        }
        
        try {
            basicauth.doAuthentication(creds);
            checkAdminIdOrName(admins);
            if( !oxresell.existsAdmin(admins) ) {
                throw new OXResellerException(OXResellerException.RESELLER_ADMIN_NOT_EXIST);
            }
            
            return oxresell.getData(admins);
        } catch (InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (OXResellerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#getData(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public ResellerAdmin getData(ResellerAdmin adm, Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        return getMultipleData(new ResellerAdmin[]{adm}, creds)[0];
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#applyRestrictionsToContext(java.util.HashSet, com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void applyRestrictionsToContext(HashSet<Restriction> restrictions, Context ctx, Credentials creds) throws InvalidDataException, StorageException, InvalidCredentialsException, OXResellerException {
        try {
            doNullCheck(ctx);
            doNullCheck(ctx.getId());
        } catch (final InvalidDataException e) {            
            log.error("Invalid data sent by client!", e);
            throw e;
        }
        
        try {
            final boolean isMasterAdmin = ClientAdminThreadExtended.cache.isMasterAdmin(creds);
            if( isMasterAdmin ) {
                basicauth.doAuthentication(creds);
            } else {
                resellerauth.doAuthentication(creds);
            }

            if( !isMasterAdmin && ! oxresell.ownsContext(ctx, creds) ) {
                throw new OXResellerException("ContextID " + ctx.getId() + " does not belong to " + creds.getLogin());
            }

            checkRestrictionsPerContext(restrictions);
            
            oxresell.applyRestrictionsToContext(restrictions, ctx);
        } catch (InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        } catch (StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#getRestrictionsFromContext(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public HashSet<Restriction> getRestrictionsFromContext(Context ctx, Credentials creds) throws RemoteException, InvalidDataException, OXResellerException, StorageException, InvalidCredentialsException {
        try {
            doNullCheck(ctx);
            doNullCheck(ctx.getId());
        } catch (final InvalidDataException e) {            
            log.error("Invalid data sent by client!", e);
            throw e;
        }
        
        try {
            final boolean isMasterAdmin = ClientAdminThreadExtended.cache.isMasterAdmin(creds);
            if( isMasterAdmin ) {
                basicauth.doAuthentication(creds);
            } else {
                resellerauth.doAuthentication(creds);
            }

            if( !isMasterAdmin && ! oxresell.ownsContext(ctx, creds) ) {
                throw new OXResellerException("ContextID " + ctx.getId() + " does not belong to " + creds.getLogin());
            }

            return oxresell.getRestrictionsFromContext(ctx);
        } catch (StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

}
