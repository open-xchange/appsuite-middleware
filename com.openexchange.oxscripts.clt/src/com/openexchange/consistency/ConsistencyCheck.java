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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * CommandLineClient to run the consistency tool.
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 * @author Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
 */
public class ConsistencyCheck {

    /**
     * Defines the actions of the CLT
     */
    private enum Action {
        list, unassigned, missing, listUnassigned, listMissing, repair, repairconfigdb, checkconfigdb;
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

    private static final String LOCALHOST = "localhost";

    /**
     * Main method used for checkconsistency clt
     *
     * @param args - arguments provided by clt
     */
    public static void main(final String[] args) {
        final SimpleLexer lexer = new SimpleLexer(args);
        final Configuration config = new Configuration();

        lexer.noise("in");

        if (lexer.consume("host")) {
            final String hostname = lexer.getCurrent();
            final String[] hostAndPort = hostname.split(":");
            config.setHost(hostAndPort[0]);
            if (hostAndPort.length > 1) {
                config.setPort(Integer.parseInt(hostAndPort[1]));
            }
            lexer.advance();
        } else {
            config.setHost(LOCALHOST);
        }

        int responseTimeoutMillis = 0;
        if (lexer.consume("responsetimeout")) {
            final String value = lexer.getCurrent();
            responseTimeoutMillis = Integer.parseInt(value) * 1000;
            lexer.advance();
        }
        config.setResponseTimeoutMillis(responseTimeoutMillis);

        if (lexer.consume(Action.repair.name())) {
            config.setAction(Action.repair.name());
        } else if (lexer.consume(Action.list.name())) {
            if (lexer.consume(Action.missing.name())) {
                config.setAction(Action.listMissing.name());
            } else if (lexer.consume(Action.unassigned.name())) {
                config.setAction(Action.listUnassigned.name());
            } else {
                System.exit(dontKnowWhatToList());
            }
        } else if (lexer.consume(Action.checkconfigdb.name())) {
            config.setAction(Action.checkconfigdb.name());
        } else if (lexer.consume(Action.repairconfigdb.name())) {
            config.setAction(Action.repairconfigdb.name());
        } else {
            System.exit(noaction());
        }
        lexer.noise("files");
        lexer.noise("errors");
        lexer.noise("in");

        if (!config.getAction().equals(Action.checkconfigdb.name()) && !config.getAction().equals(Action.repairconfigdb.name())) {
            if (lexer.consume(Source.database.name())) {
                config.setSource(Source.database.name());
                if (!parseId(lexer, config)) {
                    System.exit(noid());
                }
            } else if (lexer.consume(Source.filestore.name())) {
                config.setSource(Source.filestore.name());
                if (!parseId(lexer, config)) {
                    System.exit(noid());
                }
            } else if (lexer.consume(Source.context.name())) {
                config.setSource(Source.context.name());
                if (!parseId(lexer, config)) {
                    System.exit(noid());
                }
            } else if (lexer.consume(Source.all.name())) {
                config.setSource(Source.all.name());
            } else {
                System.exit(noproblemsource());
            }

            lexer.noise("with");
            lexer.noise("policies");

            while (!lexer.eol()) {
                if (lexer.consume(Policy.missing_file_for_infoitem.name())) {
                    lexer.noise(":");
                    if (lexer.consume(PolicyAction.create_dummy.name())) {
                        config.addPolicy(Policy.missing_file_for_infoitem.name(), PolicyAction.create_dummy.name());
                    } else if (lexer.consume(PolicyAction.delete.name())) {
                        config.addPolicy(Policy.missing_file_for_infoitem.name(), PolicyAction.delete.name());
                    } else {
                        System.exit(unknownAction(Policy.missing_file_for_infoitem.name(), lexer.getCurrent(), PolicyAction.create_dummy.name(), PolicyAction.delete.name()));
                    }
                } else if (lexer.consume(Policy.missing_file_for_attachment.name())) {
                    lexer.noise(":");
                    if (lexer.consume(PolicyAction.create_dummy.name())) {
                        config.addPolicy(Policy.missing_file_for_attachment.name(), PolicyAction.create_dummy.name());
                    } else if (lexer.consume(PolicyAction.delete.name())) {
                        config.addPolicy(Policy.missing_file_for_attachment.name(), PolicyAction.delete.name());
                    } else {
                        System.exit(unknownAction(Policy.missing_file_for_attachment.name(), lexer.getCurrent(), PolicyAction.create_dummy.name(), PolicyAction.delete.name()));
                    }
                } else if (lexer.consume(Policy.missing_file_for_snippet.name())) {
                    lexer.noise(":");
                    if (lexer.consume(PolicyAction.create_dummy.name())) {
                        config.addPolicy(Policy.missing_file_for_snippet.name(), PolicyAction.create_dummy.name());
                    } else if (lexer.consume(PolicyAction.delete.name())) {
                        config.addPolicy(Policy.missing_file_for_snippet.name(), PolicyAction.delete.name());
                    } else {
                        System.exit(unknownAction(Policy.missing_file_for_snippet.name(), lexer.getCurrent(), PolicyAction.create_dummy.name(), PolicyAction.delete.name()));
                    }
                } else if (lexer.consume(Policy.missing_entry_for_file.name())) {
                    lexer.noise(":");
                    if (lexer.consume(PolicyAction.create_admin_infoitem.name())) {
                        config.addPolicy(Policy.missing_entry_for_file.name(), PolicyAction.create_admin_infoitem.name());
                    } else if (lexer.consume(PolicyAction.delete.name())) {
                        config.addPolicy(Policy.missing_entry_for_file.name(), PolicyAction.delete.name());
                    } else {
                        System.exit(unknownAction(Policy.missing_entry_for_file.name(), lexer.getCurrent(), PolicyAction.create_admin_infoitem.name(), PolicyAction.delete.name()));
                    }
                } else if (lexer.consume(Policy.missing_file_for_vcard.name())) {
                    //VCard only supports removing references from the db.<br>
                    // Removing files should be done by using the generic file removal call Policy.missing_entry_for_file.name() + PolicyAction.delete.name()<br>
                    // In addition you are able to create files for the context admin to review them with Policy.missing_entry_for_file.name() + PolicyAction.create_admin_infoitem.name()
                    lexer.noise(":");
                    if (lexer.consume(PolicyAction.delete.name())) {
                        config.addPolicy(Policy.missing_file_for_vcard.name(), PolicyAction.delete.name());
                    } else {
                        System.exit(unknownAction(Policy.missing_file_for_vcard.name(), lexer.getCurrent(), PolicyAction.delete.name()));
                    }
                } else {
                    System.exit(unknownCondition(lexer.getCurrent(), Policy.missing_file_for_infoitem.name(), Policy.missing_file_for_attachment.name(), Policy.missing_entry_for_file.name()));
                }
            }
        }

        try {
            config.run();
        } catch (final Exception x) {
            x.printStackTrace();
            System.exit(1);
        }
    }

