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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import com.openexchange.geolocation.clt.AbstractGeoLocationCLT;
import com.openexchange.geolocation.clt.DatabaseVersion;

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

    private static final String USAGE = "[ip2location -u <database-user> [-a <database-password>] [-g <group>] [[-i <database-file>] | [-t <token>]] [-k] [-l] -A <masterAdmin> -P <masterPassword> [-p <rmiPort>] [--responsetimeout <timeout>] [-s <rmiHost>]] | [-h]";
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
    private static final String DOWNLOAD = "https://www.phrapes.org/download?token=#TOKEN#&file=#PACKAGE#";
    /**
     * Value of '-t'
     */
    private String token;

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
     * @see com.openexchange.geolocation.clt.AbstractGeoLocationCLT#getAffectedTables()
     */
    @Override
    protected String getAffectedTables() {
        return TABLE_NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.geolocation.clt.AbstractGeoLocationCLT#getDownloadUrl()
     */
    @Override
    protected String getDownloadUrl() {
        return DOWNLOAD.replaceFirst("#TOKEN#", token).replaceFirst("#PACKAGE#", getDbVersionName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.geolocation.clt.AbstractGeoLocationCLT#getExecutionEnvironment()
     */
    @Override
    protected String getImportStatement() {
        //@formatter:off
        return "SET autocommit = 0;"
            + "START TRANSACTION;"
            + "TRUNCATE `" + TABLE_NAME + "`;"
            + "LOAD DATA LOCAL INFILE '" + getDatabaseFilePath() + "' " + "INTO TABLE `" + TABLE_NAME + "` " + "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\\r\\n' IGNORE 0 LINES;"
            + "COMMIT;"
            + "SET autocommit=1;";
        //@formatter:on
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
        super.checkOptions(cmd);
        if (cmd.hasOption('i') && cmd.hasOption('t')) {
            System.out.println("The options '-i' and '-t' are mutually exclusive.");
            printHelp(options, 120);
            System.exit(1);
            return;
        }
        token = cmd.getOptionValue('t');
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.geolocation.clt.AbstractGeoLocationCLT#checkLicense()
     */
    @Override
    protected void checkLicense() {
        String checkLicense = CHECK_LICENSE.replaceFirst("#TOKEN#", token).replaceFirst("#PACKAGE#", getDbVersionName());
        URLConnection connection;
        try {
            connection = new URL(checkLicense).openConnection();
        } catch (MalformedURLException e) {
            System.out.println("Invalid URL specified for license check: '" + checkLicense + "'");
            System.exit(1);
            return;
        } catch (IOException e) {
            System.out.println("An I/O error occurred: " + e.getMessage());
            System.exit(1);
            return;
        }
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
