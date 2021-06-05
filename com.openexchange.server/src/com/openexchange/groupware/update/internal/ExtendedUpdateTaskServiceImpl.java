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

package com.openexchange.groupware.update.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.slf4j.Logger;
import com.google.common.collect.ImmutableList;
import com.openexchange.database.SchemaInfo;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.ExtendedUpdateTaskService;
import com.openexchange.groupware.update.TaskFailure;
import com.openexchange.groupware.update.TaskInfo;
import com.openexchange.groupware.update.tools.UpdateTaskToolkit;

/**
 * {@link ExtendedUpdateTaskServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ExtendedUpdateTaskServiceImpl extends UpdateTaskServiceImpl implements ExtendedUpdateTaskService {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ExtendedUpdateTaskServiceImpl.class);
    }

    /**
     * Initialises a new {@link ExtendedUpdateTaskServiceImpl}.
     */
    public ExtendedUpdateTaskServiceImpl() {
        super();
    }

    @Override
    public List<TaskFailure> runUpdateFor(int contextId) throws OXException {
        try {
            UpdateProcess updateProcess = new UpdateProcess(contextId, true, false);
            updateProcess.runUpdate();
            return getFailuresFrom(updateProcess);
        } catch (RuntimeException | Error e) {
            LoggerHolder.LOG.error("", e);
            throw e;
        }
    }

    @Override
    public List<TaskFailure> runUpdateFor(String schemaName) throws OXException {
        try {
            SchemaInfo schema = UpdateTaskToolkit.getInfoBySchemaName(schemaName);
            UpdateProcess updateProcess = new UpdateProcess(schema.getPoolId(), schema.getSchema(), true, false);
            updateProcess.runUpdate();
            return getFailuresFrom(updateProcess);
        } catch (RuntimeException | Error e) {
            LoggerHolder.LOG.error("", e);
            throw e;
        }
    }

    /**
     * Returns any failures the update task encountered
     *
     * @param updateProcess the {@link UpdateProcess}
     * @return A {@link List} with all failed tasks
     */
    private List<TaskFailure> getFailuresFrom(UpdateProcess updateProcess) {
        // Return possible failures
        Queue<TaskInfo> failures = updateProcess.getFailures();
        if (failures == null || failures.isEmpty()) {
            return ImmutableList.of();
        }

        List<TaskFailure> failuresList = new ArrayList<>(failures.size());
        for (TaskInfo taskInfo : failures) {
            failuresList.add(TaskFailure.builder().withTaskName(taskInfo.getTaskName()).withClassName(taskInfo.getClass().getName()).withSchemaName(taskInfo.getSchema()).build());
        }
        return failuresList;
    }
}