    private static boolean parseId(final SimpleLexer lexer, final Configuration config) {
        try {
            config.setSourceId(Integer.parseInt(lexer.getCurrent()));
            lexer.advance();
            return true;
        } catch (final NumberFormatException nfe) {
            return false;
        }
    }

    private static int dontKnowWhatToList() {
        System.err.println("Please tell me what to list. Either \"" + Action.list + Action.missing + "\" or \"" + Action.list + Action.unassigned + "\".");
        return 6;
    }

    private static int unknownAction(final String policy, final String action, final String... possibleActions) {
        StringBuilder actions = new StringBuilder();

        for (String possibleAction : possibleActions) {
            actions.append("\"" + possibleAction + "\", ");
        }

        System.err.println("Unknown action \"" + action + "\" for policy \"" + policy + "\". I know only " + actions.toString());
        return 5;
    }

    private static int unknownCondition(final String condition, final String... possibleConditions) {
        StringBuilder conditions = new StringBuilder();

        for (String possibleCondition : possibleConditions) {
            conditions.append("\"" + possibleCondition + "\", ");
        }

        System.err.println("Unknown condition \"" + condition + "\". I know only about: " + conditions.toString());
        return 4;
    }

    private static int noid() {
        System.err.println("Please specify an id for the context, filestore or database that I should check.");
        return 3;
    }

    private static int noproblemsource() {
        System.err.println("Please specify what to search for problems (either \"" + Source.context + " [id]\" or \"" + Source.filestore + " [id]\" or \"" + Source.database + " [id]\" or \"" + Source.all + "\".");
        return 2;
    }

