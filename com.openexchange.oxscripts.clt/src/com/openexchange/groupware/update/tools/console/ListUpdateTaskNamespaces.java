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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.groupware.update.UpdateTaskService;

/**
 * {@link ListUpdateTaskNamespaces}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public final class ListUpdateTaskNamespaces extends AbstractUpdateTasksCLT<Void> {

    //@formatter:off
    private static final String FOOTER = "This tools lists all namespaces for any update tasks and/or update task sets. The outcome of this tool can be used to " +
        " populate the property 'com.openexchange.groupware.update.excludedUpdateTasks'. Entries in that property will result in excluding all update tasks that are part " + 
        " of that particular namespace.";
    //@formatter:on

    private boolean printNamespacesOnly;

    /**
     * Entry point
     * 
     * @param args The command line arguments to pass
     */
    public static void main(String[] args) {
        new ListUpdateTaskNamespaces().execute(args);
    }

    /**
     * Initialises a new {@link ListUpdateTaskNamespaces}.
     */
    private ListUpdateTaskNamespaces() {
        super("listUpdateTaskNamespaces", FOOTER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#addOptions(org.apache.commons.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        options.addOption("n", "namespaces-only", false, "Prints only the available namespaces without their update tasks");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        UpdateTaskService updateTaskService = getRmiStub(UpdateTaskService.RMI_NAME);
        Map<String, Set<String>> namespaceAware = updateTaskService.getNamespaceAware();
        if (printNamespacesOnly) {
            printNamespaceOnly(namespaceAware);
        } else {
            printEverything(namespaceAware);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#checkOptions(org.apache.commons.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        printNamespacesOnly = cmd.hasOption('n');
    }

    /**
     * Prints everything (namespace + underlying update tasks)
     * 
     * @param map The {@link Map} containing the namespace aware update tasks
     */
    private void printEverything(Map<String, Set<String>> map) {
        for (Entry<String, Set<String>> entry : map.entrySet()) {
            printKey(entry.getKey());
            printValue(entry.getValue());
        }
    }

    /**
     * Prints only the namespace
     * 
     * @param map The {@link Map} containing the namespace aware update tasks
     */
    private void printNamespaceOnly(Map<String, Set<String>> map) {
        for (Entry<String, Set<String>> entry : map.entrySet()) {
            printKey(entry.getKey());
        }
    }

    /**
     * Prints the key
     * 
     * @param key the key to print
     */
    private void printKey(String key) {
        System.out.println("+- " + key);
    }

    /**
     * Prints the values
     * 
     * @param values The values to print
     */
    private void printValue(Set<String> values) {
        for (String c : values) {
            System.out.println("|--- " + c);
        }
    }
}
