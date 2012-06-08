
package com.anwrt.ooserver.daemon;

import java.io.FileNotFoundException;
import java.io.File;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

/**
 * Used to parse a configuration from an XML file using SAX <br>
 * creation : 28 août 07
 *
 * @author <a href="mailto:oodaemon@extraserv.net">Jounayd Id Salah</a>
 */
public class ConfigHandler extends org.xml.sax.helpers.DefaultHandler implements org.xml.sax.ContentHandler {

    private Config _config;

    private final SAXParser _parser;

    /**
     * Creates parser
     *
     * @throws Exception failed to create SAX parser
     */
    public ConfigHandler() throws Exception {
        Logger.debug("parser starting ...");
        final SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
        _parser = factory.newSAXParser();
        Logger.debug("parser OK");
    }

    @Override
    public void startDocument() {
        _config = new Config();
    }

    @Override
    public void endDocument() {
        /* DO NOTHING */
    }

    /**
     * Reads a configuration from an xml file
     *
     * @param filePath xml file filePath (absolute/relative)
     * @return configuration file
     * @throws FileNotFoundException -
     * @throws SAXException exception when parsing the xml configuration file
     * @throws java.io.IOException error when reading the xml configuration file
     */
    public Config readConfiguration(final String filePath) throws FileNotFoundException, SAXException, java.io.IOException {
        Logger.info(Config.CONFIG_TAG + "reading configuration file ... : " + filePath);
        final File in = new File(filePath);
        if (!in.canRead()) {
            throw new FileNotFoundException(Config.CONFIG_TAG + "cannot get configuration file : " + filePath);
        }
        _parser.parse(in, this);

        Logger.info(Config.CONFIG_TAG + "configuration file OK");

        return _config;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attlist) {
        if (qName.equals("acceptor")) {
            _config.acceptor = attlist.getValue((short) 0);
        } else if (qName.equals("admin-acceptor")) {
            _config.adminAcceptor = attlist.getValue((short) 0);
        } else if (qName.equals("open-office-install-path")) {
            final String path = attlist.getValue("path");
            _config.officeProgramDirectoryPath = OSAbstractLayer.concatPaths(path, "program");
        } else if (qName.equals("user-installation")) {
            _config.userInstallation.add(attlist.getValue("url"));
        } else if (qName.equals("tolerated-startuptime-per-instance")) {
            final int timeInSeconds = Integer.parseInt(attlist.getValue("value"));
            _config.toleratedStartupTimePerInstance = convertSecondsToMillis(timeInSeconds);
        } else if (qName.equals("usage-count-per-instance")) {
            _config.maxUsageCountPerInstance = Integer.parseInt(attlist.getValue("max"));
            _config.randomUsageCountPerInstance = Integer.parseInt(attlist.getValue("random"));
        } else if (qName.equals("logger")) {
            try {
                Logger.setLevel(attlist.getValue("level"));
            } catch (final IncorrectLoggerLevelException ex) {
                Logger.error(ex.getMessage());
            }
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) { /* NOTHING */
    }

    @Override
    public void warning(final SAXParseException ex) {
        Logger.warning(Config.CONFIG_TAG + ex.getMessage());
        Logger.debug(ex);
    }

    @Override
    public void error(final SAXParseException ex) {
        Logger.error(Config.CONFIG_TAG + ex.getMessage());
        Logger.debug(ex);
    }

    @Override
    public void skippedEntity(final String name) throws SAXException {
        Logger.warning(Config.CONFIG_TAG + "skipped entity : " + name);
    }

    /**
     * Convert seconds to milliseconds (yeah really useful javadoc)
     *
     * @param seconds seconds
     * @return milliseconds
     */
    private int convertSecondsToMillis(final int seconds) {
        return (seconds * 1000);
    }
}