    private static int noaction() {
        final String ls = System.getProperty("line.separator");
        System.err.println("Please specify an action, either" + ls + "\"" + Action.list + Action.missing + "\", \"" + Action.list + Action.unassigned + "\", \"" + Action.repair + "\", \"" + Action.checkconfigdb + "\" or \"" + Action.repairconfigdb + "\"" + ls +
            "You can also specify the hostname of the open-xchange server, optionally." + ls +
            "Example:" + ls +
            "checkconsistency in host 10.10.10.10 list missing [...]");
        return 1;
    }

    private static class SimpleLexer {

        private final String[] args;
        private int index;

        public SimpleLexer(final String[] args) {
            this.args = args;
            this.index = 0;
        }

        public String getCurrent() {
            if (eol()) {
                return "";
            }
            return args[index];
        }

        public void advance() {
            index++;
        }

        public boolean lookahead(final String expect) {
            return getCurrent().equals(expect);
        }

        public boolean consume(final String expect) {
            if (!lookahead(expect)) {
                return false;
            }
            advance();
            return true;
        }

        public boolean lookahead(final Pattern p) {
            return p.matcher(getCurrent()).find();
        }

        public Matcher consume(final Pattern p) {
            final Matcher m = p.matcher(getCurrent());
            if (!m.find()) {
                return null;
            }
            advance();
            return m;
        }

        public void noise(final String token) {
            consume(token);
        }

        public boolean eol() {
            return index >= args.length;
        }
    }

    private static final class Configuration {

        private String host;
        private int port = 9999;
        private String action;
        private String source;
        private int sourceId;
        private final Map<String, String> policies = new HashMap<String, String>();
        private ConsistencyMBean consistency;
        private JMXConnector jmxConnector;
        private int responseTimeoutMillis = 0;

        /**
         * Initializes a new {@link ConsistencyCheck.Configuration}.
         */
        public Configuration() {
            super();
        }

        public String getAction() {
            return action;
        }

        public void setResponseTimeoutMillis(int responseTimeoutMillis) {
            this.responseTimeoutMillis = responseTimeoutMillis;
        }

        public void setHost(final String host) {
            this.host = host;
        }

        public void setPort(final int port) {
            this.port = port;
        }

        public void setAction(final String action) {
            this.action = action;
        }

        public void setSource(final String source) {
            this.source = source;
        }

        public void setSourceId(final int sourceId) {
            this.sourceId = sourceId;
        }

        public void addPolicy(final String condition, final String action) {
            if (policies.containsKey(condition)) {
                throw new IllegalArgumentException("Condition " + condition + " already has an action assigned to it.");
            }
            policies.put(condition, action);
        }

        public void run() throws Exception {
            if (Action.listMissing.name().equals(action)) {
                listMissing();
            } else if (Action.listUnassigned.name().equals(action)) {
                listUnassigned();
            } else if (Action.checkconfigdb.name().equals(action)) {
                checkAndRepairConfigDB(false);
            } else if (Action.repairconfigdb.name().equals(action)) {
                checkAndRepairConfigDB(true);
            } else {
                repair();
            }
        }

        private void listMissing() throws MBeanException, IOException, MalformedObjectNameException, NullPointerException {
            Map<MBeanEntity, List<String>> result = null;
            try {
                connect();

                System.out.print("Fetching a list for ");
                if (Source.database.name().equals(source)) {
                    System.out.print("all mising files in database with the identifier '" + sourceId + "'...");
                    result = consistency.listMissingFilesInDatabase(sourceId);
                } else if (Source.filestore.name().equals(source)) {
                    System.out.print("all mising files in filestore with the identifier '" + sourceId + "'...");
                    result = consistency.listMissingFilesInFilestore(sourceId);
                } else if (Source.context.name().equals(source)) {
                    System.out.print("all mising files in context with the identifier '" + sourceId + "'...");
                    result = new HashMap<MBeanEntity, List<String>>();
                    result.put(new MBeanEntity(Integer.valueOf(sourceId)), consistency.listMissingFilesInContext(sourceId));
                } else if (Source.all.name().equals(source)) {
                    System.out.print("all mising files...");
                    result = consistency.listAllMissingFiles();
                }
                System.out.println(" OK.");
            } finally {
                disconnect();
            }

            print(result);
        }

