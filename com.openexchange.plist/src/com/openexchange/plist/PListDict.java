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

package com.openexchange.plist;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.codec.binary.Base64;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.plist.xml.StaxUtils;


/**
 * {@link PListDict} - Represents a PLIST dictionary.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class PListDict extends AbstractPListElement {

    private final List<AbstractPListKeyValue> elements;

    /**
     * Initializes a new {@link PListDict}.
     */
    public PListDict() {
        super();
        elements = new ArrayList<AbstractPListKeyValue>(8);
    }

    /**
     * Writes the content of this dictionary to given {@link Writer} instance.
     *
     * @param writer The writer
     * @throws IOException If writing fails
     */
    public void writeTo(Writer writer) throws IOException {
        try {
            write(StaxUtils.createXMLStreamWriter(writer));
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /**
     * Sets the <code>PayloadIdentifier</code> element.
     * <p>
     * A reverse-DNS style identifier (com.example.myprofile, for example) that identifies the profile.
     * This string is used to determine whether a new profile should replace an existing one or should be added.
     *
     * @param payloadIdentifier The payload identifier
     * @return This instance
     */
    public PListDict setPayloadIdentifier(String payloadIdentifier) {
        elements.add(new PListStringValue("PayloadIdentifier", payloadIdentifier));
        return this;
    }

    /**
     * Sets the <code>PayloadType</code> element.
     * <p>
     * The only supported value is <code>"Configuration"</code>.
     *
     * @param payloadType The payload type
     * @return This instance
     */
    public PListDict setPayloadType(String payloadType) {
        elements.add(new PListStringValue("PayloadType", payloadType));
        return this;
    }

    /**
     * Sets the <code>PayloadDisplayName</code> element.
     * <p>
     * A human-readable name for the profile. This value is displayed on the Detail screen. It does not have to be unique.
     *
     * @param payloadDisplayName The payload display name
     * @return This instance
     */
    public PListDict setPayloadDisplayName(String payloadDisplayName) {
        elements.add(new PListStringValue("PayloadDisplayName", payloadDisplayName));
        return this;
    }

    /**
     * Sets the <code>PayloadDescription</code> element.
     * <p>
     * A description of the profile, shown on the Detail screen for the profile.
     * This should be descriptive enough to help the user decide whether to install the profile.
     *
     * @param payloadDescription The payload description
     * @return This instance
     */
    public PListDict setPayloadDescription(String payloadDescription) {
        elements.add(new PListStringValue("PayloadDescription", payloadDescription));
        return this;
    }

    /**
     * Sets the <code>PayloadUUID</code> element.
     * <p>
     * A globally unique identifier for the profile. The actual content is unimportant, but it must be globally unique.
     *
     * @param payloadUUID The payload UUID
     * @return This instance
     */
    public PListDict setPayloadUUID(String payloadUUID) {
        elements.add(new PListStringValue("PayloadUUID", payloadUUID));
        return this;
    }

    /**
     * Sets the <code>PayloadOrganization</code> element.
     *
     * @param payloadOrganization The payload organization
     * @return This instance
     */
    public PListDict setPayloadOrganization(String payloadOrganization) {
        elements.add(new PListStringValue("PayloadOrganization", payloadOrganization));
        return this;
    }

    /**
     * Sets the <code>PayloadVersion</code> element.
     * <p>
     * The version number of the profile format.
     * This describes the version of the configuration profile as a whole, not of the individual profiles within it.
     * Currently, this value should be <code>1</code>.
     *
     * @param payloadVersion The payload version
     * @return This instance
     */
    public PListDict setPayloadVersion(int payloadVersion) {
        elements.add(new PListIntegerValue("PayloadVersion", payloadVersion));
        return this;
    }

    /**
     * Sets the <code>PayloadContent</code> element. Replaces any existing payload dictionaries.
     * <p>
     * A payload dictionary.
     *
     * @param payloadContent The payload content
     * @return This instance
     */
    public PListDict setPayloadContent(PListDict payloadContent) {
        for (Iterator<AbstractPListKeyValue> it = elements.iterator(); it.hasNext();) {
            AbstractPListKeyValue element = it.next();
            if ("PayloadContent".equals(element.getKey())) {
                it.remove();
            }
        }

        elements.add(new PListArrayValue("PayloadContent").add(payloadContent));
        return this;
    }

    /**
     * Adds the <code>PayloadContent</code> element. Extending any existing payload dictionaries.
     * <p>
     * A payload dictionary.
     *
     * @param payloadContent The payload content
     * @return This instance
     */
    public PListDict addPayloadContent(PListDict payloadContent) {
        PListArrayValue existingPayloadContent = null;

        for (Iterator<AbstractPListKeyValue> it = elements.iterator(); null == existingPayloadContent && it.hasNext();) {
            AbstractPListKeyValue element = it.next();
            if ("PayloadContent".equals(element.getKey())) {
                existingPayloadContent = (PListArrayValue) element;
            }
        }

        if (null == existingPayloadContent) {
            elements.add(new PListArrayValue("PayloadContent").add(payloadContent));
        } else {
            existingPayloadContent.add(payloadContent);
        }

        return this;
    }

    /**
     * Adds the specified icon
     *
     * @param in The icon's input stream
     * @return This instance
     * @throws IOException If icon's input stream cannot be read
     */
    public PListDict addIcon(InputStream in) throws IOException {
        return addDataValue("Icon", in);
    }

    /**
     * Adds the specified icon
     *
     * @param bytes The icon's bytes
     * @return This instance
     */
    public PListDict addIcon(byte[] bytes) {
        return addDataValue("Icon", bytes);
    }

    /**
     * Adds the specified string value.
     *
     * @param key The key/name
     * @param value The value
     * @return This instance
     */
    public PListDict addStringValue(String key, String value) {
        elements.add(new PListStringValue(key, value));
        return this;
    }

    /**
     * Adds the specified integer value.
     *
     * @param key The key/name
     * @param value The value
     * @return This instance
     */
    public PListDict addIntegerValue(String key, int value) {
        elements.add(new PListIntegerValue(key, value));
        return this;
    }

    /**
     * Adds the specified boolean value.
     *
     * @param key The key/name
     * @param value The value
     * @return This instance
     */
    public PListDict addBooleanValue(String key, boolean value) {
        elements.add(new PListBooleanValue(key, value));
        return this;
    }

    /**
     * Adds the specified binary value.
     *
     * @param key The key/name
     * @param value The stream
     * @return This instance
     */
    public PListDict addDataValue(String key, InputStream in) throws IOException {
        elements.add(new PListStringValue(key, Charsets.toAsciiString(Base64.encodeBase64(Streams.stream2bytes(in), false))));
        return this;
    }

    /**
     * Adds the specified binary value.
     *
     * @param key The key/name
     * @param value The byte array
     * @return This instance
     */
    public PListDict addDataValue(String key, byte[] bytes) {
        elements.add(new PListStringValue(key, Charsets.toAsciiString(Base64.encodeBase64(bytes, false))));
        return this;
    }

    @Override
    protected void write(XMLStreamWriter writer) throws XMLStreamException {
        QName qualifiedName = new QName("dict");
        String localName = qualifiedName.getLocalPart();
        String namespaceURI = qualifiedName.getNamespaceURI();
        String prefix = writer.getPrefix(namespaceURI);
        if (prefix == null) {
            prefix = qualifiedName.getPrefix();
            writer.setPrefix(prefix, namespaceURI);
        }

        if (Strings.isEmpty(namespaceURI)) {
            writer.writeStartElement(localName);
        } else {
            writer.writeStartElement(prefix, localName, namespaceURI);
            writer.writeNamespace(prefix, namespaceURI);
        }

        for (AbstractPListElement pListElement : elements) {
            pListElement.write(writer);
        }

        writer.writeEndElement();
    }

}
