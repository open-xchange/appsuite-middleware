/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.configuration.clt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.cli.AbstractCLI;

/**
 * {@link ExtractJULModificationsCLT}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ExtractJULModificationsCLT extends AbstractCLI<Integer, Void> {

    private static final String SYNTAX = "extractJULModifications [-i <input>] [-o <output>] | -h";
    private static final String HEADER = "Extracts modified logger levels from file-logging.properties.";

    /**
     * Entry point
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Integer retVal = new ExtractJULModificationsCLT().execute(args);
        if (retVal == null) {
            retVal = Integer.valueOf(1);
        }
        System.exit(retVal.intValue());
    }

    /**
     * Initialises a new {@link ExtractJULModificationsCLT}.
     */
    private ExtractJULModificationsCLT() {
        super();
    }

    @Override
    protected Integer invoke(Options option, CommandLine cmd, Void context) throws Exception {
        Properties properties = XMLUtil.parseInput(!cmd.hasOption('i'), cmd.getOptionValue('i'));
        if (null == properties) {
            return Integer.valueOf(1);
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
            OutputStream os = IOUtil.determineOutput(!cmd.hasOption('o'), cmd.getOptionValue('o'));
            if (os == null) {
                return Integer.valueOf(1);
            }
            try {
                added.store(os, "added loggers in file-logging.properties");
            } finally {
                os.close();
            }
        } catch (IOException e) {
            System.err.println("Can not read file: " + e.getMessage());
            e.printStackTrace();
            return Integer.valueOf(1);
        } finally {
            if (null != resourceStream) {
                try {
                    resourceStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
        return Integer.valueOf(0);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("i", "in", "input", "Java Util logging properties configuration file to read. If omitted this will be read vom STDIN.", false));
        options.addOption(createArgumentOption("o", "out", "output", "Added JUL logger will be written as properties configuration to this file. If this option is omitted the output will be written to STDOUT.", false));
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        // nothing to check
    }

    @Override
    protected String getHeader() {
        return HEADER;
    }

    @Override
    protected String getFooter() {
        return "";
    }

    @Override
    protected String getName() {
        return SYNTAX;
    }

    @Override
    protected Void getContext() {
        return null;
    }
}
