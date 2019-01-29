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

package com.openexchange.admin.console.util.database;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.plugin.hosting.exceptions.TargetDatabaseException;
import com.openexchange.admin.plugin.hosting.schemamove.mbean.SchemaMoveRemote;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.ObjectNamingAbstraction;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;
import com.openexchange.admin.rmi.exceptions.MissingServiceException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.groupware.update.tools.Constants;
import com.openexchange.java.Strings;

/**
 * {@link UpgradeSchemata}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.10.0
 */
public class UpgradeSchemata extends ObjectNamingAbstraction {

    private static final String PROMPT = "Type 'abort' to abort the upgrade process, or 'continue' to proceed ['abort'/'continue']:";
    private static final String ABORT = "abort";
    private static final String CONTINUE = "continue";

    private CLIOption serverNameOption;
    private CLIOption schemaNameOption;
    private CLIOption jmxHostNameOption;
    private CLIOption jmxPortNameOption;
    private CLIOption jmxLoginNameOption;
    private CLIOption jmxPasswordNameOption;
    private CLIOption forceOption;
    private CLIOption skipOption;

    private Server server;
    private String jmxHost;
    private int jmxPort;
    private boolean force;

    private Credentials credentials;

    private OXUtilInterface oxUtil;
    private SchemaMoveRemote schemaMoveUtil;
    private MBeanServerConnection mbeanConnection;
    private String startFromSchema;
    private List<String> skippedSchemata = new ArrayList<>();

    /**
     * Entry point
     * 
     * @param args The command line arguments
     */
    @SuppressWarnings("unused")
    public static void main(String args[]) {
        new UpgradeSchemata(args);
    }

    /**
     * Initialises a new {@link UpgradeSchemata}.
     * 
     * @param args The command line arguments
     */
    public UpgradeSchemata(String[] args) {
        super();

        AdminParser parser = new AdminParser("upgradeschemata");
        setOptions(parser);
        try {
            parser.ownparse(args);
            checkAndSetArguments(parser);
            initialiseRMIServices(parser);
            execute(parser);
        } catch (MissingOptionException e) {
            printErrors(null, null, e, parser);
        } catch (Exception e) {
            System.err.println("An error occurred during schema upgrade. Manual intervention is advised.");
            printErrors(null, null, e, parser);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.plugin.hosting.console.ObjectNamingAbstraction#getObjectName()
     */
    @Override
    protected String getObjectName() {
        return "upgradeschemata";
    }

    //////////////////////// HELPERS //////////////////////////
    /**
     * Initialises the RMI services
     * 
     * @throws NotBoundException if the required RMI services are absent
     * @throws RemoteException if the RMI registry cannot be contacted
     * @throws MalformedURLException if the names or the required RMI services are not appropriately formatted URLs
     * @throws IOException if an I/O error occurs
     */
    private void initialiseRMIServices(AdminParser parser) throws MalformedURLException, RemoteException, NotBoundException, IOException {
        oxUtil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);
        schemaMoveUtil = (SchemaMoveRemote) Naming.lookup(RMI_HOSTNAME + SchemaMoveRemote.RMI_NAME);
        mbeanConnection = getMBeanConnection(parser);
    }

    /**
     * Executes the command
     * 
     * @param parser The {@link AdminParser}
     * @throws InvalidDataException
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws RemoteException
     * @throws MissingServiceException
     * @throws NoSuchObjectException
     * @throws Exception if a fatal error is occurred
     */
    private void execute(AdminParser parser) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, MissingServiceException, InstanceNotFoundException, MBeanException, ReflectionException {
        registerServer();
        for (Database database : listSchemata()) {
            boolean error = false;
            String schemaName = database.getScheme();
            try {
                disableSchema(schemaName);
                changeServer(schemaName);
                runUpdates(schemaName);
                enableSchema(schemaName);
            } catch (TargetDatabaseException e) {
                error = true;
                System.err.println("An error occurred while trying to disable schema '" + schemaName + "': " + e.getMessage());
                printServerException(e, parser);
            } catch (IOException e) {
                error = true;
                System.err.println("An I/O error occurred while trying to upgrade schema '" + schemaName + "': " + e.getMessage());
                printServerException(e, parser);
            } catch (StorageException e) {
                if (false == e.getMessage().contains("The schema is empty")) {
                    throw e;
                }
                System.out.println("The schema '" + schemaName + "' is empty. Skipping.");
            } catch (NoSuchObjectException e) {
                System.out.println("Schema " + schemaName + " not found. Skipping");
            } finally {
                if (!force && error) {
                    sysexit(1);
                }
            }
        }
    }

    /**
     * Lists all known schemata
     * 
     * @return The known schemata
     * @throws RemoteException See {@link OXUtilInterface#listDatabaseSchema(String, Boolean, Credentials)}
     * @throws StorageException See {@link OXUtilInterface#listDatabaseSchema(String, Boolean, Credentials)}
     * @throws InvalidCredentialsException See {@link OXUtilInterface#listDatabaseSchema(String, Boolean, Credentials)}
     * @throws InvalidDataException See {@link OXUtilInterface#listDatabaseSchema(String, Boolean, Credentials)}
     */
    private Database[] listSchemata() throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Database[] databases = oxUtil.listDatabaseSchema("*", Boolean.FALSE, credentials);
        if (Strings.isEmpty(startFromSchema) || databases.length == 1) {
            return databases[0].getScheme().equals(startFromSchema) ? new Database[0] : databases;
        }

        // Sort arithmetically, that is by integer suffix of each database schema
        Comparator<Database> comparator = (o1, o2) -> {
            Integer i1 = Integer.parseInt(o1.getScheme().substring(o1.getScheme().lastIndexOf('_') + 1));
            Integer i2 = Integer.parseInt(o2.getScheme().substring(o2.getScheme().lastIndexOf('_') + 1));
            return i1.compareTo(i2);
        };
        Arrays.sort(databases, comparator);
        int position = Arrays.binarySearch(databases, new Database(-1, startFromSchema), comparator);
        if (position < 0) {
            return filterSchemata(databases, comparator);
        }
        System.out.println("Skipping to schema '" + startFromSchema + "'");
        return filterSchemata(Arrays.copyOfRange(databases, position + 1, databases.length), comparator);
    }

