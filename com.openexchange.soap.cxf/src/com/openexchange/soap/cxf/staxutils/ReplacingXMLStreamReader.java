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

package com.openexchange.soap.cxf.staxutils;

import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * {@link ReplacingXMLStreamReader}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ReplacingXMLStreamReader extends StreamReaderDelegate {

    private final Stack<ReplacingElement> stack = new Stack<ReplacingElement>();
    private ReplacingElement current;

    public ReplacingXMLStreamReader(BindingOperationInfo bop, XMLStreamReader reader) {
        super(reader);
        QName name = super.getName();
        ReplacingElement method = new ReplacingElement(name, name);
        stack.push(method);
        method.setXmlSchema((XmlSchemaElement) bop.getOperationInfo().getInput().getMessagePart(0).getXmlSchema());
        current = null;
    }

    @Override
    public int next() throws XMLStreamException {
        int event = super.next();
        if (XMLStreamConstants.START_ELEMENT == event) {
            final QName name = super.getName();

            ReplacingElement element = new ReplacingElement(name);
            stack.push(element);

            final XmlSchemaElement schema;
            if (isGeneric(name)) {
                String typeName = findType();
                schema = getChildSchema(stack.get(stack.size() - 2), typeName, true);
            } else if (isEmptyURI(name)) {
                schema = getChildSchema(stack.get(stack.size() - 2), name.getLocalPart(), false);
            } else {
                schema = null;
            }

            final QName expected;
            if (null != schema) {
                element.setXmlSchema(schema);
                expected = schema.getQName();
                current = element;
            } else {
                expected = name;
                current = null;
            }
            element.setExpected(expected);
        } else if (XMLStreamConstants.END_ELEMENT == event && !stack.empty()) {
            current = stack.pop();
        } else {
            current = null;
        }
        return event;
    }

    @Override
    public String getLocalName() {
        return null == current ? super.getLocalName() : current.getExpected().getLocalPart().intern();
    }

    @Override
    public String getNamespaceURI() {
        return null == current ? super.getNamespaceURI() : current.getExpected().getNamespaceURI().intern();
    }

    private static XmlSchemaElement getChildSchema(ReplacingElement parent, String name, boolean isTypeName) {
        XmlSchemaElement schema = parent.getXmlSchema();
        if ((null != schema) && (schema.getSchemaType() instanceof XmlSchemaComplexType)) {
            XmlSchemaComplexType cplxType = (XmlSchemaComplexType) schema.getSchemaType();
            XmlSchemaSequence seq = (XmlSchemaSequence) cplxType.getParticle();
            if (null == seq) {
                // SOAP Class inheritance.
                XmlSchemaContentModel contentModel = cplxType.getContentModel();
                XmlSchemaComplexContentExtension extension = (XmlSchemaComplexContentExtension) contentModel.getContent();
                seq = (XmlSchemaSequence) extension.getParticle();
                XmlSchemaElement retval = findReplacer(parent, name, seq, true, isTypeName);
                if (null == retval) {
                    // Check for the attribute in the super type.
                    QName baseTypeName = extension.getBaseTypeName();
                    XmlSchemaType superType = cplxType.getParent().getParent().getTypeByQName(baseTypeName);
                    if (superType instanceof XmlSchemaComplexType) {
                        seq = (XmlSchemaSequence) ((XmlSchemaComplexType) superType).getParticle();
                        retval = findReplacer(parent, name, seq, true, isTypeName);
                    }
                    if (null != retval) {
                        return retval;
                    }
                } else {
                    return retval;
                }
            } else {
                return findReplacer(parent, name, seq, false, isTypeName);
            }
        }
        return null;
    }

    private static XmlSchemaElement findReplacer(ReplacingElement parent, String name, XmlSchemaSequence seq, boolean strict, boolean isTypeName) {
        int rememberPosition = parent.nextChildPosition();
        XmlSchemaElement retval = null;
        // First try to use the given type name. But with PHP this type name is "Struct".
        if (null != name) {
            retval = byName(parent, seq.getItems(), name);
            // If there is a single element that can be assigned through its name, then keep this scheme for the current hierarchy level of
            // the sent XML SOAP request. This indicates, the client is able to sent correctly named elements and not only generic ones.
            // Force him to use correctly named elements here. Otherwise a wrong order causes the wrong attribute to be assigned with wrong
            // values using the byPosition() method. See bug 24484.
            if (null != retval && !isTypeName) {
                parent.setOnlyWithName();
            }
        }
        if (null != retval || (null != name && strict) || parent.isOnlyWithName()) {
            return retval;
        }
        // If no child is found using the type name, fall back to child position because of PHP using "Struct" as type name.
        parent.setChildPosition(rememberPosition);
        return byPosition(parent, seq.getItems());
    }

    private static XmlSchemaElement byPosition(ReplacingElement parent, List<XmlSchemaSequenceMember> childs) {
        int pos = parent.nextChildPosition();
        if (childs.size() <= pos) {
            return null;
        }
        return (XmlSchemaElement) childs.get(pos);
    }

    private static XmlSchemaElement byName(ReplacingElement parent, List<XmlSchemaSequenceMember> childs, String name) {
        parent.resetChildPosition();
        for (XmlSchemaSequenceMember member : childs) {
            parent.nextChildPosition();
            XmlSchemaElement element = (XmlSchemaElement) member;
            String schemaTypeName = element.getSchemaTypeName().getLocalPart();
            String attributeName = element.getName();
            if (name.equals(schemaTypeName) || name.equals(attributeName)) {
                return element;
            }
        }
        return null;
    }

    private String findType() {
        if (getAttributeCount() == 0) {
            return null;
        }
        for (int i = 0; i < getAttributeCount(); i++) {
            String name = getAttributeLocalName(i);
            if ("type".equals(name)) {
                String value = getAttributeValue(i);
                int pos = value.indexOf(':');
                if (-1 == pos) {
                    return value;
                }
                return value.substring(pos + 1);
            }
        }
        return null;
    }

    private static final Pattern GENERIC_PATTERN = Pattern.compile("(?:c-gensym|param)\\d+");

    private static boolean isGeneric(final QName name) {
        String localPart = name.getLocalPart();
        return null == localPart ? false : GENERIC_PATTERN.matcher(localPart).matches();
    }

    private static boolean isEmptyURI(QName name) {
        return XMLConstants.NULL_NS_URI.equals(name.getNamespaceURI());
    }
}
