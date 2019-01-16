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

package com.openexchange.geolocation.maxmind.clt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.geolocation.clt.AbstractGeoLocationCLT;
import com.openexchange.geolocation.clt.DatabaseVersion;
import com.openexchange.geolocation.clt.FileUtils;

/**
 * {@link MaxMindCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class MaxMindCLT extends AbstractGeoLocationCLT {

    /**
     * Returns a comma separated string with the supported MaxMind DB versions
     * 
     * @return a comma separated string with the supported MaxMind DB versions
     */
    private static final String supportedDBVersions() {
        StringBuilder b = new StringBuilder(32);
        for (MaxMindDatabaseVersion dbv : MaxMindDatabaseVersion.values()) {
            b.append(dbv.name()).append(",");
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }

    private static final String USAGE = "maxmind [[-z <zip-archive>] | [-b <ip-blocks-file> -l <ip-locations-file>]] " + ABSTRACT_USAGE;
    private static final String FOOTER = "Note that the options '-b' and '-l' must be used in conjunction and that both are mutually exclusive with the option '-z'.\n\nSupported MaxMind database versions: " + supportedDBVersions();

    private static final String DOWNLOAD = "https://geolite.maxmind.com/download/geoip/database/#VERSION#.zip";

    private String ipBlocksFilePath;
    private String ipLocationsFilePath;
    private String downloadFilePath;

    private boolean importMode = false;

    /**
     * Entry point
     * 
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        new MaxMindCLT().execute(args);
    }

    /**
     * Initialises a new {@link MaxMindCLT}.
     */
    public MaxMindCLT() {
        super("ip_blocks, ip_locations", USAGE, FOOTER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#addOptions(org.apache.commons.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        super.addOptions(options);
        options.addOption(createArgumentOption("z", "zip-archive", "zip-archive", "Imports the contents of the ZIP archive to the database. The ZIP archive will be extracted to the extraction directory specified by the '-o' options (defaults to '/tmp' if absent). The ZIP archive is expected to contain the two CSV files supplied by MaxMind, i.e. the ip_blocks and ip_locations. Mutually exclusive with '-b' and '-l' options", false));
        options.addOption(createArgumentOption("b", "ip-blocks", "ip-blocks-file", "Imports the ip_blocks csv file to the database. Must be used in conjunction with '-l' option. Mutually exclusive with the '-z' option.", false));
        options.addOption(createArgumentOption("l", "ip-locations", "ip-locations-file", "Imports the ip_locations csv file to the database. Must be used in conjunction with '-b' option. Mutually exclusive with the '-z' option.", false));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        if (isKeep()) {
            System.out.println("Temporary files will be KEPT in '" + getExtractDirectory() + "'.");
        }
        if (importMode) {
            if (FileUtils.isArchive(downloadFilePath)) {
                extractDatase();
            }
            importDatabase(optRmiHostName);
            return null;
        }
        downloadDatabase();
        extractDatase();
        importDatabase(optRmiHostName);
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#checkOptions(org.apache.commons.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        super.checkOptions(cmd);
        downloadFilePath = cmd.getOptionValue('z');
        ipBlocksFilePath = cmd.getOptionValue('b');
        ipLocationsFilePath = cmd.getOptionValue('l');
        if (ipBlocksFilePath != null && false == ipBlocksFilePath.isEmpty() && ipLocationsFilePath != null && false == ipLocationsFilePath.isEmpty()) {
            importMode = true;
        }
    }

    /////////////////////////////////////////////// HELPERS /////////////////////////////////////////

    /**
     * Downloads the requested database version from the Ip2Location servers
     */
    private void downloadDatabase() {
        String download = DOWNLOAD.replaceFirst("#VERSION#", getDatabaseVersion().getLiteName());
        System.out.println("Downloading " + getDatabaseVersion().getLiteName() + "...");
        try {
            File downloadedFile = FileUtils.downloadFile(download, getExtractDirectory(), getDatabaseVersion().getLiteName(), "application/zip");
            downloadFilePath = downloadedFile.getAbsolutePath();
        } catch (MalformedURLException e) {
            System.err.println("A malformed URL was specified: " + download);
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.out.println("Not Found: '" + download + "'.");
            System.exit(1);
            return;
        } catch (IOException e) {
            String content = e.getMessage();
            if (content.equals("NO PERMISSION")) {
                System.out.println("You have no permission to access '" + getDatabaseVersion().getLiteName() + "'.");
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
            String path = f.getAbsolutePath().toLowerCase();
            if (path.contains("locations") && path.endsWith("-en.csv")) {
                ipLocationsFilePath = f.getAbsolutePath();
                continue;
            }
            if (path.contains("blocks-ipv4") && path.endsWith(".csv")) {
                ipBlocksFilePath = f.getAbsolutePath();
                continue;
            }
        }
        if ((ipLocationsFilePath == null || ipLocationsFilePath.isEmpty()) && (ipBlocksFilePath == null || ipBlocksFilePath.isEmpty())) {
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
        String importStatements = "SET autocommit = 0;" +
            "START TRANSACTION;" +
            "TRUNCATE `ip_blocks`;" + 

            "LOAD DATA LOCAL INFILE '" + ipBlocksFilePath + "' INTO TABLE ip_blocks COLUMNS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' IGNORE 1 LINES (" + 
            "@network," + 
            "geoname_id," + 
            "registered_country_geoname_id," + 
            "represented_country_geoname_id," + 
            "is_anonymous_proxy," + 
            "is_satellite_provider," + 
            "postal_code," + 
            "latitude," + 
            "longitude," + 
            "accuracy_radius) SET " + 
            "ip_from = INET_ATON(SUBSTRING(@network, 1, LOCATE('/', @network) - 1))," + 
            "ip_to = (INET_ATON(SUBSTRING(@network, 1, LOCATE('/', @network) - 1)) + (pow(2, (32-CONVERT(SUBSTRING(@network, LOCATE('/', @network) + 1), UNSIGNED INTEGER)))-1));" + 
            
            "LOAD DATA LOCAL INFILE '" + ipLocationsFilePath + "' INTO TABLE ip_locations CHARACTER SET UTF8 COLUMNS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' IGNORE 1 LINES (" + 
            "geoname_id," + 
            "locale_code," + 
            "continent_code," + 
            "continent_name," + 
            "country_iso_code," + 
            "country_name," + 
            "subdivision_1_iso_code," + 
            "subdivision_1_name," + 
            "subdivision_2_iso_code," + 
            "subdivision_2_name," + 
            "city_name," + 
            "metro_code," + 
            "time_zone);" +
            
            "COMMIT;" + 
            "SET autocommit=1;";
        //@formatter:on
        System.out.println("Using database files '" + ipBlocksFilePath + "' and '" + ipLocationsFilePath + "'.");
        importDatabase(optRmiHostName, importStatements);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.geolocation.clt.AbstractGeoLocationCLT#parseDatabaseVersion(org.apache.commons.cli.CommandLine)
     */
    @Override
    protected DatabaseVersion parseDatabaseVersion(CommandLine cmd) {
        if (false == cmd.hasOption('d')) {
            return MaxMindDatabaseVersion.CITY;
        }
        String d = cmd.getOptionValue('d');
        try {
            return MaxMindDatabaseVersion.valueOf(d);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid database version identifier supplied: '" + d + "'. Supported database identifiers are: " + supportedDBVersions());
            System.exit(1);
            return null;
        }
    }

    /**
     * Checks the CSV formats of the ip-blocks and ip-locations files
     * 
     * @throws FileNotFoundException if any of the files does not exist
     */
    private void checkCSVFormat() throws FileNotFoundException {
        checkCSVFormat(ipBlocksFilePath, 10, "ip-blocks");
        checkCSVFormat(ipLocationsFilePath, 14, "ip-locations");
    }

    /**
     * Checks the CSV formats of the specified file
     * 
     * @param path The path of the CSV file
     * @param fields The amount of fields per row
     * @param name An abstract name for the file
     * @throws FileNotFoundException if the file denoted by the <code>path</code> does not exist
     */
    private void checkCSVFormat(String path, int fields, String name) throws FileNotFoundException {
        try {
            FileUtils.checkCSVFormat(path, fields);
        } catch (IllegalArgumentException e) {
            System.out.println("The file '" + path + "' you provided does not seem to be a valid " + name + " CSV file.");
            System.exit(1);
            return;
        }
    }
}
