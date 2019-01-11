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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * {@link Ip2LocationImportCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class Ip2LocationImportCLT extends AbstractIp2LocationCLT {

    private static final String USAGE = "ip2locationImport";
    private static final String FOOTER = "";

    /**
     * Entry point
     * 
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        new Ip2LocationImportCLT().execute(args);
    }

    /**
     * Initialises a new {@link Ip2LocationImportCLT}.
     */
    public Ip2LocationImportCLT() {
        super(USAGE, FOOTER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#addOptions(org.apache.commons.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        super.addOptions(options);
        options.addOption(createArgumentOption("i", "import", "database-file", "Imports the ip2location csv file to the database.", true));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        File f = new File(downloadFilePath);
        if (isArchive(f)) {
            extractDatase();
        } else {
            // Seems that the provided file is not archive, 
            // so use that as the source for the CSV database.
            databaseFilePath = downloadFilePath;
        }
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
        downloadFilePath = cmd.getOptionValue('i');
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
