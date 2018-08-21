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

package com.openexchange.consistency;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.consistency.rmi.ConsistencyEntity;
import com.openexchange.consistency.rmi.ConsistencyRMIService;

/**
 * {@link CheckConsistency}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CheckConsistency extends AbstractRmiCLI<Void> {

    /**
     * Defines the actions of the CLT
     */
    private enum Action {
        list_unassigned("Lists names of orphaned files held in file storage"),
        list_missing("Lists names of files that are still referenced, but do no more exist in actual file storage"),
        repair("Repairs either orphaned files or references to non-existing files according to specified \"--policy\" and associated \"--policy-action\""),
        repair_configdb("Deletes artefacts of non-existing contexts from config database. Requires no further options."),
        check_configdb("Checks for artefacts of non-existing contexts in config database. Requires no further options.");

        private final String description;

        /**
         * Initializes a new {@link CheckConsistency.Action}.
         */
        private Action(String description) {
            this.description = description;
        }

        String getDescription() {
            return description;
        }
    }

    /**
     * Defines the sources that are going to be used
     */
    private enum Source {
        database, context, filestore, all;
    }

    /**
     * Defines the policy for a 'repair' {@link Action}
     */
    private enum Policy {
        missing_entry_for_file, missing_file_for_attachment, missing_file_for_infoitem, missing_file_for_snippet, missing_file_for_vcard, missing_attachment_file_for_mail_compose;
    }

    /**
     * Defines an action for the desired {@link Policy}
     */
    private enum PolicyAction {
        delete, create_dummy, create_admin_infoitem;
    }

    private static <E extends Enum<?>> String prettyPrintEnum(Class<E> clazz) {
        E[] enumConstants = clazz.getEnumConstants();
        int length = enumConstants.length;
        if (length <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(length << 4);
        sb.append('"').append(enumConstants[0].name()).append('"');
        for (int i = 1, k = length - 2; k-- > 0; i++) {
            E e = enumConstants[i];
            sb.append(", \"").append(e.name()).append('"');
        }
        sb.append(", and \"").append(enumConstants[length - 1].name()).append('"');
        return sb.toString();
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private Action action;
    private Source source;
    private Policy policy;
    private PolicyAction policyAction;
    private int sourceId;

    /**
     * Entry point
     *
     * @param args
     */
    public static void main(String[] args) {
        new CheckConsistency().execute(args);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.cli.AbstractMBeanCLI#addOptions(org.apache.commons.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("a", "action", "action", "Defines the action\nAccepted values are: " + prettyPrintEnum(Action.class), false));
        options.addOption(createArgumentOption("o", "source", "source", "Defines the source that is going to be used\nOnly considered if \"--action\" option specifies either \"" + Action.list_missing.name() + "\", \"" + Action.list_unassigned.name() + "\" or \"" + Action.repair.name() + "\"\nAccepted values are: " + prettyPrintEnum(Source.class), false));
        options.addOption(createArgumentOption("r", "policy", "policy", "Defines the 'repair' policy\nOnly considered if \"--action\" option specifies \"" + Action.repair.name() + "\"\nAvailable repair policies are: " + prettyPrintEnum(Policy.class), false));
        options.addOption(createArgumentOption("y", "policy-action", "policyAction", "Defines an action for the desired repair policy\nOnly considered if \"--policy\" option is specified", false));
        options.addOption(createArgumentOption("i", "source-id", "sourceId", "Defines the source identifier.\nOnly considered if \"--source\" option is specified\nIf \"--source\" is set to \"all\" then this option is simply ignored", false));
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
     * @see com.openexchange.cli.AbstractCLI#checkOptions(org.apache.commons.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        action = checkAndSetOption(Action.class, cmd, 'a');
        if (Action.check_configdb.equals(action) || Action.repair_configdb.equals(action)) {
            return;
        }
        source = checkAndSetOption(Source.class, cmd, 'o');
        if (false == Source.all.equals(source)) {
            if (cmd.hasOption('i')) {
                sourceId = Integer.parseInt(cmd.getOptionValue('i'));
            }
        }

        if (false == action.equals(Action.list_missing) && false == action.equals(Action.list_unassigned)) {
            policy = checkAndSetOption(Policy.class, cmd, 'r');
            policyAction = checkAndSetOption(PolicyAction.class, cmd, 'y');
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        String policyString = getPolicyString();
        List<Object> params = new ArrayList<Object>();

        StringBuilder builder = new StringBuilder();
        builder.append("Executing '").append(action).append("'");
        if (sourceId > 0) {
            builder.append(" in '").append(source).append("' source");
            builder.append(" with sourceId: ").append(sourceId);
        }
        if (Action.repair.equals(action)) {
            builder.append(" with repair policy '").append(policyString).append("'");
        }
        System.out.println(builder.toString());

        ConsistencyRMIService rmiService = getRmiStub(optRmiHostName, ConsistencyRMIService.RMI_NAME);
        switch (action) {
            case check_configdb:
                params.add(false);
                printList(rmiService.checkOrRepairConfigDB(false));
                System.out.println("Now run 'checkconsistency' tool again with the 'repair_configdb' option to remove these inconsistent contexts from the 'configdb'.");
                break;
            case repair_configdb:
                params.add(true);
                printList(rmiService.checkOrRepairConfigDB(true));
                break;
            case list_missing:
                listMissing(rmiService);
                break;
            case list_unassigned:
                listUnassigned(rmiService);
                break;
            case repair:
                repair(rmiService);
                break;
            default:
                // Should never happen
                System.err.println("Invalid action '" + action + "'");
                printHelp();
                System.exit(-1);
        }

        System.out.println("Operation complete.");
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.cli.AbstractCLI#requiresAdministrativePermission()
     */
    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.cli.AbstractCLI#getFooter()
     */
    @Override
    protected String getFooter() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("Choose options following:\n");
        sb.append("\n");
        sb.append("1. -a,--action:\n");
        sb.append("====================================\n");
        for (Action action : Action.values()) {
            sb.append("\n- \"-a ").append(action.name()).append("\"\n").append(action.getDescription());
        }

        sb.append("\n");
        sb.append(".\n");
        sb.append("2. -o,--source / -i,--source-id:\n");
        sb.append("====================================\n");
        sb.append("Only considered for actions \"").append(Action.list_missing.name()).append("\", \"").append(Action.list_unassigned.name()).append("\" or \"").append(Action.repair).append("\"");
        sb.append("\n");
        sb.append("Possible combinations:");
        sb.append("\n- \"-o ").append(Source.context.name()).append(" -i ").append("<context-id>").append("\"\nConsiders all files of a certain context");
        sb.append("\n- \"-o ").append(Source.filestore.name()).append(" -i ").append("<filestore-id>").append("\"\nConsiders all files of a certain file store");
        sb.append("\n- \"-o ").append(Source.database.name()).append(" -i ").append("<database-id>").append("\"\nConsiders all files of all contexts that belong to a certain database's schema");
        sb.append("\n- \"-o ").append(Source.all.name()).append("\"\nConsiders all files; no matter to what context and/or file store a file belongs (the \"--source-id\" option is ignored)");

        sb.append("\n");
        sb.append(".\n");
        sb.append("3. -r,--policy / -y,--policy-action:\n");
        sb.append("====================================\n");
        sb.append("Only considered for action \"").append(Action.repair).append("\"");
        sb.append("\n");
        sb.append("Possible combinations:");
        sb.append("\n- \"-r ").append(Policy.missing_entry_for_file.name()).append(" -y ").append(PolicyAction.create_admin_infoitem.name()).append("\"\nCreates a dummy Drive entry named \"Restoredfile\" and associates it with the file");
        sb.append("\n- \"-r ").append(Policy.missing_entry_for_file.name()).append(" -y ").append(PolicyAction.delete.name()).append("\"\nSimply deletes the orphanded file from storage");
        sb.append("\n- \"-r ").append(Policy.missing_file_for_infoitem.name()).append(" -y ").append(PolicyAction.create_dummy.name()).append("\"\nCreates a dummy file in storage and associates it with the Drive item");
        sb.append("\n- \"-r ").append(Policy.missing_file_for_infoitem.name()).append(" -y ").append(PolicyAction.delete.name()).append("\"\nSimply deletes the Drive item pointing to a non-existing file");
        sb.append("\n- \"-r ").append(Policy.missing_file_for_attachment.name()).append(" -y ").append(PolicyAction.create_dummy.name()).append("\"\nCreates a dummy file in storage and associates it with the attachment item");
        sb.append("\n- \"-r ").append(Policy.missing_file_for_attachment.name()).append(" -y ").append(PolicyAction.delete.name()).append("\"\nSimply deletes the attachment item pointing to a non-existing file");
        sb.append("\n- \"-r ").append(Policy.missing_file_for_snippet.name()).append(" -y ").append(PolicyAction.create_dummy.name()).append("\"\nCreates a dummy file in storage and associates it with the snippet item");
        sb.append("\n- \"-r ").append(Policy.missing_file_for_snippet.name()).append(" -y ").append(PolicyAction.delete.name()).append("\"\nSimply deletes the snippet item pointing to a non-existing file");
        sb.append("\n- \"-r ").append(Policy.missing_attachment_file_for_mail_compose.name()).append(" -y ").append(PolicyAction.create_dummy.name()).append("\"\nCreates a dummy file in storage and associates it with the mail compose attachment item");
        sb.append("\n- \"-r ").append(Policy.missing_attachment_file_for_mail_compose.name()).append(" -y ").append(PolicyAction.delete.name()).append("\"\nSimply deletes the mail compose attachment item pointing to a non-existing file");
        sb.append("\n- \"-r ").append(Policy.missing_file_for_vcard.name()).append(" -y ").append(PolicyAction.create_dummy.name()).append("\"\nCreates a dummy file in storage and associates it with the vcard item");
        sb.append("\n- \"-r ").append(Policy.missing_file_for_vcard.name()).append(" -y ").append(PolicyAction.delete.name()).append("\"\nSimply deletes the vcard item pointing to a non-existing file");

        return sb.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.cli.AbstractCLI#getName()
     */
    @Override
    protected String getName() {
        return "checkconsistency -a <action> -o <source> [-i <sourceId>] [-r <policy> -y <policyAction>] " + BASIC_MASTER_ADMIN_USAGE;
    }

    ///////////////////////////////////////////////////////////////////// HELPERS ///////////////////////////////////////////////////////////////

    /**
     * Lists the missing files
     * 
     * @param rmiService The {@link ConsistencyRMIService}
     * @throws RemoteException if a remote error is occurred
     */
    private void listMissing(ConsistencyRMIService rmiService) throws RemoteException {
        switch (source) {
            case all:
                printMap(rmiService.listAllMissingFiles());
                break;
            case context:
                printList(rmiService.listMissingFilesInContext(sourceId));
                break;
            case database:
                printMap(rmiService.listMissingFilesInDatabase(sourceId));
                break;
            case filestore:
                printMap(rmiService.listMissingFilesInFilestore(sourceId));
                break;
            default:
                System.err.println("Invalid source '" + source + "'");
                printHelp();
                System.exit(-1);
        }
    }

    /**
     * Lists the unassigned files
     * 
     * @param rmiService The {@link ConsistencyRMIService}
     * @throws RemoteException if a remote error is occurred
     */
    private void listUnassigned(ConsistencyRMIService rmiService) throws RemoteException {
        switch (source) {
            case all:
                printMap(rmiService.listAllUnassignedFiles());
                break;
            case context:
                printList(rmiService.listUnassignedFilesInContext(sourceId));
                break;
            case database:
                printMap(rmiService.listUnassignedFilesInDatabase(sourceId));
                break;
            case filestore:
                printMap(rmiService.listUnassignedFilesInFilestore(sourceId));
                break;
            default:
                System.err.println("Invalid source '" + source + "'");
                printHelp();
                System.exit(-1);
        }

    }

    /**
     * Performs a repair
     * 
     * @param rmiService The {@link ConsistencyRMIService}
     * @throws RemoteException if a remote error is occurred
     */
    private void repair(ConsistencyRMIService rmiService) throws RemoteException {
        switch (source) {
            case all:
                rmiService.repairAllFiles(policy.name(), policyAction.name());
                break;
            case context:
                rmiService.repairFilesInContext(sourceId, policy.name(), policyAction.name());
                break;
            case database:
                rmiService.repairFilesInDatabase(sourceId, policy.name(), policyAction.name());
                break;
            case filestore:
                rmiService.repairFilesInFilestore(sourceId, policy.name(), policyAction.name());
                break;
            default:
                System.err.println("Invalid source '" + source + "'");
                printHelp();
                System.exit(-1);
        }
    }

    /**
     * Get the policy string
     *
     * @return the policy string
     */
    private String getPolicyString() {
        return (policy != null) ? policy.name() + ":" + policyAction.name() : "";
    }

    /**
     * Check and set the specified option
     *
     * @param enumClass The enum class to check it with
     * @param e The enum variable to set
     * @param cmd The command line options
     * @param option The option to check
     */

    private final <E extends Enum<E>> E checkAndSetOption(Class<E> enumClass, CommandLine cmd, char option) {
        E e = null;
        if (cmd.hasOption(option)) {
            e = parseEnumValue(enumClass, cmd.getOptionValue(option));
        } else {
            System.err.println("You must specify a valid value for the '" + option + "' option.");
            printHelp();
            System.exit(-1);
        }
        return e;
    }

    /**
     * Generic helper method for parsing an enum value
     *
     * @param enumClass The enum class to use
     * @param value The value to parse
     * @return The parsed Enum value
     * @throws IllegalArgumentException in case the value cannot be parsed to a valid enum class
     */
    private final <E extends Enum<E>> E parseEnumValue(Class<E> enumClass, String value) {
        E valueOf = null;
        try {
            valueOf = E.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid value '" + value + "' specified for '" + enumClass.getSimpleName() + "'");
            printHelp();
            System.exit(-1);
        }
        return valueOf;
    }

    /**
     * Print the result
     *
     * @param result The result to print
     */
    private void printMap(final Map<ConsistencyEntity, List<String>> result) {
        if (null == result) {
            System.out.println("No problems found.");
            return;
        }

        String formatter;
        StringBuilder dashBuilder;
        {
            // Find the widest string of the keys
            int widestEntityString;
            List<String> entities = compileListOfEntities(result.keySet());
            widestEntityString = fetchWidestString(entities);

            // Find the widest string of the values
            int widestHashString;
            List<String> flattenedList = flattenList(result.values());
            widestHashString = fetchWidestString(flattenedList);

            // Compare both and get the widest
            int widestString = (widestEntityString > widestHashString) ? widestEntityString : widestHashString;

            formatter = "| %-" + widestString + "s   |%n";

            dashBuilder = new StringBuilder();
            for (int i = 0; i < widestString; i++) {
                dashBuilder.append("-");
            }
            dashBuilder.append("--+");
        }

        for (final Map.Entry<ConsistencyEntity, List<String>> entry : result.entrySet()) {
            final List<String> brokenFiles = entry.getValue();
            ConsistencyEntity entity = entry.getKey();

            System.out.format("+--" + dashBuilder.toString() + "%n");
            System.out.format(formatter, entity.toString());
            System.out.format("+--" + dashBuilder.toString() + "%n");

            for (final String brokenFile : brokenFiles) {
                System.out.format(formatter, brokenFile);
            }

            if (!brokenFiles.isEmpty()) {
                System.out.format("+--" + dashBuilder.toString() + "%n");
            }
            System.out.format(formatter, brokenFiles.size() + " problem(s) found");
            System.out.format("+--" + dashBuilder.toString() + "%n%n");
        }
    }

    /**
     * Prints to the console the specified list of strings
     * 
     * @param results the list to print
     */
    private void printList(List<String> results) {
        for (String s : results) {
            System.out.println(s);
        }
    }

    /**
     * Converts and returns the specified {@link Set} of {@link ConsistencyEntity}s
     * as a {@link List} of Strings.
     * 
     * @param keySet The {@link Set} to convert
     * @return The converted set as a {@link List}
     */
    private List<String> compileListOfEntities(Set<ConsistencyEntity> keySet) {
        List<String> entities = new ArrayList<String>(keySet.size());
        for (ConsistencyEntity entity : keySet) {
            entities.add(entity.toString());
        }
        return entities;
    }

    /**
     * Flattens the specified list
     *
     * @param values A Collection with List of strings
     * @return the flattened collection
     */
    private List<String> flattenList(Collection<List<String>> values) {
        Iterator<List<String>> iterator = values.iterator();
        List<String> flattenedList = new ArrayList<String>();
        while (iterator.hasNext()) {
            flattenedList.addAll(iterator.next());
        }
        return flattenedList;
    }

    /**
     * Sort the results and fetch the length of the widest string
     *
     * @param results The strings to sort
     * @return The length of the widest string
     */
    private int fetchWidestString(List<String> results) {
        int widestURL = 5;
        Object[] a = results.toArray();
        ListIterator<String> i = results.listIterator();
        for (int j = 0; j < a.length; j++) {
            String p = i.next();
            if (widestURL < p.length()) {
                widestURL = p.length();
            }
            i.set((String) a[j]);
        }
        return widestURL;
    }

}
