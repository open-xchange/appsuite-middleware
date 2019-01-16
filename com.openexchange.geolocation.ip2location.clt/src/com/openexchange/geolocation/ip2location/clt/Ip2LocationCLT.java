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

package com.openexchange.geolocation.ip2location.clt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.geolocation.clt.AbstractGeoLocationCLT;
import com.openexchange.geolocation.clt.ConnectionUtils;
import com.openexchange.geolocation.clt.DatabaseVersion;
import com.openexchange.geolocation.clt.FileUtils;

/**
 * {@link Ip2LocationCLT} - Command line tool to initialise and update the 'ip2location' database
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class Ip2LocationCLT extends AbstractGeoLocationCLT {

    /**
     * Returns a comma separated string with the supported ip2location DB versions
     * 
     * @return a comma separated string with the supported ip2location DB versions
     */
    private static final String supportedDBVersions() {
        StringBuilder b = new StringBuilder(32);
        for (Ip2LocationDatabaseVersion dbv : Ip2LocationDatabaseVersion.values()) {
            b.append(dbv.name()).append(",");
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }

    private static final String USAGE = "ip2location [[-i <database-file>] | [-t <token>]] " + ABSTRACT_USAGE;
    private static final String FOOTER = "Note that the options '-i' and '-t' are mutually exclusive.\n\nSupported ip2location database versions: " + supportedDBVersions();

    /**
     * Table name of the ip2location database
     */
    private static final String TABLE_NAME = "ip2location";
    /**
     * URL to check whether the token is valid for the specified database version
     */
    private static final String CHECK_LICENSE = "https://www.ip2location.com/download-info?token=#TOKEN#&package=#PACKAGE#";
    /**
     * URL to download the database
     */
    private static final String DOWNLOAD = "https://www.ip2location.com/download?token=#TOKEN#&file=#PACKAGE#";
    /**
     * Value of '-t'
     */
    private String token;
    /**
     * The requested Ip2Location database version. Influenced by '-l'
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

    private boolean importMode = false;

    /**
     * Entry point
     * 
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        new Ip2LocationCLT().execute(args);
    }

    /**
     * Initialises a new {@link Ip2LocationCLT}.
     */
    public Ip2LocationCLT() {
        super(TABLE_NAME, USAGE, FOOTER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#addOptions(com.openexchange.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        super.addOptions(options);
        options.addOption(createSwitch("l", "lite", "Switch to indicate that the 'lite' version of the database is requested. If absent, then the full version of the database will be requested. Has no effect when in import mode (-i option)", false));

        OptionGroup og = new OptionGroup();
        og.addOption(createArgumentOption("t", "token", "token", "Download Token. Mutually exclusive with -i option.", true));
        og.addOption(createArgumentOption("i", "import", "database-file", "Imports the ip2location csv file to the database. If the supplied file is a ZIP archive, it will be extracted to the extraction directory specified by the '-o' option (defaults to '/tmp' if absent). Mutually exclusive with -t option.", true));
        options.addOptionGroup(og);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#checkOptions(com.openexchange.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        super.checkOptions(cmd);
        if (cmd.hasOption('i') && cmd.hasOption('t')) {
            System.out.println("The options '-i' and '-t' are mutually exclusive.");
            printHelp(options);
            System.exit(1);
            return;
        }

        downloadFilePath = cmd.getOptionValue('i');
        if (downloadFilePath != null && false == downloadFilePath.isEmpty()) {
            importMode = true;
            return;
        }
        token = cmd.getOptionValue('t');
        dbVersionName = cmd.hasOption('l') ? getDatabaseVersion().getLiteName() : getDatabaseVersion().getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        if (isKeep()) {
            System.out.println("Temporary files will be KEPT in " + getExtractDirectory() + ".");
        }
        if (importMode) {
            if (FileUtils.isArchive(downloadFilePath)) {
                extractDatase();
            } else {
                // Seems that the provided file is not an archive, 
                // so use that as the source for the CSV database.
                databaseFilePath = downloadFilePath;
            }
            importDatabase(optRmiHostName);
            return null;
        }
        checkLicense();
        downloadDatabase();
        extractDatase();
        importDatabase(optRmiHostName);
        return null;
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
                if (split != null && split.length == getDatabaseVersion().getNumberOfFields()) {
                    return;
                }
                counter++;
            }
        }
        System.out.println("The file you provided does not seem to be a valid ip2location CSV file.");
        System.exit(1);

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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getFooter()
     */
    @Override
    protected String getFooter() {
        return FOOTER;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getName()
     */
    @Override
    protected String getName() {
        return USAGE;
    }

    ///////////////////////////////////////////// HELPERS /////////////////////////////////////////////

    /**
     * Checks whether the specified license is valid for the specified IP2Location database version
     * 
     * @throws IOException if an I/O error is occurred
     */
    private void checkLicense() throws IOException {
        String checkLicense = CHECK_LICENSE.replaceFirst("#TOKEN#", token).replaceFirst("#PACKAGE#", dbVersionName);
        URLConnection connection = new URL(checkLicense).openConnection();
        String contentType = connection.getContentType();
        if (false == contentType.startsWith("text")) {
            System.out.println("Invalid license key.");
            System.exit(1);
            return;
        }
        String content = ConnectionUtils.readTextResponse(connection);
        if (content.equals("INVALID")) {
            System.out.println("Invalid license key.");
            System.exit(1);
            return;
        }
        System.out.println("License key " + content);
    }

    /**
     * Downloads the requested database version from the Ip2Location servers
     */
    private void downloadDatabase() {
        String download = DOWNLOAD.replaceFirst("#TOKEN#", token).replaceFirst("#PACKAGE#", dbVersionName);
        System.out.println("Downloading " + dbVersionName + "...");
        try {
            File downloadedFile = FileUtils.downloadFile(download, getExtractDirectory(), dbVersionName, "application/zip");
            downloadFilePath = downloadedFile.getAbsolutePath();
        } catch (MalformedURLException e) {
            System.err.println("A malformed URL was specified: " + download);
            System.exit(1);
        } catch (IOException e) {
            String content = e.getMessage();
            if (content.equals("NO PERMISSION")) {
                System.out.println("You have no permission to access '" + dbVersionName + "'.");
                System.exit(1);
                return;
            }
            System.err.println("An I/O error occurred: " + content);
            System.exit(1);
            return;
        }
    }

    /**
     * Extracts the database file to '/tmp' and sets the 'databaseFilename' path for future use.
     * 
     * @throws IOException if an I/O error is occurred
     */
    private void extractDatase() throws IOException {
        List<File> extractedFiles = FileUtils.extractArchive(downloadFilePath, getExtractDirectory(), isKeep());
        for (File f : extractedFiles) {
            // It is always expected to be one and only one CSV file in the archive.
            if (f.getAbsolutePath().toLowerCase().endsWith(".csv")) {
                databaseFilePath = f.getAbsolutePath();
            }
        }
        if (databaseFilePath == null || databaseFilePath.isEmpty()) {
            System.out.println("No viable database file was found in the extracted files. Manual intervention is required. Data was downloaded and extracted in '" + getExtractDirectory() + "'");
            System.exit(1);
            return;
        }
    }

    /**
     * Imports the data into the specified database
     */
    private void importDatabase(String optRmiHostName) throws Exception {
        checkCSVFormat();
        //@formatter:off
        String importStatements = "SET autocommit = 0;"
                + "START TRANSACTION;"
                + "TRUNCATE `" + TABLE_NAME + "`;"
                + "LOAD DATA LOCAL INFILE '" + databaseFilePath + "' " + "INTO TABLE `" + TABLE_NAME + "` " + "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\\r\\n' IGNORE 0 LINES;"
                + "COMMIT;"
                + "SET autocommit=1;";
        //@formatter:on
        System.out.println("Using database file '" + databaseFilePath + "'.");
        importDatabase(optRmiHostName, importStatements);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.geolocation.clt.AbstractGeoLocationCLT#parseDatabaseVersion()
     */
    @Override
    protected DatabaseVersion parseDatabaseVersion(CommandLine cmd) {
        if (false == cmd.hasOption('d')) {
            return Ip2LocationDatabaseVersion.DB9;
        }
        String d = cmd.getOptionValue('d');
        try {
            return Ip2LocationDatabaseVersion.valueOf(d);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid database version identifier supplied: '" + d + "'. Supported database identifiers are: " + supportedDBVersions());
            System.exit(1);
            return null;
        }
    }
}
