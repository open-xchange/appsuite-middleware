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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.reseller.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.OXResellerTools;
import com.openexchange.admin.reseller.rmi.OXResellerTools.ClosureInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException.Code;
import com.openexchange.admin.reseller.storage.interfaces.OXResellerStorageInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.BasicAuthenticator;
import com.openexchange.admin.rmi.impl.OXCommonImpl;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.GenericChecks;

/**
 * @author choeger
 */
public class OXReseller extends OXCommonImpl implements OXResellerInterface {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXReseller.class);

    private final AdminCache cache;
    private final BasicAuthenticator basicauth;
    private final OXResellerStorageInterface oxresell;
    private final ResellerAuth resellerauth;

    public OXReseller() throws StorageException {
        super();
        log.debug("Class loaded: {}", this.getClass().getName());
        cache = ClientAdminThread.cache;
        basicauth = new BasicAuthenticator();
        resellerauth = new ResellerAuth();
        try {
            oxresell = OXResellerStorageInterface.getInstance();
        } catch (final StorageException e) {
            log.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#change(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public void change(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        try {
            doNullCheck(adm);
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        }

        try {
            int pid = 0;
            final Credentials masterCredentials = cache.getMasterCredentials();
            if( null != masterCredentials && masterCredentials.getLogin().equals(creds.getLogin()) ) {
                basicauth.doAuthentication(creds);
            } else {
                resellerauth.doAuthentication(creds);
                pid = oxresell.getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin(), creds.getPassword()) })[0].getId();
                if (adm.getParentId() != null ) {
                    throw new OXResellerException(Code.SUBAMIN_NOT_ALLOWED_TO_CHANGE_PARENTID);
                }
            }

            checkIdOrName(adm);

            if (adm.getId() != null && !oxresell.existsAdmin(adm)) {
                throw new OXResellerException(Code.RESELLER_ADMIN_NOT_EXIST, adm.getName());
            }

            GenericChecks.checkChangeValidPasswordMech(adm);

            // if no password mech supplied, use the old one as set in db
            final ResellerAdmin dbadm = oxresell.getData(new ResellerAdmin[] { adm })[0];
            if( pid > 0 && dbadm.getParentId() != pid ) {
                log.error("unathorized access to {} by {}", dbadm.getName(), creds.getLogin());
                throw new InvalidCredentialsException("authentication failed");
            }
            if (adm.getPasswordMech() == null) {
                adm.setPasswordMech(dbadm.getPasswordMech());
            }

            final Integer parentId = adm.getParentId();
            if ( parentId != null && 0 != parentId ) {
                if( !oxresell.existsAdmin(new ResellerAdmin(adm.getParentId())) ) {
                    throw new OXResellerException(Code.RESELLER_ADMIN_NOT_EXIST, "with parentId=" + adm.getParentId());
                }
                final ResellerAdmin parentAdmin = oxresell.getData(new ResellerAdmin[]{new ResellerAdmin(adm.getParentId())})[0];
                if( parentAdmin.getParentId() > 0 ) {
                    throw new OXResellerException(Code.CANNOT_SET_PARENTID_TO_SUBSUBADMIN);
                }
            }

            Restriction[] res = adm.getRestrictions();
            if( res != null ) {
                if( res.length > 0 && dbadm.getParentId() > 0 ) {
                    throw new OXResellerException(Code.SUBSUBADMIN_NOT_ALLOWED_TO_CHANGE_RESTRICTIONS);
                }
            }

            final String newname = adm.getName();
            final String dbname = dbadm.getName();

            if (newname != null && adm.getId() != null && !newname.equals(dbname)) {
                // want to change name?, check whether new name does already
                // exist
                if (oxresell.existsAdmin(new ResellerAdmin(newname))) {
                    throw new OXResellerException(Code.RESELLER_ADMIN_EXISTS, adm.getName());
                }
            }

            checkRestrictionsPerSubadmin(adm);
            oxresell.change(adm);
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        } catch (final StorageException e) {
            log.error("", e);
            throw e;
        } catch (final OXResellerException e) {
            log.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error("", e);
            throw e;
        } catch (final RuntimeException e) {
            log.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#create(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public ResellerAdmin create(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException, OXResellerException {
        try {
            doNullCheck(adm);
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        }
        int pid = 0;
        try {
            final Credentials masterCredentials = cache.getMasterCredentials();
            if( null != masterCredentials && masterCredentials.getLogin().equals(creds.getLogin()) ) {
                basicauth.doAuthentication(creds);
            } else {
                resellerauth.doAuthentication(creds);
                oxresell.checkPerSubadminRestrictions(
                    creds,
                    null,
                    true,
                    Restriction.SUBADMIN_CAN_CREATE_SUBADMINS,
                    Restriction.MAX_SUBADMIN_PER_SUBADMIN
                );
                if( oxresell.existsAdmin(new ResellerAdmin(creds.getLogin(), creds.getPassword())) ) {
                    pid = oxresell.getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin(), creds.getPassword()) })[0].getId();
                }
            }

            if (oxresell.existsAdmin(adm)) {
                throw new OXResellerException(Code.RESELLER_ADMIN_EXISTS, adm.getName());
            }

            GenericChecks.checkCreateValidPasswordMech(adm);
            if (adm.getPassword() == null || adm.getPassword().trim().length() == 0) {
                throw new InvalidDataException("Empty password is not allowed");
            }
            if (!adm.mandatoryCreateMembersSet()) {
                throw new InvalidDataException("Mandatory fields not set: " + adm.getUnsetMembers());
            }

            Restriction[] res = adm.getRestrictions();
            if( res != null ) {
                if( res.length > 0 && pid > 0 ) {
                    throw new OXResellerException(Code.SUBSUBADMIN_NOT_ALLOWED_TO_CHANGE_RESTRICTIONS);
                }
            }

            adm.setParentId(pid);

            checkRestrictionsPerSubadmin(adm);

            return oxresell.create(adm);
        } catch (final StorageException e) {
            log.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error("", e);
            throw e;
        } catch (final OXResellerException e) {
            log.error("", e);
            throw e;
        } catch (final EnforceableDataObjectException e) {
            log.error("", e);
            throw new InvalidDataException(e.getMessage());
        } catch (final RuntimeException e) {
            log.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#delete(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public void delete(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        try {
            doNullCheck(adm);
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        }

        try {
            boolean isMaster = false;
            final Credentials masterCredentials = cache.getMasterCredentials();
            if( null != masterCredentials && masterCredentials.getLogin().equals(creds.getLogin()) ) {
                basicauth.doAuthentication(creds);
                isMaster = true;
            } else {
                resellerauth.doAuthentication(creds);
            }

            checkIdOrName(adm);

            if (!oxresell.existsAdmin(adm)) {
                throw new OXResellerException(Code.RESELLER_ADMIN_NOT_EXIST, adm.getName());
            }
            if(! isMaster ) {
                ResellerAdmin sadmdata = oxresell.getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin(), creds.getPassword()) })[0];
                ResellerAdmin dadmdata = oxresell.getData(new ResellerAdmin[] { adm })[0];
                if( !dadmdata.getParentId().equals(sadmdata.getId()) ) {
                    throw new OXResellerException(Code.SUBADMIN_DOES_NOT_BELONG_TO_SUBADMIN, dadmdata.getName(), sadmdata.getName());
                }
            }
            if (adm.getName() == null) {
                if (oxresell.ownsContext(null, adm.getId())) {
                    throw new OXResellerException(Code.UNABLE_TO_DELETE, adm.getId().toString());
                }
            } else {
                if (oxresell.checkOwnsContextAndSetSid(null, new Credentials(adm.getName(), null))) {
                    throw new OXResellerException(Code.UNABLE_TO_DELETE, adm.getName());
                }
            }
            oxresell.delete(adm);
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        } catch (final StorageException e) {
            log.error("", e);
            throw e;
        } catch (final OXResellerException e) {
            log.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error("", e);
            throw e;
        } catch (final RuntimeException e) {
            log.error("", e);
            throw e;
        }
    }

    @Override
    public Restriction[] getAvailableRestrictions(final Credentials creds) throws InvalidCredentialsException, StorageException, OXResellerException {
        if (null == creds) {
            throw new InvalidCredentialsException("Credentials are missing.");
        }

        try {
            final Credentials masterCredentials = cache.getMasterCredentials();
            if( null != masterCredentials && masterCredentials.getLogin().equals(creds.getLogin())) {
                basicauth.doAuthentication(creds);
            } else {
                resellerauth.doAuthentication(creds);
            }

            final Map<String, Restriction> validRestrictions = oxresell.listRestrictions("*");
            if (validRestrictions == null || validRestrictions.size() <= 0) {
                throw new OXResellerException(Code.UNABLE_TO_LOAD_AVAILABLE_RESTRICTIONS_FROM_DATABASE);
            }

            final HashSet<Restriction> ret = new HashSet<Restriction>();
            for (final Entry<String, Restriction> entry : validRestrictions.entrySet()) {
                ret.add(entry.getValue());
            }
            return ret.toArray(new Restriction[ret.size()]);
        } catch (final InvalidCredentialsException e) {
            log.error("", e);
            throw e;
        } catch (final RuntimeException e) {
            log.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#getData(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public ResellerAdmin getData(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        return getMultipleData(new ResellerAdmin[] { adm }, creds)[0];
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#getData(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin[])
     */
    @Override
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
            int pid = 0;
            final Credentials masterCredentials = cache.getMasterCredentials();
            if( null != masterCredentials && masterCredentials.getLogin().equals(creds.getLogin()) ) {
                basicauth.doAuthentication(creds);
            } else {
                resellerauth.doAuthentication(creds);
                pid = oxresell.getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin(), creds.getPassword()) })[0].getId();
            }

            checkAdminIdOrName(admins);
            for (final ResellerAdmin admin : admins) {
                if (!oxresell.existsAdmin(admin, pid)) {
                    throw new OXResellerException(Code.RESELLER_ADMIN_NOT_EXIST, admin.getName());
                }
            }

            return oxresell.getData(admins);
        } catch (final InvalidCredentialsException e) {
            log.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error("", e);
            throw e;
        } catch (final StorageException e) {
            log.error("", e);
            throw e;
        } catch (final OXResellerException e) {
            log.error("", e);
            throw e;
        } catch (final RuntimeException e) {
            log.error("", e);
            throw e;
        }

    }

    /*
     * (non-Javadoc)
     * @see
     * com.openexchange.admin.reseller.rmi.OXResellerInterface#getRestrictionsFromContext(com.openexchange.admin.rmi.dataobjects.Context,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public Restriction[] getRestrictionsFromContext(final Context ctx, final Credentials creds) throws RemoteException, InvalidDataException, OXResellerException, StorageException, InvalidCredentialsException {
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

            if (!isMasterAdmin && !oxresell.checkOwnsContextAndSetSid(ctx, creds)) {
                throw new OXResellerException(Code.CONTEXT_DOES_NOT_BELONG, String.valueOf(ctx.getId()), creds.getLogin());
            }

            return oxresell.getRestrictionsFromContext(ctx);
        } catch (final StorageException e) {
            log.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error("", e);
            throw e;
        } catch (final RuntimeException e) {
            log.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.openexchange.admin.reseller.rmi.OXResellerInterface#initDatabaseRestrictions(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public void initDatabaseRestrictions(final Credentials creds) throws StorageException, InvalidCredentialsException, OXResellerException {
        try {
            basicauth.doAuthentication(creds);
            final Map<String, Restriction> validRestrictions = oxresell.listRestrictions("*");
            if (validRestrictions != null && validRestrictions.size() > 0) {
                throw new OXResellerException(Code.DATABASE_ALREADY_CONTAINS_RESTRICTIONS);
            }
            oxresell.initDatabaseRestrictions();
        } catch (final InvalidCredentialsException e) {
            log.error("", e);
            throw e;
        } catch (final StorageException e) {
            log.error("", e);
            throw e;
        } catch (final OXResellerException e) {
            log.error("", e);
            throw e;
        } catch (final RuntimeException e) {
            log.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#list(java.lang.String,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public ResellerAdmin[] list(final String search_pattern, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException {
        try {
            doNullCheck(search_pattern);
        } catch (final InvalidDataException e) {
            log.error("Invalid data sent by client!", e);
            throw e;
        }

        try {
            final Credentials masterCredentials = cache.getMasterCredentials();
            if( null != masterCredentials && masterCredentials.getLogin().equals(creds.getLogin()) ) {
                basicauth.doAuthentication(creds);
                return oxresell.list(search_pattern);
            } else {
                resellerauth.doAuthentication(creds);
                int pid = oxresell.getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin(), creds.getPassword()) })[0].getId();
                return oxresell.list(search_pattern, pid);
            }
        } catch (final StorageException e) {
            log.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            log.error("", e);
            throw e;
        } catch (final RuntimeException e) {
            log.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.openexchange.admin.reseller.rmi.OXResellerInterface#removeDatabaseRestrictions(com.openexchange.admin.rmi.dataobjects.Credentials
     * )
     */
    @Override
    public void removeDatabaseRestrictions(final Credentials creds) throws RemoteException, InvalidCredentialsException, StorageException, OXResellerException {
        try {
            basicauth.doAuthentication(creds);
            final Map<String, Restriction> validRestrictions = oxresell.listRestrictions("*");
            if (validRestrictions == null || validRestrictions.size() == 0) {
                throw new OXResellerException(Code.NO_RESTRICTIONS_AVAILABLE_TO, "remove");
            }

            oxresell.removeDatabaseRestrictions();
        } catch (final InvalidCredentialsException e) {
            log.error("", e);
            throw e;
        } catch (final StorageException e) {
            log.error("", e);
            throw e;
        } catch (final OXResellerException e) {
            log.error("", e);
            throw e;
        } catch (final RuntimeException e) {
            log.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#updateModuleAccessRestrictions(java.lang.String)
     */
    @Override
    public void updateDatabaseModuleAccessRestrictions(final Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException {
        try {
            basicauth.doAuthentication(creds);

            final Map<String, Restriction> validRestrictions = oxresell.listRestrictions("*");
            if (validRestrictions == null || validRestrictions.size() == 0) {
                throw new OXResellerException(Code.NO_RESTRICTIONS_AVAILABLE_TO, "update");
            }

            oxresell.updateModuleAccessRestrictions();
        } catch (final InvalidCredentialsException e) {
            log.error("", e);
            throw e;
        } catch (final StorageException e) {
            log.error("", e);
            throw e;
        } catch (final RuntimeException e) {
            log.error("", e);
            throw e;
        } catch (OXResellerException e) {
            log.error("", e);
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
        if (null == validRestrictions || validRestrictions.size() <= 0) {
            throw new OXResellerException(Code.UNABLE_TO_LOAD_AVAILABLE_RESTRICTIONS_FROM_DATABASE);
        }

        final HashSet<Restriction> res = OXResellerTools.array2HashSet(adm.getRestrictions());
        if (null != res) {
            OXResellerTools.checkRestrictions(res, validRestrictions, "subadmin", new ClosureInterface() {
                @Override
                public boolean checkAgainstCorrespondingRestrictions(final String rname) {
                    return !(rname.equals(Restriction.MAX_CONTEXT_PER_SUBADMIN) || rname.equals(Restriction.MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN) || rname.equals(Restriction.MAX_OVERALL_USER_PER_SUBADMIN) || rname.equals(Restriction.SUBADMIN_CAN_CREATE_SUBADMINS) || rname.equals(Restriction.MAX_SUBADMIN_PER_SUBADMIN) || rname.startsWith(Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX));
                }
            });
        }
    }

    @Override
    public void updateDatabaseRestrictions(Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException {
        try {
            basicauth.doAuthentication(creds);

            final Map<String, Restriction> validRestrictions = oxresell.listRestrictions("*");
            if (validRestrictions == null || validRestrictions.size() == 0) {
                throw new OXResellerException(Code.NO_RESTRICTIONS_AVAILABLE_TO, "update");
            }

            oxresell.updateRestrictions();
        } catch (final InvalidCredentialsException e) {
            log.error("", e);
            throw e;
        } catch (final StorageException e) {
            log.error("", e);
            throw e;
        } catch (final RuntimeException e) {
            log.error("", e);
            throw e;
        } catch (OXResellerException e) {
            log.error("", e);
            throw e;
        }
    }

}
