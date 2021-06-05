/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
