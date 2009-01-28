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
import java.util.Map;
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
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.BasicAuthenticator;
import com.openexchange.admin.rmi.impl.OXCommonImpl;
import com.openexchange.admin.tools.GenericChecks;

/**
 * @author choeger
 */
public class OXReseller extends OXCommonImpl implements OXResellerInterface {

    private final static Log log = LogFactory.getLog(OXReseller.class);

    private final BasicAuthenticator basicauth;

    private final ResellerAuth resellerauth;

    private final OXResellerStorageInterface oxresell;

    public OXReseller() throws StorageException {
        super();
        if (log.isDebugEnabled()) {
            log.debug("Class loaded: " + this.getClass().getName());
        }
        basicauth = new BasicAuthenticator();
        resellerauth = new ResellerAuth();
        try {
            oxresell = OXResellerStorageInterface.getInstance();
        } catch (final StorageException e) {
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
        if (adm.getId() == null && adm.getName() == null) {
            throw new InvalidDataException("either ID or name must be specified");
        }
    }

    /**
     * Check whether creator supplied any {@link Restriction} and check if those exist within the database. If, add the corresponding
     * Restriction id. Check whether Restrictions can be applied to context. If not, throw {@link InvalidDataException} or
     * {@link StorageException} if there are no Restrictions defined within the database. Check whether Restrictions contain duplicate
     * Restriction entries and throws {@link InvalidDataException} if that is the case.
     * 
     * @param restrictions
     * @throws StorageException
     * @throws InvalidDataException
     * @throws OXResellerException
     */
    private void checkRestrictionsPerContext(final HashSet<Restriction> restrictions) throws StorageException, InvalidDataException, OXResellerException {
        final Map<String, Restriction> validRestrictions = oxresell.listRestrictions("*");
        if (validRestrictions == null || validRestrictions.size() <= 0) {
            throw new OXResellerException("unable to load available restrictions from database");
        }

        final Iterator<Restriction> i = restrictions.iterator();
        final HashSet<String> dupcheck = new HashSet<String>();
        while (i.hasNext()) {
            final Restriction r = i.next();
            final String rname = r.getName();
            if (dupcheck.contains(rname)) {
                throw new InvalidDataException("Duplicate entry for restriction \"" + rname + "\"");
            }
            dupcheck.add(rname);
            final String rval = r.getValue();
            if (rname == null) {
                throw new InvalidDataException("Restriction name must be set");
            }
            if (!(rname.equals(Restriction.MAX_USER_PER_CONTEXT) || rname.startsWith(Restriction.MAX_OVERALL_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX))) {
                throw new InvalidDataException("Restriction " + rname + " cannot be applied to context");
            }
            if (!validRestrictions.containsKey(rname)) {
                throw new InvalidDataException("No restriction named " + rname + " found in database");
            }
            if (rval == null) {
                throw new InvalidDataException("Restriction value must be set");
            }
            r.setId(validRestrictions.get(rname).getId());
        }
    }

    /**
     * Check whether creator supplied any {@link Restriction} and check if those exist within the database. If, add the corresponding
     * Restriction id. Check whether Restrictions can be applied to subadmin. If not, throw {@link InvalidDataException} or
     * {@link StorageException} if there are no Restrictions defined within the database. Check whether Restrictions contain duplicate
     * Restriction entries and throws {@link InvalidDataException} if that is the case.
     * 
     * @throws StorageException
     * @throws InvalidDataException
     * @throws OXResellerException
     */
    private void checkRestrictionsPerSubadmin(final ResellerAdmin adm) throws StorageException, InvalidDataException, OXResellerException {
        final Map<String, Restriction> validRestrictions = oxresell.listRestrictions("*");
        if (validRestrictions == null || validRestrictions.size() <= 0) {
            throw new OXResellerException("unable to load available restrictions from database");
        }

        final HashSet<Restriction> res = adm.getRestrictions();
        if (res != null) {
            final Restriction[] rarr = res.toArray(new Restriction[res.size()]);
            final HashSet<String> dupcheck = new HashSet<String>();
            for (int i = 0; i < res.size(); i++) {
                final String rname = rarr[i].getName();
                if (dupcheck.contains(rname)) {
                    throw new InvalidDataException("Duplicate entry for restriction \"" + rname + "\"");
                }
                dupcheck.add(rname);
                final String rval = rarr[i].getValue();
                if (rname == null) {
                    throw new InvalidDataException("Restriction name must be set");
                }
                if (!(rname.equals(Restriction.MAX_CONTEXT_PER_SUBADMIN) || rname.equals(Restriction.MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN) || rname.equals(Restriction.MAX_OVERALL_USER_PER_SUBADMIN) || rname.startsWith(Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX))) {
                    throw new InvalidDataException("Restriction " + rname + " cannot be applied to subadmin");
                }
                if (rval == null) {
                    throw new InvalidDataException("Restriction value must be set");
                }
                if (!validRestrictions.containsKey(rname)) {
                    throw new InvalidDataException("No restriction named " + rname + " found in database");
                }
                rarr[i].setId(validRestrictions.get(rname).getId());
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#change(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void change(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        try {
            doNullCheck(adm);
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        }

        try {
            basicauth.doAuthentication(creds);

            checkIdOrName(adm);

            if (adm.getId() != null && !oxresell.existsAdmin(adm)) {
                throw new OXResellerException(OXResellerException.RESELLER_ADMIN_NOT_EXIST + ": " + adm.getName());
            }

            GenericChecks.checkChangeValidPasswordMech(adm);

            // if no password mech supplied, use the old one as set in db
            final ResellerAdmin dbadm = oxresell.getData(new ResellerAdmin[] { adm })[0];
            if (adm.getPasswordMech() == null) {
                adm.setPasswordMech(dbadm.getPasswordMech());
            }

            final String newname = adm.getName();
            final String dbname = dbadm.getName();

            if (newname != null && adm.getId() != null && !newname.equals(dbname)) {
                // want to change name?, check whether new name does already
                // exist
                if (oxresell.existsAdmin(new ResellerAdmin(newname))) {
                    throw new OXResellerException(OXResellerException.RESELLER_ADMIN_EXISTS + ": " + adm.getName());
                }
            }

            checkRestrictionsPerSubadmin(adm);
            oxresell.change(adm);
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final OXResellerException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#create(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public ResellerAdmin create(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException, OXResellerException {
        try {
            doNullCheck(adm);
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        }

        try {
            basicauth.doAuthentication(creds);

            if (oxresell.existsAdmin(adm)) {
                throw new OXResellerException(OXResellerException.RESELLER_ADMIN_EXISTS + ": " + adm.getName());
            }

            GenericChecks.checkCreateValidPasswordMech(adm);
            if (adm.getPassword() == null || adm.getPassword().trim().length() == 0) {
                throw new InvalidDataException("Empty password is not allowed");
            }
            if (!adm.mandatoryCreateMembersSet()) {
                throw new InvalidDataException("Mandatory fields not set: " + adm.getUnsetMembers());
            }

            // TODO: parent id must be the ID of the creator
            adm.setParentId(0);

            checkRestrictionsPerSubadmin(adm);

            return oxresell.create(adm);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final OXResellerException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final EnforceableDataObjectException e) {
            log.error(e.getMessage(), e);
            throw new InvalidDataException(e.getMessage());
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#delete(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void delete(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        try {
            doNullCheck(adm);
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        }

        try {
            basicauth.doAuthentication(creds);

            checkIdOrName(adm);

            if (!oxresell.existsAdmin(adm)) {
                throw new OXResellerException(OXResellerException.RESELLER_ADMIN_NOT_EXIST + ": " + adm.getName());
            }
            if (adm.getName() == null) {
                if (oxresell.ownsContext(null, adm.getId())) {
                    throw new OXResellerException("Unable to delete " + adm.getName() + ", still owns Context(s)");
                }
            } else {
                if (oxresell.ownsContext(null, new Credentials(adm.getName(), null))) {
                    throw new OXResellerException("Unable to delete " + adm.getName() + ", still owns Context(s)");
                }
            }
            oxresell.delete(adm);
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final OXResellerException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#list(java.lang.String,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public ResellerAdmin[] list(final String search_pattern, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException {
        try {
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        }

        try {
            basicauth.doAuthentication(creds);

            return oxresell.list(search_pattern);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final RuntimeException e) {
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
        for (final ResellerAdmin adm : admins) {
            if (null == adm) {
                throw new InvalidDataException("cannot handle null object");
            }
            final Integer id = adm.getId();
            final String name = adm.getName();
            if (null == id && null == name) {
                throw new InvalidDataException("either ID or name must be specified.");
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#getData(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin[])
     */
    public ResellerAdmin[] getMultipleData(final ResellerAdmin[] admins, final Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        try {
            doNullCheck((Object[]) admins);
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
            if (!oxresell.existsAdmin(admins)) {
                throw new OXResellerException(OXResellerException.RESELLER_ADMIN_NOT_EXIST);
            }

            return oxresell.getData(admins);
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final OXResellerException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#getData(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public ResellerAdmin getData(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        return getMultipleData(new ResellerAdmin[] { adm }, creds)[0];
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#applyRestrictionsToContext(java.util.HashSet,
     * com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void applyRestrictionsToContext(final HashSet<Restriction> restrictions, final Context ctx, final Credentials creds) throws InvalidDataException, StorageException, InvalidCredentialsException, OXResellerException {
        try {
            doNullCheck(ctx);
            doNullCheck(ctx.getId());
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        }

        try {
            final boolean isMasterAdmin = ClientAdminThreadExtended.cache.isMasterAdmin(creds);
            if (isMasterAdmin) {
                basicauth.doAuthentication(creds);
            } else {
                resellerauth.doAuthentication(creds);
            }

            if (!isMasterAdmin && !oxresell.ownsContext(ctx, creds)) {
                throw new OXResellerException("ContextID " + ctx.getId() + " does not belong to " + creds.getLogin());
            }

            checkRestrictionsPerContext(restrictions);

            oxresell.applyRestrictionsToContext(restrictions, ctx);
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.openexchange.admin.reseller.rmi.OXResellerInterface#getRestrictionsFromContext(com.openexchange.admin.rmi.dataobjects.Context,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public HashSet<Restriction> getRestrictionsFromContext(final Context ctx, final Credentials creds) throws RemoteException, InvalidDataException, OXResellerException, StorageException, InvalidCredentialsException {
        try {
            doNullCheck(ctx);
            doNullCheck(ctx.getId());
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        }

        try {
            final boolean isMasterAdmin = ClientAdminThreadExtended.cache.isMasterAdmin(creds);
            if (isMasterAdmin) {
                basicauth.doAuthentication(creds);
            } else {
                resellerauth.doAuthentication(creds);
            }

            if (!isMasterAdmin && !oxresell.ownsContext(ctx, creds)) {
                throw new OXResellerException("ContextID " + ctx.getId() + " does not belong to " + creds.getLogin());
            }

            return oxresell.getRestrictionsFromContext(ctx);
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.openexchange.admin.reseller.rmi.OXResellerInterface#initDatabaseRestrictions(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void initDatabaseRestrictions(final Credentials creds) throws StorageException, InvalidCredentialsException, OXResellerException {
        try {
            basicauth.doAuthentication(creds);
            final Map<String, Restriction> validRestrictions = oxresell.listRestrictions("*");
            if (validRestrictions != null && validRestrictions.size() > 0) {
                throw new OXResellerException("Database already contains restrictions.");
            }
            oxresell.initDatabaseRestrictions();
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final OXResellerException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.openexchange.admin.reseller.rmi.OXResellerInterface#removeDatabaseRestrictions(com.openexchange.admin.rmi.dataobjects.Credentials
     * )
     */
    public void removeDatabaseRestrictions(final Credentials creds) throws RemoteException, InvalidCredentialsException, StorageException {
        try {
            basicauth.doAuthentication(creds);

            oxresell.removeDatabaseRestrictions();
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#updateModuleAccessRestrictions(java.lang.String)
     */
    public void updateDatabaseModuleAccessRestrictions(final String olddefinition_file, final Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException {
        try {
            basicauth.doAuthentication(creds);

            oxresell.updateModuleAccessRestrictions();
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.openexchange.admin.reseller.rmi.OXResellerInterface#getAvailableRestrictions(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public HashSet<Restriction> getAvailableRestrictions(final Credentials creds) throws RemoteException, InvalidCredentialsException, StorageException, OXResellerException {
        try {
            basicauth.doAuthentication(creds);

            Map<String, Restriction> validRestrictions = oxresell.listRestrictions("*");
            if (validRestrictions == null || validRestrictions.size() <= 0) {
                throw new OXResellerException("unable to load available restrictions from database");
            }

            final HashSet<Restriction> ret = new HashSet<Restriction>();
            for (final String key : validRestrictions.keySet()) {
                ret.add(validRestrictions.get(key));
            }
            return ret;
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

}
