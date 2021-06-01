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

import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import com.openexchange.groupware.update.UpdateTaskService;
import com.openexchange.groupware.update.tools.console.comparators.NameComparator;
import com.openexchange.tools.console.TableWriter.ColumnFormat;
import com.openexchange.tools.console.TableWriter.ColumnFormat.Align;

/**
 * {@link ListUpdateTasksCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.10.2
 */
public final class ListUpdateTasksCLT extends AbstractUpdateTasksCLT<Void> {

    /**
     * Entry point
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new ListUpdateTasksCLT().execute(args);
    }

    private static final ColumnFormat[] FORMATS = { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) };
    private static final String[] COLUMNS_PENDING = { "taskName", "state", "reason" };
    private static final String[] COLUMNS_EXECUTED = { "taskName", "successful", "lastModified" };

    private boolean excludedViaProperties;
    private boolean excludedViaNamespace;
    private boolean pending;
    private boolean run;
    private ColumnFormat[] formats;
    private String[] columns;
    private String schemaName;

    /**
     * Initialises a new {@link ListUpdateTasksCLT}.
     */
    public ListUpdateTasksCLT() {
        //@formatter:off
        super("listUpdateTasks [[-a | -g | -e | -x | -xf | -xn] -n <schemaName> -A <masterAdminUser> -P <masterAdminPassword> [-p <port> -s <server> --responsetime <responseTime>]] | -h", 
           "Lists executed, pending and excluded update tasks of a schema specified by the '-n' switch (mandatory). The switches '-a', '-e', '-g', '-x', '-xf' and '-xn' are mutually exclusive AND mandatory."
            + "\n\n An overall database status of all schemata can be retrieved via the 'checkdatabase' command line tool.\n\n"
            + "An update task may be in 'pending' state for one of the following reasons:"
            + "\n  a) It was never executed before and is due for execution. Use the 'runupdate' tool to run all (previously never executed) pending tasks."
            + "\n  b) It is excluded via a namespace. Use the 'listUpdateTaskNamespaces' tool to list all namespaces."
            + "\n  c) It is excluded via an entry in the 'excludeupdatetasks.properties'."
            + "\n  d) It was not successfully executed. Use the 'forceupdatetask' tool to re-run the failed task.");
        //@formatter:on
    }

    @Override
    protected void addOptions(Options options) {
        Option schemaOption = createArgumentOption("n", "name", "schemaName", "A valid schema name.", true);
        schemaOption.setType(String.class);
        options.addOption(schemaOption);

        OptionGroup optionGroup = new OptionGroup();
        optionGroup.addOption(createOption("a", "all", false, "Lists all pending and excluded update tasks (both via excludeupdatetasks.properties' file and namespace)", false));
        optionGroup.addOption(createOption("g", "pending", false, "Lists only the pending update tasks, i.e. those that were never executed but are due for execution or were executed at least once and failed.", false));
        optionGroup.addOption(createOption("x", "excluded", false, "Lists only the update tasks excluded both via excludedupdate.properties' file and namespace", false));
        optionGroup.addOption(createOption("xf", "excluded-via-file", false, "Lists only the update tasks excluded via 'excludeupdatetasks.properties' file", false));
        optionGroup.addOption(createOption("xn", "excluded-via-namespace", false, "Lists only the update tasks excluded via namespace", false));
        optionGroup.addOption(createOption("e", "executed", false, "Lists all executed (ran at least once) update tasks of a schema", false));
        optionGroup.setRequired(true);
        options.addOptionGroup(optionGroup);
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        UpdateTaskService updateTaskService = getRmiStub(UpdateTaskService.RMI_NAME);
        List<Map<String, Object>> taskList = run ? updateTaskService.getExecutedTasksList(schemaName) : updateTaskService.getPendingTasksList(schemaName, pending, excludedViaProperties, excludedViaNamespace);
        writeCompositeList(taskList, columns, formats, new NameComparator());
        return null;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (!cmd.hasOption('n')) {
            System.err.println("Schema name must be defined.");
            printHelp();
            System.exit(1);
        }

        columns = COLUMNS_PENDING;
        formats = FORMATS;
        schemaName = cmd.getOptionValue('n');

        if (cmd.hasOption('e')) {
            run = true;
            columns = COLUMNS_EXECUTED;
            formats = FORMATS;
            return;
        }

        if (cmd.hasOption('a')) {
            pending = excludedViaProperties = excludedViaNamespace = true;
            return;
        }

        pending = cmd.hasOption('g');
        excludedViaProperties = cmd.hasOption("xf");
        excludedViaNamespace = cmd.hasOption("xn");
        if (cmd.hasOption('x')) {
            excludedViaProperties = excludedViaNamespace = true;
        }
    }
}
