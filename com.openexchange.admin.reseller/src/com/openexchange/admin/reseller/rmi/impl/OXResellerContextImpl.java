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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.admin.plugins.PluginException;
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
public class OXResellerContextImpl implements OXContextPluginInterface {

    private final Log log = LogFactory.getLog(this.getClass());
    
    private OXResellerStorageInterface oxresell = null;

    /**
     * @throws StorageException 
     * 
     */
    public OXResellerContextImpl() throws StorageException {
        oxresell = OXResellerStorageInterface.getInstance();
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#change(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void change(Context ctx, Credentials auth) throws PluginException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#changeModuleAccess(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.UserModuleAccess, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void changeModuleAccess(Context ctx, UserModuleAccess access, Credentials auth) throws PluginException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#changeModuleAccess(com.openexchange.admin.rmi.dataobjects.Context, java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void changeModuleAccess(Context ctx, String access_combination_name, Credentials auth) throws PluginException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#create(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.User, java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Context create(Context ctx, User admin_user, Credentials auth) throws PluginException {
        return create(ctx, admin_user, null, auth);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#create(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.User, java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Context create(Context ctx, User admin_user, String access_combination_name, Credentials auth) throws PluginException {
        try {
            oxresell.ownContextToAdmin(ctx, auth);
        } catch (StorageException e) {
            log.error(e.getMessage(),e);
            throw new PluginException(e);
        }
        return ctx;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#delete(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void delete(Context ctx, Credentials auth) throws PluginException {
        try {
            if( oxresell.ownsContext(ctx, auth) ) {
                oxresell.unownContextFromAdmin(ctx, auth);
            } else {
                throw new PluginException("ContextID " + ctx.getId() + " does not belong to " + auth.getLogin());
            }
        } catch (StorageException e) {
            log.error(e.getMessage(),e);
            throw new PluginException(e);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#disable(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void disable(Context ctx, Credentials auth) throws PluginException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#disableAll(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void disableAll(Credentials auth) throws PluginException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#downgrade(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void downgrade(Context ctx, Credentials auth) throws PluginException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#enable(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void enable(Context ctx, Credentials auth) throws PluginException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#enableAll(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void enableAll(Credentials auth) throws PluginException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#getAccessCombinationName(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public String getAccessCombinationName(Context ctx, Credentials auth) throws PluginException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#getData(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Context getData(Context ctx, Credentials auth) throws PluginException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#getModuleAccess(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public UserModuleAccess getModuleAccess(Context ctx, Credentials auth) throws PluginException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.plugins.OXContextPluginInterface#list(java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Context[] list(String search_pattern, Credentials auth) throws PluginException {
        return new Context[]{new Context(1)};
    }

}
