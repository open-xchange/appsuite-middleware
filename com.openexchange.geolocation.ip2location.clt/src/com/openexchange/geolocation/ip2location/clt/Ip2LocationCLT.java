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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.geolocation.GeoLocationRMIService;

/**
 * {@link Ip2LocationCLT} - Command line tool to initialise and update the 'ip2location' database
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class Ip2LocationCLT extends AbstractRmiCLI<Void> {

    /**
     * {@link DatabaseVersion} - Defines the amount of fields for every supported Ip2Location database
     */
    private enum DatabaseVersion {
        DB5(8),
        DB9(9),
        DB11(10);

        private final int numberOfFields;

        /**
         * Initialises a new {@link Ip2LocationCLT.DatabaseVersion}.
         */
        private DatabaseVersion(int numberOfFields) {
            this.numberOfFields = numberOfFields;
        }

        /**
         * Gets the numberOfFields
         *
         * @return The numberOfFields
         */
        public int getNumberOfFields() {
            return numberOfFields;
        }

        public String getLiteName() {
            return name() + "LITECSV";
        }

        public String getName() {
            return name() + "CSV";
        }
    }

    private static final String supportedDBVersions() {
        StringBuilder b = new StringBuilder(32);
        for (DatabaseVersion dbv : DatabaseVersion.values()) {
            b.append(dbv.name()).append(",");
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }

    private static final String USAGE = "[ip2location -u <database-user> [-a <database-password>] [-g <group>] [[-i <database-file>] | [-t <token>]] [-k] [-l] -A <masterAdmin> -P <masterPassword> [-p <rmiPort>] [--responsetimeout <timeout>] [-s <rmiHost>]] | [-h]";
    private static final String FOOTER = "Note that the options '-i' and '-t' are mutually exclusive.\n\nSupported ip2location database versions: " + supportedDBVersions();

    /**
     * Table name of the ip2location database
     */
    private static final String TABLE_NAME = "ip2location";
    /**
     * The extraction working directory
     */
    private static final String EXTRACT_DIRECTORY = File.separator + "tmp";
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
     * Value of '-d'
     */
    private DatabaseVersion databaseVersion = DatabaseVersion.DB9;
    /**
     * The requested Ip2Location database version. Influenced by '-l'
     */
    private String dbVersionName = databaseVersion.getName();
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
     * Influenced by '-k'
     */
    private boolean keep = false;

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
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#addOptions(com.openexchange.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("u", "database-user", "database-user", "The database user for importing the data.", true));
        options.addOption(createArgumentOption("a", "database-password", "database-password", "The database password for importing the data.", false));
        options.addOption(createArgumentOption("g", "database-group", "group", "The global database group. If absent it falls-back to 'default'", false));
        options.addOption(createSwitch("k", "keep", "Keeps the temporary files produced from this command line tool (zip archives, downloaded and extracted files).", false));
        options.addOption(createSwitch("l", "lite", "Switch to indicate that the 'lite' version of the database is requested. If absent, then the full version of the database will be requested. Has no effect when in import mode (-i option)", false));
        options.addOption(createArgumentOption("d", "database-version", "database-version", "The database version identifier to download and import. If absent falls back to 'DB9'. The import mode is affected by this switch. Be sure to supply the correct version for the CSV you are importing.", false));

        OptionGroup og = new OptionGroup();
        og.addOption(createArgumentOption("t", "token", "token", "Download Token. Mutually exclusive with -i option.", true));
        og.addOption(createArgumentOption("i", "import", "database-file", "Imports the ip2location csv file to the database. Mutually exclusive with -t option.", true));
        options.addOptionGroup(og);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#checkOptions(com.openexchange.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        if (cmd.hasOption('i') && cmd.hasOption('t')) {
            System.out.println("The options '-i' and '-t' are mutually exclusive.");
            printHelp(options, 120);
            System.exit(1);
            return;
        }
        if (cmd.hasOption('u')) {
            dbUser = cmd.getOptionValue('u');
        }
        if (cmd.hasOption('a')) {
            dbPassword = cmd.getOptionValue('a');
        }
        if (cmd.hasOption('g')) {
            dbGroup = cmd.getOptionValue('g');
        }
        if (cmd.hasOption('d')) {
            String d = cmd.getOptionValue('d');
            try {
                databaseVersion = DatabaseVersion.valueOf(d);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid database version identifier supplied: '" + d + "'. Supported database identifiers are: " + supportedDBVersions());
                System.exit(1);
                return;
            }
        }
        keep = cmd.hasOption('k');
        downloadFilePath = cmd.getOptionValue('i');
        if (downloadFilePath != null && false == downloadFilePath.isEmpty()) {
            importMode = true;
            return;
        }
        token = cmd.getOptionValue('t');
        if (cmd.hasOption('l')) {
            dbVersionName = databaseVersion.getLiteName();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        if (keep) {
            System.out.println("Temporary files will be KEPT in " + EXTRACT_DIRECTORY + ".");
        }
        if (importMode) {
            File f = new File(downloadFilePath);
            if (isArchive(f)) {
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
                if (split != null && split.length == databaseVersion.getNumberOfFields()) {
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
            System.exit(-1);
            return;
        }
        String content = readTextResponse(connection);
        if (content.equals("INVALID")) {
            System.out.println("Invalid license key.");
            System.exit(-1);
            return;
        }
        System.out.println("License key " + content);
    }

    /**
     * Downloads the requested database version from the Ip2Location servers
     * 
     * @throws IOException if an I/O error is occurred
     */
    private void downloadDatabase() throws IOException {
        String download = DOWNLOAD.replaceFirst("#TOKEN#", token).replaceFirst("#PACKAGE#", dbVersionName);
        System.out.print("Downloading " + dbVersionName + "...");

        URLConnection connection = new URL(download).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);

        checkContentType(connection);

        String downloadFilename = extractFilename(connection.getHeaderField("Content-Disposition"));

        try (InputStream inputStream = connection.getInputStream()) {
            File dbfile = Paths.get(EXTRACT_DIRECTORY, downloadFilename).toFile();
            if (false == keep) {
                dbfile.deleteOnExit();
            }
            this.downloadFilePath = dbfile.getAbsolutePath();
            FileOutputStream output = FileUtils.openOutputStream(dbfile);
            try {
                IOUtils.copy(inputStream, output);
                output.close();
            } finally {
                IOUtils.closeQuietly(output);
            }
        }
        System.err.println("OK");
    }

    /**
     * Extracts the database file to '/tmp' and sets the 'databaseFilename' path for future use.
     * 
     * @throws IOException if an I/O error is occurred
     */
    private void extractDatase() throws IOException {
        System.out.println("Extracting the archive '" + downloadFilePath + "' in '" + EXTRACT_DIRECTORY + "'...");
        byte[] buffer = new byte[4096];
        try (FileInputStream fis = new FileInputStream(new File(downloadFilePath)); ZipInputStream zipInputStream = new ZipInputStream(fis)) {
            while (true) {
                ZipEntry zipEntry = null;
                try {
                    zipEntry = zipInputStream.getNextEntry();
                    if (zipEntry == null) {
                        break;
                    }
                    extractZipEntry(zipInputStream, zipEntry, buffer);
                } finally {
                    if (zipEntry != null) {
                        zipInputStream.closeEntry();
                    }
                }
            }
        }
        if (databaseFilePath == null || databaseFilePath.isEmpty()) {
            System.out.println("No viable database file was found in the extracted files. Manual intervention is required. Data was downloaded and extracted in '" + EXTRACT_DIRECTORY + "'");
            System.exit(-1);
            return;
        }
    }

    /**
     * Extracts the zip entries from the specified {@link ZipInputStream}
     * 
     * @param zipInputStream The {@link ZipInputStream} containing the entries
     * @param zipEntry The ZipEntry to extract
     * @param buffer the buffer to use when writing the extracted entry
     * @throws IOException if an I/O error is occurred
     */
    private void extractZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry, byte[] buffer) throws IOException {
        String fileName = zipEntry.getName();
        File newFile = Paths.get(EXTRACT_DIRECTORY, fileName).toFile();
        if (false == keep) {
            newFile.deleteOnExit();
        }
        if (newFile.getAbsolutePath().toLowerCase().endsWith(".csv")) {
            databaseFilePath = newFile.getAbsolutePath();
        }
        System.out.print("Extracting to '" + newFile.getAbsolutePath() + "'...");

        new File(newFile.getParent()).mkdirs();
        try (FileOutputStream fos = new FileOutputStream(newFile)) {
            int len;
            while ((len = zipInputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            System.out.println("failed.");
            throw e;
        }
        System.out.println("OK");
    }

    /**
     * Imports the data into the specified database
     */
    private void importDatabase(String optRmiHostName) throws Exception {
        checkCSVFormat();
        GeoLocationRMIService rmiService = getRmiStub(optRmiHostName, GeoLocationRMIService.RMI_NAME);
        String dbName = rmiService.getGlobalDatabaseName(dbGroup);
        //@formatter:off
        String[] importData = { "/usr/local/mysql/bin/mysql", "-u", dbUser, "-p" + dbPassword, dbName, "-e", "SET autocommit = 0;"
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
     * Checks the first four bytes of the specified file ot determine whether
     * it is a ZIP archive. The signatures of a ZIP archive are listed
     * <a href="https://en.wikipedia.org/wiki/List_of_file_signatures">here</a>.
     * 
     * @param f The file to check
     * @return <code>true</code> if the file is an archive; <code>false</code> otherwise.
     * @throws IOException if an I/O error is occurred
     */
    private boolean isArchive(File f) throws IOException {
        int fileSignature = 0;
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            fileSignature = raf.readInt();
        }
        switch (fileSignature) {
            case 0x504B0304:
                return true;
            case 0x504B0506:
                System.out.println("ERROR: It seems that the archive you provided is empty.");
                System.exit(1);
            case 0x504B0708:
                System.out.println("ERROR: It seems that the archive you provided is spanned over multiple files.");
                System.exit(1);
            default:
                return false;
        }
    }
}
