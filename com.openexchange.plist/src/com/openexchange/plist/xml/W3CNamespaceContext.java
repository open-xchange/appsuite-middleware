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
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * {@link W3CNamespaceContext} - Copy of <code>org.apache.cxf.staxutils.W3CNamespaceContext</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class W3CNamespaceContext implements NamespaceContext {

    private Element currentNode;
    private NamespaceContext outNamespaceContext;

    /**
     * Initializes a new {@link W3CNamespaceContext}.
     */
    public W3CNamespaceContext() {
        super();
    }

    public W3CNamespaceContext(final Element el) {
        currentNode = el;
    }

    public void setOutNamespaceContext(final NamespaceContext context) {
        outNamespaceContext = context;
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        String name = prefix;
        name = name.length() == 0 ? "xmlns" : "xmlns:" + prefix;
        return getNamespaceURI(currentNode, name);
    }

    private String getNamespaceURI(final Element e, final String name) {
        if (e == null) {
            return null;
        }
        // check the outside namespace URI
        if (outNamespaceContext != null) {
            final String result = outNamespaceContext.getNamespaceURI(name);
            if (result != null) {
                return result;
            }
        }

        final Attr attr = e.getAttributeNode(name);
        if (attr == null) {
            final Node n = e.getParentNode();
            if (n instanceof Element && n != e) {
                return getNamespaceURI((Element) n, name);
            }
        } else {
            return attr.getValue();
        }

        return null;
    }

    @Override
    public String getPrefix(final String uri) {
        return getPrefix(currentNode, uri);
    }

    private String getPrefix(final Element e, final String uri) {
        if (e == null) {
            return null;
        }
        // check the outside namespace URI
        if (outNamespaceContext != null) {
            final String result = outNamespaceContext.getPrefix(uri);
            if (result != null) {
                return result;
            }
        }

        final NamedNodeMap attributes = e.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                final Attr a = (Attr) attributes.item(i);

                final String val = a.getValue();
                if (val != null && val.equals(uri)) {
                    final String name = a.getLocalName();
                    return "xmlns".equals(name) ? "" : name;
                }
            }
        }

        final Node n = e.getParentNode();
        if (n instanceof Element && n != e) {
            return getPrefix((Element) n, uri);
        }

        return null;
    }

    @Override
    public Iterator<String> getPrefixes(final String uri) {
        final List<String> prefixes = new ArrayList<String>();

        final String prefix = getPrefix(uri);
        if (prefix != null) {
            prefixes.add(prefix);
        }

        return prefixes.iterator();
    }

    public Element getElement() {
        return currentNode;
    }

    public void setElement(final Element node) {
        currentNode = node;
    }

}