    /**
     * Filters the specified database schemata and removes those that are present in the 'skippedSchemata'
     * 
     * @param databases The database schemata array
     * @param comparator The comparator to be used to perform the search
     * @return The filtered schemata
     */
    private Database[] filterSchemata(Database[] databases, Comparator<Database> comparator) {
        System.out.print("Filtering out skipped schemata: ");

        Set<Integer> indexesToSkip = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        for (String schema : skippedSchemata) {
            sb.append("'").append(schema).append("', ");
            int position = Arrays.binarySearch(databases, new Database(-1, schema), comparator);
            if (position >= 0) {
                // Remember database position to be skipped
                indexesToSkip.add(position);
            }
        }

        // Copy array and skip relevant databases
        Database[] filtered = new Database[databases.length - indexesToSkip.size()];
        for (int i = 0, j = 0; i < databases.length && j < filtered.length; i++) {
            if (false == indexesToSkip.contains(Integer.valueOf(i))) {
                filtered[j++] = databases[i];
            }
        }

        if (!skippedSchemata.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }
        System.out.println(sb.toString());

        return filtered;
    }

    /**
     * Run the updates on the specified schema
     * 
     * @param schemaName The schema name for which to run the updates
     * @throws InstanceNotFoundException if the required MBean does not exist in the registry
     * @throws MBeanException if an error during the runUpdate method is occurred
     * @throws ReflectionException if an invocation error is occurred
     * @throws IOExceptionif an I/O error is occurred
     */
    private void runUpdates(String schemaName) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        System.out.print("Running updates...");
        Object failures = mbeanConnection.invoke(Constants.OBJECT_NAME, "runUpdate", new Object[] { schemaName }, null);
        if (failures == null) {
            ok();
            return;
        }

        System.err.println("\nErrors while running updates for schema '" + schemaName + "': ");
        String message = failures.toString();
        message = message.replaceAll("\\\\R", System.getProperty("line.separator"));
        System.err.println(message);

