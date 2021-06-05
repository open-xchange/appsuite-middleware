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
import org.apache.commons.cli.Options;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.openexchange.cli.AbstractCLI;

/**
 * {@link ExtractLog4JModificationsCLT}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ExtractLog4JModificationsCLT extends AbstractCLI<Integer, Void> {

    private static final String SYNTAX = "log4JModifications [-i <input>] [-o <output>] | -h";
    private static final String HEADER = "Reads a log4j.xml and outputs modified logger levels as JUL properties format.";
    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    private static final XPathFactory xf = XPathFactory.newInstance();

    /**
     * Entry point
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Integer retVal = new ExtractLog4JModificationsCLT().execute(args);
        if (retVal == null) {
            retVal = Integer.valueOf(1);
        }
        System.exit(retVal.intValue());
    }

    /**
     * Initialises a new {@link ExtractLog4JModificationsCLT}.
     */
    private ExtractLog4JModificationsCLT() {
        super();
    }

    @Override
    protected Integer invoke(Options option, CommandLine cmd, Void context) throws Exception {
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            db.setEntityResolver(new ClassloaderEntityResolver());
        } catch (ParserConfigurationException e) {
            System.err.println("Can not configure XML parser: " + e.getMessage());
            e.printStackTrace();
            return Integer.valueOf(1);
        }
        try {
            InputStream resourceAsStream = ExtractLog4JModificationsCLT.class.getClassLoader().getResourceAsStream("log4j.xml");
            Document original;
            try {
                original = db.parse(resourceAsStream);
            } finally {
                resourceAsStream.close();
            }
            Document document = XMLUtil.parseInput(IOUtil.determineInput(!cmd.hasOption('i'), cmd.getOptionValue('i')));
            // Find differences
            Properties properties = extractDifferences(original, document);
            properties.putAll(extractRootLevel(original, document));
            // Write output
            OutputStream os = IOUtil.determineOutput(!cmd.hasOption('o'), cmd.getOptionValue('o'));
            if (os == null) {
                return Integer.valueOf(1);
            }
            try {
                properties.store(os, "file-logging.properties");
            } finally {
                os.close();
            }
        } catch (SAXException e) {
            System.err.println("Can not parse XML document: " + e.getMessage());
            e.printStackTrace();
            return Integer.valueOf(1);
        } catch (XPathExpressionException e) {
            System.err.println("Can not parse XPath expression: " + e.getMessage());
            e.printStackTrace();
            return Integer.valueOf(1);
        } catch (IOException e) {
            System.err.println("Can not read XML file: " + e.getMessage());
            e.printStackTrace();
            return Integer.valueOf(1);
        }
        return Integer.valueOf(0);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("i", "in", "input", "XML document is read from this file. If omitted the input will be read from STDIN.", false));
        options.addOption(createArgumentOption("o", "out", "output", "JUL properties configuration file is written to this file. If this option is omitted the output will be written to STDOUT.", false));
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

    ////////////////////////////////////// HELPERS ///////////////////////////////

    private Properties extractRootLevel(Document original, Document current) throws XPathExpressionException {
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

    private Properties extractDifferences(Document original, Document current) throws XPathExpressionException {
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

    private Set<Logger> parseLogger(Document original) throws XPathExpressionException {
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

    private String convertLevel(String log4JLevel) {
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

    @Override
    protected Void getContext() {
        return null;
    }
}
