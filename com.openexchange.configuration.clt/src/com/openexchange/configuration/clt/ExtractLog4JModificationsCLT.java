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
import static com.openexchange.configuration.clt.XMLModifierCLT.parseInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * {@link ExtractLog4JModificationsCLT}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ExtractLog4JModificationsCLT {

    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    private static final XPathFactory xf = XPathFactory.newInstance();

    public ExtractLog4JModificationsCLT() {
        super();
    }

    public static void main(String[] args) {
        System.exit(writeLog4JModifications(args));
    }

    private static int writeLog4JModifications(String[] args) {
        Options options = new Options();
        options.addOption(createOption("h", "help", false, "Prints a help text.", false));
        options.addOption(createOption("i", "in", true, "XML document is read from this file. If omitted the input will be read from STDIN.", false));
        options.addOption(createOption("o", "out", true, "JUL properties configuration file is written to this file. If this option is omitted the output will be written to STDOUT.", false));
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
            formatter.printHelp("log4JModifications", "Reads a log4j.xml and outputs modified logger levels as JUL properties format.", options, null, false);
            return 0;
        }
        final DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            db.setEntityResolver(new ClassloaderEntityResolver());
        } catch (ParserConfigurationException e) {
            System.err.println("Can not configure XML parser: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
        try {
            InputStream resourceAsStream = ExtractLog4JModificationsCLT.class.getClassLoader().getResourceAsStream("log4j.xml");
            final Document original;
            try {
                original = db.parse(resourceAsStream);
            } finally {
                resourceAsStream.close();
            }
            Document document = parseInput(!cmd.hasOption('i'), cmd.getOptionValue('i'));
            // Find differences
            Properties properties = extractDifferences(original, document);
            properties.putAll(extractRootLevel(original, document));
            // Write output
            final OutputStream os = determineOutput(!cmd.hasOption('o'), cmd.getOptionValue('o'));
            try {
                properties.store(os, "file-logging.properties");
            } finally {
                os.close();
            }
        } catch (SAXException e) {
            System.err.println("Can not parse XML document: " + e.getMessage());
            e.printStackTrace();
            return 1;
        } catch (XPathExpressionException e) {
            System.err.println("Can not parse XPath expression: " + e.getMessage());
            e.printStackTrace();
            return 1;
        } catch (IOException e) {
            System.err.println("Can not read XML file: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    private static Properties extractRootLevel(Document original, Document current) throws XPathExpressionException {
        Properties retval = new Properties();
        XPath path = xf.newXPath();
        XPathExpression expression = path.compile("/configuration/root/level/@value");
        Node currentNode = (Node) expression.evaluate(current, XPathConstants.NODE);
        String currentValue = currentNode.getNodeValue();
        Node origNode = (Node) expression.evaluate(original, XPathConstants.NODE);
        String origValue = origNode.getNodeValue();
        if (!origValue.equals(currentValue)) {
            retval.put(".level", convertLevel(currentValue));
        }
        return retval;
    }

    private static Properties extractDifferences(Document original, Document current) throws XPathExpressionException {
        Properties retval = new Properties();
        Set<Logger> origLogger = parseLogger(original);
        Set<Logger> configuredLogger = parseLogger(current);
        Set<Logger> added = new HashSet<Logger>(configuredLogger);
        Set<Logger> changed = new HashSet<Logger>(configuredLogger);
        added.removeAll(origLogger);
        changed.retainAll(origLogger);
        for (Logger logger : added) {
            retval.put(logger.getName() + ".level", convertLevel(logger.getLevel()));
        }
        loop: for (Logger change : changed) {
            for (Logger orig : origLogger) {
                if (orig.equals(change) && !change.getLevel().equals(orig.getLevel())) {
                    retval.put(change.getName() + ".level", convertLevel(change.getLevel()));
                    continue loop;
                }
            }
        }
        return retval;
    }

    private static Set<Logger> parseLogger(Document original) throws XPathExpressionException {
        XPath path = xf.newXPath();
        NodeList list = (NodeList) path.compile("/configuration/logger/@name").evaluate(original, XPathConstants.NODESET);
        Set<Logger> retval = new HashSet<Logger>(list.getLength());
        for (int i = 0; i < list.getLength(); i++) {
            Node nameAttribute = list.item(i);
            final String name = nameAttribute.getNodeValue();
            Node valueAttribute = (Node) path.compile("../level/@value").evaluate(nameAttribute, XPathConstants.NODE);
            final String value = valueAttribute.getNodeValue();
            retval.add(new Logger(name, value));
        }
        return retval;
    }

    private static String convertLevel(String log4JLevel) {
        String retval = log4JLevel;
        // OFF
        if ("ERROR".equals(log4JLevel) || "FATAL".equals(log4JLevel)) {
            retval = "SEVERE";
        }
        if ("WARN".equals(log4JLevel)) {
            retval = "WARNING";
        }
        // INFO
        if ("DEBUG".equals(log4JLevel)) {
            retval = "FINE";
        }
        if ("TRACE".equals(log4JLevel)) {
            retval = "FINE";
        }
        // ALL
        return retval;
    }
}
