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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractAdministrativeCLI#requiresAdministrativePermission()
     */
    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#administrativeAuth(java.lang.String, java.lang.String, org.apache.commons.cli.CommandLine, com.openexchange.auth.rmi.RemoteAuthenticator)
     */
    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getName()
     */
    @Override
    protected String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getFooter()
     */
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
        List<List<Object>> data = new ArrayList<List<Object>>();
        List<Object> valuesList = new ArrayList<Object>(columns.length);
        for (String column : columns) {
            valuesList.add(column);
        }
        final List<Object> hr = valuesList;
        for (Map<String, Object> executedTask : compositeList) {
            valuesList = new ArrayList<Object>(columns.length);
            for (String column : columns) {
                valuesList.add(executedTask.get(column));
            }
            data.add(valuesList);
        }
        valuesList = null;
        if (sortingComparator != null) {
            // Sort rows
            Collections.sort(data, sortingComparator);
        }
        // Add header row
        data.add(0, hr);
        new TableWriter(System.out, formats, data).write();
    }
}
