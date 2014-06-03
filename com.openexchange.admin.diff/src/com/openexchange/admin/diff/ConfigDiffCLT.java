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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.admin.diff;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import com.openexchange.admin.diff.result.DiffResult;

/**
 * CLT to execute the configuration diff tool.
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@SuppressWarnings("static-access")
public class ConfigDiffCLT {

    /**
     * Default folder for original configuration files
     */
    private static String originalFolder = "/opt/open-xchange/bundles";

    /**
     * Default folder for installed configuration files
     */
    private static String installationFolder = "/opt/open-xchange/etc";

    private static final Options options = new Options();
    static {
        options.addOption(OptionBuilder.withLongOpt("original").hasArgs(1).withDescription("The original configuration folder provided by OX; '/opt/open-xchange/bundles/' if nothing provided.").isRequired(false).create("o"));
        options.addOption(OptionBuilder.withLongOpt("installed").hasArgs(1).withDescription("The installed configuration folder; '/opt/open-xchange/etc/' if nothing provided.").isRequired(false).create("i"));
        options.addOption(OptionBuilder.withLongOpt("file").hasArgs(1).withDescription("Export diff to file").isRequired(false).create("f"));
        options.addOption(OptionBuilder.withLongOpt("help").hasArg(false).withDescription("Print usage").isRequired(false).create("h"));
    }

    /**
     * Entry point
     * 
     * @param args
     */
    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        final String file;
        try {
            CommandLine cl = parser.parse(options, args);

            if (cl.hasOption("h")) {
                printUsage(0);
            }
            if (cl.hasOption("f")) {
                file = cl.getOptionValue("f");
            } else {
                file = null;
            }
            if (cl.hasOption("o")) {
                ConfigDiffCLT.originalFolder = cl.getOptionValue("o");
            }
            if (cl.hasOption("i")) {
                ConfigDiffCLT.installationFolder = cl.getOptionValue("i");
            }
            executeDiff(ConfigDiffCLT.originalFolder, ConfigDiffCLT.installationFolder, file);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printUsage(-1);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            printUsage(-1);
        }
    }

    /**
     * Execute diff
     * 
     * @param original folder
     * @param installed folder
     * @param file optional file to store the diff
     * @throws IOException
     */
    private static void executeDiff(String original, String installed, String file) throws IOException {
        ConfigDiff configDiff = new ConfigDiff(original, installed);
        DiffResult diffResult = configDiff.run();

        if (file == null) {
            System.out.println(diffResult.toString());
        } else {
            File output = new File(file);

            FileUtils.write(output, diffResult.toString());
        }
    }

    /**
     * Print usage
     * 
     * @param exitCode
     */
    private static final void printUsage(int exitCode) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(80);
        hf.printHelp("Help", options);
        System.exit(exitCode);
    }

}