        if (!force) {
            System.err.println("Schema upgrade for schema '" + schemaName + "' was aborted. Manual intervention might be required.");
            sysexit(1);
        }
    }

    /**
     * Enables the schema with the specified name
     * 
     * @param schemaName
     * @throws StorageException
     * @throws NoSuchObjectException
     * @throws MissingServiceException
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     */
    private void enableSchema(String schemaName) throws StorageException, NoSuchObjectException, MissingServiceException, RemoteException, InvalidCredentialsException, InvalidDataException {
        System.out.print("Enabling schema '" + schemaName + "'...");
        schemaMoveUtil.enableSchema(credentials, schemaName);
        schemaMoveUtil.invalidateContexts(credentials, schemaName, false);
        ok();
    }

    /**
     * Changes the server
     * 
     * @param schemaName
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     */
    private void changeServer(String schemaName) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        System.out.print("Changing server for schema '" + schemaName + "' to '" + server.getName() + "'...");
        oxUtil.changeServer(server, schemaName, credentials);
        ok();
    }

    /**
     * Disables the schema with the specified name
     * 
     * @param schemaName
     * @throws TargetDatabaseException
     * @throws StorageException
     * @throws MissingServiceException
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     * @throws NoSuchObjectException
     */
    private void disableSchema(String schemaName) throws StorageException, MissingServiceException, RemoteException, InvalidCredentialsException, InvalidDataException, TargetDatabaseException, NoSuchObjectException {
        System.out.print("Disabling schema '" + schemaName + "'...");
        schemaMoveUtil.disableSchema(credentials, schemaName);
        schemaMoveUtil.invalidateContexts(credentials, schemaName, true);
        ok();
    }

    /**
     * Registers the server
     * 
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     */
    private void registerServer() throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        Server[] listServer = oxUtil.listServer(server.getName(), credentials);
        if (listServer != null && listServer.length == 1) {
            // If the 'schema-name' is present it means that there is a continuation which implies
            // that a server was previously registered, thus simply use the already existing server.
            Server s = listServer[0];
            if (Strings.isNotEmpty(startFromSchema)) {
                System.out.println("The server '" + s.getName() + "' with id '" + s.getId() + "' will be used to point the updated schemata after the update tasks complete.");
                System.out.println(PROMPT);
                prompt();
                server = s;
                return;
            }

            // The 'schema-name' is not present, and a server with the same name exist.
            System.out.println("WARNING: The specified server is already registered with id '" + s.getId() + "'.");
            System.out.println("         If you continue the already existing server will be used to point the updated schemata after the update tasks complete.");
            System.out.println("         " + PROMPT);
            prompt();
            server = s;
            return;
        }

        // All good, proceed with server registration
        System.out.print("Registering the server with name '" + server.getName() + "'...");
        server = oxUtil.registerServer(server, credentials);
        ok();
    }

    /**
     * Prompts and waits for user interaction
     */
    private void prompt() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String word = scanner.next();
            switch (word) {
                case ABORT:
                    System.out.println("OK, aborting upgrade");
                    sysexit(0);
                case CONTINUE:
                    scanner.close();
                    System.out.println("OK, proceeding with the upgrade");
                    return;
                default:
                    System.err.println("Unrecognized command: '" + word + "'");
                    System.out.println(PROMPT);
            }
        }
    }

    /**
     * Prints OK
     */
    private void ok() {
        System.out.println("OK");
    }

    /**
     * Create an {@link MBeanServerConnection}
     * 
     * @param parser The {@link AdminParser} to extract the optional JMX user and password
     * @return The {@link MBeanServerConnection}
     * @throws MalformedURLException if the URL is malformed
     * @throws IOException if an I/O error occurs
     */
    private MBeanServerConnection getMBeanConnection(AdminParser parser) throws MalformedURLException, IOException {
        String jmxUsername = null;
        if (parser.hasOption(jmxLoginNameOption)) {
            jmxUsername = (String) parser.getOptionValue(jmxLoginNameOption);
        }
        String jmxPassword = null;
        if (parser.hasOption(jmxPasswordNameOption)) {
            jmxPassword = (String) parser.getOptionValue(jmxPasswordNameOption);
        }
        Map<String, Object> environment;
        if (jmxUsername == null || jmxPassword == null) {
            environment = null;
        } else {
            environment = new HashMap<String, Object>(1);
            String[] creds = new String[] { jmxUsername, jmxPassword };
            environment.put(JMXConnector.CREDENTIALS, creds);
        }

        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + jmxHost + ":" + jmxPort + "/server");
        JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
        return jmxConnector.getMBeanServerConnection();
    }

    /**
     * Checks the arguments
     * 
     * @param parser The {@link AdminParser}
     */
    private void checkAndSetArguments(AdminParser parser) {
        // Parse the server name
        String serverName = (String) parser.getOptionValue(serverNameOption);
        server = new Server();
        server.setName(serverName);

        startFromSchema = (String) parser.getOptionValue(schemaNameOption);
        if (Strings.isNotEmpty(startFromSchema) && startFromSchema.lastIndexOf('_') < 0) {
            System.err.println("Invalid/Malformed schema name: '" + startFromSchema + "'");
            parser.printUsage();
            System.exit(1);
        }

        force = parser.hasOption(forceOption);

        String skippedSchemata = (String) parser.getOptionValue(skipOption);
        if (skippedSchemata != null) {
            String[] split = Strings.splitByComma(",");
            for (String s : split) {
                this.skippedSchemata.add(s);
            }
        }

        // Parse the optional JMX host
        jmxHost = parseHost(parser, jmxHostNameOption, "localhost");
        // Parse the optional JMX port
        jmxPort = parsePort(parser, jmxPortNameOption, 9999);

        credentials = credentialsparsing(parser);
    }

    /**
     * Parses the host from the specified {@link CLIOption}
     * 
     * @param parser The {@link AdminParser}
     * @param option The {@link CLIOption}
     * @param defaultValue The default value
     * @return The hostname or the default value
     */
    private String parseHost(AdminParser parser, CLIOption option, String defaultValue) {
        if (parser.hasOption(option)) {
            String tmp = (String) parser.getOptionValue(option);
            return Strings.isNotEmpty(tmp) ? tmp.trim() : defaultValue;
        }
        return defaultValue;
    }

    /**
     * Parses the port from the specified {@link CLIOption}
     * 
     * @param parser The {@link AdminParser}
     * @param option The {@link CLIOption}
     * @param defaultValue The default value
     * @return the port or the default value
     */
    private int parsePort(AdminParser parser, CLIOption option, int defaultValue) {
        int port = defaultValue;
        String value = (String) parser.getOptionValue(option);
        if (Strings.isEmpty(value)) {
            return port;
        }
        try {
            port = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("Port parameter is not a number: " + value);
            parser.printUsage();
            System.exit(1);
        }
        if (port < 1 || port > 65535) {
            System.err.println("Port parameter is out of range: " + value + ". Valid range is from 1 to 65535.");
            parser.printUsage();
            System.exit(1);
        }
        return port;
    }

    /**
     * Set additional options
     * 
     * @param parser the {@link AdminParser}
     */
    private void setOptions(AdminParser parser) {
        serverNameOption = setShortLongOpt(parser, 'n', "server-name", "The name of the server to register and point all upgraded schemata to", true, NeededQuadState.needed);
        jmxHostNameOption = setShortLongOpt(parser, 'H', "host", "The optional JMX host (default:localhost)", true, NeededQuadState.possibly);
        jmxPortNameOption = setShortLongOpt(parser, 'p', "port", "The optional JMX port (default:9999)", true, NeededQuadState.possibly);
        jmxLoginNameOption = setShortLongOpt(parser, 'l', "login", "The optional JMX login (if JMX has authentication enabled)", true, NeededQuadState.possibly);
        jmxPasswordNameOption = setShortLongOpt(parser, 's', "password", "The optional JMX password (if JMX has authentication enabled)", true, NeededQuadState.possibly);
        schemaNameOption = setShortLongOpt(parser, 'm', "schema-name", "The optional schema name to continue from", true, NeededQuadState.possibly);
        forceOption = setShortLongOpt(parser, 'f', "force", "Forces the upgrade even if the updates fail in some schemata", false, NeededQuadState.notneeded);
        skipOption = setShortLongOpt(parser, 'k', "skip-schemata", "Defines the names of the schemata as a comma separated list that should be skipped during the upgrade phase", true, NeededQuadState.possibly);

        setDefaultCommandLineOptionsWithoutContextID(parser);
    }
}
