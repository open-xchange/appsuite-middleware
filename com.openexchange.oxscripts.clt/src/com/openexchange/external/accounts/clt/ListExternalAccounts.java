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

package com.openexchange.external.accounts.clt;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.openexchange.cli.AsciiTable;
import com.openexchange.external.account.ExternalAccount;
import com.openexchange.external.account.ExternalAccountModule;
import com.openexchange.external.account.ExternalAccountRMIService;
import com.openexchange.java.Strings;

/**
 * {@link ListExternalAccounts}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class ListExternalAccounts extends AbstractExternalAccountCLT {

    private static final String SYNTAX = "listexternalaccounts " + USAGE + " [-r <providerId>] [-o <sortById>]" + BASIC_MASTER_ADMIN_USAGE;
    private static final String FOOTER = "";
    private static final String[] COLUMNS = { "Context Id", "User Id", "Account Id", "Module", "Provider" };

    private int contextId;
    private int userId;
    private ExternalAccountModule module;
    private String providerId;
    private Comparator<ExternalAccount> comparator;

    private Executor executor;

    /**
     * Initializes a new {@link ListExternalAccounts}.
     */
    private ListExternalAccounts() {
        super(SYNTAX, FOOTER);
    }

    /**
     * Entry point
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new ListExternalAccounts().execute(args);
    }

    @Override
    protected void addOptions(Options options) {
        super.addOptions(options);
        options.addOption(createArgumentOption("r", "provider", "providerId", "The provider identifier", false));
        options.addOption(createArgumentOption("o", "sort-by", "sortById", "The sort identifier. It can be one of the following: [" + SortBy.getCommandLineIds() + "]. Defaults to 'u'.", false));
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        ExternalAccountRMIService service = getRmiStub(ExternalAccountRMIService.RMI_NAME);
        printAccounts(executor.executeWith(service, new ExecutorContext(contextId, userId, providerId, module)));
        return null;
    }

    @Override
    protected void checkOptions(CommandLine cmd) throws ParseException {
        this.contextId = getMandatoryInt('c', -1, cmd, options);
        this.executor = Executor.CONTEXT;

        this.providerId = cmd.getOptionValue('r');
        boolean providerSet = Strings.isNotEmpty(providerId);

        this.userId = parseInt('u', 0, cmd, options);
        boolean userSet = userId > 0;

        this.comparator = getComparator(cmd.getOptionValue('s'));

        if (false == cmd.hasOption('m')) {
            if (providerSet) {
                this.executor = userSet ? Executor.CONTEXT_USER_PROVIDER : Executor.CONTEXT_PROVIDER;
            } else {
                this.executor = userSet ? Executor.CONTEXT_USER : Executor.CONTEXT;
            }
            return;
        }
        this.module = getModule(cmd);
        if (providerSet) {
            this.executor = userSet ? Executor.CONTEXT_USER_PROVIDER_MODULE : Executor.CONTEXT_PROVIDER_MODULE;
        } else {
            this.executor = userSet ? Executor.CONTEXT_USER_MODULE : Executor.CONTEXT_MODULE;
        }
    }

    //////////////////////////////// HELPERS //////////////////////////////

    /**
     * Extracts the optional sorting comparator from the specified option value
     * 
     * @param optionValue The option value
     * @return The comparator
     */
    private Comparator<ExternalAccount> getComparator(String optionValue) {
        if (Strings.isEmpty(optionValue)) {
            return SortBy.USER_ID.getComparator();
        }
        if (optionValue.length() > 1) {
            System.err.println("Invalid sorting identifier. Please use one of the following: [" + SortBy.getCommandLineIds() + "]");
            System.exit(1);
        }
        char c = optionValue.toCharArray()[0];
        switch (c) {
            case 'a':
                return SortBy.ACCOUNT_ID.getComparator();
            case 'm':
                return SortBy.MODULE_ID.getComparator();
            case 'p':
                return SortBy.PROVIDER_ID.getComparator();
            case 'u':
            default:
                return SortBy.USER_ID.getComparator();
        }
    }

    /**
     * Prints the accounts in tabular format
     *
     * @param accounts The accounts to print
     */
    private void printAccounts(List<ExternalAccount> accounts) {
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }
        AsciiTable table = new AsciiTable();
        table.setMaxColumnWidth(50);
        for (String c : COLUMNS) {
            table.addColumn(new AsciiTable.Column(c));
        }
        Collections.sort(accounts, comparator);
        for (ExternalAccount ea : accounts) {
            AsciiTable.Row row = new AsciiTable.Row();
            row.addValue(Integer.toString(ea.getContextId()));
            row.addValue(Integer.toString(ea.getUserId()));
            row.addValue(Integer.toString(ea.getId()));
            row.addValue(ea.getModule().name());
            row.addValue(ea.getProviderId());
            table.addData(row);
        }
        table.calculateColumnWidth();
        table.render();
    }

    /////////////////////////////////// NESTED /////////////////////////////

    /**
     * {@link Executor}
     */
    private enum Executor {

        CONTEXT("Listing all external accounts in context '#CONTEXT#'.", (service, ctx) -> service.list(ctx.getContextId())),
        CONTEXT_PROVIDER("Listing all external accounts in context '#CONTEXT#' for provider '#PROVIDER#'.", (service, ctx) -> service.list(ctx.getContextId(), ctx.getProviderId())),
        CONTEXT_MODULE("Listing all external accounts in context '#CONTEXT#' for module '#MODULE#'.", (service, ctx) -> service.list(ctx.getContextId(), ctx.getModule())),
        CONTEXT_PROVIDER_MODULE("Listing all external accounts in context '#CONTEXT#' for provider '#PROVIDER#' and module '#MODULE#'.", (service, ctx) -> service.list(ctx.getContextId(), ctx.getProviderId(), ctx.getModule())),
        CONTEXT_USER("Listing all external accounts for user '#USER#' in context '#CONTEXT#'.", (service, ctx) -> service.list(ctx.getContextId(), ctx.getUserId())),
        CONTEXT_USER_PROVIDER("Listing all external accounts for user '#USER#' in context '#CONTEXT#' and provider '#PROVIDER#'.", (service, ctx) -> service.list(ctx.getContextId(), ctx.getUserId(), ctx.getProviderId())),
        CONTEXT_USER_MODULE("Listing all external accounts for user '#USER#' in context '#CONTEXT#' and module '#MODULE#'.", (service, ctx) -> service.list(ctx.getContextId(), ctx.getUserId(), ctx.getModule())),
        CONTEXT_USER_PROVIDER_MODULE("Listing all external accounts for user '#USER#' in context '#CONTEXT#' for provider '#PROVIDER#' and module '#MODULE#'.", (service, ctx) -> service.list(ctx.getContextId(), ctx.getUserId(), ctx.getProviderId(), ctx.getModule()));

        private final String displayMessage;
        private final RMIExecutor executor;

        /**
         * Initializes a new {@link ListExternalAccounts.Executor}.
         */
        private Executor(String displayMessage, RMIExecutor executor) {
            this.displayMessage = displayMessage;
            this.executor = executor;
        }

        /**
         * Returns the display message
         *
         * @param args The arguments of the message
         * @return the formatted display message
         */
        private String getDisplayMessage(ExecutorContext context) {
            String ret = displayMessage;
            if (context.getModule() != null) {
                ret = ret.replaceAll("#MODULE#", context.getModule().name());
            }
            if (Strings.isNotEmpty(context.getProviderId())) {
                ret = ret.replaceAll("#PROVIDER#", context.getProviderId());
            }
            return ret.replaceAll("#CONTEXT#", Integer.toString(context.getContextId())).replaceAll("#USER#", Integer.toString(context.getUserId()));
        }

        /**
         * Executes with the specified context
         *
         * @param service The service
         * @param executorContext The executor context
         * @return The accounts
         * @throws RemoteException if an error is occurred
         */
        public List<ExternalAccount> executeWith(ExternalAccountRMIService service, ExecutorContext executorContext) throws RemoteException {
            System.out.println(getDisplayMessage(executorContext));
            return executor.execute(service, executorContext);
        }
    }

    /**
     * {@link RMIExecutor}
     */
    @FunctionalInterface
    private interface RMIExecutor {

        /**
         * Executes the implemented method with the specified service and context
         *
         * @param service The service
         * @param executorContext The executor context
         * @return The external accounts
         * @throws RemoteException if an error is occurred
         */
        List<ExternalAccount> execute(ExternalAccountRMIService service, ExecutorContext executorContext) throws RemoteException;
    }

    /**
     * {@link ExecutorContext}
     */
    private final class ExecutorContext {

        private final int cid;
        private final int uid;
        private final String pid;
        private final ExternalAccountModule m;

        /**
         * Initializes a new {@link ListExternalAccounts.ExecutorContext}.
         */
        ExecutorContext(int contextId, int userId, String providerId, ExternalAccountModule module) {
            super();
            this.cid = contextId;
            this.uid = userId;
            this.pid = providerId;
            this.m = module;
        }

        /**
         * Gets the contextId
         *
         * @return The contextId
         */
        int getContextId() {
            return cid;
        }

        /**
         * Gets the userId
         *
         * @return The userId
         */
        int getUserId() {
            return uid;
        }

        /**
         * Gets the providerId
         *
         * @return The providerId
         */
        String getProviderId() {
            return pid;
        }

        /**
         * Gets the module
         *
         * @return The module
         */
        ExternalAccountModule getModule() {
            return m;
        }
    }

    /**
     * {@link SortBy}
     */
    private enum SortBy {

        USER_ID('u', (o1, o2) -> compare(o1.getUserId(), o2.getUserId())),
        ACCOUNT_ID('a', (o1, o2) -> compare(o1.getId(), o2.getId())),
        MODULE_ID('m', (o1, o2) -> o1.getModule().toString().compareTo(o2.getModule().toString())),
        PROVIDER_ID('p', (o1, o2) -> o1.getProviderId().compareTo(o2.getProviderId()));

        private final Comparator<ExternalAccount> comparator;
        private final char commandLineId;

        /**
         * Initializes a new {@link ListExternalAccounts.SortBy}.
         */
        private SortBy(char commandLineId, Comparator<ExternalAccount> comparator) {
            this.commandLineId = commandLineId;
            this.comparator = comparator;
        }

        /**
         * Returns the {@link Comparator}
         *
         * @return the {@link Comparator}
         */
        public Comparator<ExternalAccount> getComparator() {
            return comparator;
        }

        /**
         * Gets the commandLineId
         *
         * @return The commandLineId
         */
        public char getCommandLineId() {
            return commandLineId;
        }

        /**
         * Returns a comma-separated list with
         * all available command line identifiers
         *
         * @return a comma-separated list with
         *         all available command line identifiers
         */
        public static String getCommandLineIds() {
            StringBuilder sb = new StringBuilder(16);
            for (SortBy sortBy : values()) {
                sb.append(sortBy.getCommandLineId()).append(",");
            }
            sb.setLength(sb.length() - 1);
            return sb.toString();
        }

        /**
         * Compares the two integers and returns <code>-1</code>,
         * <code>0</code> or <code>1</code>, if a is less than, equals
         * to or greater than b respectively.
         * 
         *
         * @param a Value a
         * @param b Value b
         * @return <code>-1</code>,
         *         <code>0</code> or <code>1</code>, if a is less than, equals
         *         to or greater than b respectively
         */
        private static int compare(int a, int b) {
            if (a > b) {
                return 1;
            } else if (a < b) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
