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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.tools.console.TableWriter;
import com.openexchange.tools.console.TableWriter.ColumnFormat;

/**
 * {@link AbstractUpdateTasksCLT} - An abstraction for all the Update Tasks related command line tools
 *
 * @param <R> - The return type of the invocation method
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
abstract class AbstractUpdateTasksCLT<R> extends AbstractRmiCLI<R> {

    private final String name;
    private final String footer;

    /**
     * Initialises a new {@link AbstractUpdateTasksCLT}.
     */
    AbstractUpdateTasksCLT(String name) {
        this(name, "");
    }

    /**
     * Initialises a new {@link AbstractUpdateTasksCLT}.
     */
    AbstractUpdateTasksCLT(String name, String footer) {
        super();
        this.name = name;
        this.footer = footer;
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return null;
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    protected String getFooter() {
        return footer;
    }

    /**
     * Writes the specified composite list to the console
     *
     * @param compositeList The composite list to write
     * @param columns The column names
     * @param formats The formatting of the columns
     */
    void writeCompositeList(List<Map<String, Object>> compositeList, String[] columns, ColumnFormat[] formats) {
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
    void writeCompositeList(List<Map<String, Object>> compositeList, String[] columns, ColumnFormat[] formats, Comparator<List<Object>> sortingComparator) {
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
