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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.cli.AbstractMBeanCLI;

/**
 * {@link ConsistencyCheck}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ConsistencyCheck extends AbstractMBeanCLI<Void> {

    /**
     * Defines the actions of the CLT
     */
    private enum Action {
        list_unassigned("listUnassignedFiles", "Lists names of orphaned files held in file storage"),
        list_missing("listMissingFiles", "Lists names of files that are still referenced, but do no more exist in actual file storage"),
        repair("repairFiles", "Repairs either orphaned files or references to non-existing files according to specified \"--policy\" and associated \"--policy-action\""),
        repair_configdb("checkOrRepairConfigDB", "Deletes artefacts of non-existing contexts from config database. Requires no further options."),
        check_configdb("checkOrRepairConfigDB", "Checks for artefacts of non-existing contexts in config database. Requires no further options.");

        private final String methodName;
        private final String description;

        /**
         * Initializes a new {@link ConsistencyCheck.Action}.
         */
        private Action(String methodName, String description) {
            this.methodName = methodName;
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
        missing_entry_for_file, missing_file_for_attachment, missing_file_for_infoitem, missing_file_for_snippet, missing_file_for_vcard;
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
        new ConsistencyCheck().execute(args);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.cli.AbstractMBeanCLI#administrativeAuth(java.lang.String, java.lang.String, org.apache.commons.cli.CommandLine, com.openexchange.auth.mbean.AuthenticatorMBean)
     */
    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, AuthenticatorMBean authenticator) throws MBeanException {
        //no-op, we only support JMX authentication for this tool
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.cli.AbstractMBeanCLI#addOptions(org.apache.commons.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        options.addOption("a", "action", true, "Defines the action\nAccepted values are: " + prettyPrintEnum(Action.class));
        options.addOption("o", "source", true, "Defines the source that is going to be used\nOnly considered if \"--action\" option specifies either \""+Action.list_missing.name()+"\", \""+Action.list_unassigned.name()+"\" or \""+Action.repair.name()+"\"\nAccepted values are: " + prettyPrintEnum(Source.class));
        options.addOption("r", "policy", true, "Defines the 'repair' policy\nOnly considered if \"--action\" option specifies \""+Action.repair.name()+"\"\nAvailable repair policies are: " + prettyPrintEnum(Policy.class));
        options.addOption("y", "policy-action", true, "Defines an action for the desired repair policy\nOnly considered if \"--policy\" option is specified");
        options.addOption("i", "source-id", true, "Defines the source identifier.\nOnly considered if \"--source\" option is specified\nIf \"--source\" is set to \"all\" then this option is simply ignored");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.cli.AbstractMBeanCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, javax.management.MBeanServerConnection)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Void invoke(Options option, CommandLine cmd, MBeanServerConnection mbsc) throws Exception {
        String operationName = getOperationName();
        String policyString = getPolicyString();
        List<Object> params = new ArrayList<Object>();

        StringBuilder builder = new StringBuilder();
        builder.append("Executing '").append(action).append("'");
        if (sourceId > 0) {
            builder.append(" in '").append(source).append("' source");
            builder.append(" with sourceId: ").append(sourceId);
        }

        switch (action) {
            case check_configdb:
                params.add(false);
                operationName = action.methodName;
                break;
            case repair_configdb:
                params.add(true);
                operationName = action.methodName;
                break;
            case list_missing:
                if (source.equals(Source.all)) {
                    operationName = "listAllMissingFiles";
                } else {
                    params.add(sourceId);
                }
                break;
            case list_unassigned:
                if (source.equals(Source.all)) {
                    operationName = "listAllUnassignedFiles";
                } else {
                    params.add(sourceId);
                }
                break;
            case repair:
                if (source.equals(Source.all)) {
                    operationName = "repairAllFiles";
                } else {
                    params.add(sourceId);
                }
                params.add(policyString);
                builder.append(" with repair policy '").append(policyString).append("'");
                break;
            default:
                // Should never happen
                System.err.println("Invalid action '" + action + "'");
                printHelp();
                System.exit(-1);
        }

        System.out.println(builder.toString());
        Object resultObject = mbsc.invoke(MBeanNamer.getName(), operationName, params.toArray(new Object[params.size()]), getSignatureOf(ConsistencyMBean.class, operationName));

        if (action.equals(Action.check_configdb)) {
            printList((List<String>) resultObject);
            System.out.println("Now run 'checkconsistency' tool again with the 'repair_configdb' option to remove these inconsistent contexts from the 'configdb'.");
        } else {
            printResult(resultObject);
        }
        return null;
    }

    /**
     * Get the operation name
     *
     * @return
     */
    private String getOperationName() {
        return (source != null) ? action.methodName + "In" + source.name().substring(0, 1).toUpperCase() + source.name().substring(1) : action.methodName;
    }

    /**
     * Get the policy string
     *
     * @return
     */
    private String getPolicyString() {
        return (policy != null) ? policy.name() + ":" + policyAction.name() : new String();
    }

    /**
     * Prints the result
     *
     * @param result
     */
    @SuppressWarnings("unchecked")
    private void printResult(Object result) {
        if (result == null) {
            System.out.println("Operation complete.");
        } else {
            if (result != null && result instanceof List) {
                Map<MBeanEntity, List<String>> results = new HashMap<MBeanEntity, List<String>>();
                results.put(new MBeanEntity(Integer.valueOf(sourceId)), (List<String>) result);
                printMap(results);
            } else if (result != null && result instanceof Map) {
                printMap((Map<MBeanEntity, List<String>>) result);
            } else {
                System.out.println("Cannot print result object. Here's the stringified version: \n" + result.toString());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.cli.AbstractCLI#checkOptions(org.apache.commons.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        action = checkAndSetOption(Action.class, cmd, 'a');
        if (!action.equals(Action.check_configdb) && !action.equals(Action.repair_configdb)) {
            source = checkAndSetOption(Source.class, cmd, 'o');
            if (!source.equals(Source.all)) {
                if (cmd.hasOption('i')) {
                    sourceId = Integer.parseInt(cmd.getOptionValue('i'));
                }
            }
            if (!action.equals(Action.list_missing) && !action.equals(Action.list_unassigned)) {
                policy = checkAndSetOption(Policy.class, cmd, 'r');
                policyAction = checkAndSetOption(PolicyAction.class, cmd, 'y');
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.cli.AbstractCLI#requiresAdministrativePermission()
     */
    @Override
    protected boolean requiresAdministrativePermission() {
        return false;
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
        sb.append(".\n");
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
        sb.append("\n- \"-o ").append(source.context.name()).append(" -i ").append("<context-id>").append("\"\nConsiders all files of a certain context");
        sb.append("\n- \"-o ").append(source.filestore.name()).append(" -i ").append("<filestore-id>").append("\"\nConsiders all files of a certain file store");
        sb.append("\n- \"-o ").append(source.database.name()).append(" -i ").append("<database-id>").append("\"\nConsiders all files of all contexts that belong to a certain database's schema");
        sb.append("\n- \"-o ").append(source.all.name()).append("\"\nConsiders all files; no matter to what context and/or file store a file belongs (the \"--source-id\" option is ignored)");

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
        return "checkconsistency -a <action> -o <source> [-i <sourceId>] [-r <policy> -y <policyAction>] [-l <jmxUser>] [-s <jmxPassword>] [-t <jmxHost>] [-p <jmxPort>] [--responsetimeout <responseTimeout>]";
    }

    ///////////////////////////////////////////////////////////////////// HELPERS ///////////////////////////////////////////////////////////////

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
     * Get the signature of the specified method as an array of Strings
     *
     * @param clazz The class from which to get the signature
     * @param methodName The method name
     * @return
     */
    private final String[] getSignatureOf(Class<?> clazz, String methodName) {
        String[] signature = null;
        Class<?>[] types = null;
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(methodName)) {
                types = m.getParameterTypes();
                break;
            }
        }

        if (types != null) {
            signature = new String[types.length];
            int s = 0;
            for (Class<?> c : types) {
                signature[s++] = c.getName();
            }
            return signature;
        }

        System.err.println("Invalid operation '" + methodName + "'");
        printHelp();
        System.exit(-1);

        return null;
    }

    /**
     * Print the result
     *
     * @param result The result to print
     */
    private void printMap(final Map<MBeanEntity, List<String>> result) {
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

        for (final Map.Entry<MBeanEntity, List<String>> entry : result.entrySet()) {
            final List<String> brokenFiles = entry.getValue();
            MBeanEntity entity = entry.getKey();

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

    private void printList(List<String> results) {
        for (String s : results) {
            System.out.println(s);
        }
    }

    /**
     * @param keySet
     * @return
     */
    private List<String> compileListOfEntities(Set<MBeanEntity> keySet) {
        List<String> entities = new ArrayList<String>(keySet.size());
        for (MBeanEntity entity : keySet) {
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
