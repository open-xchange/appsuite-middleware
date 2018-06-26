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

package com.openexchange.admin.rmi.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;

/**
 * {@link MaintenanceReasonManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MaintenanceReasonManager extends AbstractManager {

    private static final Logger LOG = LoggerFactory.getLogger(MaintenanceReasonManager.class);

    private static MaintenanceReasonManager INSTANCE;

    /**
     * Gets the instance of the {@link MaintenanceReasonManager}
     * 
     * @param host The rmi host
     * @param masterCredentials the master {@link Credentials}
     * @return The {@link MaintenanceReasonManager} instance
     */
    public static MaintenanceReasonManager getInstance(String host, Credentials masterCredentials) {
        if (INSTANCE == null) {
            INSTANCE = new MaintenanceReasonManager(host, masterCredentials);
        }
        return INSTANCE;
    }

    /**
     * Initialises a new {@link MaintenanceReasonManager}.
     * 
     * @param rmiEndPointURL
     * @param masterCredentials
     */
    public MaintenanceReasonManager(String rmiEndPointURL, Credentials masterCredentials) {
        super(rmiEndPointURL, masterCredentials);
    }

    /**
     * Creates a maintenance reason
     * 
     * @param maintenanceReason The {@link MaintenanceReason} to create
     * @return The created {@link MaintenanceReason}
     * @throws Exception if an error is occurred
     */
    public MaintenanceReason create(MaintenanceReason maintenanceReason) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        MaintenanceReason mr = utilInterface.createMaintenanceReason(maintenanceReason, getMasterCredentials());
        managedObjects.put(mr.getId(), mr);
        return mr;
    }

    /**
     * Lists all maintenance reasons that match the specified pattern
     * 
     * @return An array with all maintenance reasons that match the specified pattern
     * @throws Exception if an error is occurred
     */
    public MaintenanceReason[] search(String pattern) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        return utilInterface.listMaintenanceReason(pattern, getMasterCredentials());
    }

    /**
     * Deletes the specified {@link MaintenanceReason}
     * 
     * @param maintenanceReason The {@link MaintenanceReason} to delete
     * @throws Exception if an error is occurred
     */
    public void delete(MaintenanceReason maintenanceReason) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        utilInterface.deleteMaintenanceReason(new MaintenanceReason[] { maintenanceReason }, getMasterCredentials());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.manager.AbstractManager#clean(java.lang.Object)
     */
    @Override
    boolean clean(Object object) {
        if (!(object instanceof MaintenanceReason)) {
            LOG.error("The specified object is not of type MaintenanceReason", object.toString());
            return false;
        }

        MaintenanceReason mr = (MaintenanceReason) object;
        try {
            delete(mr);
            return true;
        } catch (Exception e) {
            LOG.error("The maintenance reason '{}' could not be deleted!", mr.getId(), e);
            return false;
        }
    }
}
