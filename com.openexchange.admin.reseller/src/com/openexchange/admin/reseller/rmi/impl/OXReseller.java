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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.reseller.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.reseller.plugins.OXResellerPluginInterface;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.OXResellerTools;
import com.openexchange.admin.reseller.rmi.OXResellerTools.ClosureInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException.Code;
import com.openexchange.admin.reseller.services.PluginInterfaces;
import com.openexchange.admin.reseller.storage.interfaces.OXResellerStorageInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.AbstractAdminRmiException;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.RemoteExceptionUtils;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.BasicAuthenticator;
import com.openexchange.admin.rmi.impl.OXCommonImpl;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.GenericChecks;
import com.openexchange.exception.LogLevel;

/**
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 */
public class OXReseller extends OXCommonImpl implements OXResellerInterface {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OXReseller.class);

    private final AdminCache cache;
    private final BasicAuthenticator basicauth;
    private final OXResellerStorageInterface oxresell;
    private final ResellerAuth resellerauth;

    public OXReseller() throws StorageException {
        super();
        log(LogLevel.DEBUG, LOGGER, null, null, "Class loaded: {}", this.getClass().getName());
        cache = ClientAdminThread.cache;
        basicauth = BasicAuthenticator.createNonPluginAwareAuthenticator();
        resellerauth = new ResellerAuth();
        try {
            oxresell = OXResellerStorageInterface.getInstance();
        } catch (StorageException e) {
            log(LogLevel.ERROR, LOGGER, null, e, "");
            throw e;
        }
    }

    private void logAndEnhanceException(Throwable t, final Credentials credentials) {
        if (t instanceof AbstractAdminRmiException) {
            logAndReturnException(LOGGER, ((AbstractAdminRmiException) t), credentials);
        } else if (t instanceof RemoteException) {
            RemoteException remoteException = (RemoteException) t;
            String exceptionId = AbstractAdminRmiException.generateExceptionId();
            RemoteExceptionUtils.enhanceRemoteException(remoteException, exceptionId);
            logAndReturnException(LOGGER, remoteException, exceptionId, credentials);
        }
    }

    @Override
    public void change(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        try {
            try {
                doNullCheck(adm);
            } catch (InvalidDataException e) {
                log(LogLevel.ERROR, LOGGER, creds, e, "Invalid data sent by client!");
                throw e;
            }
            ResellerAdmin parent = null;
            final Credentials masterCredentials = cache.getMasterCredentials();
            if (null != masterCredentials && masterCredentials.getLogin().equals(creds.getLogin())) {
                basicauth.doAuthentication(creds);
            } else {
                resellerauth.doAuthentication(creds);
                parent = oxresell.getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin(), creds.getPassword()) })[0];
                if (adm.getParentId() != null) {
                    throw new OXResellerException(Code.SUBAMIN_NOT_ALLOWED_TO_CHANGE_PARENTID);
                }
            }

            checkIdOrName(adm);

            if (adm.getId() != null && !oxresell.existsAdmin(adm)) {
                throw new OXResellerException(Code.RESELLER_ADMIN_NOT_EXIST, adm.getName());
            }

            GenericChecks.checkChangeValidPasswordMech(adm);

            final ResellerAdmin dbadm = oxresell.getData(new ResellerAdmin[] { adm })[0];
            if (null != parent && false == dbadm.getParentId().equals(parent.getId())) {
                log(LogLevel.ERROR, LOGGER, creds, null, "unathorized access to {} by {}", dbadm.getName(), creds.getLogin());
                throw new InvalidCredentialsException("authentication failed");
            }
            // if no password mech supplied, use the old one as set in db
            if (adm.getPasswordMech() == null) {
                adm.setPasswordMech(dbadm.getPasswordMech());
            }

            final Integer changedParentId = adm.getParentId();
            if (changedParentId != null && 0 != changedParentId.intValue()) {
                if (!oxresell.existsAdmin(new ResellerAdmin(i(changedParentId)))) {
                    throw new OXResellerException(Code.RESELLER_ADMIN_NOT_EXIST, "with parentId=" + changedParentId);
                }
                final ResellerAdmin changedParentAdmin = oxresell.getData(new ResellerAdmin[] { new ResellerAdmin(i(changedParentId)) })[0];
                if (changedParentAdmin.getParentId().intValue() > 0) {
                    throw new OXResellerException(Code.CANNOT_SET_PARENTID_TO_SUBSUBADMIN);
                }
                parent = changedParentAdmin;
            }

            Restriction[] res = adm.getRestrictions();
            if (res != null) {
                if (res.length > 0 && dbadm.getParentId().intValue() > 0) {
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

            adm.setParentId(null != parent ? parent.getId() : I(0));
            adm.setParentName(null != parent ? parent.getName() : null);

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXResellerPluginInterface oxresellpi : pluginInterfaces.getResellerPlugins().getServiceList()) {
                        try {
                            log(LogLevel.DEBUG, LOGGER, creds, null, "Calling beforeChange for plugin: {}", oxresellpi.getClass().getName());
                            oxresellpi.beforeChange(adm, creds);
                        } catch (PluginException | RuntimeException e) {
                            log(LogLevel.ERROR, LOGGER, creds, e, "Error while calling beforeChange for plugin: {}", oxresellpi.getClass().getName());
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }

            oxresell.change(adm);

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXResellerPluginInterface oxresellpi : pluginInterfaces.getResellerPlugins().getServiceList()) {
                        try {
                            log(LogLevel.DEBUG, LOGGER, creds, null, "Calling change for plugin: {}", oxresellpi.getClass().getName());
                            oxresellpi.change(adm, creds);
                        } catch (PluginException | RuntimeException e) {
                            log(LogLevel.ERROR, LOGGER, creds, e, "Error while calling change for plugin: {}", oxresellpi.getClass().getName());
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            logAndEnhanceException(e, creds);
            throw e;
        }
    }

    @Override
    public ResellerAdmin create(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException, OXResellerException {
        try {
            try {
                doNullCheck(adm);
            } catch (InvalidDataException e) {
                log(LogLevel.ERROR, LOGGER, creds, e, "Invalid data sent by client!");
                throw e;
            }
            ResellerAdmin parent = null;

            final Credentials masterCredentials = cache.getMasterCredentials();
            if (null != masterCredentials && masterCredentials.getLogin().equals(creds.getLogin())) {
                basicauth.doAuthentication(creds);
            } else {
                resellerauth.doAuthentication(creds);
                oxresell.checkPerSubadminRestrictions(creds, null, true, Restriction.SUBADMIN_CAN_CREATE_SUBADMINS, Restriction.MAX_SUBADMIN_PER_SUBADMIN);
                if (oxresell.existsAdmin(new ResellerAdmin(creds.getLogin(), creds.getPassword()))) {
                    parent = oxresell.getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin(), creds.getPassword()) })[0];
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
            if (res != null) {
                if (res.length > 0 && null != parent) {
                    throw new OXResellerException(Code.SUBSUBADMIN_NOT_ALLOWED_TO_CHANGE_RESTRICTIONS);
                }
            }

            adm.setParentId(null != parent ? parent.getId() : I(0));
            adm.setParentName(null != parent ? parent.getName() : null);

            checkRestrictionsPerSubadmin(adm);

            ResellerAdmin ra = oxresell.create(adm);

            // Trigger plugin extensions
            {
                final List<OXResellerPluginInterface> interfacelist = new ArrayList<OXResellerPluginInterface>();
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXResellerPluginInterface oxresellpi : pluginInterfaces.getResellerPlugins().getServiceList()) {
                        final String bundlename = oxresellpi.getClass().getName();
                        try {
                            log(LogLevel.DEBUG, LOGGER, creds, null, "Calling create for plugin: {}", bundlename);
                            oxresellpi.create(ra, creds);
                            interfacelist.add(oxresellpi);
                        } catch (PluginException e) {
                            log(LogLevel.ERROR, LOGGER, creds, e, "Error while calling create for plugin: {}", bundlename);
                            log(LogLevel.INFO, LOGGER, creds, null, "Now doing rollback for everything until now...");
                            for (final OXResellerPluginInterface oxresellerinterface : interfacelist) {
                                try {
                                    oxresellerinterface.delete(adm, creds);
                                } catch (PluginException e1) {
                                    log(LogLevel.ERROR, LOGGER, creds, e1, "Error doing rollback for plugin: {}", bundlename);
                                }
                            }
                            try {
                                oxresell.delete(adm);
                            } catch (StorageException e1) {
                                log(LogLevel.ERROR, LOGGER, creds, e1, "Error rolling back reseller creation in database");
                            }
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }

            return ra;
        } catch (EnforceableDataObjectException e) {
            RemoteException remoteException = RemoteExceptionUtils.convertException(e);
            logAndReturnException(LOGGER, remoteException, e.getExceptionId(), creds);
            throw remoteException;
        } catch (Throwable e) {
            logAndEnhanceException(e, creds);
            throw e;
        }
    }

    @Override
    public void delete(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        try {
            try {
                doNullCheck(adm);
            } catch (InvalidDataException e) {
                log(LogLevel.ERROR, LOGGER, creds, e, "Invalid data sent by client!");
                throw e;
            }
            boolean isMaster = false;
            final Credentials masterCredentials = cache.getMasterCredentials();
            if (null != masterCredentials && masterCredentials.getLogin().equals(creds.getLogin())) {
                basicauth.doAuthentication(creds);
                isMaster = true;
            } else {
                resellerauth.doAuthentication(creds);
            }

            checkIdOrName(adm);

            if (!oxresell.existsAdmin(adm)) {
                throw new OXResellerException(Code.RESELLER_ADMIN_NOT_EXIST, adm.getName());
            }

            final ResellerAdmin dbadm = oxresell.getData(new ResellerAdmin[] { adm })[0];
            ResellerAdmin parent = null;
            if (!isMaster) {
                parent = oxresell.getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin(), creds.getPassword()) })[0];
                if (!dbadm.getParentId().equals(parent.getId())) {
                    throw new OXResellerException(Code.SUBADMIN_DOES_NOT_BELONG_TO_SUBADMIN, dbadm.getName(), parent.getName());
                }
            }

            if (oxresell.ownsContext(null, dbadm.getId().intValue())) {
                throw new OXResellerException(Code.UNABLE_TO_DELETE, dbadm.getId().toString());
            }

            dbadm.setParentName(null != parent ? parent.getName() : null);

            final ArrayList<OXResellerPluginInterface> interfacelist = new ArrayList<OXResellerPluginInterface>();

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXResellerPluginInterface oxresellpi : pluginInterfaces.getResellerPlugins().getServiceList()) {
                        final String bundlename = oxresellpi.getClass().getName();
                        try {
                            log(LogLevel.DEBUG, LOGGER, creds, null, "Calling delete for plugin: {}", bundlename);
                            oxresellpi.delete(dbadm, creds);
                            interfacelist.add(oxresellpi);
                        } catch (PluginException e) {
                            log(LogLevel.ERROR, LOGGER, creds, e, "Error while calling delete for plugin: {}", bundlename);
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }

            oxresell.delete(dbadm);
        } catch (Throwable e) {
            logAndEnhanceException(e, creds);
            throw e;
        }
    }

    @Override
    public Restriction[] getAvailableRestrictions(final Credentials creds) throws RemoteException, InvalidCredentialsException, StorageException, OXResellerException {
        try {
            if (null == creds) {
                throw new InvalidCredentialsException("Credentials are missing.");
            }
            final Credentials masterCredentials = cache.getMasterCredentials();
            if (null != masterCredentials && masterCredentials.getLogin().equals(creds.getLogin())) {
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
        } catch (Throwable e) {
            logAndEnhanceException(e, creds);
            throw e;
        }
    }

    @Override
    public ResellerAdmin getData(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        return getMultipleData(new ResellerAdmin[] { adm }, creds)[0];
    }

    @Override
    public ResellerAdmin[] getMultipleData(final ResellerAdmin[] admins, final Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        try {
            try {
                doNullCheck((Object[]) admins);
                if (admins.length <= 0) {
                    throw new InvalidDataException();
                }
            } catch (InvalidDataException e) {
                log(LogLevel.ERROR, LOGGER, creds, e, "Invalid data sent by client!");
                throw e;
            }
            int pid = 0;
            final Credentials masterCredentials = cache.getMasterCredentials();
            if (null != masterCredentials && masterCredentials.getLogin().equals(creds.getLogin())) {
                basicauth.doAuthentication(creds);
            } else {
                resellerauth.doAuthentication(creds);
                pid = oxresell.getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin(), creds.getPassword()) })[0].getId().intValue();
            }

            checkAdminIdOrName(admins);
            for (final ResellerAdmin admin : admins) {
                if (!oxresell.existsAdmin(admin, pid)) {
                    throw new OXResellerException(Code.RESELLER_ADMIN_NOT_EXIST, admin.getName());
                }
            }

            return oxresell.getData(admins);
        } catch (Throwable e) {
            logAndEnhanceException(e, creds);
            throw e;
        }
    }

    @Override
    public Restriction[] getRestrictionsFromContext(final Context ctx, final Credentials creds) throws RemoteException, InvalidDataException, OXResellerException, StorageException, InvalidCredentialsException {
        try {
            try {
                doNullCheck(ctx);
                doNullCheck(ctx.getId());
            } catch (InvalidDataException e) {
                log(LogLevel.ERROR, LOGGER, creds, e, "Invalid data sent by client!");
                throw e;
            }
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
        } catch (Throwable e) {
            logAndEnhanceException(e, creds);
            throw e;
        }
    }

    @Override
    public void initDatabaseRestrictions(final Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException {
        try {
            basicauth.doAuthentication(creds);
            final Map<String, Restriction> validRestrictions = oxresell.listRestrictions("*");
            if (validRestrictions != null && validRestrictions.size() > 0) {
                throw new OXResellerException(Code.DATABASE_ALREADY_CONTAINS_RESTRICTIONS);
            }
            oxresell.initDatabaseRestrictions();
        } catch (Throwable e) {
            logAndEnhanceException(e, creds);
            throw e;
        }
    }

    @Override
    public ResellerAdmin[] list(final String search_pattern, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException {
        try {
            try {
                doNullCheck(search_pattern);
            } catch (InvalidDataException e) {
                log(LogLevel.ERROR, LOGGER, creds, e, "Invalid data sent by client!");
                throw e;
            }
            final Credentials masterCredentials = cache.getMasterCredentials();
            if (null != masterCredentials && masterCredentials.getLogin().equals(creds.getLogin())) {
                basicauth.doAuthentication(creds);
                return oxresell.list(search_pattern);
            }
            resellerauth.doAuthentication(creds);
            int pid = oxresell.getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin(), creds.getPassword()) })[0].getId().intValue();
            return oxresell.list(search_pattern, pid);
        } catch (Throwable e) {
            logAndEnhanceException(e, creds);
            throw e;
        }
    }

    @Override
    public void removeDatabaseRestrictions(final Credentials creds) throws RemoteException, InvalidCredentialsException, StorageException, OXResellerException {
        try {
            basicauth.doAuthentication(creds);
            final Map<String, Restriction> validRestrictions = oxresell.listRestrictions("*");
            if (validRestrictions == null || validRestrictions.size() == 0) {
                throw new OXResellerException(Code.NO_RESTRICTIONS_AVAILABLE_TO, "remove");
            }

            oxresell.removeDatabaseRestrictions();
        } catch (Throwable e) {
            logAndEnhanceException(e, creds);
            throw e;
        }
    }

    @Override
    public void updateDatabaseModuleAccessRestrictions(final Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException {
        try {
            basicauth.doAuthentication(creds);

            final Map<String, Restriction> validRestrictions = oxresell.listRestrictions("*");
            if (validRestrictions == null || validRestrictions.size() == 0) {
                throw new OXResellerException(Code.NO_RESTRICTIONS_AVAILABLE_TO, "update");
            }

            oxresell.updateModuleAccessRestrictions();
        } catch (Throwable e) {
            logAndEnhanceException(e, creds);
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
        } catch (Throwable e) {
            logAndEnhanceException(e, creds);
            throw e;
        }
    }

}