        private void repair() throws MBeanException, IOException, MalformedObjectNameException, NullPointerException {
            if (policies.isEmpty()) {
                System.out.println("Please specify a policy (either \"" + Policy.missing_entry_for_file.name() + "\" or \"" + Policy.missing_file_for_attachment.name() + "\" or \"" + Policy.missing_file_for_infoitem.name() + "\" or \"" + Policy.missing_file_for_vcard.name() + "\").");
                return;
            }
            try {
                connect();

                System.out.print("Repairing ");
                String policyString = getPolicyString();
                if (Source.database.name().equals(source)) {
                    System.out.print("all files with policy '" + policyString + "' in the database with the identifier '" + sourceId + "'... ");
                    consistency.repairFilesInDatabase(sourceId, policyString);
                } else if (Source.filestore.name().equals(source)) {
                    System.out.print("all files with policy '" + policyString + "' in the filestore with the identifier '" + sourceId + "'... ");
                    consistency.repairFilesInFilestore(sourceId, policyString);
                } else if (Source.context.name().equals(source)) {
                    System.out.print("all files with policy '" + policyString + "' in the context with the identifier '" + sourceId + "'... ");
                    consistency.repairFilesInContext(sourceId, policyString);
                } else if (Source.all.name().equals(source)) {
                    System.out.print("all files with policy '" + policyString + "'... ");
                    consistency.repairAllFiles(policyString);
                }
                System.out.println(" OK.");
            } finally {
                disconnect();
            }
        }

        private String getPolicyString() {
            final StringBuilder sb = new StringBuilder();
            for (final String condition : policies.keySet()) {
                sb.append(condition).append(':').append(policies.get(condition)).append(',');
            }
            sb.setLength(sb.length() - 1);
            return sb.toString();
        }

        private void disconnect() {
            System.out.print("Closing connection... ");
            if (null != jmxConnector) {
                try {
                    jmxConnector.close();
                } catch (final Exception e) {
                    System.out.println(" Error while closing connection: '" + e.getMessage() + '.');
                }
            }
            System.out.println("OK.");
        }

        private void connect() throws IOException, MalformedObjectNameException, NullPointerException {
            if (responseTimeoutMillis > 0) {
                /*
                 * The value of this property represents the length of time (in milliseconds) that the client-side Java RMI runtime will
                 * use as a socket read timeout on an established JRMP connection when reading response data for a remote method invocation.
                 * Therefore, this property can be used to impose a timeout on waiting for the results of remote invocations;
                 * if this timeout expires, the associated invocation will fail with a java.rmi.RemoteException.
                 * 
                 * Setting this property should be done with due consideration, however, because it effectively places an upper bound on the
                 * allowed duration of any successful outgoing remote invocation. The maximum value is Integer.MAX_VALUE, and a value of
                 * zero indicates an infinite timeout. The default value is zero (no timeout).
                 */
                System.setProperty("sun.rmi.transport.tcp.responseTimeout", Integer.toString(responseTimeoutMillis));
            }
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/server");
            System.out.print("Connecting to '" + url + "'... ");
            jmxConnector = JMXConnectorFactory.connect(url, null);

            MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
            ObjectName name = MBeanNamer.getName();

            consistency = new MBeanConsistency(mbsc, name);
            System.out.println("OK.");
        }

        private void checkAndRepairConfigDB(final boolean repair) throws IOException, MalformedObjectNameException, NullPointerException, MBeanException {

            List<String> result = null;
            try {
                connect();
                result = consistency.checkOrRepairConfigDB(repair);
            } finally {
                disconnect();
            }

            if (null != result && result.size() > 0) {
                for (final String ctx : result) {
                    System.out.println(ctx);
                }
                if (!repair) {
                    System.out.println("Now run repairconfigdb to remove these inconsistent contexts from configdb");
                }
            }
        }

        private void listUnassigned() throws MBeanException, IOException, MalformedObjectNameException, NullPointerException {

            Map<MBeanEntity, List<String>> result = null;
            try {
                connect();
                if (Source.database.name().equals(source)) {
                    result = consistency.listUnassignedFilesInDatabase(sourceId);
                } else if (Source.filestore.name().equals(source)) {
                    result = consistency.listUnassignedFilesInFilestore(sourceId);
                } else if (Source.context.name().equals(source)) {
                    result = new HashMap<MBeanEntity, List<String>>();
                    result.put(new MBeanEntity(sourceId), consistency.listUnassignedFilesInContext(sourceId));
                } else if (Source.all.name().equals(source)) {
                    result = consistency.listAllUnassignedFiles();
                }
            } finally {
                disconnect();
            }

            print(result);
        }

        private void print(final Map<MBeanEntity, List<String>> result) {
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
}
