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

import java.util.ArrayList;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;
import com.openexchange.plist.xml.helper.DOMUtils;

/**
 * {@link W3CDOMStreamReader}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class W3CDOMStreamReader extends AbstractDOMStreamReader<Node, Node> {
    private Node content;

    private Document document;

    private W3CNamespaceContext context;

    private String sysId;

    /**
     * @param element
     */
    public W3CDOMStreamReader(final Element element) {
        super(new ElementFrame<Node, Node>(element, null));
        content = element;
        newFrame(getCurrentFrame());

        document = element.getOwnerDocument();
    }
    public W3CDOMStreamReader(final Element element, final String systemId) {
        this(element);
        sysId = systemId;
    }
    public W3CDOMStreamReader(final Document doc) {
        super(new ElementFrame<Node, Node>(doc, false) {
            @Override
            public boolean isDocument() {
                return true;
            }
        });
        document = doc;
    }
    public W3CDOMStreamReader(final DocumentFragment docfrag) {
        super(new ElementFrame<Node, Node>(docfrag, true) {
            @Override
            public boolean isDocumentFragment() {
                return true;
            }
        });
        document = docfrag.getOwnerDocument();
    }

    /**
     * Get the document associated with this stream.
     *
     * @return
     */
    public Document getDocument() {
        return document;
    }
    @Override
    public String getSystemId() {
        return sysId == null ? document.getDocumentURI() : sysId;
    }
    /**
     * Find name spaces declaration in atrributes and move them to separate
     * collection.
     */
    @Override
    protected final void newFrame(final ElementFrame<Node, Node> frame) {
        final Node element = getCurrentNode();
        frame.uris = new ArrayList<String>();
        frame.prefixes = new ArrayList<String>();
        frame.attributes = new ArrayList<Object>();

        if (context == null) {
            context = new W3CNamespaceContext();
        }
        if (element instanceof Element) {
            context.setElement((Element)element);
        }

        final NamedNodeMap nodes = element.getAttributes();

        String ePrefix = element.getPrefix();
        if (ePrefix == null) {
            ePrefix = "";
        }

        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                final Node node = nodes.item(i);
                String prefix = node.getPrefix();
                final String localName = node.getLocalName();
                final String value = node.getNodeValue();
                final String name = node.getNodeName();

                if (prefix == null) {
                    prefix = "";
                }

                if (name != null && "xmlns".equals(name)) {
                    frame.uris.add(value);
                    frame.prefixes.add("");
                } else if (prefix.length() > 0 && "xmlns".equals(prefix)) {
                    frame.uris.add(value);
                    frame.prefixes.add(localName);
                } else if (name.startsWith("xmlns:")) {
                    prefix = name.substring(6);
                    frame.uris.add(value);
                    frame.prefixes.add(prefix);
                } else {
                    frame.attributes.add(node);
                }
            }
        }
    }

    @Override
    protected void endElement() {
        super.endElement();
    }

    public final Node getCurrentNode() {
        return getCurrentFrame().element;
    }
    public final Element getCurrentElement() {
        return (Element)getCurrentFrame().element;
    }

    @Override
    protected ElementFrame<Node, Node> getChildFrame() {
        return new ElementFrame<Node, Node>(getCurrentFrame().currentChild,
                                getCurrentFrame());
    }

    @Override
    protected boolean hasMoreChildren() {
        if (getCurrentFrame().currentChild == null) {
            return getCurrentNode().getFirstChild() != null;
        }
        return getCurrentFrame().currentChild.getNextSibling() != null;
    }

    @Override
    protected int nextChild() {
        final ElementFrame<Node, Node> frame = getCurrentFrame();
        if (frame.currentChild == null) {
            content = getCurrentNode().getFirstChild();
        } else {
            content = frame.currentChild.getNextSibling();
        }

        frame.currentChild = content;
        switch (content.getNodeType()) {
        case Node.ELEMENT_NODE:
            return START_ELEMENT;
        case Node.TEXT_NODE:
            return CHARACTERS;
        case Node.COMMENT_NODE:
            return COMMENT;
        case Node.CDATA_SECTION_NODE:
            return CDATA;
        case Node.ENTITY_REFERENCE_NODE:
            return ENTITY_REFERENCE;
        default:
            throw new IllegalStateException("Found type: " + content.getClass().getName());
        }
    }

    @Override
    public String getElementText() throws XMLStreamException {
        final String result = DOMUtils.getRawContent(content);

        final ElementFrame<Node, Node> frame = getCurrentFrame();
        frame.ended = true;
        currentEvent = END_ELEMENT;
        endElement();

        // we should not return null according to the StAx API javadoc
        return result != null ? result : "";
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        ElementFrame<Node, Node> frame = getCurrentFrame();

        while (null != frame) {
            final int index = frame.prefixes.indexOf(prefix);
            if (index != -1) {
                return frame.uris.get(index);
            }

            if (frame.parent == null && frame.getElement() instanceof Element) {
                return ((Element)frame.getElement()).lookupNamespaceURI(prefix);
            }
            frame = frame.parent;
        }

        return null;
    }

    @Override
    public String getAttributeValue(final String ns, final String local) {
        Attr at;
        if (ns == null || ns.equals("")) {
            at = getCurrentElement().getAttributeNode(local);
        } else {
            at = getCurrentElement().getAttributeNodeNS(ns, local);
        }

        if (at == null) {
            return null;
        }
        return at.getNodeValue();
    }

    @Override
    public int getAttributeCount() {
        return getCurrentFrame().attributes.size();
    }

    Attr getAttribute(final int i) {
        return (Attr)getCurrentFrame().attributes.get(i);
    }

    private String getLocalName(final Attr attr) {

        String name = attr.getLocalName();
        if (name == null) {
            name = attr.getNodeName();
        }
        return name;
    }

    @Override
    public QName getAttributeName(final int i) {
        final Attr at = getAttribute(i);

        final String prefix = at.getPrefix();
        final String ln = getLocalName(at);
        // at.getNodeName();
        final String ns = at.getNamespaceURI();

        if (prefix == null) {
            return new QName(ns, ln);
        } else {
            return new QName(ns, ln, prefix);
        }
    }

    @Override
    public String getAttributeNamespace(final int i) {
        return getAttribute(i).getNamespaceURI();
    }

    @Override
    public String getAttributeLocalName(final int i) {
        final Attr attr = getAttribute(i);
        return getLocalName(attr);
    }

    @Override
    public String getAttributePrefix(final int i) {
        return getAttribute(i).getPrefix();
    }

    @Override
    public String getAttributeType(final int i) {
        final Attr attr = getAttribute(i);
        if (attr.isId()) {
            return "ID";
        }
        TypeInfo schemaType = null;
        try {
            schemaType = attr.getSchemaTypeInfo();
        } catch (final Throwable t) {
            //DOM level 2?
            schemaType = null;
        }
        return (schemaType == null) ? "CDATA"
            : schemaType.getTypeName() == null ? "CDATA" : schemaType.getTypeName();
    }


    @Override
    public String getAttributeValue(final int i) {
        return getAttribute(i).getValue();
    }

    @Override
    public boolean isAttributeSpecified(final int i) {
        return getAttribute(i).getValue() != null;
    }

    @Override
    public int getNamespaceCount() {
        return getCurrentFrame().prefixes.size();
    }

    @Override
    public String getNamespacePrefix(final int i) {
        return getCurrentFrame().prefixes.get(i);
    }

    @Override
    public String getNamespaceURI(final int i) {
        return getCurrentFrame().uris.get(i);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return context;
    }

    @Override
    public String getText() {
        if (content instanceof Text) {
            return ((Text)content).getData();
        } else if (content instanceof Comment) {
            return ((Comment)content).getData();
        }
        return DOMUtils.getRawContent(getCurrentNode());
    }

    @Override
    public char[] getTextCharacters() {
        return getText().toCharArray();
    }

    @Override
    public int getTextStart() {
        return 0;
    }

    @Override
    public int getTextLength() {
        return getText().length();
    }

    @Override
    public String getEncoding() {
        return null;
    }

    @Override
    public QName getName() {
        final Node el = getCurrentNode();

        final String prefix = getPrefix();
        final String ln = getLocalName();

        return prefix == null ? new QName(el.getNamespaceURI(), ln) : new QName(el.getNamespaceURI(), ln, prefix);
    }

    @Override
    public String getLocalName() {
        String ln = getCurrentNode().getLocalName();
        if (ln == null) {
            ln = getCurrentNode().getNodeName();
            if (ln.indexOf(":") != -1) {
                ln = ln.substring(ln.indexOf(":") + 1);
            }
        }
        return ln;
    }

    @Override
    public String getNamespaceURI() {
        String ln = getCurrentNode().getLocalName();
        if (ln == null) {
            ln = getCurrentNode().getNodeName();
            if (ln.indexOf(":") == -1) {
                ln = getNamespaceURI("");
            } else {
                ln = getNamespaceURI(ln.substring(0, ln.indexOf(":")));
            }
            return ln;
        }
        return getCurrentNode().getNamespaceURI();
    }

    @Override
    public String getPrefix() {
        String prefix = getCurrentNode().getPrefix();
        if (prefix == null) {
            final String nodeName = getCurrentNode().getNodeName();
            if (nodeName.indexOf(":") != -1) {
                prefix = nodeName.substring(0, nodeName.indexOf(":"));
            }  else {
                prefix = "";
            }
        }
        return prefix;
    }

    @Override
    public String getPITarget() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPIData() {
        throw new UnsupportedOperationException();
    }
    @Override
    public Location getLocation() {
        try {
            final Object o = getCurrentNode().getUserData("location");
            if (o instanceof Location) {
                return (Location)o;
            }
        } catch (final Throwable ex) {
            //ignore, probably not DOM level 3
        }
        return super.getLocation();
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
