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

package com.openexchange.admin.reseller.storage.interfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.reseller.tools.AdminCacheExtended;
import com.openexchange.admin.reseller.tools.PropertyHandlerExtended;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * @author choeger
 *
 */
public abstract class OXResellerStorageInterface {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXResellerStorageInterface.class);

    private static volatile OXResellerStorageInterface instance;

    /**
     * Creates a new instance implementing the reseller storage factory.
     * @return an instance implementing the reseller storage factory.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException Storage exception
     */
    public static OXResellerStorageInterface getInstance() throws StorageException {
        OXResellerStorageInterface inst = instance;
        if (null == inst) {
            synchronized (OXResellerStorageInterface.class) {
                inst = instance;
                if (null == inst) {
                    AdminCacheExtended cache = com.openexchange.admin.reseller.daemons.ClientAdminThreadExtended.cache;
                    PropertyHandlerExtended prop = cache.getProperties();
                    Class<? extends OXResellerStorageInterface> implementingClass;
                    final String className = prop.getProp(PropertyHandlerExtended.RESELLER_STORAGE, null);
                    if (null != className) {
                        try {
                            implementingClass = Class.forName(className).asSubclass(OXResellerStorageInterface.class);
                        } catch (final ClassNotFoundException e) {
                            log.error("", e);
                            throw new StorageException(e);
                        }
                    } else {
                        final StorageException storageException = new StorageException("Property for reseller_storage not defined");
                        log.error("", storageException);
                        throw storageException;
                    }

                    Constructor<? extends OXResellerStorageInterface> cons;
                    try {
                        cons = implementingClass.getConstructor(new Class[] {});
                        inst = cons.newInstance(new Object[] {});
                        instance = inst;
                    } catch (final SecurityException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final NoSuchMethodException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final IllegalArgumentException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final InstantiationException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final IllegalAccessException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final InvocationTargetException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    }
                }
            }
        }
        return inst;
    }

    /**
     * @param adm
     * @return
     * @throws StorageException
     */
    public abstract ResellerAdmin create(final ResellerAdmin adm) throws StorageException;

    /**
     * @param adm
     * @throws StorageException
     */
    public abstract void delete(final ResellerAdmin adm) throws StorageException;

    /**
     * @param adm
     * @throws StorageException
     */
    public abstract void change(final ResellerAdmin adm) throws StorageException;

    /**
     * @param search_pattern
     * @return
     * @throws StorageException
     */
    public abstract ResellerAdmin[] list(final String search_pattern) throws StorageException;

    /**
     * @param search_pattern
     * @param pid
     * @return
     * @throws StorageException
     */
    public abstract ResellerAdmin[] list(final String search_pattern, int pid) throws StorageException;

    /**
     * @param adm
     * @return The right ResellerAdmin[] data, in any other case an exception is raised
     * @throws StorageException
     */
    public abstract ResellerAdmin[] getData(final ResellerAdmin[] admins) throws StorageException;

    /**
     * @param adm
     * @return The right ResellerAdmin[] data, in any other case an exception is raised
     * @throws StorageException
     */
    public abstract ResellerAdmin[] getData(final ResellerAdmin[] admins, int pid) throws StorageException;

    /**
     * @param adm
     * @param pid
     * @return
     * @throws StorageException
     */
    public abstract boolean existsAdmin(final ResellerAdmin adm, int pid) throws StorageException;

    /**
     * @param adm
     * @return
     * @throws StorageException
     */
    public abstract boolean existsAdmin(final ResellerAdmin adm) throws StorageException;

    /**
     * @param admins
     * @return
     * @throws StorageException
     */
    public abstract boolean existsAdmin(final ResellerAdmin[] admins) throws StorageException;

    /**
     * @param ctx
     * @param adm
     * @throws StorageException
     */
    public abstract void ownContextToAdmin(final Context ctx, final Credentials creds) throws StorageException;

    /**
     * @param ctx
     * @param creds
     * @throws StorageException
     */
    public abstract void unownContextFromAdmin(final Context ctx, final Credentials creds) throws StorageException;

    /**
     * @param ctx
     * @param adm
     * @throws StorageException
     */
    public abstract void unownContextFromAdmin(final Context ctx, final ResellerAdmin adm) throws StorageException;

    /**
     * @param ctx
     * @param creds
     * @return
     * @throws StorageException
     */
    public abstract boolean checkOwnsContextAndSetSid(final Context ctx, final Credentials creds) throws StorageException;

    /**
     * @param ctx
     * @param adm
     * @return
     * @throws StorageException
     */
    public abstract boolean ownsContext(final Context ctx, final int admid) throws StorageException;

    /**
     * @param ctx
     * @param admid
     * @return
     * @throws StorageException
     */
    public abstract boolean ownsContextOrIsPidOfOwner(final Context ctx, final int admid) throws StorageException;

    /**
     * Checks whether context is owned by a subadmin
     *
     * @param ctx
     * @return {@link ResellerAdmin} null cannot be returned, if the context is owner by no special admin the master admin is returned
     * @throws StorageException
     */
    public abstract ResellerAdmin getContextOwner(final Context ctx) throws StorageException;

    /**
     * @param search_pattern
     * @return
     * @throws StorageException
     */
    public abstract Map<String, Restriction> listRestrictions(final String search_pattern) throws StorageException;

    /**
     * Check whether any of the restrictions bound to subadmin apply. UserModuleAccess must be provided in order
     * to check the per module access restrictions.
     *
     * @param creds
     * @param access
     * @param contextAdmin
     * @param restriction_types
     * @throws StorageException
     */
    public abstract void checkPerSubadminRestrictions(final Credentials creds, final UserModuleAccess access, boolean contextAdmin, final String... restriction_types) throws StorageException;

    /**
     * @param ctx
     * @param access
     * @param contextAdmin
     * @param restriction_types
     * @throws StorageException
     */
    public abstract void checkPerContextRestrictions(final Context ctx, final UserModuleAccess access, boolean contextAdmin, final String... restriction_types) throws StorageException;

    /**
     * @param creds
     * @param restrictions
     * @throws StorageException
     */
    public abstract void applyRestrictionsToContext(final Restriction[] restrictions, final Context ctx) throws StorageException;

    /**
     * @param ctx
     * @return
     * @throws StorageException
     */
    public abstract Restriction[] getRestrictionsFromContext(final Context ctx) throws StorageException;

    /**
     * @throws StorageException
     */
    public abstract void initDatabaseRestrictions() throws StorageException;

    /**
     * @throws StorageException
     */
    public abstract void removeDatabaseRestrictions() throws StorageException;

    /**
     * @throws StorageException
     * @throws OXResellerException
     */
    public abstract void updateModuleAccessRestrictions() throws StorageException, OXResellerException;

    /**
     * @throws StorageException
     * @throws OXResellerException
     */
    public abstract void updateRestrictions() throws StorageException, OXResellerException;

    /**
     * @param ctx
     * @return
     * @throws StorageException
     */
    public abstract String getCustomId(final Context ctx) throws StorageException;

    /**
     * @param ctx
     * @throws StorageException
     */
    public abstract void writeCustomId(final Context ctx) throws StorageException;

    /**
     * @param ctx
     * @throws StorageException
     */
    public abstract void deleteCustomFields(final Context ctx) throws StorageException;

    /**
     * @param ctx
     * @throws StorageException
     */
    public abstract void generateCreateTimestamp(final Context ctx) throws StorageException;

    /**
     * @param ctx
     * @throws StorageException
     */
    public abstract void updateModifyTimestamp(final Context ctx) throws StorageException;
}
