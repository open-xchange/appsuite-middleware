/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.openexchange.plist.xml;

import java.util.Collections;
import java.util.Map;
import java.util.Stack;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.openexchange.plist.xml.helper.MapNamespaceContext;
import com.openexchange.plist.xml.helper.XMLUtils;


/**
 * {@link W3CDOMStreamWriter} - Copy of <code>org.apache.cxf.staxutils.W3CDOMStreamWriter</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class W3CDOMStreamWriter implements XMLStreamWriter {

    static final String XML_NS = "http://www.w3.org/2000/xmlns/";
    private final Stack<Node> stack = new Stack<Node>();
    private final Document document;
    private Node currentNode;
    private NamespaceContext context = new W3CNamespaceContext();
    private boolean nsRepairing;
    private Map<String, Object> properties = Collections.emptyMap();

    public W3CDOMStreamWriter() throws ParserConfigurationException {
        document = XMLUtils.newDocument();
    }

    public W3CDOMStreamWriter(final DocumentBuilder builder) {
        document = builder.newDocument();
    }

    public W3CDOMStreamWriter(final Document document) {
        this.document = document;
    }
    public W3CDOMStreamWriter(final DocumentFragment frag) {
        document = frag.getOwnerDocument();
        currentNode = frag;
    }

    public W3CDOMStreamWriter(final Element e) {
        document = e.getOwnerDocument();

        currentNode = e;
        ((W3CNamespaceContext)context).setElement(e);
    }
    public W3CDOMStreamWriter(final Document owner, final Element e) {
        document = owner;
        currentNode = e;
        ((W3CNamespaceContext)context).setElement(e);
    }

    public Element getCurrentNode() {
        if (currentNode instanceof Element) {
            return (Element)currentNode;
        }
        return null;
    }
    public DocumentFragment getCurrentFragment() {
        if (currentNode instanceof DocumentFragment) {
            return (DocumentFragment)currentNode;
        }
        return null;
    }

    public void setNsRepairing(final boolean b) {
        nsRepairing = b;
    }
    public boolean isNsRepairing() {
        return nsRepairing;
    }
    public void setProperties(final Map<String, Object> properties) {
        this.properties = properties;
    }

    public Document getDocument() {
        return document;
    }

    @Override
    public void writeStartElement(final String local) throws XMLStreamException {
        createAndAddElement(null, local, null);
    }

    protected void newChild(final Element element) {
        setChild(element, true);
    }
    protected void setChild(final Element element, final boolean append) {
        if (currentNode != null) {
            stack.push(currentNode);
            if (append) {
                currentNode.appendChild(element);
            }
        } else {
            if (append) {
                document.appendChild(element);
            }
        }
        if (!(context instanceof W3CNamespaceContext)) {
            // set the outside namespace context
            final W3CNamespaceContext childContext = new W3CNamespaceContext();
            childContext.setOutNamespaceContext(context);
            context = childContext;
        }
        ((W3CNamespaceContext)context).setElement(element);
        currentNode = element;
    }

    @Override
    public void writeStartElement(final String namespace, final String local) throws XMLStreamException {
        createAndAddElement(null, local, namespace);
    }

    @Override
    public void writeStartElement(final String prefix, final String local, final String namespace) throws XMLStreamException {
        if (prefix == null || prefix.equals("")) {
            writeStartElement(namespace, local);
        } else {
            createAndAddElement(prefix, local, namespace);
            if (nsRepairing
                && !prefix.equals(getNamespaceContext().getPrefix(namespace))) {
                writeNamespace(prefix, namespace);
            }
        }
    }
    protected void createAndAddElement(final String prefix, final String local, final String namespace) {
        if (prefix == null) {
            if (namespace == null) {
                newChild(document.createElementNS(null, local));
            } else {
                newChild(document.createElementNS(namespace, local));
            }
        } else {
            newChild(document.createElementNS(namespace, prefix + ":" + local));
        }
    }

    @Override
    public void writeEmptyElement(final String namespace, final String local) throws XMLStreamException {
        writeStartElement(namespace, local);
        writeEndElement();
    }

    @Override
    public void writeEmptyElement(final String prefix, final String local, final String namespace) throws XMLStreamException {
        writeStartElement(prefix, local, namespace);
        writeEndElement();
    }

    @Override
    public void writeEmptyElement(final String local) throws XMLStreamException {
        writeStartElement(local);
        writeEndElement();
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        if (stack.size() > 0) {
            currentNode = stack.pop();
        } else {
            currentNode = null;
        }
        if (context instanceof W3CNamespaceContext && currentNode instanceof Element) {
            ((W3CNamespaceContext)context).setElement((Element)currentNode);
        } else if (context instanceof MapNamespaceContext) {
            ((MapNamespaceContext) context).setTargetNode(currentNode);
        }
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        // Empty method
    }

    @Override
    public void writeAttribute(final String local, final String value) throws XMLStreamException {
        Attr a;
        if (local.startsWith("xmlns") && (local.length() == 5 || local.charAt(5) == ':')) {
            a = document.createAttributeNS(XML_NS, local);
        } else {
            a = document.createAttributeNS(null, local);
        }
        a.setValue(value);
        ((Element)currentNode).setAttributeNode(a);
    }

    @Override
    public void writeAttribute(final String prefix, final String namespace, String local, final String value)
        throws XMLStreamException {
        if (prefix.length() > 0) {
            local = prefix + ":" + local;
        }

        final Attr a = document.createAttributeNS(namespace, local);
        a.setValue(value);
        ((Element)currentNode).setAttributeNodeNS(a);
        if (nsRepairing
            && !prefix.equals(getNamespaceContext().getPrefix(namespace))) {
            writeNamespace(prefix, namespace);
        }
    }

    @Override
    public void writeAttribute(final String namespace, final String local, final String value) throws XMLStreamException {
        final Attr a = document.createAttributeNS(namespace, local);
        a.setValue(value);
        ((Element)currentNode).setAttributeNodeNS(a);
    }

    @Override
    public void writeNamespace(final String prefix, final String namespace) throws XMLStreamException {
        if (prefix.length() == 0) {
            writeDefaultNamespace(namespace);
        } else {
            final Attr attr = document.createAttributeNS(XML_NS, "xmlns:" + prefix);
            attr.setValue(namespace);
            ((Element)currentNode).setAttributeNodeNS(attr);
        }
    }

    @Override
    public void writeDefaultNamespace(final String namespace) throws XMLStreamException {
        final Attr attr = document.createAttributeNS(XML_NS, "xmlns");
        attr.setValue(namespace);
        ((Element)currentNode).setAttributeNodeNS(attr);
    }

    @Override
    public void writeComment(final String value) throws XMLStreamException {
        if (currentNode == null) {
            document.appendChild(document.createComment(value));
        } else {
            currentNode.appendChild(document.createComment(value));
        }
    }

    @Override
    public void writeProcessingInstruction(final String target) throws XMLStreamException {
        if (currentNode == null) {
            document.appendChild(document.createProcessingInstruction(target, null));
        } else {
            currentNode.appendChild(document.createProcessingInstruction(target, null));
        }
    }

    @Override
    public void writeProcessingInstruction(final String target, final String data) throws XMLStreamException {
        if (currentNode == null) {
            document.appendChild(document.createProcessingInstruction(target, data));
        } else {
            currentNode.appendChild(document.createProcessingInstruction(target, data));
        }
    }

    @Override
    public void writeCData(final String data) throws XMLStreamException {
        currentNode.appendChild(document.createCDATASection(data));
    }

    @Override
    public void writeDTD(final String arg0) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeEntityRef(final String ref) throws XMLStreamException {
        currentNode.appendChild(document.createEntityReference(ref));
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        // Empty method
    }

    @Override
    public void writeStartDocument(final String version) throws XMLStreamException {
        try {
            document.setXmlVersion(version);
        } catch (final Exception ex) {
            //ignore - likely not DOM level 3
        }
    }

    @Override
    public void writeStartDocument(final String encoding, final String version) throws XMLStreamException {
        try {
            document.setXmlVersion(version);
        } catch (final Exception ex) {
            //ignore - likely not DOM level 3
        }
    }

    @Override
    public void writeCharacters(final String text) throws XMLStreamException {
        currentNode.appendChild(document.createTextNode(text));
    }

    @Override
    public void writeCharacters(final char[] text, final int start, final int len) throws XMLStreamException {
        writeCharacters(new String(text, start, len));
    }

    @Override
    public String getPrefix(final String uri) throws XMLStreamException {
        return context == null ? null : context.getPrefix(uri);
    }

    @Override
    public void setPrefix(final String arg0, final String arg1) throws XMLStreamException {
        // Empty method
    }

    @Override
    public void setDefaultNamespace(final String arg0) throws XMLStreamException {
        // Empty method
    }

    @Override
    public void setNamespaceContext(final NamespaceContext ctx) throws XMLStreamException {
        context = ctx;
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return context;
    }

    @Override
    public Object getProperty(final String prop) throws IllegalArgumentException {
        return properties.get(prop);
    }

    @Override
    public void close() throws XMLStreamException {
        // Empty method
    }

    @Override
    public void flush() throws XMLStreamException {
        // Empty method
    }

    @Override
    public String toString() {
        if (document == null) {
            return "<null>";
        }
        if (document.getDocumentElement() == null) {
            return "<null document element>";
        }
        try {
            return StaxUtils.toString(document);
        } catch (final XMLStreamException e) {
            return super.toString();
        } catch (final Throwable t) {
            t.printStackTrace();
            return super.toString();
        }
    }

}
