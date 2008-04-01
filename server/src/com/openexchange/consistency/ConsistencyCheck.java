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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.groupware.AbstractOXException;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnector;
import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;

/**
 * CommandLineClient to run the consistency tool.
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ConsistencyCheck {
    public static void main(String[] args) {
        // in host 127.0.0.1:9999 (repair | list (missing | unassigned) (errors in) (database 1 | filestore 1 | context 1 | all ) (with policies) (missing_file_for_infoitem : (delete | create_dummy)) (missing_file_for_attachment : (delete | create_dummy)) (missing_entry_for_file : (delete | create_admin_infoitem))
        SimpleLexer lexer = new SimpleLexer(args);
        Configuration config = new Configuration();

        lexer.noise("in");
        if(lexer.consume("host")) {
            String hostname = lexer.getCurrent();
            String[] hostAndPort = hostname.split(":");
            config.setHost(hostAndPort[0]);
            if(hostAndPort.length > 1) {
                config.setPort(Integer.parseInt(hostAndPort[1]));
            }
            lexer.advance();
        } else {
            config.setHost("localhost");
        }

        if(lexer.consume("repair")) {
            config.setAction("repair");
        } else if (lexer.consume("list")) {
            if(lexer.consume("missing")) {
                config.setAction("listMissing");
            } else if (lexer.consume("unassigned")) {
                config.setAction("listUnassigned");
            } else {
                System.exit( dontKnowWhatToList() );
            }
        } else {
            System.exit( noaction() );
        }
        lexer.noise("files");
        lexer.noise("errors");
        lexer.noise("in");
        
        if(lexer.consume("database")) {
            config.setSource("database");
            if (! parseId(lexer, config)) {
                System.exit( noid() );
            };
        } else if (lexer.consume("filestore")) {
            config.setSource("filestore");
            if (! parseId(lexer, config)) {
                System.exit( noid() );
            };
        } else if (lexer.consume("context")) {
            config.setSource("context");
            if (! parseId(lexer, config)) {
                System.exit( noid() );
            };
        } else if (lexer.consume("all")) {
            config.setSource("all");
        } else {
            System.exit( noproblemsource() );
        }

        lexer.noise("with");
        lexer.noise("policies");

        while(!lexer.eol()) {
            if(lexer.consume("missing_file_for_infoitem")) {
                lexer.noise(":");
                if(lexer.consume("create_dummy")){
                    config.addPolicy("missing_file_for_infoitem" , "create_dummy");
                } else if(lexer.consume("delete")) {
                    config.addPolicy("missing_file_for_infoitem" , "delete");
                } else {
                    System.exit( unknownAction("missing_file_for_infoitem", lexer.getCurrent(), "create_dummy, delete") );
                }
            } else if (lexer.consume("missing_file_for_attachment")) {
                lexer.noise(":");
                if(lexer.consume("create_dummy")){
                    config.addPolicy("missing_file_for_attachment" , "create_dummy");                    
                } else if(lexer.consume("delete")) {
                    config.addPolicy("missing_file_for_attachment" , "delete");
                } else {
                    System.exit( unknownAction("missing_file_for_infoitem", lexer.getCurrent(), "create_dummy, delete") );
                }
            } else if (lexer.consume("missing_entry_for_file")) {
                lexer.noise(":");
                if(lexer.consume("create_admin_infoitem")){
                    config.addPolicy("missing_entry_for_file" , "create_admin_infoitem");
                } else if(lexer.consume("delete")) {
                    config.addPolicy("missing_entry_for_file" , "delete");
                } else {
                    System.exit( unknownAction("missing_file_for_infoitem", lexer.getCurrent(), "create_admin_infoitem, delete") );
                }
            } else {
                System.exit( unknownCondition(lexer.getCurrent(), "missing_file_for_infoitem, missing_file_for_attachment, missing_entry_for_file") );
            }
        }

        try {
            config.run();
            System.out.println("Done");
        } catch (Exception x) {
            x.printStackTrace();
        }

    }


    private static boolean parseId(SimpleLexer lexer, Configuration config) {
        try {
            config.setSourceId(Integer.parseInt(lexer.getCurrent()));
            lexer.advance();
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private static int dontKnowWhatToList() {
        System.err.println("Please tell me what to list. Either list missing or list unassigned.");
        return 6;
    }

    private static int unknownAction(String condition, String action, String possibleActions) {
        System.err.println("Unknown action "+action+" for condition "+condition+". I know only "+possibleActions);
        return 5;
    }

    private static int unknownCondition(String condition, String possibleConditions) {
        System.err.println("Unknown condition "+condition+" I know only about: "+possibleConditions);
        return 4;
    }

    private static int noid() {
        System.err.println("Please specify an id for the context, filestore or database that I should check.");
        return 3;
    }

    private static int noproblemsource() {
        System.err.println("Please specify what to search for problems (either \"context [id]\" or \"filestore [id]\" or  \"database [id]\".");
        return 2;
    }

    private static int noaction() {
        System.err.println("Please specify an action (either \"list missing\", \"list unassigned\" or \"repair\" ");
        return 1;
    }


    private static class SimpleLexer {
        private String[] args;
        private int index;

        public SimpleLexer(String[] args) {
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

        public boolean lookahead(String expect) {
            return getCurrent().equals(expect);
        }

        public boolean consume(String expect) {
            if(lookahead(expect)) {
                advance();
                return true;
            } else {
                return false;
            }
        }

        public boolean lookahead(Pattern p) {
            return p.matcher(getCurrent()).find();
        }

        public Matcher consume(Pattern p) {
            Matcher m = p.matcher(getCurrent());
            if(m.find()) {
                advance();
                return m;
            } else {
                return null;
            }
        }

        public void noise(String token) {
            consume(token);
        }

        public boolean eol() {
            return index >= args.length;
        }
    }

    private static final class Configuration {
        private String host;
        private int port = 9999 ;
        private String action;
        private String source;
        private int sourceId;
        private Map<String, String> policies = new HashMap<String,String>();
        private ConsistencyMBean consistency;
        private JMXConnector jmxConnector;

        public void setHost(String host) {
            this.host = host;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public void setSourceId(int sourceId) {
            this.sourceId = sourceId;
        }

        public void addPolicy(String condition, String action) {
            if(policies.containsKey(condition)) {
                throw new IllegalArgumentException("Condition "+condition+" already has an action assigned to it.");
            }
            policies.put(condition, action);
        }

        public void run() throws Exception {
            if("listMissing".equals(action)) {
                listMissing();
            } else if ("listUnassigned".equals(action)) {
                listUnassigned();
            } else {
                repair();
            }
        }

        private void listMissing() throws AbstractOXException, IOException {

            Map<Integer, List<String>> result = null;
            try {
                connect();
                if("database".equals(source)) {
                    result = consistency.listMissingFilesInDatabase(sourceId);
                } else if ("filestore".equals(source)) {
                    result = consistency.listMissingFilesInFilestore(sourceId);
                } else if ("context".equals(source)) {
                    result = new HashMap<Integer, List<String>>();
                    result.put(sourceId, consistency.listMissingFilesInContext(sourceId));
                } else if ("all".equals(source)) {
                    result = consistency.listAllMissingFiles();
                }
            } finally {
                disconnect();
            }

            print(result);
        }

        private void repair() throws AbstractOXException, IOException {
            if(policies.isEmpty()) {
                System.out.println("Nothing to be done. Please specify one or more resolver policies");
                return;
            }
            try {
                connect();
                if("database".equals(source)) {
                    consistency.repairFilesInDatabase(sourceId, getPolicyString());
                } else if ("filestore".equals(source)) {
                    consistency.repairFilesInFilestore(sourceId, getPolicyString());
                } else if ("context".equals(source)) {
                    consistency.repairFilesInContext(sourceId, getPolicyString());
                } else if ("all".equals(source)) {
                    consistency.repairAllFiles(getPolicyString());
                }
            } finally {
                disconnect();
            }
        }

        private String getPolicyString() {
            StringBuilder sb = new StringBuilder();
            for(String condition : policies.keySet()) {
                sb.append(condition).append(":").append(policies.get(condition)).append(",");
            }
            sb.setLength(sb.length()-1);
            return sb.toString();
        }

        private void disconnect() {
            try {
                if(jmxConnector != null) {
                    jmxConnector.close();                     
                }
            } catch (IOException e) {
                //IGNORE
            }
        }

        private void connect() throws IOException {
            final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"
                    + host + ":" + port + "/server");

            jmxConnector = JMXConnectorFactory.connect(url, null);

            MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();

            ObjectName name = JMXToolkit.getObjectName();

            consistency = new MBeanConsistency(mbsc, name);
        }

        private void listUnassigned() throws AbstractOXException, IOException {

            Map<Integer, List<String>> result = null;
            try {
                connect();
                if("database".equals(source)) {
                    result = consistency.listUnassignedFilesInDatabase(sourceId);
                } else if ("filestore".equals(source)) {
                    result = consistency.listUnassignedFilesInFilestore(sourceId);
                } else if ("context".equals(source)) {
                    result = new HashMap<Integer, List<String>>();
                    result.put(sourceId, consistency.listUnassignedFilesInContext(sourceId));
                } else if ("all".equals(source)) {
                    result = consistency.listAllUnassignedFiles();
                }
            } finally {
                disconnect();
            }

            print(result);
        }

        private void print(Map<Integer, List<String>> result) {
            for(int ctxId : result.keySet()) {
                List<String> brokenFiles = result.get(ctxId);
                System.out.println("I found "+brokenFiles.size()+" problems in context "+ctxId);
                for (String brokenFile : brokenFiles) {
                    System.out.println("\t"+brokenFile);
                }
            }
        }



    }


}
