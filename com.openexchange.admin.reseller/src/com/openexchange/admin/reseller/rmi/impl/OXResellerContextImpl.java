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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import com.openexchange.admin.lib.plugins.OXContextPluginInterface;
import com.openexchange.admin.lib.plugins.PluginException;
import com.openexchange.admin.reseller.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.reseller.rmi.OXResellerTools;
import com.openexchange.admin.reseller.rmi.OXResellerTools.ClosureInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException.Code;
import com.openexchange.admin.reseller.rmi.extensions.OXContextExtensionImpl;
import com.openexchange.admin.reseller.storage.interfaces.OXResellerStorageInterface;
import com.openexchange.admin.reseller.storage.mysqlStorage.ResellerContextFilter;
import com.openexchange.admin.reseller.storage.mysqlStorage.ResellerExtensionLoader;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;
import com.openexchange.admin.storage.interfaces.OXContextStorageInterface;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.tools.pipesnfilters.Filter;

/**
 * @author choeger
 */
public class OXResellerContextImpl implements OXContextPluginInterface {

    private static AdminCache cache = null;

    private OXResellerStorageInterface oxresell = null;

    /**
     * @throws StorageException
     */
    public OXResellerContextImpl() throws StorageException {
        cache = ClientAdminThreadExtended.cache;
        oxresell = OXResellerStorageInterface.getInstance();
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#change(com.openexchange.admin.rmi.dataobjects.Context,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void change(final Context ctx, final Credentials auth) throws PluginException {
        if (!cache.isMasterAdmin(auth)) {
            checkOwnerShipAndSetSid(ctx, auth);
        }
        applyRestrictionsPerContext(ctx);
        try {
            oxresell.writeCustomId(ctx);
            oxresell.updateModifyTimestamp(ctx);
        } catch (StorageException e) {
            throw new PluginException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#changeModuleAccess(com.openexchange.admin.rmi.dataobjects.Context,
     * com.openexchange.admin.rmi.dataobjects.UserModuleAccess, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void changeModuleAccess(final Context ctx, final UserModuleAccess access, final Credentials auth) throws PluginException {
        if (cache.isMasterAdmin(auth)) {
            return;
        }
        try {
            oxresell.updateModifyTimestamp(ctx);
        } catch (StorageException e) {
            throw new PluginException(e);
        }
        checkOwnerShipAndSetSid(ctx, auth);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#changeModuleAccess(com.openexchange.admin.rmi.dataobjects.Context,
     * java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void changeModuleAccess(final Context ctx, final String access_combination_name, final Credentials auth) throws PluginException {
        if (cache.isMasterAdmin(auth)) {
            return;
        }
        try {
            oxresell.updateModifyTimestamp(ctx);
        } catch (StorageException e) {
            throw new PluginException(e);
        }
        checkOwnerShipAndSetSid(ctx, auth);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#create(com.openexchange.admin.rmi.dataobjects.Context,
     * com.openexchange.admin.rmi.dataobjects.User, java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Context postCreate(final Context ctx, final User admin_user, final UserModuleAccess access, final Credentials auth) throws PluginException {
        try {
            oxresell.generateCreateTimestamp(ctx);
        } catch (StorageException e1) {
            throw new PluginException(e1);
        }
        applyRestrictionsPerContext(ctx);
        if (cache.isMasterAdmin(auth)) {
            try {
                oxresell.writeCustomId(ctx);
            } catch (StorageException e) {
                throw new PluginException(e);
            }
            return ctx;
        }
        try {
            oxresell.checkPerSubadminRestrictions(
                auth,
                access,
                Restriction.MAX_CONTEXT_PER_SUBADMIN,
                Restriction.MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN,
                Restriction.MAX_OVERALL_USER_PER_SUBADMIN,
                Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX
                );
            oxresell.writeCustomId(ctx);
            oxresell.ownContextToAdmin(ctx, auth);
        } catch (final StorageException e) {
            try {
                // own context to subadmin; if we don't do that, the deletion
                // in the cleanup of postCreate will deny to delete
                oxresell.ownContextToAdmin(ctx, auth);
            } catch (StorageException e1) {
                throw new PluginException(e1);
            }
            throw new PluginException(e);
        }
        return ctx;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#create(com.openexchange.admin.rmi.dataobjects.Context,
     * com.openexchange.admin.rmi.dataobjects.User, java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Context preCreate(final Context ctx, final User admin_user, final Credentials auth) throws PluginException {
        return ctx;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#delete(com.openexchange.admin.rmi.dataobjects.Context,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void delete(final Context ctx, final Credentials auth) throws PluginException {
        final boolean ismasteradmin = cache.isMasterAdmin(auth);
        try {
            if (ismasteradmin) {
                oxresell.applyRestrictionsToContext(null, ctx);
                oxresell.deleteCustomFields(ctx);
                final ResellerAdmin owner = oxresell.getContextOwner(ctx);
                if (0 == owner.getId().intValue()) {
                    // context does not belong to anybody, so it is save to be removed
                    return;
                } else {
                    // context belongs to somebody, so we must remove the ownership
                    oxresell.unownContextFromAdmin(ctx, owner);
                }
            } else {
                if (oxresell.checkOwnsContextAndSetSid(ctx, auth)) {
                    oxresell.applyRestrictionsToContext(null, ctx);
                    oxresell.deleteCustomFields(ctx);
                    oxresell.unownContextFromAdmin(ctx, auth);
                } else {
                    throw new PluginException("ContextID " + ctx.getId() + " does not belong to " + auth.getLogin());
                }
            }
        } catch (final StorageException e) {
            throw new PluginException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#disable(com.openexchange.admin.rmi.dataobjects.Context,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void disable(final Context ctx, final Credentials auth) throws PluginException {
        if (cache.isMasterAdmin(auth)) {
            return;
        }
        checkOwnerShipAndSetSid(ctx, auth);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#disableAll(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void disableAll(final Credentials auth) throws PluginException {
        if (cache.isMasterAdmin(auth)) {
            return;
        }

        try {
            final MaintenanceReason reason = new MaintenanceReason(42);
            final OXContextStorageInterface oxctx = OXContextStorageInterface.getInstance();
            oxctx.disableAll(reason, "context2subadmin", "WHERE context2subadmin.cid=context.cid");
        } catch (final StorageException e) {
            throw new PluginException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#downgrade(com.openexchange.admin.rmi.dataobjects.Context,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void downgrade(final Context ctx, final Credentials auth) throws PluginException {
        if (cache.isMasterAdmin(auth)) {
            return;
        }
        checkOwnerShipAndSetSid(ctx, auth);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#enable(com.openexchange.admin.rmi.dataobjects.Context,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void enable(final Context ctx, final Credentials auth) throws PluginException {
        if (cache.isMasterAdmin(auth)) {
            return;
        }
        checkOwnerShipAndSetSid(ctx, auth);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#enableAll(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void enableAll(final Credentials auth) throws PluginException {
        if (cache.isMasterAdmin(auth)) {
            return;
        }
        try {
            final OXContextStorageInterface oxctx = OXContextStorageInterface.getInstance();
            oxctx.enableAll("context2subadmin", "WHERE context2subadmin.cid=context.cid");
        } catch (final StorageException e) {
            throw new PluginException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#getAccessCombinationName(com.openexchange.admin.rmi.dataobjects.Context,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public String getAccessCombinationName(final Context ctx, final Credentials auth) throws PluginException {
        if (cache.isMasterAdmin(auth)) {
            return null;
        }
        checkOwnerShipAndSetSid(ctx, auth);
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#getData(com.openexchange.admin.rmi.dataobjects.Context,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public List<OXCommonExtension> getData(final List<Context> ctxs, final Credentials auth) throws PluginException {
        final ArrayList<OXCommonExtension> retval = new ArrayList<OXCommonExtension>();
        for (final Context ctx : ctxs) {
            if (cache.isMasterAdmin(auth)) {
                try {
                    final OXContextExtensionImpl ctxext = new OXContextExtensionImpl(oxresell.getContextOwner(ctx), oxresell.getRestrictionsFromContext(ctx));
                    ctxext.setCustomid(oxresell.getCustomId(ctx));
                    retval.add(ctxext);
                } catch (final StorageException e) {
                    throw new PluginException(e);
                }
            } else {
                checkOwnerShipAndSetSid(ctx, auth);
                try {
                    final OXContextExtensionImpl contextExtension = (OXContextExtensionImpl) ctx.getFirstExtensionByName(OXContextExtensionImpl.class.getName());
                    final ResellerAdmin[] data = oxresell.getData(new ResellerAdmin[] { new ResellerAdmin(contextExtension.getSid()) });
                    contextExtension.setOwner(data[0]);
                    contextExtension.setRestriction(oxresell.getRestrictionsFromContext(ctx));
                    contextExtension.setCustomid(oxresell.getCustomId(ctx));
                    retval.add(contextExtension);
                    ctx.removeExtension(contextExtension);
                } catch (final StorageException e) {
                    throw new PluginException(e);
                }
            }
        }
        return retval;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#getModuleAccess(com.openexchange.admin.rmi.dataobjects.Context,
     * com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public UserModuleAccess getModuleAccess(final Context ctx, final Credentials auth) throws PluginException {
        if (cache.isMasterAdmin(auth)) {
            return null;
        }
        checkOwnerShipAndSetSid(ctx, auth);
        return null;
    }

    /**
     *
     * @see com.openexchange.admin.lib.plugins.OXContextPluginInterface#list(java.lang.String,
     * com.openexchange.admin.rmi.dataobjects.dataobjects.Credentials)
     */
    public Filter<Context, Context> list(final String search_pattern, final Credentials auth) throws PluginException {
        return new ResellerExtensionLoader(cache);
    }

    public Filter<Integer, Integer> filter(final Credentials auth) throws PluginException {
        try {
            if( ! ClientAdminThreadExtended.cache.isMasterAdmin(auth) ) {
                ResellerAdmin adm = null;
                adm = (this.oxresell.getData(new ResellerAdmin[]{ new ResellerAdmin(auth.getLogin()) } ))[0];
                return new ResellerContextFilter(cache, adm);
            }
            return null;
        } catch (StorageException e) {
            throw new PluginException(e);
        }
    }
    
    private void applyRestrictionsPerContext(final Context ctx) throws PluginException {
        // Handle the extension...
        final OXContextExtensionImpl firstExtensionByName = (OXContextExtensionImpl) ctx.getFirstExtensionByName(OXContextExtensionImpl.class.getName());
        if (null != firstExtensionByName) {
            final HashSet<Restriction> restrictions = firstExtensionByName.getRestriction();
            try {
                checkRestrictionsPerContext(restrictions, this.oxresell);
                this.oxresell.applyRestrictionsToContext(restrictions, ctx);
            } catch (final StorageException e) {
                throw new PluginException(e);
            } catch (final InvalidDataException e) {
                throw new PluginException(e);
            } catch (final OXResellerException e) {
                throw new PluginException(e.getMessage());
            }
        }
    }

    /**
     * Check whether context is owned by owner specified in {@link Credentials}. Throw {@link PluginException} if not.
     * 
     * @param ctx
     * @param auth
     * @throws PluginException
     */
    private void checkOwnerShipAndSetSid(final Context ctx, final Credentials auth) throws PluginException {
        try {
            if (!oxresell.checkOwnsContextAndSetSid(ctx, auth)) {
                throw new PluginException("ContextID " + ctx.getId() + " does not belong to " + auth.getLogin());
            }
        } catch (final StorageException e) {
            throw new PluginException(e);
        }
    }
    
    /**
     * Check whether creator supplied any {@link Restriction} and check if those exist within the database. If, add the corresponding
     * Restriction id. Check whether Restrictions can be applied to context. If not, throw {@link InvalidDataException} or
     * {@link StorageException} if there are no Restrictions defined within the database. Check whether Restrictions contain duplicate
     * Restriction entries and throws {@link InvalidDataException} if that is the case.
     * 
     * @param restrictions
     * @param storageInterface TODO
     * @throws StorageException
     * @throws InvalidDataException
     * @throws OXResellerException
     */
    private void checkRestrictionsPerContext(final HashSet<Restriction> restrictions, OXResellerStorageInterface storageInterface) throws StorageException, InvalidDataException, OXResellerException {
        final Map<String, Restriction> validRestrictions = storageInterface.listRestrictions("*");
        if (validRestrictions == null || validRestrictions.size() <= 0) {
            throw new OXResellerException(Code.UNABLE_TO_LOAD_AVAILABLE_RESTRICTIONS_FROM_DATABASE);
        }

        if (null != restrictions) {
            OXResellerTools.checkRestrictions(restrictions, validRestrictions, "context", new ClosureInterface() {
                public boolean checkAgainstCorrespondingRestrictions(final String rname) {
                    return !(rname.equals(Restriction.MAX_USER_PER_CONTEXT) || rname.startsWith(Restriction.MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX));
                }
            });
        }
    }

    public Boolean checkMandatoryMembersContextCreate(Context ctx) throws PluginException {
        return new Boolean(true);
    }

}
