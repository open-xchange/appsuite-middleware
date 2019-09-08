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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.gdpr.dataexport.clt;

import static com.openexchange.java.Autoboxing.I;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.gdpr.dataexport.rmi.DataExportRMIService;
import com.openexchange.tools.console.TableWriter;
import com.openexchange.tools.console.TableWriter.ColumnFormat;
import com.openexchange.tools.console.TableWriter.ColumnFormat.Align;

/**
 * {@link DataExportConsistencyCLT}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.3
 */
public class DataExportConsistencyCLT extends AbstractRmiCLI<Void> {

    private static final ColumnFormat[] FORMATS_TASK_ITEM = { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) };
    private static final String[] COLUMNS_TASK_ITEM = { DataExportRMIService.COLUMN_TASK, DataExportRMIService.COLUMN_MODULE, DataExportRMIService.COLUMN_USER, DataExportRMIService.COLUMN_CONTEXT, DataExportRMIService.COLUMN_FILESTORE, DataExportRMIService.COLUMN_LOCATION };

    private static final ColumnFormat[] FORMATS_FILESTORE_LOCATION = { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) };
    private static final String[] COLUMNS_FILESTORE_LOCATION = { DataExportRMIService.COLUMN_FILESTORE, DataExportRMIService.COLUMN_FILESTORE_URI, DataExportRMIService.COLUMN_LOCATION };

    private static final ColumnFormat[] FORMATS_RESULT_FILE = { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) };
    private static final String[] COLUMNS_RESULT_FILEM = { DataExportRMIService.COLUMN_TASK, DataExportRMIService.COLUMN_PACKAGE, DataExportRMIService.COLUMN_USER, DataExportRMIService.COLUMN_CONTEXT, DataExportRMIService.COLUMN_FILESTORE, DataExportRMIService.COLUMN_LOCATION };

    private boolean repair;
    private List<Integer> filestoreIds;

    /**
     * Initializes a new {@link DataExportListCLT}.
     */
    public DataExportConsistencyCLT() {
        super();
    }

    /**
     * Entry point
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        new DataExportConsistencyCLT().execute(args);
    }

    @Override
    protected void addOptions(Options options) {
        Option filestoresOption = createArgumentOption("f", "filestores", "filestoreIds", "Accepts one or more file storage identifiers", true);
        filestoresOption.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(filestoresOption);
        options.addOption(createSwitch("r", "repair", "Repairs orphaned files and export task items.", false));
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        if (repair) {
            return repair(optRmiHostName);
        }

        return list(optRmiHostName);
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected void checkOptions(CommandLine cmd) throws ParseException {
        repair = cmd.hasOption("r");
        String[] values = cmd.getOptionValues("f");
        if (values == null) {
            throw new MissingOptionException("File storage identifier(s) missing. Please specify one or more file storage identifier(s) through -f/--filestores option.");
        }
        filestoreIds = new ArrayList<>(values.length);
        for (String value : values) {
            try {
                filestoreIds.add(I(Integer.parseInt(value)));
            } catch (NumberFormatException nfe) {
                System.err.println("Invalid filestore identifier: ``" + value + "\u00b4\u00b4");
                printHelp(options);
                System.exit(1);
            }
        }
        if (filestoreIds.isEmpty()) {
            System.err.println("Missing filestore identifiers.");
            printHelp(options);
            System.exit(1);
        }
    }

    @Override
    protected String getFooter() {
        return "Command line tool to list and repair orphaned files, export task items and result files.";
    }

    @Override
    protected String getName() {
        return "dataexportconsistency -f <filestoreId_1> ... [-r] " + BASIC_MASTER_ADMIN_USAGE;
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    private Void repair(String optRmiHostName) throws MalformedURLException, RemoteException, NotBoundException {
        DataExportRMIService rmiService = getRmiStub(optRmiHostName, DataExportRMIService.RMI_NAME);
        int fixedEntries = rmiService.fixOrphanedEntries(filestoreIds);
        System.out.println("Fixed a total of " + fixedEntries + " entries.");
        return null;
    }

    private Void list(String optRmiHostName) throws MalformedURLException, RemoteException, NotBoundException {
        DataExportRMIService rmiService = getRmiStub(optRmiHostName, DataExportRMIService.RMI_NAME);
        List<Map<String, Object>> orphanedFileStoreLocations = rmiService.getOrphanedFileStoreLocations(filestoreIds);
        List<Map<String, Object>> orphanedWorkItems = rmiService.getOrphanedWorkItems();
        List<Map<String, Object>> orphanedResultFiles = rmiService.getOrphanedResultFiles();

        if (!orphanedFileStoreLocations.isEmpty()) {
            System.out.println("\nOrphaned file store locations:");
            writeCompositeList(orphanedFileStoreLocations, COLUMNS_FILESTORE_LOCATION, FORMATS_FILESTORE_LOCATION, null);
        } else {
            System.out.println("No orphaned file store locations found.");
        }
        System.out.println();
        if (!orphanedWorkItems.isEmpty()) {
            System.out.println("Orphaned work items:");
            writeCompositeList(orphanedWorkItems, COLUMNS_TASK_ITEM, FORMATS_TASK_ITEM, null);
        } else {
            System.out.println("No orphaned work items found.");
        }
        System.out.println();
        if (!orphanedResultFiles.isEmpty()) {
            System.out.println("Orphaned result files:");
            writeCompositeList(orphanedResultFiles, COLUMNS_RESULT_FILEM, FORMATS_RESULT_FILE, null);
        } else {
            System.out.println("No orphaned result files found.");
        }
        return null;
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
