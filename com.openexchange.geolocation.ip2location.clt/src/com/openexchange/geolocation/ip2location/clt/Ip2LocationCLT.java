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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * {@link Ip2LocationCLT} - Command line tool to initialise and update the 'ip2location' database
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class Ip2LocationCLT extends AbstractIp2LocationCLT {

    private static final String USAGE = "ip2location";
    private static final String FOOTER = "";

    /**
     * The identifier of the LITE version
     */
    private static final String LITE_DB_VERSION = "DB9LITECSV";
    /**
     * The identifier of the PAID version
     */
    private static final String DB_VERSION = "DB9CSV";
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
    private String dbVersion = DB_VERSION;

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
        super(USAGE, FOOTER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#addOptions(com.openexchange.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        super.addOptions(options);
        options.addOption(createArgumentOption("t", "token", "token", "Download Token", true));
        options.addOption(createSwitch("l", "lite", "Switch to indicate that the 'lite' version of the database is requested. If absent, then the full version of the database will be requested.", false));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#checkOptions(com.openexchange.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        super.checkOptions(cmd);
        keep = cmd.hasOption('k');
        token = cmd.getOptionValue('t');
        if (cmd.hasOption('l')) {
            dbVersion = LITE_DB_VERSION;
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
        checkLicense();
        downloadDatabase();
        extractDatase();
        importDatabase(optRmiHostName);
        return null;
    }

    ///////////////////////////////////////////// HELPERS /////////////////////////////////////////////

    /**
     * Checks whether the specified license is valid for the specified IP2Location database version
     * 
     * @throws IOException if an I/O error is occurred
     */
    private void checkLicense() throws IOException {
        String checkLicense = CHECK_LICENSE.replaceFirst("#TOKEN#", token).replaceFirst("#PACKAGE#", dbVersion);
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
        String download = DOWNLOAD.replaceFirst("#TOKEN#", token).replaceFirst("#PACKAGE#", dbVersion);
        System.out.print("Downloading " + DB_VERSION + "...");

        URLConnection connection = new URL(download).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);

        checkContentType(connection);

        String downloadFilename = extractFilename(connection.getHeaderField("Content-Disposition"));

        InputStream inputStream = connection.getInputStream();
        try {
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
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        System.err.println("OK");
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
        System.out.println("You have no permission to access '" + dbVersion + "'.");
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
            return dbVersion;
        }
        int index = contentDisposition.indexOf("filename=");
        if (index < 0) {
            return dbVersion;
        }
        return contentDisposition.substring(index + "filename=".length()).replaceAll("\"", "");
    }
}
