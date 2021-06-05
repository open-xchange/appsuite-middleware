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
import org.apache.commons.cli.Options;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.cli.AbstractCLI;

/**
 * {@link ConvertJUL2LogbackCLT}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ConvertJUL2LogbackCLT extends AbstractCLI<Integer, Void> {

    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    private static final TransformerFactory tf = TransformerFactory.newInstance();
    private static final String HEADER = "Reads Java Util logging properties configuration files and converts that to a LogBack XML configuration.";
    private static final String SYNTAX = "convertJUL2Logback [-i <input>] [-o <output>] | -h";

    /**
     * Entry point
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Integer retVal = new ConvertJUL2LogbackCLT().execute(args);
        if (retVal == null) {
            retVal = Integer.valueOf(1);
        }
        System.exit(retVal.intValue());
    }

    /**
     * Initialises a new {@link ConvertJUL2LogbackCLT}.
     */
    private ConvertJUL2LogbackCLT() {
        super();
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        // nothing to check
    }

    @Override
    protected Integer invoke(Options option, CommandLine cmd, Void context) throws Exception {
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            System.err.println("Can not configure XML parser: " + e.getMessage());
            e.printStackTrace();
            return Integer.valueOf(1);
        }
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
            System.err.println("Can not configure XML writer: " + e.getMessage());
            e.printStackTrace();
            return Integer.valueOf(1);
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        Properties properties = XMLUtil.parseInput(!cmd.hasOption('i'), cmd.getOptionValue('i'));
        if (null == properties) {
            return Integer.valueOf(1);
        }
        try {
            Document document = db.newDocument();
            convert(properties, document);
            OutputStream os = IOUtil.determineOutput(!cmd.hasOption('o'), cmd.getOptionValue('o'));
            if (null == os) {
                return Integer.valueOf(1);
            }
            try {
                transformer.transform(new DOMSource(document), new StreamResult(os));
            } finally {
                os.close();
            }
        } catch (IOException e) {
            System.err.println("Can not write file: " + e.getMessage());
            e.printStackTrace();
            return Integer.valueOf(1);
        } catch (TransformerException e) {
            System.err.println("Can not write XML document" + e.getMessage());
            e.printStackTrace();
            return Integer.valueOf(1);
        }
        return Integer.valueOf(0);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("i", "in", "input", "Java Util logging properties configuration file to read. If omitted this will be read vom STDIN.", false));
        options.addOption(createArgumentOption("o", "out", "output", "File for writing the Logback XML configuration fragment. If omitted this will be written to STDOUT.", false));
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

    //////////////////////////////////////////// HELPERS /////////////////////////////////////

    /**
     * Converts the specified {@link Properties} to a {@link Document}
     * 
     * @param properties The {@link Properties} to convert
     * @param document The {@link Document} to convert to
     */
    private void convert(Properties properties, Document document) {
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

    /**
     * Converts the specified level
     * 
     * @param julLevel the level to convert
     * @return The converted level
     */
    private String convertLevel(String julLevel) {
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

    @Override
    protected Void getContext() {
        return null;
    }
}
