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

package com.openexchange.configuration.clt;

import static com.openexchange.configuration.clt.ConvertJUL2LogbackCLT.determineOutput;
import static com.openexchange.configuration.clt.XMLModifierCLT.createOption;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * {@link ExtractJULModificationsCLT}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ExtractJULModificationsCLT {

    public ExtractJULModificationsCLT() {
        super();
    }

    public static void main(String[] args) {
        System.exit(writeJULModifications(args));
    }

    private static int writeJULModifications(String[] args) {
        Options options = new Options();
        options.addOption(createOption("h", "help", false, "Prints a help text.", false));
        options.addOption(createOption("i", "in", true, "Java Util logging properties configuration file to read. If omitted this will be read vom STDIN.", false));
        options.addOption(createOption("o", "out", true, "Added JUL logger will be written as properties configuration to this file. If this option is omitted the output will be written to STDOUT.", false));
        CommandLineParser parser = new PosixParser();
        final CommandLine cmd;
        try {
            cmd = parser.parse(options, args, true);
        } catch (ParseException e) {
            System.err.println("Parsing the command line failed: " + e.getMessage());
            return 1;
        }
        if (cmd.hasOption('h')) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("extractJULModifications", "Extracts modified logger levels from file-logging.properties.", options, null, false);
            return 0;
        }
        Properties properties = ConvertJUL2LogbackCLT.parseInput(!cmd.hasOption('i'), cmd.getOptionValue('i'));
        if (null == properties) {
            return 1;
        }

        InputStream resourceStream = null;
        try {
            Properties original = new Properties();
            resourceStream = ExtractJULModificationsCLT.class.getClassLoader().getResourceAsStream("file-logging.properties");
            original.load(resourceStream);
            Properties added = new Properties();
            added.putAll(properties);
            // new
            for (String name : original.stringPropertyNames()) {
                added.remove(name);
            }
            // changed
            for (String name : original.stringPropertyNames()) {
                if (properties.containsKey(name) && !properties.getProperty(name).equals(original.getProperty(name))) {
                    added.put(name, properties.getProperty(name));
                }
            }
            // Write output
            final OutputStream os = determineOutput(!cmd.hasOption('o'), cmd.getOptionValue('o'));
            try {
                added.store(os, "added loggers in file-logging.properties");
            } finally {
                os.close();
            }
        } catch (IOException e) {
            System.err.println("Can not read file: " + e.getMessage());
            e.printStackTrace();
            return 1;
        } finally {
            if (null != resourceStream) {
                try {
                    resourceStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
        return 0;
    }
}
