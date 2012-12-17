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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.datamining;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import com.mysql.jdbc.MySQLConnection;

/**
 * This is a simple Tool to get an idea how a specific installation of Open-Xchange is used. Operating on the MySQL-database exclusively it
 * is quite fast and uses few resources. Off-hours are still recommended for its usage to limit any performance-impact, though. It will find its required parameters automatically in the file
 * /opt/open-xchange/etc/groupware/configdb.properties. Otherwise it is possible to specify all parameters explicitly. Output is a single
 * text-file. The filename starts with "open-xchange_datamining" and includes the current date in YYYY-MM-DD format. The content of the file
 * is camelCased-Parameters, unique and one per line. This should make using these files as input, for example for a visualization, pretty
 * easy.
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class Datamining {

    /**
     * 
     */
    private static final String AVERAGE_FILESTORE_SIZE = "averageFilestoreSize";

    public static final String AVERAGE_NUMBER_OF_INFOSTORE_OBJECTS_PER_SCHEMA = "averageNumberOfInfostoreObjectsPerSchema";

    public static final String AVERAGE_NUMBER_OF_INFOSTORE_OBJECTS_PER_CONTEXT = "averageNumberOfInfostoreObjectsPerContext";

    public static final String NUMBER_OF_CONTEXTS = "numberOfContexts";

    public static final String NUMBER_OF_SCHEMATA = "numberOfSchemata";

    private static MySQLConnection instance = null;

    private static Connection configdbConnection = null;

    private static String hostname = "";

    private static String dbPort = "3306";

    private static String dbName = "";

    private static String configDBURL = "";

    private static String configDBUser = "";

    private static String configDBPassword = "";

    private static boolean verbose = false;

    private static boolean helpPrinted = false;

    private static String reportfilePath = "";

    private static String filename = "";

    private static OptionParser optionParser = new OptionParser();

    private static StringBuilder reportStringBuilder = new StringBuilder();

    public static ArrayList<String> allTheQuestions = new ArrayList<String>();

    private static HashMap<String, String> allTheAnswers = new HashMap<String, String>();

    private static ArrayList<Schema> allSchemata;

    public static void main(String[] args) {        
        Calendar rightNow = Calendar.getInstance();
        final long before = rightNow.getTime().getTime();

        setReportFilename();
        readParameters(args);
        if ((configDBURL.equals("") || configDBUser.equals("") || configDBPassword.equals("")) && !helpPrinted) {
            readProperties();
        }

        if (configDBURL.equals("") || configDBUser.equals("") || configDBPassword.equals("")) {
            if (!helpPrinted)
                printHelp();
        } else if (!helpPrinted) {
            configdbConnection = getDBConnection(configDBURL, configDBUser, configDBPassword);
            allSchemata = getAllSchemata();
            report(NUMBER_OF_SCHEMATA, Integer.toString(allSchemata.size()));

            reportAverageFilestoreSize();
            reportNumberOfContexts();
            Questions.reportNumberOfUsers();
            Questions.reportNumberOfUsersWithEventsInPrivateCalendar();
            Questions.reportNumberOfUsersWithEventsInPrivateCalendarThatAreInTheFutureAndAreNotYearlySeries();
            Questions.reportNumberOfUsersWhoChangedTheirCalendarInTheLast30Days();
            Questions.reportNumberOfInfostoreObjects();
            reportAverageNumberOfInfostoreObjectsPerContext();
            reportAverageNumberOfInfostoreObjectsPerSchema();
            Questions.reportNumberOfNewInfostoreObjectsInTheLast30Days();
            Questions.reportNumberOfChangedInfostoreObjectsInTheLast30Days();
            Questions.reportNumberOfUsersWithNewInfostoreObjectsInTheLast30Days();
            Questions.reportSliceAndDiceOnDocumentSize();
            Questions.reportNumberOfContacts();
            Questions.reportNumberOfUsersWhoCreatedContacts();
            Questions.reportNumberOfUsersWhoChangedTheirContactsInTheLast30Days();
            Questions.reportAverageNumberOfContactsPerUserWhoHasContactsAtAll();
            Questions.reportNumberOfUsersWithLinkedSocialNetworkingAccounts();
            Questions.reportNumberOfUsersWhoSelectedTeamViewAsCalendarDefault();
            Questions.reportNumberOfUsersWhoSelectedCalendarViewAsCalendarDefault();
            Questions.reportNumberOfUsersWhoSelectedListViewAsCalendarDefault();
            Questions.reportNumberOfUsersWhoSelectedCardsViewAsContactsDefault();
            Questions.reportNumberOfUsersWhoSelectedListViewAsContactsDefault();
            Questions.reportNumberOfUsersWhoSelectedListViewAsTasksDefault();
            Questions.reportNumberOfUsersWhoSelectedHSplitViewAsTasksDefault();
            Questions.reportNumberOfUsersWhoSelectedListViewAsInfostoreDefault();
            Questions.reportNumberOfUsersWhoSelectedHSplitViewAsInfostoreDefault();
            Questions.reportNumberOfUsersWhoActivatedMiniCalendar();

            rightNow = Calendar.getInstance();
            final long after = rightNow.getTime().getTime();
            report("durationInSeconds", Long.toString((after - before) / 1000));

            sanityCheck();

            printReport();

            try {
                configdbConnection.close();
            } catch (SQLException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }
    }

    private static void sanityCheck() {
        ArrayList<String> openQuestions = new ArrayList<String>();
        for (String question : allTheQuestions) {
            if (!allTheAnswers.containsKey(question)) {
                openQuestions.add(question);
            }
        }
        if (openQuestions.size() == 0) {
            report("sanityCheck", "ok");
        } else {
            report("sanityCheck", "problems");
            report("questionsNotAnswered", openQuestions.toString());
        }
    }

    private static void printHelp() {
        try {
            System.out.println("---");
            System.out.println("Usage of the Open-Xchange datamining-tool:\n\n");
            optionParser.printHelpOn(System.out);
            System.out.println("\nEither specify these parameters manually or use this tool on an Open-Xchange application server where all necessary info\n is automatically found in the file /opt/open-xchange/etc/groupware/configdb.properties");
            System.out.println("---");
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private static void reportNumberOfContexts() {
        allTheQuestions.add(NUMBER_OF_CONTEXTS);
        if (configdbConnection != null) {
            Statement query;
            try {
                query = configdbConnection.createStatement();

                String sql = "SELECT count(*) FROM context";
                ResultSet result = query.executeQuery(sql);

                while (result.next()) {
                    String count = result.getString(1);
                    report(NUMBER_OF_CONTEXTS, count);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getOneAnswer(String parameter) {
        return allTheAnswers.get(parameter);
    }

    private static void readParameters(String[] args) {        
        // mandatory
        optionParser.acceptsAll(Arrays.asList("hostname", "n"), "Host where the Open-Xchange MySQL-database is running").withRequiredArg().describedAs(
            "hostname").ofType(String.class);
        optionParser.acceptsAll(Arrays.asList("dbUser", "u"), "Name of the MySQL-User for configdb").withRequiredArg().describedAs(
            "dbuser").ofType(String.class);
        optionParser.acceptsAll(Arrays.asList("dbPassword", "p"), "Password for the user specified with \"-dbUser\"").withRequiredArg().describedAs(
            "dbpassword").ofType(String.class);
        // optional
        optionParser.acceptsAll(Arrays.asList("dbName", "d"), "Name of the MySQL-database that contains the Open-Xchange configDB").withRequiredArg().describedAs(
            "dbname").ofType(String.class).defaultsTo("configdb");
        optionParser.acceptsAll(Arrays.asList("reportfilePath"), "Path where the report-file is saved").withRequiredArg().describedAs(
            "path").ofType(String.class).defaultsTo("");
        optionParser.acceptsAll(Arrays.asList("v", "verbose"), "With this the tool prints what it is doing live");
        optionParser.acceptsAll(Arrays.asList("dbPort"), "Port where MySQL is running on the host specified with \"-hostname\"").withRequiredArg().describedAs(
            "port").defaultsTo("3306");
        optionParser.acceptsAll(Arrays.asList("h", "help", "?"), "Print this helpfile");

        boolean helpCalled = false;
        for (int i = 0; i < args.length; i++) {
            String string = args[i];
            if (string.equals("-h") || string.equals("-?") || string.equals("--help")) {
                helpCalled = true;
                printHelp();
                helpPrinted = true;
                break;
            }
        }

        for (int i = 0; i < args.length; i++) {
            String string = args[i];
            if (string.equals("-v") || string.equals("--verbose")) {
                verbose = true;
                break;
            }
        }

        if (!helpCalled) {
            try {
                OptionSet options = optionParser.parse(args);
                hostname = setItIfThereIsAValueForIt("hostname", options);
                dbName = setItIfThereIsAValueForIt("dbName", options);
                configDBUser = setItIfThereIsAValueForIt("dbUser", options);
                configDBPassword = setItIfThereIsAValueForIt("dbPassword", options);
                verbose = options.has("verbose");
                // there is _always_ a value for this one
                dbPort = (String) options.valueOf("dbPort");
                if (!hostname.equals("") && !dbName.equals("") && !dbPort.equals("")){
                    configDBURL = "jdbc:mysql://" + hostname + ":" + dbPort + "/" + dbName;
                }
                reportfilePath = setItIfThereIsAValueForIt("reportfilePath", options);
            } catch (OptionException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void readProperties() {
        // Try to read configdb.properties
        System.out.println("Trying /opt/open-xchange/etc/groupware/configdb.properties");
        Properties configdbProperties = new Properties();
        try {
            configdbProperties.load(new FileInputStream("/opt/open-xchange/etc/groupware/configdb.properties"));
            configDBURL = configdbProperties.getProperty("readUrl");

            configDBUser = configdbProperties.getProperty("readProperty.1").substring(5);
            configDBPassword = configdbProperties.getProperty("readProperty.2").substring(9);
            if (configDBURL != null && configDBUser != null && configDBPassword != null) {
                System.out.println("All necessary parameters found in configdb.properties");
            }
        } catch (FileNotFoundException e) {
            System.out.println("File /opt/open-xchange/etc/groupware/configdb.properties not available");
        } catch (IOException e) {
            System.out.println("File /opt/open-xchange/etc/groupware/configdb.properties not available");
        }
    }

    protected static void report(String parameter, String value) {
        String combined = parameter + "=" + value;
        allTheAnswers.put(parameter, value);
        reportStringBuilder.append(combined + "\n");
        if (verbose) {
            System.out.println(combined);
        }
    }

    private static void printReport() {
        try {
            FileOutputStream fos = new FileOutputStream(reportfilePath + filename);
            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
            out.write(reportStringBuilder.toString());
            out.close();
            fos.close();
            System.out.println("report written to this file : " + reportfilePath + filename);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static MySQLConnection getDBConnection(String url, String user, String password) {
        try {

            // load mysql driver
            Class.forName("com.mysql.jdbc.Driver");

            // connect
            Connection conn = DriverManager.getConnection(url + "?" + "user=" + user + "&" + "password=" + password);
            return (MySQLConnection) conn;
        } catch (ClassNotFoundException e) {
            System.out.println("Error : JDBC driver not found");
        } catch (SQLException e) {
            System.out.println("Error : No SQL-connection possible to this URL: " + url + " with this user and password : (" + user + " / " + password + ")");
            System.out.println(e.getMessage());
            System.exit(0);
        }
        return null;
    }

    private static void setReportFilename() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            Calendar cal = Calendar.getInstance();
            String date = Integer.toString(cal.get(Calendar.YEAR)) + "-" + Integer.toString(cal.get(Calendar.MONTH)) + "-" + Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
            filename = "open-xchange_datamining_" + addr.getHostAddress() + "_" + date + ".txt";
            report("hostIPAddress", addr.getHostAddress());
            report("hostname", addr.getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<Schema> getAllSchemata() {
        allTheQuestions.add(NUMBER_OF_SCHEMATA);
        ArrayList<Schema> schemata = new ArrayList<Schema>();
        try {
            if (configdbConnection != null) {
                report("configdbUrl", configDBURL);
                Statement query;
                try {
                    query = configdbConnection.createStatement();

                    String sql = "SELECT DISTINCT csp.db_schema, csp.read_db_pool_id, dp.url, dp.login, dp.password FROM context_server2db_pool csp, db_pool dp WHERE csp.read_db_pool_id = dp.db_pool_id;";
                    ResultSet result = query.executeQuery(sql);

                    while (result.next()) {
                        Schema schema = new Schema(
                            result.getString("db_schema"),
                            result.getString("read_db_pool_id"),
                            result.getString("url"),
                            result.getString("login"),
                            result.getString("password"));
                        schemata.add(schema);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schemata;
    }

    private static ArrayList<String> reportAverageFilestoreSize() {
        allTheQuestions.add(AVERAGE_FILESTORE_SIZE);
        ArrayList<String> schemata = new ArrayList<String>();
        try {
            if (configdbConnection != null) {
                Statement query;
                try {
                    query = configdbConnection.createStatement();

                    String sql = "SELECT ROUND(AVG(size)) FROM filestore";
                    ResultSet result = query.executeQuery(sql);

                    while (result.next()) {
                        String size = result.getString(1);
                        report(AVERAGE_FILESTORE_SIZE, size);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
        }
        return schemata;
    }

    private static void reportAverageNumberOfInfostoreObjectsPerContext() {
        allTheQuestions.add(AVERAGE_NUMBER_OF_INFOSTORE_OBJECTS_PER_CONTEXT);
        Float numberInAllSchemata = new Float(0.0);
        try {
            for (Schema schema : allSchemata) {
                String url = schema.getUrl().substring(0, schema.getUrl().lastIndexOf("/")) + ":" + dbPort + "/" + schema.getSchemaname();
                MySQLConnection conn = getDBConnection(url, schema.getLogin(), schema.getPassword());
                if (conn != null) {
                    Statement query;
                    try {
                        query = conn.createStatement();

                        String sql = "SELECT AVG(number_per_context) FROM (SELECT COUNT(id) AS number_per_context FROM infostore GROUP BY cid) AS T";
                        ResultSet result = query.executeQuery(sql);

                        while (result.next()) {
                            String numberInOneSchema = result.getString(1);
                            numberInAllSchemata += new Float(numberInOneSchema);
                        }
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
        }
        report(AVERAGE_NUMBER_OF_INFOSTORE_OBJECTS_PER_CONTEXT, Float.toString(numberInAllSchemata / allSchemata.size()));
    }

    private static void reportAverageNumberOfInfostoreObjectsPerSchema() {
        allTheQuestions.add(AVERAGE_NUMBER_OF_INFOSTORE_OBJECTS_PER_SCHEMA);
        Float numberInAllSchemata = new Float(0.0);
        try {
            for (Schema schema : allSchemata) {
                String url = schema.getUrl().substring(0, schema.getUrl().lastIndexOf("/")) + ":" + dbPort + "/" + schema.getSchemaname();
                MySQLConnection conn = getDBConnection(url, schema.getLogin(), schema.getPassword());
                if (conn != null) {
                    Statement query;
                    try {
                        query = conn.createStatement();

                        String sql = "SELECT COUNT(*) FROM infostore";
                        ResultSet result = query.executeQuery(sql);

                        while (result.next()) {
                            String numberInOneSchema = result.getString(1);
                            numberInAllSchemata += new Float(numberInOneSchema);
                        }
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
        }
        report(AVERAGE_NUMBER_OF_INFOSTORE_OBJECTS_PER_SCHEMA, Float.toString(numberInAllSchemata / allSchemata.size()));
    }

    protected static BigInteger countOverAllSchemata(String sql) {
        BigInteger numberOfObjects = new BigInteger("0");
        for (Schema schema : allSchemata) {
            String url = schema.getUrl().substring(0, schema.getUrl().lastIndexOf("/")) + ":" + dbPort + "/" + schema.getSchemaname();
            MySQLConnection conn = Datamining.getDBConnection(url, schema.getLogin(), schema.getPassword());
            if (conn != null) {
                Statement query;
                try {
                    query = conn.createStatement();
                    ResultSet result = query.executeQuery(sql);
                    while (result.next()) {
                        String numberInOneSchema = result.getString(1);
                        numberOfObjects = numberOfObjects.add(new BigInteger(numberInOneSchema));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return numberOfObjects;
    }
    
    private static String setItIfThereIsAValueForIt(String parameterName, OptionSet allParameters){
        if (allParameters.has(parameterName)){
            return (String) allParameters.valueOf(parameterName);
        } else {
            return "";
        }
    }

    public static String getFilename(){
        return filename;
    }
}
