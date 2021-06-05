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

package com.openexchange.groupware.update.tools.console;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.groupware.update.UpdateTaskService;
import com.openexchange.java.Strings;

/**
 * {@link GetUpdateTaskStatusCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class GetUpdateTaskStatusCLT extends AbstractUpdateTasksCLT<Void> {

    /**
     * Entry point
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new GetUpdateTaskStatusCLT().execute(args);
    }

    /**
     * Initialises a new {@link GetUpdateTaskStatusCLT}.
     */
    public GetUpdateTaskStatusCLT() {
        //@formatter:off
        super("getUpdateTaskStatus -j <job-id> " + BASIC_MASTER_ADMIN_USAGE, "Retrieves the status of a running or completed update task job.  If the job already finished and its "
            + "status has not yet retrieved, then its status will be returned and it will be removed from the pool. A further invocation of this tool will "
            + "yield no results for the same job identifier. If the job is still running then its invocation will return its current status.");
        //@formatter:on
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("j", "job-id", "job-id", "The job identifier for which to retrieve its status.", true));
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        UpdateTaskService updateTaskService = getRmiStub(UpdateTaskService.RMI_NAME);
        String jobId = cmd.getOptionValue('j');
        String jobStatus = updateTaskService.getJobStatus(jobId);
        if (jobStatus == null) {
            System.out.println("No job found for id '" + jobId + "'.");
            return null;
        }
        System.out.println(Strings.replaceSequenceWith(jobStatus, "\\R", Strings.getLineSeparator()));
        return null;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        //no-op
    }
}
