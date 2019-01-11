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
 *     Copyright (C) 2019-2020 OX Software GmbH
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

package com.openexchange.geolocation.ip2location.clt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.geolocation.GeoLocationRMIService;

/**
 * {@link AbstractIp2LocationCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
abstract class AbstractIp2LocationCLT extends AbstractRmiCLI<Void> {

    /**
     * Table name of the ip2location database
     */
    protected static final String TABLE_NAME = "ip2location";
    /**
     * The extraction working directory
     */
    protected static final String EXTRACT_DIRECTORY = File.separator + "tmp";

    private final String usage;
    private final String footer;

    /**
     * Value of '-g'
     */
    protected String dbGroup = "default";
    /**
     * Value of '-u'
     */
    protected String dbUser;
    /**
     * Value of '-a'
     */
    protected String dbPassword;
    /**
     * The global database name retrieved via RMI
     */
    private String dbName;
    /**
     * Influenced by '-k'
     */
    protected boolean keep = false;

    /**
     * The absolute path of the downloaded file
     */
    protected String downloadFilePath;
    /**
     * The absolute path of the database file contained within the downloaded zip file
     */
    protected String databaseFilePath;

    /**
     * Initialises a new {@link AbstractIp2LocationCLT}.
     */
    public AbstractIp2LocationCLT(String usage, String footer) {
        super();
        this.usage = usage;
        this.footer = footer;
    }

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
        //TODO: switch to 'true'
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getFooter()
     */
    @Override
    protected String getFooter() {
        return footer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getName()
     */
    @Override
    protected String getName() {
        return usage;
    }

    /////////////////////////////////////////////////// HELPERS /////////////////////////////////////////////

    /**
     * 
     * @param optRmiHostName
     * @return
     * @throws RemoteException
     * @throws MalformedURLException
     * @throws NotBoundException
     */
    protected String getGlobalDatabaseName(String optRmiHostName) throws RemoteException, MalformedURLException, NotBoundException {
        if (dbName == null || dbName.isEmpty()) {
            GeoLocationRMIService rmiService = getRmiStub(optRmiHostName, GeoLocationRMIService.RMI_NAME);
            dbName = rmiService.getGlobalDatabaseName(dbGroup);
        }
        return dbName;
    }

    /**
     * Extracts the database file to '/tmp' and sets the 'databaseFilename' path for future use.
     * 
     * @throws IOException if an I/O error is occurred
     */
    protected void extractDatase() throws IOException {
        System.out.println("Extracting the archive '" + downloadFilePath + "' in '" + EXTRACT_DIRECTORY + "'...");
        FileInputStream fis = new FileInputStream(new File(downloadFilePath));
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry ze = zis.getNextEntry();
        byte[] buffer = new byte[1024];
        while (ze != null) {
            String fileName = ze.getName();
            File newFile = Paths.get(EXTRACT_DIRECTORY, fileName).toFile();
            if (false == keep) {
                newFile.deleteOnExit();
            }
            if (newFile.getAbsolutePath().toLowerCase().endsWith(".csv")) {
                databaseFilePath = newFile.getAbsolutePath();
            }
            System.out.println("Extracting to " + newFile.getAbsolutePath());

            new File(newFile.getParent()).mkdirs();
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }

            fos.close();

            zis.closeEntry();
            ze = zis.getNextEntry();
        }
        //close last ZipEntry
        zis.closeEntry();
        zis.close();
        fis.close();

        if (databaseFilePath == null || databaseFilePath.isEmpty()) {
            System.out.println("No viable database file was found in the extracted files. Manual intervention is required. Data was downloaded and extracted in '" + EXTRACT_DIRECTORY + "'");
            System.exit(-1);
            return;
        }
        System.out.println("OK");
    }

    /**
     * Imports the data into the specified database
     */
    protected void importDatabase(String optRmiHostName) throws Exception {
        String dbName = getGlobalDatabaseName(optRmiHostName);
        //@formatter:off
        String[] importData = { "mysql", "-u", dbUser, "-p" + dbPassword, dbName, "-e", "SET autocommit = 0;"
                + "START TRANSACTION;"
                + "TRUNCATE `" + TABLE_NAME + "`;"
                + "LOAD DATA LOCAL INFILE '" + databaseFilePath + "' " + "INTO TABLE `" + TABLE_NAME + "` " + "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\\r\\n' IGNORE 0 LINES;"
                + "COMMIT;"
                + "SET autocommit=1;"};
        //@formatter:on
        Process runtimeProcess;
        try {
            System.out.println("Using database file '" + databaseFilePath + "'.");
            System.out.print("Importing data to schema '" + dbName + "' in table '" + TABLE_NAME + "'...");

            ProcessBuilder processBuilder = new ProcessBuilder(importData);
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
        }
    }

    /**
     * Prints any errors that were encountered during processing
     * 
     * @param inputStream the {@link InputStream} that holds the errors
     * @throws IOException if an I/O error is occurred
     */
    private void printErrors(InputStream inputStream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = r.readLine()) != null) {
            System.out.println(line);
        }
        r.close();
    }

    /**
     * Reads the text response from the specified {@link URLConnection}
     * 
     * @param connection The {@link URLConnection} from which to read the text response
     * @return The text response
     * @throws IOException if an I/O error is occurred
     */
    protected String readTextResponse(URLConnection connection) throws IOException {
        try (InputStream inputStream = connection.getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder(128);
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            return builder.toString();
        }
    }
}
