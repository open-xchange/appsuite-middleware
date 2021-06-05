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

package com.openexchange.mail.autoconfig.xmlparser;

import static com.openexchange.mail.autoconfig.xmlparser.ClientConfig.CLIENT_CONFIG;
import static com.openexchange.mail.autoconfig.xmlparser.ClientConfig.EMAIL_PROVIDER;
import static com.openexchange.mail.autoconfig.xmlparser.Documentation.DESC;
import static com.openexchange.mail.autoconfig.xmlparser.Documentation.LANG;
import static com.openexchange.mail.autoconfig.xmlparser.Documentation.URL;
import static com.openexchange.mail.autoconfig.xmlparser.EmailProvider.DISPLAY_NAME;
import static com.openexchange.mail.autoconfig.xmlparser.EmailProvider.DISPLAY_SHORT_NAME;
import static com.openexchange.mail.autoconfig.xmlparser.EmailProvider.DOCUMENTATION;
import static com.openexchange.mail.autoconfig.xmlparser.EmailProvider.DOMAIN;
import static com.openexchange.mail.autoconfig.xmlparser.EmailProvider.INCOMING_SERVER;
import static com.openexchange.mail.autoconfig.xmlparser.EmailProvider.INSTRUCTION;
import static com.openexchange.mail.autoconfig.xmlparser.EmailProvider.OUTGOING_SERVER;
import static com.openexchange.mail.autoconfig.xmlparser.Server.AUTHENTICATION;
import static com.openexchange.mail.autoconfig.xmlparser.Server.HOSTNAME;
import static com.openexchange.mail.autoconfig.xmlparser.Server.PORT;
import static com.openexchange.mail.autoconfig.xmlparser.Server.SOCKET_TYPE;
import static com.openexchange.mail.autoconfig.xmlparser.Server.TYPE;
import static com.openexchange.mail.autoconfig.xmlparser.Server.USERNAME;
import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.START_TAG;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.autoconfig.AutoconfigException;

