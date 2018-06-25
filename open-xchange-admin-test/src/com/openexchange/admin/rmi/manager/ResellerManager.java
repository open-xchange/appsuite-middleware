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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.admin.rmi.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * {@link ResellerManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ResellerManager extends AbstractManager {

    private static ResellerManager INSTANCE;

    /**
     * Gets the instance of the {@link ResellerManager}
     * 
     * @param host
     * @param masterCredentials
     * @return
     */
    public static ResellerManager getInstance(String host, Credentials masterCredentials) {
        if (INSTANCE == null) {
            INSTANCE = new ResellerManager(host, masterCredentials);
        }
        return INSTANCE;
    }

    private static final Logger LOG = LoggerFactory.getLogger(ContextManager.class);

    /**
     * Initialises a new {@link ResellerManager}.
     * 
     * @param rmiEndPointURL
     * @param masterCredentials
     */
    public ResellerManager(String rmiEndPointURL, Credentials masterCredentials) {
        super(rmiEndPointURL, masterCredentials);
    }

    /**
     * Creates the specified {@link ResellerAdmin}
     * 
     * @param resellerAdmin The {@link ResellerAdmin} to create
     * @return The created {@link ResellerAdmin}
     * @throws Exception if an error is occurred
     */
    public ResellerAdmin create(ResellerAdmin resellerAdmin) throws Exception {
        OXResellerInterface resellerInterface = getResellerInterface();
        return resellerInterface.create(resellerAdmin, getMasterCredentials());
    }

    /**
     * Changes/Updates the specified {@link ResellerAdmin}
     * 
     * @param resellerAdmin The {@link ResellerAdmin} to change
     * @throws Exception if an error is occurred
     */
    public void change(ResellerAdmin resellerAdmin) throws Exception {
        OXResellerInterface resellerInterface = getResellerInterface();
        resellerInterface.change(resellerAdmin, getMasterCredentials());
    }

    /**
     * Fetches all data for the specified {@link ResellerAdmin}
     * 
     * @param resellerAdmin The {@link ResellerAdmin} to fetch the data
     * @return The data of the {@link ResellerAdmin}
     * @throws Exception if an error is occurred
     */
    public ResellerAdmin getData(ResellerAdmin resellerAdmin) throws Exception {
        OXResellerInterface resellerInterface = getResellerInterface();
        return resellerInterface.getData(resellerAdmin, getMasterCredentials());
    }

    /**
     * Retrieve a list of all restrictions applied to given {@link Context}
     * 
     * @param context The {@link Context} for which to retrieve the restrictions
     * @return An array with all restrictions applied to the specified {@link Context}
     * @throws Exception if an error is occurred
     */
    public Restriction[] getContextRestrictions(Context context) throws Exception {
        OXResellerInterface resellerInterface = getResellerInterface();
        return resellerInterface.getRestrictionsFromContext(context, getMasterCredentials());
    }

    /**
     * Deletes the specified {@link ResellerAdmin}
     * 
     * @param resellerAdmin The {@link ResellerAdmin} to delete
     * @throws Exception if an error is occurred
     */
    public void delete(ResellerAdmin resellerAdmin) throws Exception {
        OXResellerInterface resellerInterface = getResellerInterface();
        resellerInterface.delete(resellerAdmin, getMasterCredentials());
    }

    /**
     * Returns an array with all found {@link ResellerAdmin}s that match the
     * specified search pattern
     * 
     * @param searchPattern The search pattern
     * @return an array with all found {@link ResellerAdmin}s that match the
     *         specified search pattern
     * @throws Exception if an error is occurred
     */
    public ResellerAdmin[] search(String searchPattern) throws Exception {
        OXResellerInterface resellerInterface = getResellerInterface();
        return resellerInterface.list(searchPattern, getMasterCredentials());
    }

    /**
     * Update all restrictions based on module access combinations in case of changes to
     * <code>/opt/open-xchange/etc/admindaemon/ModuleAccessDefinitions.properties</code>
     * 
     * @throws Exception if an error is occurred
     */
    public void updateDatabaseModuleAccessRestrictions() throws Exception {
        OXResellerInterface resellerInterface = getResellerInterface();
        resellerInterface.updateDatabaseModuleAccessRestrictions(getMasterCredentials());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.manager.AbstractManager#clean(java.lang.Object)
     */
    @Override
    boolean clean(Object object) {
        if (!(object instanceof ResellerAdmin)) {
            LOG.error("The specified object is not of type ResellerAdmin", object.toString());
            return false;
        }

        ResellerAdmin resellerAdmin = (ResellerAdmin) object;
        try {
            delete(resellerAdmin);
            return true;
        } catch (Exception e) {
            LOG.error("The reseller admin '{}' could not be deleted!", resellerAdmin.getId(), e);
            return false;
        }
    }
    //////////////////////////// RMI LOOK-UPS //////////////////////////////

    /**
     * Returns the {@link OXResellerInterface}
     * 
     * @return The {@link OXResellerInterface}
     * @throws Exception if an error is occurred during RMI look-up
     */
    private OXResellerInterface getResellerInterface() throws Exception {
        return (OXResellerInterface) getRemoteInterface(OXResellerInterface.RMI_NAME, OXResellerInterface.class);
    }
}
