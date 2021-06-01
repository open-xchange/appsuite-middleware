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
import com.openexchange.groupware.update.TaskStatus;
import com.openexchange.groupware.update.UpdateTaskService;

/**
 * {@link UpdateTaskRunAllUpdateCLT} - Command-Line access to run update process for a certain schema via update task toolkit.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class UpdateTaskRunAllUpdateCLT extends AbstractUpdateTasksCLT<Void> {

    /**
     * Entry point
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new UpdateTaskRunAllUpdateCLT().execute(args);
    }

    boolean failOnError = false;

    /**
     * Initializes a new {@link UpdateTaskRunAllUpdateCLT}.
     */
    private UpdateTaskRunAllUpdateCLT() {
        super("runallupdate [-e] " + BASIC_MASTER_ADMIN_USAGE, "Runs the update on all schemas.");
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createSwitch("e", "error", "The flag indicating whether process is supposed to be stopped if an error occurs when trying to update a schema.", false));
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        UpdateTaskService updateTaskService = getRmiStub(UpdateTaskService.RMI_NAME);
        TaskStatus taskStatus = updateTaskService.runAllUpdates(failOnError);
        System.out.println("Scheduled an asynchronous job with id: " + taskStatus.getJobId() + "\n" + taskStatus.getStatusText());
        return null;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (cmd.hasOption("error")) {
            failOnError = true;
        }
    }
}
