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

package com.openexchange.documentation.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import com.openxchange.documentation.tools.internal.ConfigDocu;
import com.openxchange.documentation.tools.internal.Property;

/**
 * {@link ShowConfig} is a CLT which provides access to the config documentation.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
@SuppressWarnings("static-access")
public class ShowConfig {

    private static final Options options = new Options();
    private static final String DEFAULT_FOLDER = "/opt/openexchange/documentation/etc";

    private static final String SEARCH_OPTION = "s";
    private static final String TAG_OPTION = "t";
    private static final String KEY_OPTION = "k";
    private static final String HELP_OPTION = "h";
    private static final String ANSI_OPTION = "a";

    static {
        options.addOption(OptionBuilder.withLongOpt("search").hasArgs(1).withDescription("If set only properties which match the given search term are returned.").isRequired(false).create(SEARCH_OPTION));
        options.addOption(OptionBuilder.withLongOpt("tag").hasArgs(1).withDescription("If set only properties with the given tag are returned.").isRequired(false).create(TAG_OPTION));
        options.addOption(OptionBuilder.withLongOpt("key").hasArgs(1).withDescription("If set only properties with a key which contains the given term are returned.").isRequired(false).create(KEY_OPTION));
        options.addOption(OptionBuilder.withLongOpt("help").hasArg(false).withDescription("Prints this usage.").isRequired(false).create(HELP_OPTION));
        options.addOption(OptionBuilder.withLongOpt("useANSI").hasArg(true).withDescription("If set to 'true' the output will use ansi formatting. Defaults to 'true'.").isRequired(false).create(ANSI_OPTION));
    }

    public static void main(String[] args) {

        CommandLineParser parser = new PosixParser();
        try {
            CommandLine parse = parser.parse(options, args);
            if(parse.hasOption(HELP_OPTION)) {
                printUsage(1);
            }

            File yamlFolder = new File(DEFAULT_FOLDER);
            if (!yamlFolder.exists() || !yamlFolder.isDirectory()) {
                System.out.println("Unable to find the config documentation. The folder containing the docu doesn't exists ('"+DEFAULT_FOLDER+"').");
                System.exit(2);
            }

            try {
                ConfigDocu configDocu = new ConfigDocu(yamlFolder);

                List<Property> props = null;
                if(parse.hasOption(TAG_OPTION)) {
                    props = configDocu.getProperties(parse.getOptionValue(TAG_OPTION));
                } else {
                    props = configDocu.getProperties();
                }

                if(parse.hasOption(KEY_OPTION)) {
                    String term = parse.getOptionValue(KEY_OPTION);
                    props = props.stream()
                        .filter(prop -> prop.getKey().contains(term))
                        .collect(Collectors.toList());
                }
                if(parse.hasOption(SEARCH_OPTION)) {
                    String term = parse.getOptionValue(SEARCH_OPTION);
                    props = props.stream()
                        .filter(prop -> prop.contains(term))
                        .collect(Collectors.toList());
                }

                boolean useAnsi = true;
                if(parse.hasOption(ANSI_OPTION)) {
                    useAnsi = Boolean.valueOf(parse.getOptionValue(ANSI_OPTION));
                }
                printProperties(props, useAnsi);
            } catch (FileNotFoundException e) {
                handleError(e);
            }
        } catch (ParseException e1) {
            handleError(e1);
        }
    }

    private static void printProperties(List<Property> props, boolean useAnsi){
        for(Property prop: props) {
            printProperty(prop, useAnsi);
            System.out.println(format("\n-------------------------------------------------\n", ANSI_RED, useAnsi));
        }
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_FRAME = "\u001B[51m";

    private static void printProperty(Property prop, boolean useAnsi) {
        printKeyValue("Key", prop.getKey(), useAnsi);
        System.out.println(format("Description", ANSI_RED, useAnsi)+":");
        System.out.println(formatDescription(prop.getDescription(), useAnsi));
        printKeyValue("Default", prop.getDefaultValue(), true, useAnsi);
        printKeyValue("File", prop.getFile(), useAnsi);
        printKeyValue("Version", prop.getVersion(), useAnsi);
        printKeyValue("Package", prop.getPackageName(), useAnsi);
        System.out.print(format("Tags", ANSI_RED, useAnsi)+": ");
        boolean first = true;
        for(String tag: prop.getTags()) {
            if(first) {
                System.out.print(tag);
                first = false;
            } else {
                System.out.print(format(" | ", ANSI_RED, useAnsi)+tag);
            }
        }
    }

    private static String formatDescription(String value, boolean useAnsi) {
        if(useAnsi) {
            // todo
            value = value.replaceAll("<code>|\\[\\[", ANSI_FRAME+" ");
            value = value.replaceAll("</code>|\\]\\]", " "+ANSI_RESET);
            value = value.replaceAll("<b>", ANSI_BOLD);
            value = value.replaceAll("</b>", ANSI_RESET);
        } else {
            value = value.replaceAll("\\[\\[|\\]\\]", "");
        }
        value = value.replaceAll("<li>", "-");
        return value.replaceAll("\\<[^>]*>","");
    }

    private static String format(String value, String color, boolean useAnsi) {
        if(useAnsi) {
            return color+value+ANSI_RESET;
        }
        return value;
    }

    private static void printKeyValue(String key, String value, boolean useAnsi) {
        printKeyValue(key, value, false, useAnsi);
    }

    private static void printKeyValue(String key, String value, boolean printEmptyValue, boolean useAnsi) {
        if(value!=null) {
            System.out.println(format(key, ANSI_RED, useAnsi)+": "+value);
        } else if(printEmptyValue) {
            System.out.println(format(key, ANSI_RED, useAnsi)+": ");
        }
    }

    /**
     * Prints an error message with exit code 3
     *
     * @param e The error
     */
    private static final void handleError(Exception e){
        System.out.println("An error occured: "+e.getMessage());
        System.exit(3);
    }

    /**
     * Print usage
     *
     * @param exitCode
     */
    private static final void printUsage(int exitCode) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(80);
        hf.printHelp("showconfig", options);
        System.exit(exitCode);
    }
}
