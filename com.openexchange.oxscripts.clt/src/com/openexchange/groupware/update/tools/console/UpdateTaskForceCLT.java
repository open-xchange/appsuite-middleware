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

/**
 * {@link UpdateTaskForceCLT} - Command-Line access to reset version via update task toolkit.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class UpdateTaskForceCLT extends AbstractUpdateTasksCLT<Void> {

    /**
     * Entry point
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new UpdateTaskForceCLT().execute(args);
    }

    private String schemaName;
    private String className;
    private int contextId = -1;

    private static final String FOOTER = "Force (re-)run of update task denoted by given class name on a specific schema or on all schemata or on a specific context.";

    /**
     * Initializes a new {@link UpdateTaskForceCLT}.
     */
    private UpdateTaskForceCLT() {
        super("forceupdatetask -t <taskName> [-c <contextId> | -n <schemaName>] " + BASIC_MASTER_ADMIN_USAGE, FOOTER);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("t", "task", "taskName", "The update task's class name", false));

        StringBuilder sb = new StringBuilder(128);
        sb.append("A valid context identifier contained in target schema;");
        sb.append(" if missing and '-n/--name' option is also absent all schemas are considered.");
        options.addOption(createArgumentOption("c", "context", "contextId", sb.toString(), false));

        sb.setLength(0);
        sb.append("A valid schema name. This option is a substitute for '-c/--context' option.");
        sb.append(" If both are present '-c/--context' is preferred. If both absent all schemas are considered.");
        options.addOption(createArgumentOption("n", "name", "schemaName", sb.toString(), false));
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        UpdateTaskService updateTaskService = getRmiStub(UpdateTaskService.RMI_NAME);
        boolean noName = (null == schemaName);
        if (noName && (-1 == contextId)) {
            updateTaskService.forceUpdateTaskOnAllSchemata(className);
            return null;
        }
        if (noName) {
            updateTaskService.forceUpdateTask(contextId, className);
            return null;
        }
        updateTaskService.forceUpdateTask(schemaName, className);
        return null;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (!cmd.hasOption('t')) {
            System.err.println("Missing update task's class name.");
            printHelp();
            System.exit(1);
        } else {
            className = cmd.getOptionValue('t');
        }
        if (cmd.hasOption('c')) {
            String optionValue = cmd.getOptionValue('c');
            try {
                contextId = Integer.parseInt(optionValue.trim());
            } catch (NumberFormatException e) {
                System.err.println("Context identifier parameter is not a number: " + optionValue);
                printHelp();
                System.exit(1);
            }
        } else {
            if (cmd.hasOption('n')) {
                schemaName = cmd.getOptionValue('n');
            }
        }
    }
}
