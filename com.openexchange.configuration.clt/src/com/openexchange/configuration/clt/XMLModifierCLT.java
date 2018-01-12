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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.openexchange.configuration.clt.scr.Logback;
import com.openexchange.configuration.clt.scr.SCRException;

/**
 * {@link XMLModifierCLT} is a command line tool to maintain XML configuration files. Currently it only allows to add new XML fragments at
 * defined positions in the existing XML configuration file.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class XMLModifierCLT {

    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    private static final TransformerFactory     tf  = TransformerFactory.newInstance();
    private static final XPathFactory           xf  = XPathFactory.newInstance();

    public XMLModifierCLT() {
        super();
    }

    public static void main(String[] args) {
        System.exit(configureXML(args));
    }

    private static int configureXML(String[] args) {
        Options options = new Options();
        options.addOption(createOption("h", "help", false, "Prints a help text.", false));
        options.addOption(createOption("i", "in", true, "XML document is read from this file.", false));
        options.addOption(createOption("o", "out", true, "Modified XML document is written to this file.", false));
        options.addOption(createOption("x", "xpath", true, "XPath to the elements that should be modified.", false));
        options.addOption(createOption("a", "add", true, "XML file that should add the elements denoted by the XPath. - can be used to read from STDIN.", false));
        options.addOption(createOption("r", "replace", true, "XML file that should replace the elements denoted by the XPath. - can be used to read from STDIN.", false));
        options.addOption(createOption("d", "id", true, "Defines the identifying attribute as XPath (relative to \"-x\") to determine if an element should be replaced (-r). If omitted all matches will be replaced.", false));
        options.addOption(createOption("z", "zap", false, "Defines if duplicate matching elements should be removed(zapped) instead of only being replaced (-r).", false));
        options.addOption(createOption("s", "scr", true, "Specifies which scr should be executed on the xml document selected via -i", false));

        CommandLineParser parser = new PosixParser();
        final CommandLine cmd;
        try {
            cmd = parser.parse(options, args, true);
        } catch (ParseException e) {
            System.err.println("Parsing the command line failed: " + e.getMessage());
            return 1;
        }
        if (cmd.hasOption('h') || 0 == args.length) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("xmlModifier", "Can modify XML configuration files. Currently only allows to add XML fragments.", options, null, false);
            return 0;
        }
        final Transformer transformer;
        try {
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
            System.err.println("Can not configure XML writer: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
        Document document = parseInput(!cmd.hasOption('i'), cmd.getOptionValue('i'));
        if (null == document) {
            return 1;
        }
        try {
            if (cmd.hasOption('s')) {
                String scr = cmd.getOptionValue('s');
                scr = scr == null ? "" : scr.trim();
                switch (scr) {
                    case "4249":
                        scr_4249(document);
                        break;
                    default:
                        throw new SCRException("Unable to find specified Software Change Request: " + scr);
                }
            } else if (cmd.hasOption('a')) {
                XPath path = xf.newXPath();
                String xPath = cmd.getOptionValue('x');
                XPathExpression expression = path.compile(xPath);
                Document add = parseInput("-".equals(cmd.getOptionValue('a')), cmd.getOptionValue('a'));
                doAdd(document, expression, add);
            } else if (cmd.hasOption('r')) {
                XPath path = xf.newXPath();
                String xPath = cmd.getOptionValue('x');
                XPathExpression expression = path.compile(xPath);
                Document replace = parseInput("-".equals(cmd.getOptionValue('r')), cmd.getOptionValue('r'));
                doReplace(cmd.getOptionValue('d'), cmd.hasOption('z'), document, expression, replace);
            } else {
                System.err.println("Unknown operation mode.");
            }
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            final OutputStream os = determineOutput(!cmd.hasOption('o'), cmd.getOptionValue('o'));
            if (null == os) {
                return 1;
            }
            try {
                transformer.transform(new DOMSource(document), new StreamResult(os));
            } finally {
                os.close();
            }
        } catch (XPathExpressionException e) {
            System.err.println("Can not parse XPath expression: " + e.getMessage());
            e.printStackTrace();
            return 1;
        } catch (TransformerException e) {
            System.err.println("Can not write XML document" + e.getMessage());
            e.printStackTrace();
            return 1;
        } catch (IOException e) {
            System.err.println("Can not read XML file: " + e.getMessage());
            e.printStackTrace();
            return 1;
        } catch (SCRException e) {
            System.err.println("Error whily trying to apply Software Change Request: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    private static Node findParentNode(Document document, XPathExpression expression, Document insert) throws XPathExpressionException {
        NodeList insertList = (NodeList) expression.evaluate(insert, XPathConstants.NODESET);
        final Node insertParentNode;
        if (insertList.getLength() > 0) {
            insertParentNode = insertList.item(0).getParentNode();
        } else {
            return null;
        }
        XPathExpression parentExpression = xf.newXPath().compile(getXPathFromParents(insertParentNode));
        NodeList parentList = (NodeList) parentExpression.evaluate(document, XPathConstants.NODESET);
        if (parentList.getLength() == 0 || parentList.getLength() > 1) {
            return null;
        }
        return parentList.item(0);
    }

    private static String getXPathFromParents(Node node) {
        String retval = "";
        if (null == node.getParentNode() || node.getParentNode().isEqualNode(node) || node.getOwnerDocument().getDocumentElement().equals(node)) {
            retval = "";
        } else {
            retval = getXPathFromParents(node.getParentNode());
        }
        return retval + "/" + node.getNodeName();
    }

    private static void doAdd(Document document, XPathExpression expression, Document add) throws XPathExpressionException {
        NodeList origList = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
        final Node parentNode;
        if (origList.getLength() > 0) {
            parentNode = origList.item(0).getParentNode();
        } else {
            parentNode = findParentNode(document, expression, add);
        }
        if (null == parentNode) {
            throw new XPathExpressionException("Can not find any parent node to attach the new nodes to.");
        }
        NodeList toAddList = (NodeList) expression.evaluate(add, XPathConstants.NODESET);
        for (int i = 0; i < toAddList.getLength(); i++) {
            Node toAdd = toAddList.item(i);
            Node imported = document.importNode(toAdd, true);
            parentNode.appendChild(imported);
        }
    }

    private static void doReplace(String identifier, boolean removeDuplicates, Document document, XPathExpression expression, Document replace) throws XPathExpressionException {
        NodeList toReplaceList = (NodeList) expression.evaluate(replace, XPathConstants.NODESET);
        for (int i = 0; i < toReplaceList.getLength(); i++) {
            Node toReplace = toReplaceList.item(i);
            final String toReplaceIdentifier = getIdentifier(identifier, toReplace);
            // Try to find the matching original node
            boolean found = false;
            NodeList origList = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            for (int j = 0; j < origList.getLength(); j++) {
                Node original = origList.item(j);
                final String origIdentifier = getIdentifier(identifier, original);
                if (toReplaceIdentifier.equals(origIdentifier)) {
                    Node imported = document.importNode(toReplace, true);
                    Node parent = original.getParentNode();
                    if (!found) {
                        parent.replaceChild(imported, original);
                        found = true;
                    } else {
                        //already seen it, should we remove duplicates? 
                        if (removeDuplicates) {
                            //indentation is a text node which has to be removed
                            Node prev = original.getPreviousSibling();
                            if (prev != null && prev.getNodeType() == Node.TEXT_NODE && prev.getNodeValue().trim().isEmpty()) {
                                parent.removeChild(prev);
                            }
                            parent.removeChild(original);
                        } else {
                            //don't zap, simply replace
                            parent.replaceChild(imported, original);
                        }
                    }
                }
            }
            if (!found) {
                Node imported = document.importNode(toReplace, true);
                final Node parentNode;
                if (origList.getLength() > 0) {
                    parentNode = origList.item(0).getParentNode();
                } else {
                    parentNode = findParentNode(document, expression, replace);
                }
                parentNode.appendChild(imported);
            }
        }
    }

    /**
     * Transform a node into plain text
     * 
     * @param tf {@link TransformerFactory} to use for transformer creation
     * @param node The {@link Node} to transform to plain text
     * @return The transformed node
     * @throws TransformerException If an unrecoverable error occurs during the course of the transformation.
     */
    private static String transformToString(TransformerFactory tf, Node node) throws TransformerException {
        StringWriter sWriter = new StringWriter();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(node), new StreamResult(sWriter));
        return sWriter.toString();
    }

    private static String getIdentifier(String identifier, Node node) throws XPathExpressionException {
        String retval = "";
        if (null != identifier) {
            XPath path = xf.newXPath();
            XPathExpression identifierExpression = path.compile(identifier);
            retval = identifierExpression.evaluate(node);
        }
        return retval;
    }

    /**
     * Creates a commandline option
     * 
     * @param shortOpt The short version of the argumente e.g.: -h
     * @param longOpt The long version of the argument e.g.: --help
     * @param hasArg true If the option needs an argument or not e.g. --input /tmp/somefile
     * @param description The description of the command option
     * @param required If the command option is mandatory or optional
     * @return The new option
     */
    static Option createOption(String shortOpt, String longOpt, boolean hasArg, String description, boolean required) {
        Option option = new Option(shortOpt, longOpt, hasArg, description);
        option.setRequired(required);
        return option;
    }

    static Document parseInput(boolean stdin, String filename) {
        return parseInput(determineInput(stdin, filename));
    }

    static Document parseInput(InputStream is) {
        if (null == is) {
            return null;
        }
        final DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            db.setEntityResolver(new ClassloaderEntityResolver());
        } catch (ParserConfigurationException e) {
            System.err.println("Can not configure XML parser: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        final Document document;
        try {
            try {
                document = db.parse(is);
            } finally {
                is.close();
            }
        } catch (SAXException e) {
            System.err.println("Can not parse XML document: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            System.err.println("Can not read XML file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return document;
    }

    static InputStream determineInput(boolean stdin, String filename) {
        final InputStream is;
        if (!stdin) {
            File input = new File(filename);
            if (!input.exists() || !input.isFile() || !input.canRead()) {
                System.err.println("Can not open input file: \"" + input.getAbsolutePath() + "\".");
                return null;
            }
            try {
                is = new FileInputStream(input);
            } catch (IOException e) {
                System.err.println("Can not read input file: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } else {
            is = System.in;
        }
        return is;
    }

    /**
     * Apply SCR 4249: Dropped alternative file logger specification in logback
     * configuration file as only one of FILE and FILE_COMPAT can be configured
     * as they both would use the same log file which causes error logging to
     * open-xchange-console.log
     * 
     * @param doc The document to which the SCR should be applied
     * @throws SCRException if applying the Software Change Request fails
     */
    static void scr_4249(Document doc) throws SCRException {
        try {
            Map<String, Element> allAppenders = Logback.nodeListToNamedElementMap(doc.getElementsByTagName("appender"));

            // Get the appender-refs and associated appenders defined below the root logger
            Set<Element> rootAppenderRefs = Logback.getAppenderRefs(doc.getElementsByTagName("root"));
            Map<String, Element> activeAppenders = Logback.getMatchingAppenders(allAppenders, rootAppenderRefs);

            // Get appenders referenced by loggers besides the root logger and add them to the already found ones
            Map<String, Element> loggerMap = Logback.nodeListToNamedElementMap(doc.getElementsByTagName("logger"));
            for (Element logger : loggerMap.values()) {
                Set<Element> appenderRefs = Logback.getAppenderRefs(logger);
                activeAppenders.putAll(Logback.getMatchingAppenders(allAppenders, appenderRefs));
            }

            final String FILE = "FILE", FILEC = "FILE_COMPAT";
            /*
             * Comment either the FILE or FILE_COMPAT appender if both exists but one:
             * - is an existing appender
             * - isn't in use below the root logger
             * - isn't in use in any other logger
             * - has the same logfile configured as the other FILE or FILE_COMPAT appender
             * But error out if both appenders are still in active use
             */
            // Do both appenders still exist uncommented?
            if (allAppenders.containsKey(FILE) && allAppenders.containsKey(FILEC)) {
                String logfile = Logback.getLogFileFromAppender(allAppenders.get(FILE));
                String logfileCompact = Logback.getLogFileFromAppender(allAppenders.get(FILEC));
                // Will logback complain as both still use the same logfile?
                if (logfile.equals(logfileCompact)) {
                    if (activeAppenders.containsKey(FILE) && activeAppenders.containsKey(FILEC)) {
                        throw new Exception(String.format("Can't apply SCR 4249 as both the FILE and FILE_COMPAT appender are in use. Please deactivate one of them manually."));
                    } else if (!activeAppenders.containsKey(FILEC)) {
                        Element fileCompactAppender = allAppenders.get(FILEC);
                        Node parentNode = fileCompactAppender.getParentNode();
                        Comment commentedCompactAppender = doc.createComment(transformToString(tf, fileCompactAppender));
                        parentNode.replaceChild(commentedCompactAppender, fileCompactAppender);
                        System.out.println("Commented the FILE_COMPAT appender based on Software Change Request 4249");
                    } else if (!activeAppenders.containsKey(FILE)) {
                        Element fileAppender = allAppenders.get(FILE);
                        Node parentNode = fileAppender.getParentNode();
                        Comment commentedAppender = doc.createComment(transformToString(tf, fileAppender));
                        parentNode.replaceChild(commentedAppender, fileAppender);
                        System.out.println("Commented the FILE appender based on Software Change Request 4249");
                    }
                }
            }
        } catch (Exception e) {
            throw new SCRException(e);
        }
    }
}
