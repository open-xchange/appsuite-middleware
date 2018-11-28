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

    private static final ColumnFormat[] FORMATS_PENDING = { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) };
    private static final String[] COLUMNS_PENDING = { "taskName", "state" };

    private static final ColumnFormat[] FORMATS_EXECUTED = { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) };
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
        super("listUpdateTasks [[-e | -r | -x | -xf | -xn] -A <adminUser> -P <adminPassword> [-p <port> -s <server> --responsetime <responseTime>]] | -h", "Lists executed, pending and excluded update tasks of a schema. The switches '-a', '-e', '-r', '-x', '-xf' and '-xn' are mutually exclusive AND mandatory.\n\n An overall database status of all schemata can be retrieved via the 'checkdatabase' command line tool.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#addOptions(org.apache.commons.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        Option schemaOption = new Option("n", "name", true, "A valid schema name.");
        schemaOption.setType(String.class);
        options.addOption(schemaOption);

        OptionGroup optionGroup = new OptionGroup();
        optionGroup.addOption(createOption("a", "all", false, "Lists all pending and excluded update tasks (both via excludedupdate.properties' file and namespace)", false));
        optionGroup.addOption(createOption("e", "pending", false, "Lists only the pending update tasks", false));
        optionGroup.addOption(createOption("x", "excluded", false, "Lists only the update tasks excluded both via excludedupdate.properties' file and namespace", false));
        optionGroup.addOption(createOption("xf", "excluded-via-file", false, "Lists only the update tasks excluded via 'excludedupdate.properties' file", false));
        optionGroup.addOption(createOption("xn", "excluded-via-namespace", false, "Lists only the update tasks excluded via namespace", false));
        optionGroup.addOption(createOption("r", "executed", false, "Lists executed update tasks of a schema", false));
        optionGroup.setRequired(true);
        options.addOptionGroup(optionGroup);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        UpdateTaskService updateTaskService = getRmiStub(UpdateTaskService.RMI_NAME);
        List<Map<String, Object>> taskList = run ? updateTaskService.getExecutedTasksList(schemaName) : updateTaskService.getPendingTasksList(schemaName, pending, excludedViaProperties, excludedViaNamespace);
        writeCompositeList(taskList, columns, formats, new NameComparator());
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#checkOptions(org.apache.commons.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        if (!cmd.hasOption('n')) {
            System.err.println("Schema name must be defined.");
            printHelp();
            System.exit(1);
        }

        columns = COLUMNS_PENDING;
        formats = FORMATS_PENDING;
        schemaName = cmd.getOptionValue('n');

        if (cmd.hasOption('r')) {
            run = true;
            columns = COLUMNS_EXECUTED;
            formats = FORMATS_EXECUTED;
            return;
        }

        if (cmd.hasOption('a')) {
            pending = excludedViaProperties = excludedViaNamespace = true;
            return;
        }

        pending = cmd.hasOption('e');
        excludedViaProperties = cmd.hasOption("xf");
        excludedViaNamespace = cmd.hasOption("xn");
        if (cmd.hasOption('x')) {
            excludedViaProperties = excludedViaNamespace = true;
        }
    }
}
