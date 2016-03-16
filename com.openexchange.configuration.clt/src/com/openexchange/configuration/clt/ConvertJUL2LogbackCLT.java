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

import static com.openexchange.configuration.clt.XMLModifierCLT.createOption;
import static com.openexchange.configuration.clt.XMLModifierCLT.determineInput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * {@link ConvertJUL2LogbackCLT}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ConvertJUL2LogbackCLT {

    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    private static final TransformerFactory tf = TransformerFactory.newInstance();

    public ConvertJUL2LogbackCLT() {
        super();
    }

    public static void main(String[] args) {
        System.exit(convert(args));
    }

    private static int convert(String[] args) {
        Options options = new Options();
        options.addOption(createOption("h", "help", false, "Prints a help text.", false));
        options.addOption(createOption("i", "in", true, "Java Util logging properties configuration file to read. If omitted this will be read vom STDIN.", false));
        options.addOption(createOption("o", "out", true, "File for writing the Logback XML configuration fragment. If omitted this will be written to STDOUT.", false));
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
            formatter.printHelp("convertJUL2Logback", "Reads Java Util logging properties configuration files and converts that to a LogBack XML configuration.", options, null, false);
            return 0;
        }
        final DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            System.err.println("Can not configure XML parser: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
        final Transformer transformer;
        try {
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
            System.err.println("Can not configure XML writer: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        Properties properties = parseInput(!cmd.hasOption('i'), cmd.getOptionValue('i'));
        if (null == properties) {
            return 1;
        }
        try {
            Document document = db.newDocument();
            convert(properties, document);
            final OutputStream os = determineOutput(!cmd.hasOption('o'), cmd.getOptionValue('o'));
            if (null == os) {
                return 1;
            }
            try {
                transformer.transform(new DOMSource(document), new StreamResult(os));
            } finally {
                os.close();
            }
        } catch (IOException e) {
            System.err.println("Can not write file: " + e.getMessage());
            e.printStackTrace();
            return 1;
        } catch (TransformerException e) {
            System.err.println("Can not write XML document" + e.getMessage());
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    private static void convert(Properties properties, Document document) {
        Element configuration = document.createElement("configuration");
        document.appendChild(configuration);
        for (String name : properties.stringPropertyNames()) {
            if (name.equals(".level")) {
                continue;
            }
            Element logger = document.createElement("logger");
            configuration.appendChild(logger);
            logger.setAttribute("name", name.substring(0, name.length() - ".level".length()));
            logger.setAttribute("level", convertLevel(properties.getProperty(name)));
        }
        if (properties.containsKey(".level")) {
            Element root = document.createElement("root");
            configuration.appendChild(root);
            root.setAttribute("level", convertLevel(properties.getProperty(".level")));
            Element appender_ref = document.createElement("appender-ref");
            root.appendChild(appender_ref);
            appender_ref.setAttribute("ref", "ASYNC");
        }
    }

    private static String convertLevel(String julLevel) {
        String retval = julLevel;
        // OFF
        if ("SEVERE".equals(julLevel)) {
            retval = "ERROR";
        }
        if ("WARNING".equals(julLevel)) {
            retval = "WARN";
        }
        // INFO
        if ("CONFIG".equals(julLevel) || "FINE".equals(julLevel)) {
            retval = "DEBUG";
        }
        if ("FINER".equals(julLevel) || "FINEST".equals(julLevel)) {
            retval = "TRACE";
        }
        // ALL
        return retval;
    }

    static Properties parseInput(boolean stdin, String filename) {
        Properties properties = new Properties();
        try {
            final InputStream is = determineInput(stdin, filename);
            if (null == is) {
                return null;
            }
            try {
                properties.load(is);
            } finally {
                is.close();
            }
        } catch (IOException e) {
            System.err.println("Can not read XML file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return properties;
    }

    static OutputStream determineOutput(boolean stdout, String filename) {
        OutputStream os = null;
        if (!stdout) {
            File output = new File(filename);
            try {
                if (!output.createNewFile() && !output.canWrite()) {
                    System.err.println("Can not write to output file: \"" + output.getAbsolutePath() + "\".");
                    return null;
                }
                os = new FileOutputStream(output);
            } catch (IOException e) {
                System.err.println("Can not write output file: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } else {
            os = System.out;
        }
        return os;
    }
}
