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

package com.openexchange.geolocation.clt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.Scanner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.geolocation.GeoLocationRMIService;
import com.openexchange.java.Strings;

/**
 * {@link AbstractGeoLocationCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public abstract class AbstractGeoLocationCLT extends AbstractRmiCLI<Void> {

    /**
     * The extraction working directory
     */
    protected static final String EXTRACT_DIRECTORY = File.separator + "tmp";

    /**
     * Value of '-g'
     */
    private String dbGroup = "default";
    /**
     * Value of '-u'
     */
    private String dbUser;
    /**
     * Value of '-a'
     */
    private String dbPassword;
    /**
     * Value of '-d'
     */
    private DatabaseVersion databaseVersion;
    /**
     * The requested database version. Influenced by '-l'
     */
    private String dbVersionName;
    /**
     * The absolute path of the downloaded file
     */
    private String downloadFilePath;
    /**
     * The absolute path of the database file contained within the downloaded zip file
     */
    private String databaseFilePath;
    /**
     * Influenced by '-k'
     */
    private boolean keep = false;

    private boolean importMode = false;

    private final String tables;

    /**
     * Initialises a new {@link AbstractGeoLocationCLT}.
     * 
     * @param a comma separated list with the database tables
     */
    public AbstractGeoLocationCLT(String tables) {
        super();
        this.tables = tables;
    }

    /**
     * Parses the database version from the provided '-d' flag and returns the DatabaseVersion
     * 
     * @return returns the {@link DatabaseVersion}
     * @throws IllegalArgumentException if no valid database version can be parsed
     */
    protected abstract DatabaseVersion parseDatabaseVersion();

    /**
     * Checks whether the provided license is valid for the specified database version
     */
    protected abstract void checkLicense();
    
    /**
     * 
     * @return
     */
    protected abstract String getDownloadUrl();

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#addOptions(org.apache.commons.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("u", "database-user", "database-user", "The database user for importing the data.", true));
        options.addOption(createArgumentOption("a", "database-password", "database-password", "The database password for importing the data.", false));
        options.addOption(createArgumentOption("g", "database-group", "group", "The global database group. If absent it falls-back to 'default'", false));
        options.addOption(createSwitch("k", "keep", "Keeps the temporary files produced from this command line tool (zip archives, downloaded and extracted files).", false));
        options.addOption(createArgumentOption("d", "database-version", "database-version", "The database version identifier to download and import. If absent falls back to 'DB9'. The import mode is affected by this switch. Be sure to supply the correct version for the CSV you are importing.", false));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#checkOptions(org.apache.commons.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        if (cmd.hasOption('u')) {
            dbUser = cmd.getOptionValue('u');
        }
        if (cmd.hasOption('a')) {
            dbPassword = cmd.getOptionValue('a');
        }
        if (cmd.hasOption('g')) {
            dbGroup = cmd.getOptionValue('g');
        }
        databaseVersion = parseDatabaseVersion();
        dbVersionName = cmd.hasOption('l') ? databaseVersion.getLiteName() : databaseVersion.getName();
        keep = cmd.hasOption('k');
        downloadFilePath = cmd.getOptionValue('i');
        if (downloadFilePath != null && false == downloadFilePath.isEmpty()) {
            importMode = true;
        }
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
     * @see com.openexchange.cli.AbstractAdministrativeCLI#requiresAdministrativePermission()
     */
    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    /////////////////////////////////////// GETTERS //////////////////////////////

    /**
     * Gets the dbGroup
     *
     * @return The dbGroup
     */
    public String getDbGroup() {
        return dbGroup;
    }

    /**
     * Gets the dbUser
     *
     * @return The dbUser
     */
    public String getDbUser() {
        return dbUser;
    }

    /**
     * Gets the dbPassword
     *
     * @return The dbPassword
     */
    public String getDbPassword() {
        return dbPassword;
    }

    /**
     * Gets the keep
     *
     * @return The keep
     */
    public boolean isKeep() {
        return keep;
    }

    /**
     * Gets the importMode
     *
     * @return The importMode
     */
    public boolean isImportMode() {
        return importMode;
    }

    //////////////////////////////////////// HELPERS ///////////////////////////////

    /**
     * Downloads the requested database version from the Ip2Location servers
     * 
     * @throws IOException if an I/O error is occurred
     */
    protected void downloadDatabase(String downloadUrl) throws IOException {
        URLConnection connection = new URL(downloadUrl).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);

        checkContentType(connection);

        String downloadFilename = extractFilename(connection.getHeaderField("Content-Disposition"));
        long contentLength = connection.getContentLength();
        System.out.println("Database size: " + Strings.humanReadableByteCount(contentLength, true) + ".\n");
        try (InputStream inputStream = connection.getInputStream()) {
            File dbfile = Paths.get(EXTRACT_DIRECTORY, downloadFilename).toFile();
            if (false == isKeep()) {
                dbfile.deleteOnExit();
            }
            this.downloadFilePath = dbfile.getAbsolutePath();
            FileUtils.writeFile(inputStream, dbfile, contentLength);
        }
        System.out.println();
    }

    /**
     * Imports the data into the 'global' database
     * 
     * @param the RMI hostname of the node on which to query the name of the global database
     * @param environment The execution environment
     */
    protected void importDatabase(String rmiHostName, String[] environment) throws Exception {
        checkCSVFormat();
        GeoLocationRMIService rmiService = getRmiStub(rmiHostName, GeoLocationRMIService.RMI_NAME);
        String dbName = rmiService.getGlobalDatabaseName(dbGroup);
        Process runtimeProcess;
        try {
            System.out.println("Using database file '" + databaseFilePath + "'.");
            System.out.print("Importing data to schema '" + dbName + "' in table(s) '" + tables + "'...");

            ProcessBuilder processBuilder = new ProcessBuilder(environment);
            runtimeProcess = processBuilder.start();
            int processComplete = runtimeProcess.waitFor();
            if (processComplete == 0) {
                System.out.println("OK.");
                return;
            }
            System.out.println("Could not import the data.");
            printErrors(runtimeProcess.getInputStream());
            printErrors(runtimeProcess.getErrorStream());
        } catch (IOException e) {
            if (e.getMessage().contains("No such file or directory")) {
                System.out.println("\nERROR: Couldn't find the 'mysql' executable. Ensure that 'mysql' is installed and in your $PATH");
                System.exit(1);
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return;
        }
    }
    
    /**
     * Runs the process with the specified execution environment
     * 
     * @param executionEnvironment The execution environment, i.e. the executable with all its arguments
     */
    protected void runProcess(String[] executionEnvironment) {
        if (executionEnvironment == null || executionEnvironment.length == 0) {
            System.out.println("The execution environment is empty.");
            System.exit(1);
            return;
        }
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(executionEnvironment);
            Process runtimeProcess = processBuilder.start();
            int processComplete = runtimeProcess.waitFor();
            if (processComplete == 0) {
                return;
            }
            printErrors(runtimeProcess.getInputStream());
            printErrors(runtimeProcess.getErrorStream());
        } catch (IOException e) {
            if (e.getMessage().contains("No such file or directory")) {
                System.out.println("\nERROR: Couldn't find the '" + executionEnvironment[0] + "' executable. Ensure that '" + executionEnvironment[0] + "' is installed and in your $PATH");
                System.exit(1);
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return;
        }
    }

    /**
     * Checks whether the appropriate content type is returned before unzipping
     * 
     * @param connection The {@link URLConnection}
     * @throws IOException if an I/O error is occurred
     */
    private void checkContentType(URLConnection connection) throws IOException {
        String contentType = connection.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            return;
        }
        if (false == contentType.startsWith("text") && contentType.toLowerCase().contains("zip")) {
            return;
        }
        String content = readTextResponse(connection);
        if (false == content.equals("NO PERMISSION")) {
            return;
        }
        System.out.println("You have no permission to access '" + dbVersionName + "'.");
        System.exit(-1);
    }

    /**
     * Extracts the 'filename' from the specified content disposition header
     * 
     * @param contentDisposition The content disposition header
     * @return The filename value
     */
    private String extractFilename(String contentDisposition) {
        if (contentDisposition == null || contentDisposition.isEmpty()) {
            return dbVersionName;
        }
        int index = contentDisposition.indexOf("filename=");
        if (index < 0) {
            return dbVersionName;
        }
        return contentDisposition.substring(index + "filename=".length()).replaceAll("\"", "");
    }

    /**
     * Prints any errors that were encountered during processing
     * 
     * @param inputStream the {@link InputStream} that holds the errors
     * @throws IOException if an I/O error is occurred
     */
    private void printErrors(InputStream inputStream) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = r.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    /**
     * Reads the text response from the specified {@link URLConnection}
     * 
     * @param connection The {@link URLConnection} from which to read the text response
     * @return The text response
     * @throws IOException if an I/O error is occurred
     */
    private String readTextResponse(URLConnection connection) throws IOException {
        try (InputStream inputStream = connection.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder builder = new StringBuilder(128);
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
    }

    /**
     * Checks whether the specified databaseFilePath denotes a valid ip2location CSV file
     * corresponding to the specified {@link DatabaseVersion}
     * 
     * @throws FileNotFoundException if the file does not exist
     */
    private void checkCSVFormat() throws FileNotFoundException {
        try (Scanner input = new Scanner(new File(databaseFilePath))) {
            int counter = 0;
            int maxLines = 5;
            while (input.hasNextLine() && counter < maxLines) {
                String line = input.nextLine();
                String[] split = line.split(",");
                if (split != null && split.length == databaseVersion.getNumberOfFields()) {
                    return;
                }
                counter++;
            }
        }
        System.out.println("The file you provided does not seem to be a valid ip2location CSV file.");
        System.exit(1);
    }
}
