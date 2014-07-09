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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ConsistencyCheck {

    /**
     * Used to identify if list is desired (listUnassigned or listMissing)
     */
    private static final String ACTION_LIST = "list";

    /**
     * Used to identify listUnassigned
     */
    private static final String ACTION_UNASSIGNED = "unassigned";

    /**
     * Used to identify listMissing
     */
    private static final String ACTION_MISSING = "missing";

    private static final String ACTION_LIST_UNASSIGNED = "listUnassigned";

    private static final String ACTION_LIST_MISSING = "listMissing";

    private static final String ACTION_REPAIR = "repair";

    private static final String ACTION_REPAIRCONFIGDB = "repairconfigdb";

    private static final String ACTION_CHECKCONFIGDB = "checkconfigdb";

    private static final String SOURCE_ALL = "all";

    private static final String SOURCE_DATABASE = "database";

    private static final String SOURCE_CONTEXT = "context";

    private static final String SOURCE_FILESTORE = "filestore";

    private static final String POLICY_MISSING_ENTRY_FOR_FILE = "missing_entry_for_file";

    private static final String POLICY_MISSING_FILE_FOR_ATTACHMENT = "missing_file_for_attachment";

    private static final String POLICY_MISSING_FILE_FOR_INFOITEM = "missing_file_for_infoitem";

    private static final String POLICY_ACTION_DELETE = "delete";

    private static final String POLICY_ACTION_CREATE_DUMMY = "create_dummy";

    private static final String POLICY_ACTION_CREATE_ADMIN_INFOITEM = "create_admin_infoitem";

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
        if(lexer.consume("host")) {
            final String hostname = lexer.getCurrent();
            final String[] hostAndPort = hostname.split(":");
            config.setHost(hostAndPort[0]);
            if(hostAndPort.length > 1) {
                config.setPort(Integer.parseInt(hostAndPort[1]));
            }
            lexer.advance();
        } else {
            config.setHost(LOCALHOST);
        }

        if (lexer.consume(ACTION_REPAIR)) {
            config.setAction(ACTION_REPAIR);
        } else if (lexer.consume(ACTION_LIST)) {
            if (lexer.consume(ACTION_MISSING)) {
                config.setAction(ACTION_LIST_MISSING);
            } else if (lexer.consume(ACTION_UNASSIGNED)) {
                config.setAction(ACTION_LIST_UNASSIGNED);
            } else {
                System.exit( dontKnowWhatToList() );
            }
        } else if (lexer.consume(ACTION_CHECKCONFIGDB)) {
            config.setAction(ACTION_CHECKCONFIGDB);
        } else if (lexer.consume(ACTION_REPAIRCONFIGDB)) {
            config.setAction(ACTION_REPAIRCONFIGDB);
        } else {
            System.exit( noaction() );
        }
        lexer.noise("files");
        lexer.noise("errors");
        lexer.noise("in");

        if (!config.getAction().equals(ACTION_CHECKCONFIGDB) && !config.getAction().equals(ACTION_REPAIRCONFIGDB)) {
            if (lexer.consume(SOURCE_DATABASE)) {
                config.setSource(SOURCE_DATABASE);
                if (! parseId(lexer, config)) {
                    System.exit( noid() );
                }
            } else if (lexer.consume(SOURCE_FILESTORE)) {
                config.setSource(SOURCE_FILESTORE);
                if (! parseId(lexer, config)) {
                    System.exit( noid() );
                }
            } else if (lexer.consume(SOURCE_CONTEXT)) {
                config.setSource(SOURCE_CONTEXT);
                if (! parseId(lexer, config)) {
                    System.exit( noid() );
                }
            } else if (lexer.consume(SOURCE_ALL)) {
                config.setSource(SOURCE_ALL);
            } else {
                System.exit( noproblemsource() );
            }

            lexer.noise("with");
            lexer.noise("policies");

            while(!lexer.eol()) {
                if (lexer.consume(POLICY_MISSING_FILE_FOR_INFOITEM)) {
                    lexer.noise(":");
                    if(lexer.consume(POLICY_ACTION_CREATE_DUMMY)){
                        config.addPolicy(POLICY_MISSING_FILE_FOR_INFOITEM, POLICY_ACTION_CREATE_DUMMY);
                    } else if(lexer.consume(POLICY_ACTION_DELETE)) {
                        config.addPolicy(POLICY_MISSING_FILE_FOR_INFOITEM, POLICY_ACTION_DELETE);
                    } else {
                        System.exit(unknownAction(POLICY_MISSING_FILE_FOR_INFOITEM, lexer.getCurrent(), POLICY_ACTION_CREATE_DUMMY, POLICY_ACTION_DELETE));
                    }
                } else if (lexer.consume(POLICY_MISSING_FILE_FOR_ATTACHMENT)) {
                    lexer.noise(":");
                    if(lexer.consume(POLICY_ACTION_CREATE_DUMMY)){
                        config.addPolicy(POLICY_MISSING_FILE_FOR_ATTACHMENT, POLICY_ACTION_CREATE_DUMMY);
                    } else if(lexer.consume(POLICY_ACTION_DELETE)) {
                        config.addPolicy(POLICY_MISSING_FILE_FOR_ATTACHMENT, POLICY_ACTION_DELETE);
                    } else {
                        System.exit(unknownAction(POLICY_MISSING_FILE_FOR_INFOITEM, lexer.getCurrent(), POLICY_ACTION_CREATE_DUMMY, POLICY_ACTION_DELETE));
                    }
                } else if (lexer.consume(POLICY_MISSING_ENTRY_FOR_FILE)) {
                    lexer.noise(":");
                    if (lexer.consume(POLICY_ACTION_CREATE_ADMIN_INFOITEM)) {
                        config.addPolicy(POLICY_MISSING_ENTRY_FOR_FILE, POLICY_ACTION_CREATE_ADMIN_INFOITEM);
                    } else if(lexer.consume(POLICY_ACTION_DELETE)) {
                        config.addPolicy(POLICY_MISSING_ENTRY_FOR_FILE, POLICY_ACTION_DELETE);
                    } else {
                        System.exit(unknownAction(POLICY_MISSING_FILE_FOR_INFOITEM, lexer.getCurrent(), POLICY_ACTION_CREATE_ADMIN_INFOITEM, POLICY_ACTION_DELETE));
                    }
                } else {
                    System.exit(unknownCondition(lexer.getCurrent(), POLICY_MISSING_FILE_FOR_INFOITEM, POLICY_MISSING_FILE_FOR_ATTACHMENT, POLICY_MISSING_ENTRY_FOR_FILE));
                }
            }
        }

        try {
            config.run();
            System.out.println("Done");
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
        System.err.println("Please tell me what to list. Either \"list missing\" or \"list unassigned\".");
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
        System.err.println("Please specify what to search for problems (either \"context [id]\" or \"filestore [id]\" or \"database [id]\" or \"all\".");
        return 2;
    }

    private static int noaction() {
        final String ls = System.getProperty("line.separator");
        System.err.println("Please specify an action, either"+ls+"\"list missing\", \"list unassigned\", \"repair\", \"checkconfigdb\" or \"repairconfigdb\"" + ls +
            "You can also specify the hostname of the open-xchange server, optionally."+ls +
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
            if(eol()) {
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
            if(!m.find()) {
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

        /**
         * Initializes a new {@link ConsistencyCheck.Configuration}.
         */
        public Configuration() {
            super();
        }

        public String getAction() {
            return action;
        }

        private String host;
        private int port = 9999 ;
        private String action;
        private String source;
        private int sourceId;
        private final Map<String, String> policies = new HashMap<String,String>();
        private ConsistencyMBean consistency;
        private JMXConnector jmxConnector;

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
            if(policies.containsKey(condition)) {
                throw new IllegalArgumentException("Condition "+condition+" already has an action assigned to it.");
            }
            policies.put(condition, action);
        }

        public void run() throws Exception {
            if (ACTION_LIST_MISSING.equals(action)) {
                listMissing();
            } else if (ACTION_LIST_UNASSIGNED.equals(action)) {
                listUnassigned();
            } else if (ACTION_CHECKCONFIGDB.equals(action)) {
                checkAndRepairConfigDB(false);
            } else if (ACTION_REPAIRCONFIGDB.equals(action)) {
                checkAndRepairConfigDB(true);
            } else {
                repair();
            }
        }

        private void listMissing() throws MBeanException, IOException, MalformedObjectNameException, NullPointerException {

            Map<Integer, List<String>> result = null;
            try {
                connect();
                if (SOURCE_DATABASE.equals(source)) {
                    result = consistency.listMissingFilesInDatabase(sourceId);
                } else if (SOURCE_FILESTORE.equals(source)) {
                    result = consistency.listMissingFilesInFilestore(sourceId);
                } else if (SOURCE_CONTEXT.equals(source)) {
                    result = new HashMap<Integer, List<String>>();
                    result.put(Integer.valueOf(sourceId), consistency.listMissingFilesInContext(sourceId));
                } else if (SOURCE_ALL.equals(source)) {
                    result = consistency.listAllMissingFiles();
                }
            } finally {
                disconnect();
            }

            print(result);
        }

        private void repair() throws MBeanException, IOException, MalformedObjectNameException, NullPointerException {
            if(policies.isEmpty()) {
                System.out.println("Please specify a policy (either \"missing_entry_for_file\" or \"missing_file_for_attachment\" or \"missing_file_for_infoitem\").");
                return;
            }
            try {
                connect();
                if (SOURCE_DATABASE.equals(source)) {
                    consistency.repairFilesInDatabase(sourceId, getPolicyString());
                } else if (SOURCE_FILESTORE.equals(source)) {
                    consistency.repairFilesInFilestore(sourceId, getPolicyString());
                } else if (SOURCE_CONTEXT.equals(source)) {
                    consistency.repairFilesInContext(sourceId, getPolicyString());
                } else if (SOURCE_ALL.equals(source)) {
                    consistency.repairAllFiles(getPolicyString());
                }
            } finally {
                disconnect();
            }
        }

        private String getPolicyString() {
            final StringBuilder sb = new StringBuilder();
            for(final String condition : policies.keySet()) {
                sb.append(condition).append(':').append(policies.get(condition)).append(',');
            }
            sb.setLength(sb.length()-1);
            return sb.toString();
        }

        private void disconnect() {
            if (null != jmxConnector) {
                try {
                    jmxConnector.close();
                } catch (final Exception e) {
                    // Ignore
                }
            }
        }

        private void connect() throws IOException, MalformedObjectNameException, NullPointerException {
            final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"
                + host + ":" + port + "/server");

            jmxConnector = JMXConnectorFactory.connect(url, null);

            final MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();

            final ObjectName name = MBeanNamer.getName();

            consistency = new MBeanConsistency(mbsc, name);
        }

        private void checkAndRepairConfigDB(final boolean repair) throws IOException, MalformedObjectNameException, NullPointerException, MBeanException {

            List<String> result = null;
            try {
                connect();
                result = consistency.checkOrRepairConfigDB(repair);
            } finally {
                disconnect();
            }

            if( null != result && result.size() > 0) {
                for(final String ctx : result) {
                    System.out.println(ctx);
                }
                if( ! repair ) {
                    System.out.println("Now run repairconfigdb to remove these inconsistent contexts from configdb");
                }
            }
        }

        private void listUnassigned() throws MBeanException, IOException, MalformedObjectNameException, NullPointerException {

            Map<Integer, List<String>> result = null;
            try {
                connect();
                if (SOURCE_DATABASE.equals(source)) {
                    result = consistency.listUnassignedFilesInDatabase(sourceId);
                } else if (SOURCE_FILESTORE.equals(source)) {
                    result = consistency.listUnassignedFilesInFilestore(sourceId);
                } else if (SOURCE_CONTEXT.equals(source)) {
                    result = new HashMap<Integer, List<String>>();
                    result.put(Integer.valueOf(sourceId), consistency.listUnassignedFilesInContext(sourceId));
                } else if (SOURCE_ALL.equals(source)) {
                    result = consistency.listAllUnassignedFiles();
                }
            } finally {
                disconnect();
            }

            print(result);
        }

        private void print(final Map<Integer, List<String>> result) {
            if (null == result) {
                return;
            }
            for(final Map.Entry<Integer, List<String>> entry : result.entrySet()) {
                final int ctxId = entry.getKey().intValue();
                final List<String> brokenFiles = entry.getValue();
                System.out.println("I found " + brokenFiles.size() + " problem(s) in context " + ctxId);
                for (final String brokenFile : brokenFiles) {
                    System.out.println("\t"+brokenFile);
                }
                System.out.println("I found " + brokenFiles.size() + " problem(s) in context " + ctxId);
            }
        }
    }
}