/**
 * {@link AutoconfigParser}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AutoconfigParser {

    /**
     * Initializes a new {@link AutoconfigParser}.
     */
    public AutoconfigParser() {
        super();
    }

    /**
     * Gets the configuration from passed stream.
     *
     * @param is The input stream to read from
     * @return The parsed configuration
     * @throws OXException If an error occurs
     */
    public ClientConfig getConfig(InputStream is) throws OXException {
        ClientConfig clientConfig = new ClientConfig();
        XmlPullParser parser = new KXmlParser();
        try {
            parser.setInput(new InputStreamReader(is, StandardCharsets.UTF_8));
            parser.nextTag();
            parser.require(START_TAG, null, CLIENT_CONFIG);
            Collection<EmailProvider> emailProvider = new ArrayList<EmailProvider>();
            clientConfig.setEmailProvider(emailProvider);
            while (parser.nextTag() == START_TAG) {
                String name = parser.getName();
                if (name.equalsIgnoreCase(EMAIL_PROVIDER)) {
                    parser.require(START_TAG, null, EMAIL_PROVIDER);
                    EmailProvider ep = parseEmailProvider(parser);
                    emailProvider.add(ep);
                } else {
                    int depth = 1;
                    while (depth > 0) {
                        int tag = parser.next();
                        String name2 = parser.getName();
                        if (tag == START_TAG && name2.equalsIgnoreCase(name)) {
                            depth++;
                        } else if (tag == END_TAG && name2.equalsIgnoreCase(name)) {
                            depth--;
                        }
                    }
                }
            }
            parser.require(END_TAG, null, CLIENT_CONFIG);
            parser.next();
            parser.require(XmlPullParser.END_DOCUMENT, null, null);
        } catch (XmlPullParserException e) {
            throw AutoconfigException.xml(e);
        } catch (IOException e) {
            throw AutoconfigException.io(e);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                // Ignore
            }

        }
        return clientConfig;
    }

    /**
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private EmailProvider parseEmailProvider(XmlPullParser parser) throws XmlPullParserException, IOException {
        EmailProvider ep = new EmailProvider();
        Collection<String> domains = new ArrayList<String>();
        ep.setDomains(domains);
        Collection<Documentation> docs = new ArrayList<Documentation>();
        ep.setDocumentations(docs);
        Collection<Instruction> instructions = new ArrayList<Instruction>();
        ep.setInstructions(instructions);
        Collection<IncomingServer> incomingServer = new ArrayList<IncomingServer>();
        ep.setIncomingServer(incomingServer);
        Collection<OutgoingServer> outgoingServer = new ArrayList<OutgoingServer>();
        ep.setOutgoingServer(outgoingServer);

        while (parser.nextTag() == START_TAG) {
            String name = parser.getName();
            if (name.equalsIgnoreCase(DOMAIN)) {
                domains.add(parseSimpleText(parser, DOMAIN));
            } else if (name.equalsIgnoreCase(DISPLAY_NAME)) {
                ep.setDisplayName(parseSimpleText(parser, DISPLAY_NAME));
            } else if (name.equalsIgnoreCase(DISPLAY_SHORT_NAME)) {
                ep.setDisplayShortName(parseSimpleText(parser, DISPLAY_SHORT_NAME));
            } else if (name.equalsIgnoreCase(INCOMING_SERVER)) {
                incomingServer.add((IncomingServer) parseServer(parser, INCOMING_SERVER));
            } else if (name.equalsIgnoreCase(OUTGOING_SERVER)) {
                outgoingServer.add((OutgoingServer) parseServer(parser, OUTGOING_SERVER));
            } else if (name.equalsIgnoreCase(DOCUMENTATION)) {
                docs.add(parseDocumentation(parser));
            } else if (name.equalsIgnoreCase(INSTRUCTION)) {
                instructions.add(parseInstruction(parser));
            } else {
                ignoreTag(parser, name);
            }
        }
        parser.require(END_TAG, null, EMAIL_PROVIDER);
        return ep;
    }

    /**
     * @param parser
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    private Documentation parseDocumentation(XmlPullParser parser) throws XmlPullParserException, IOException {
        Documentation doc = new Documentation();
        Map<String, String> descriptions = new HashMap<String, String>();
        doc.setDescriptions(descriptions);
        parser.require(START_TAG, null, DOCUMENTATION);
        doc.setUrl(parser.getAttributeValue(null, URL));
        while (parser.nextTag() == START_TAG) {
            parser.require(START_TAG, null, DESC);
            String lang = parser.getAttributeValue(null, LANG);
            String desc = parser.nextText();
            descriptions.put(lang, desc);
            parser.require(END_TAG, null, DESC);
        }
        parser.require(END_TAG, null, DOCUMENTATION);
        return doc;
    }

    /**
     * @param parser
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    private Instruction parseInstruction(XmlPullParser parser) throws XmlPullParserException, IOException {
        Instruction instruction = new Instruction();
        Map<String, String> descriptions = new HashMap<String, String>();
        instruction.setDescriptions(descriptions);
        parser.require(START_TAG, null, INSTRUCTION);
        instruction.setUrl(parser.getAttributeValue(null, URL));
        while (parser.nextTag() == START_TAG) {
            parser.require(START_TAG, null, DESC);
            String lang = parser.getAttributeValue(null, LANG);
            String desc = parser.nextText();
            descriptions.put(lang, desc);
            parser.require(END_TAG, null, DESC);
        }
        parser.require(END_TAG, null, INSTRUCTION);
        return instruction;
    }

    /**
     * @param parser
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    private Server parseServer(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
        Server server = tag.equalsIgnoreCase(INCOMING_SERVER) ? new IncomingServer() : new OutgoingServer();
        parser.require(START_TAG, null, tag);
        server.setType(parser.getAttributeValue(null, TYPE));
        while (parser.nextTag() == START_TAG) {
            String name = parser.getName();
            if (name.equalsIgnoreCase(AUTHENTICATION)) {
                server.setAuthentication(parseSimpleText(parser, AUTHENTICATION));
            } else if (name.equalsIgnoreCase(HOSTNAME)) {
                server.setHostname(parseSimpleText(parser, HOSTNAME));
            } else if (name.equalsIgnoreCase(PORT)) {
                server.setPort(Integer.parseInt(parseSimpleText(parser, PORT)));
            } else if (name.equalsIgnoreCase(SOCKET_TYPE)) {
                server.setSocketType(parseSimpleText(parser, SOCKET_TYPE));
            } else if (name.equalsIgnoreCase(USERNAME)) {
                server.setUsername(parseSimpleText(parser, USERNAME));
            } else {
                ignoreTag(parser, name);
            }
        }
        parser.require(END_TAG, null, tag);
        return server;
    }

    /**
     * @param parser
     * @param name
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void ignoreTag(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
        while (parser.next() != END_TAG || !parser.getName().equalsIgnoreCase(name)) {
            ;
        }
        parser.require(END_TAG, null, name);
    }

    /**
     * @param parser
     * @param tag
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String parseSimpleText(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
        parser.require(START_TAG, null, tag);
        String retval = parser.nextText();
        parser.require(END_TAG, null, tag);
        return retval;
    }
}
