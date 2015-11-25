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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.onboarding.plist;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.codec.binary.Base64;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.onboarding.plist.xml.StaxUtils;


/**
 * {@link PListDict}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class PListDict implements PListElement {

    public static PListDict newInstanceFor(String payloadIdentifier, String payloadType, String payloadDisplayName, String payloadUUID, int payloadVersion) {
        PListDict dict = new PListDict();
        dict.setPayloadIdentifier(payloadIdentifier).setPayloadType(payloadType).setPayloadDisplayName(payloadDisplayName);
        dict.setPayloadUUID(payloadUUID).setPayloadVersion(payloadVersion);
        return dict;
    }

    // --------------------------------------------------------------------

    private final List<PListElement> elements;

    /**
     * Initializes a new {@link PListDict}.
     */
    public PListDict() {
        super();
        elements = new ArrayList<PListElement>(8);
    }

    public void writeTo(Writer writer) throws XMLStreamException {
        write(StaxUtils.createXMLStreamWriter(writer));
    }

    public PListDict setPayloadIdentifier(String payloadIdentifier) {
        elements.add(new PListStringValue("PayloadIdentifier", payloadIdentifier));
        return this;
    }

    public PListDict setPayloadType(String payloadType) {
        elements.add(new PListStringValue("PayloadType", payloadType));
        return this;
    }

    public PListDict setPayloadDisplayName(String payloadDisplayName) {
        elements.add(new PListStringValue("PayloadDisplayName", payloadDisplayName));
        return this;
    }

    public PListDict setPayloadDescription(String payloadDescription) {
        elements.add(new PListStringValue("PayloadDescription", payloadDescription));
        return this;
    }

    public PListDict setPayloadUUID(String payloadUUID) {
        elements.add(new PListStringValue("PayloadUUID", payloadUUID));
        return this;
    }

    public PListDict setPayloadVersion(int payloadVersion) {
        elements.add(new PListIntegerValue("PayloadVersion", payloadVersion));
        return this;
    }

    public PListDict setPayloadContent(PListDict payloadContent) {
        elements.add(new PListArrayValue("PayloadContent").add(payloadContent));
        return this;
    }

    public PListDict addIcon(InputStream in) throws IOException {
        return addDataValue("Icon", in);
    }

    public PListDict addIcon(byte[] bytes) {
        return addDataValue("Icon", bytes);
    }

    public PListDict addStringValue(String key, String value) {
        elements.add(new PListStringValue(key, value));
        return this;
    }

    public PListDict addIntegerValue(String key, int value) {
        elements.add(new PListIntegerValue(key, value));
        return this;
    }

    public PListDict addBooleanValue(String key, boolean value) {
        elements.add(new PListBooleanValue(key, value));
        return this;
    }

    public PListDict addDataValue(String key, InputStream in) throws IOException {
        elements.add(new PListStringValue(key, Charsets.toAsciiString(Base64.encodeBase64(Streams.stream2bytes(in), false))));
        return this;
    }

    public PListDict addDataValue(String key, byte[] bytes) {
        elements.add(new PListStringValue(key, Charsets.toAsciiString(Base64.encodeBase64(bytes, false))));
        return this;
    }

    @Override
    public void write(XMLStreamWriter writer) throws XMLStreamException {
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

        for (PListElement pListElement : elements) {
            pListElement.write(writer);
        }

        writer.writeEndElement();
    }

}
