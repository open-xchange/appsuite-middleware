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

package com.openexchange.gdpr.dataexport.clt;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.gdpr.dataexport.rmi.DataExportRMIService;
import com.openexchange.java.Strings;
import com.openexchange.tools.console.TableWriter;
import com.openexchange.tools.console.TableWriter.ColumnFormat;
import com.openexchange.tools.console.TableWriter.ColumnFormat.Align;

/**
 * {@link DataExportListCLT}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportListCLT extends AbstractRmiCLI<Void> {

    /**
     * Entry point
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        new DataExportListCLT().execute(args);
    }

    private static final ColumnFormat[] FORMATS_TASK = { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) };
    private static final String[] COLUMNS_TASK = { DataExportRMIService.COLUMN_ID, DataExportRMIService.COLUMN_USER, DataExportRMIService.COLUMN_CONTEXT, DataExportRMIService.COLUMN_CREATION_TIME, DataExportRMIService.COLUMN_START_TIME, DataExportRMIService.COLUMN_STATUS };

    private static final ColumnFormat[] FORMATS_WORK_ITEM = { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) };
    private static final String[] COLUMNS_WORK_ITEM = { DataExportRMIService.COLUMN_ID, DataExportRMIService.COLUMN_MODULE, DataExportRMIService.COLUMN_STATUS, DataExportRMIService.COLUMN_INFO, DataExportRMIService.COLUMN_LOCATION };

    private static final ColumnFormat[] FORMATS_RESULT_FILE = { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) };
    private static final String[] COLUMNS_RESULT_FILE = { DataExportRMIService.COLUMN_NUMBER, DataExportRMIService.COLUMN_CONTENT_TYPE, DataExportRMIService.COLUMN_SIZE, DataExportRMIService.COLUMN_LOCATION };

    private int contextId;
    private int userId;

    /**
     * Initializes a new {@link DataExportListCLT}.
     */
    public DataExportListCLT() {
        super();
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        // Try context administrator authentication first
        if (contextId > 0) {
            try {
                authenticator.doAuthentication(login, password, contextId);
                return;
            } catch (RemoteException e) {
                if (e.getMessage() == null || Strings.asciiLowerCase(e.getMessage()).indexOf("authentication failed") < 0) {
                    throw e;
                }
                // Context administrator authentication failed. Try with master authentication
            }
        }

        // Master authentication
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("c", "context", "contextId", "The context identifier. If context identifier is given, only data export tasks associated with denoted context are listed; otherwise all data export tasks are listed.", false));
        options.addOption(createArgumentOption("i", "userid", "userId", "The user identifier. If user identifier is given, only the data export task associated with denoted user is listed. This user-sensitive output also includes the task-associated work items and result files.", false));
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (cmd.hasOption("c")) {
            contextId = parseInt('c', -1, cmd, options);
        }
        if (cmd.hasOption("i")) {
            userId = parseInt('i', -1, cmd, options);
        }
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        DataExportRMIService rmiService = getRmiStub(optRmiHostName, DataExportRMIService.RMI_NAME);

        if (contextId > 0) {
            if (userId > 0) {
                // List for specific user
                Map<String, Object> dataExportTask = rmiService.getDataExportTask(userId, contextId);
                writeCompositeList(Collections.singletonList(dataExportTask), COLUMNS_TASK, FORMATS_TASK);
                List<Map<String, Object>> workItems = (List<Map<String, Object>>) dataExportTask.get(DataExportRMIService.COLUMN_WORK_ITEMS);
                if (!workItems.isEmpty()) {
                    System.out.println();
                    System.out.println(" Work items");
                    System.out.println("--------------");
                    writeCompositeList(workItems, COLUMNS_WORK_ITEM, FORMATS_WORK_ITEM);
                }
                List<Map<String, Object>> resultFiles = (List<Map<String, Object>>) dataExportTask.get(DataExportRMIService.COLUMN_RESULT_FILES);
                if (!resultFiles.isEmpty()) {
                    System.out.println();
                    System.out.println(" Result files");
                    System.out.println("--------------");
                    writeCompositeList(resultFiles, COLUMNS_RESULT_FILE, FORMATS_RESULT_FILE);
                }
            } else {
                // List for specific context
                List<Map<String, Object>> tasks = rmiService.getDataExportTasks(contextId);
                writeCompositeList(tasks, COLUMNS_TASK, FORMATS_TASK);
            }
        } else {
            // List them all
            List<Map<String, Object>> tasks = rmiService.getDataExportTasks();
            writeCompositeList(tasks, COLUMNS_TASK, FORMATS_TASK);
        }

        return null;
    }

    @Override
    protected String getFooter() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("Command line tool to list data exports\n");
        sb.append("\n");
        sb.append("Examples\n");
        sb.append("====================================\n");
        sb.append("List all data exports:\n");
        sb.append("\n");
        sb.append("  $ listdataexports -A oxadminmaster -P secret\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("List all data exports of a certain context:\n");
        sb.append("\n");
        sb.append("  $ listdataexports -c 1234 -A oxadminmaster -P secret\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("List the data export of a certain user. This outputs includes task-associated work items and result files (if any):\n");
        sb.append("\n");
        sb.append("  $ listdataexports -c 1234 -i 3 -A oxadminmaster -P secret\n");
        return sb.toString();
    }

    @Override
    protected String getName() {
        return "listdataexports [-c <contextId>] [-i <userId>] " + BASIC_CONTEXT_ADMIN_USAGE;
    }

    //////////////////////////// HELPERS ///////////////////////////

    /**
     * Writes the specified composite list to the console
     *
     * @param compositeList The composite list to write
     * @param columns The column names
     * @param formats The formatting of the columns
     */
    private void writeCompositeList(List<Map<String, Object>> compositeList, String[] columns, ColumnFormat[] formats) {
        writeCompositeList(compositeList, columns, formats, null);
    }

    /**
     * Writes the specified composite list to the console
     *
     * @param compositeList The composite list to write
     * @param columns The column names
     * @param formats The formatting of the columns
     * @param comparator The optional sorting {@link Comparator}. If <code>null</code> the entries will not be sorted
     */
    private void writeCompositeList(List<Map<String, Object>> compositeList, String[] columns, ColumnFormat[] formats, Comparator<List<Object>> sortingComparator) {
        if (compositeList == null || compositeList.isEmpty()) {
            System.out.println("No entries found.");
            return;
        }
        List<List<Object>> data = prepareData(compositeList, columns);
        if (sortingComparator != null) {
            Collections.sort(data, sortingComparator);
        }
        // Add header row
        data.add(0, prepareHeader(columns));
        new TableWriter(System.out, formats, data).write();
    }

    /**
     * Prepare the header row
     *
     * @param columns The header columns
     * @return A {@link List} with the header columns
     */
    private List<Object> prepareHeader(String[] columns) {
        List<Object> header = new ArrayList<Object>(columns.length);
        for (String column : columns) {
            header.add(column);
        }
        return header;
    }

    /**
     * Prepares the table data
     *
     * @param compositeList The composite list to read the data from
     * @param columns The column names
     * @return The prepared data
     */
    private List<List<Object>> prepareData(List<Map<String, Object>> compositeList, String[] columns) {
        List<List<Object>> data = new ArrayList<List<Object>>(compositeList.size());
        for (Map<String, Object> executedTask : compositeList) {
            data.add(prepareRow(columns, executedTask));
        }
        return data;
    }

    /**
     * Prepares a row for the table data
     *
     * @param columns The columns
     * @param executedTask The executed task metadata
     * @return A {@link List} with the row data
     */
    private List<Object> prepareRow(String[] columns, Map<String, Object> executedTask) {
        List<Object> valuesList = new ArrayList<Object>(columns.length);
        for (String column : columns) {
            valuesList.add(executedTask.get(column));
        }
        return valuesList;
    }

}
