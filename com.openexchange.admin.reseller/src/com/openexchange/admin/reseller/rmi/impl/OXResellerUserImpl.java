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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.admin.plugins.OXUserPluginInterface;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.storage.interfaces.OXResellerStorageInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * @author choeger
 *
 */
public class OXResellerUserImpl implements OXUserPluginInterface {

    private final Log log = LogFactory.getLog(this.getClass());
    
    private OXResellerStorageInterface oxresell = null;

    /**
     * @throws StorageException 
     * 
     */
    public OXResellerUserImpl() throws StorageException {
        oxresell = OXResellerStorageInterface.getInstance();
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXUserPluginInterface#canHandleContextAdmin()
     */
    @Override
    public boolean canHandleContextAdmin() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXUserPluginInterface#change(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.User, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public void change(Context ctx, User usrdata, Credentials auth) throws PluginException {
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXUserPluginInterface#create(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.User, com.openexchange.admin.rmi.dataobjects.UserModuleAccess, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public void create(Context ctx, User usr, UserModuleAccess access, Credentials cred) throws PluginException {
        try {
            final ResellerAdmin owner = oxresell.getContextOwner(ctx);
            if( 0 == owner.getId().intValue() ) {
                // if context has no owner, restriction checks cannot be done and
                // context has been created by master admin
                return;
            }
            //long tstart = System.currentTimeMillis();
            oxresell.checkPerContextRestrictions(ctx, access,
                Restriction.MAX_USER_PER_CONTEXT,
                Restriction.MAX_OVERALL_USER_PER_SUBADMIN,
                Restriction.MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX,
                Restriction.MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX);
            //long tend = System.currentTimeMillis();
            //System.out.println("Time: " + (tend - tstart) + " ms");
        } catch (StorageException e) {
            log.error(e.getMessage(),e);
            throw new PluginException(e);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXUserPluginInterface#delete(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.User[], com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public void delete(Context ctx, User[] user, Credentials cred) throws PluginException {
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXUserPluginInterface#getData(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.User[], com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public User[] getData(Context ctx, User[] users, Credentials cred) {
        // pass-through
        return users;
    }

}
