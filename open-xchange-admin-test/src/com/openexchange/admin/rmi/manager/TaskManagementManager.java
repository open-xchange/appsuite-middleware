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

import com.openexchange.admin.rmi.OXTaskMgmtInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * {@link TaskManagementManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class TaskManagementManager extends AbstractManager {

    private static TaskManagementManager INSTANCE;

    /**
     * Gets the instance of the {@link TaskManagementManager}
     * 
     * @param host
     * @param masterCredentials
     * @return
     */
    public static TaskManagementManager getInstance(String host, Credentials masterCredentials) {
        if (INSTANCE == null) {
            INSTANCE = new TaskManagementManager(host, masterCredentials);
        }
        return INSTANCE;
    }

    /**
     * Initialises a new {@link TaskManagementManager}.
     * 
     * @param rmiEndPointURL
     * @param masterCredentials
     */
    private TaskManagementManager(String rmiEndPointURL, Credentials masterCredentials) {
        super(rmiEndPointURL, masterCredentials);
    }

    /**
     * Gets the result from the task with the specified id
     * 
     * @param context The {@link Context}
     * @param jobId The job identifier
     * @param adminCredentials The context admin credentials
     * @return an object which has to be casted to the return value specified in the method which adds the job
     * @throws Exception if an error is occurred
     */
    public Object getTaskResults(Context context, int jobId, Credentials adminCredentials) throws Exception {
        OXTaskMgmtInterface taskManagementInterface = getTaskManagementInterface();
        return taskManagementInterface.getTaskResults(context, adminCredentials, jobId);
    }

    /**
     * Deletes finished jobs from the list
     * 
     * @param context The {@link Context}
     * @param jobId The job identifier
     * @param adminCredentials The context admin credentials
     * @throws Exception if an error is occurred
     */
    public void deleteJob(Context context, int jobId, Credentials adminCredentials) throws Exception {
        OXTaskMgmtInterface taskManagementInterface = getTaskManagementInterface();
        taskManagementInterface.deleteJob(context, adminCredentials, jobId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.manager.AbstractManager#clean(java.lang.Object)
     */
    @Override
    void clean(Object object) {
        // No clean-up applicable
    }

    /**
     * Returns the {@link OXTaskMgmtInterface}
     * 
     * @return The {@link OXTaskMgmtInterface}
     * @throws Exception if an error is occurred during RMI look-up
     */
    OXTaskMgmtInterface getTaskManagementInterface() throws Exception {
        return (OXTaskMgmtInterface) getRemoteInterface(OXTaskMgmtInterface.RMI_NAME, OXTaskMgmtInterface.class);
    }
}
