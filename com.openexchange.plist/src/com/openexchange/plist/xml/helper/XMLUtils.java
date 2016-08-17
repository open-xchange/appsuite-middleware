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

package com.openexchange.plist.xml.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.openexchange.java.Strings;

/**
 * {@link XMLUtils} - Copy of <code>org.apache.cxf.helpers.XMLUtils</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public final class XMLUtils {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(XMLUtils.class);

    /**
     * Initializes a new {@link XMLUtils}.
     */
    private XMLUtils() {
        super();
    }

    private static final Map<ClassLoader, DocumentBuilderFactory> DOCUMENT_BUILDER_FACTORIES = Collections.synchronizedMap(new WeakHashMap<ClassLoader, DocumentBuilderFactory>());

    private static final Map<ClassLoader, TransformerFactory> TRANSFORMER_FACTORIES = Collections.synchronizedMap(new WeakHashMap<ClassLoader, TransformerFactory>());

    private static final Pattern XML_ESCAPE_CHARS = Pattern.compile("[\"'&<>]");
    private static final Map<String, String> XML_ENCODING_TABLE;
    static {
        XML_ENCODING_TABLE = new HashMap<String, String>();
        XML_ENCODING_TABLE.put("\"", "&quot;");
        XML_ENCODING_TABLE.put("'", "&apos;");
        XML_ENCODING_TABLE.put("<", "&lt;");
        XML_ENCODING_TABLE.put(">", "&gt;");
        XML_ENCODING_TABLE.put("&", "&amp;");
    }

    private static TransformerFactory getTransformerFactory() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = XMLUtils.class.getClassLoader();
        }
        if (loader == null) {
            return TransformerFactory.newInstance();
        }
        TransformerFactory factory = TRANSFORMER_FACTORIES.get(loader);
        if (factory == null) {
            factory = TransformerFactory.newInstance();
            TRANSFORMER_FACTORIES.put(loader, factory);
        }
        return factory;
    }

    private static DocumentBuilderFactory getDocumentBuilderFactory() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = XMLUtils.class.getClassLoader();
        }
        if (loader == null) {
            return safeDbf(DocumentBuilderFactory.newInstance());
        }
        DocumentBuilderFactory factory = DOCUMENT_BUILDER_FACTORIES.get(loader);
        if (factory == null) {
            factory = safeDbf(DocumentBuilderFactory.newInstance());
            factory.setNamespaceAware(true);
            DOCUMENT_BUILDER_FACTORIES.put(loader, factory);
        }
        return factory;
    }

    private static DocumentBuilderFactory safeDbf(DocumentBuilderFactory dbf) {
        if (null == dbf) {
            return dbf;
        }

        // From http://stackoverflow.com/questions/26488319/how-to-prevent-xml-injection-like-xml-bomb-and-xxe-attack
        String FEATURE = null;
        try {
            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
            // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
            FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
            dbf.setFeature(FEATURE, true);

            // If you can't completely disable DTDs, then at least do the following:
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
            FEATURE = "http://xml.org/sax/features/external-general-entities";
            dbf.setFeature(FEATURE, false);

            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
            FEATURE = "http://xml.org/sax/features/external-parameter-entities";
            dbf.setFeature(FEATURE, false);

            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks" (see reference below)
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);

            // And, per Timothy Morgan: "If for some reason support for inline DOCTYPEs are a requirement, then
            // ensure the entity settings are disabled (as shown above) and beware that SSRF attacks
            // (http://cwe.mitre.org/data/definitions/918.html) and denial
            // of service attacks (such as billion laughs or decompression bombs via "jar:") are a risk."

        } catch (ParserConfigurationException e) {
            LOGGER.warn("ParserConfigurationException was thrown. The feature '{}' is probably not supported by your XML processor.", FEATURE, e);
        }

        return dbf;
    }

    public static Transformer newTransformer() throws TransformerConfigurationException {
        return getTransformerFactory().newTransformer();
    }

    public static Transformer newTransformer(final int indent) throws TransformerConfigurationException {
        if (indent > 0) {
            final TransformerFactory f = TransformerFactory.newInstance();
            try {
                // sun way of setting indent
                f.setAttribute("indent-number", Integer.toString(indent));
            } catch (final Throwable t) {
                // ignore
            }
            return f.newTransformer();
        }
        return getTransformerFactory().newTransformer();
    }

    public static DocumentBuilder getParser() throws ParserConfigurationException {
        return getDocumentBuilderFactory().newDocumentBuilder();
    }

    public static Document parse(final InputSource is) throws ParserConfigurationException, SAXException, IOException {
        return getParser().parse(is);
    }

    public static Document parse(final File is) throws ParserConfigurationException, SAXException, IOException {
        return getParser().parse(is);
    }

    public static Document parse(final InputStream in) throws ParserConfigurationException, SAXException, IOException {
        if (in == null) {
            LOGGER.debug("XMLUtils trying to parse a null inputstream");
        }
        return getParser().parse(in);
    }

    public static Document parse(final String in) throws ParserConfigurationException, SAXException, IOException {
        return parse(in.getBytes());
    }

    public static Document parse(final byte[] in) throws ParserConfigurationException, SAXException, IOException {
        if (in == null) {
            LOGGER.debug("XMLUtils trying to parse a null bytes");
            return null;
        }
        return getParser().parse(new ByteArrayInputStream(in));
    }

    public static Document newDocument() throws ParserConfigurationException {
        return getParser().newDocument();
    }

    public static void writeTo(final Node node, final OutputStream os) {
        writeTo(new DOMSource(node), os);
    }

    public static void writeTo(final Node node, final OutputStream os, final int indent) {
        writeTo(new DOMSource(node), os, indent);
    }

    public static void writeTo(final Source src, final OutputStream os) {
        writeTo(src, os, -1);
    }

    public static void writeTo(final Node node, final Writer os) {
        writeTo(new DOMSource(node), os);
    }

    public static void writeTo(final Node node, final Writer os, final int indent) {
        writeTo(new DOMSource(node), os, indent);
    }

    public static void writeTo(final Source src, final Writer os) {
        writeTo(src, os, -1);
    }

    public static void writeTo(final Source src, final OutputStream os, final int indent) {
        String enc = null;
        if (src instanceof DOMSource && ((DOMSource) src).getNode() instanceof Document) {
            try {
                enc = ((Document) ((DOMSource) src).getNode()).getXmlEncoding();
            } catch (final Exception ex) {
                // ignore - not DOM level 3
            }
        }
        writeTo(src, os, indent, enc, "no");
    }

    public static void writeTo(final Source src, final Writer os, final int indent) {
        String enc = null;
        if (src instanceof DOMSource && ((DOMSource) src).getNode() instanceof Document) {
            try {
                enc = ((Document) ((DOMSource) src).getNode()).getXmlEncoding();
            } catch (final Exception ex) {
                // ignore - not DOM level 3
            }
        }
        writeTo(src, os, indent, enc, "no");
    }

    public static void writeTo(final Source src, final OutputStream os, final int indent, String charset, final String omitXmlDecl) {
        Transformer it;
        try {
            if (Strings.isEmpty(charset)) {
                charset = "utf-8";
            }

            it = newTransformer(indent);
            it.setOutputProperty(OutputKeys.METHOD, "xml");
            if (indent > -1) {
                it.setOutputProperty(OutputKeys.INDENT, "yes");
                it.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));
            }
            it.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDecl);
            it.setOutputProperty(OutputKeys.ENCODING, charset);
            it.transform(src, new StreamResult(os));
        } catch (final TransformerException e) {
            throw new RuntimeException("Failed to configure TRaX", e);
        }
    }

    public static void writeTo(final Source src, final Writer os, final int indent, String charset, final String omitXmlDecl) {
        Transformer it;
        try {
            if (Strings.isEmpty(charset)) {
                charset = "utf-8";
            }

            it = newTransformer(indent);
            it.setOutputProperty(OutputKeys.METHOD, "xml");
            if (indent > -1) {
                it.setOutputProperty(OutputKeys.INDENT, "yes");
                it.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));
            }
            it.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDecl);
            it.setOutputProperty(OutputKeys.ENCODING, charset);
            it.transform(src, new StreamResult(os));
        } catch (final TransformerException e) {
            throw new RuntimeException("Failed to configure TRaX", e);
        }
    }

    public static String toString(final Source source) throws TransformerException, IOException {
        return toString(source, null);
    }

    public static String toString(final Source source, Properties props) throws TransformerException, IOException {
        final StringWriter bos = new StringWriter();
        final StreamResult sr = new StreamResult(bos);
        final Transformer trans = newTransformer();
        if (props == null) {
            props = new Properties();
            props.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        trans.setOutputProperties(props);
        trans.transform(source, sr);
        bos.close();
        return bos.toString();
    }

    public static String toString(final Node node, final int indent) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeTo(node, out, indent);
        return out.toString();
    }

    public static String toString(final Node node) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeTo(node, out);
        return out.toString();
    }

    public static void printDOM(final Node node) {
        printDOM("", node);
    }

    public static void printDOM(final String words, final Node node) {
        System.out.println(words);
        System.out.println(toString(node));
    }

    public static Attr getAttribute(final Element el, final String attrName) {
        return el.getAttributeNode(attrName);
    }

    public static void replaceAttribute(final Element element, final String attr, final String value) {
        if (element.hasAttribute(attr)) {
            element.removeAttribute(attr);
        }
        element.setAttribute(attr, value);
    }

    public static boolean hasAttribute(final Element element, final String value) {
        final NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            final Node node = attributes.item(i);
            if (value.equals(node.getNodeValue())) {
                return true;
            }
        }
        return false;
    }

    public static void printAttributes(final Element element) {
        final NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            final Node node = attributes.item(i);
            System.err.println("## prefix=" + node.getPrefix() + " localname:" + node.getLocalName() + " value=" + node.getNodeValue());
        }
    }

    public static QName getNamespace(final Map<String, String> namespaces, final String str, final String defaultNamespace) {
        String prefix = null;
        String localName = null;

        final StringTokenizer tokenizer = new StringTokenizer(str, ":");
        if (tokenizer.countTokens() == 2) {
            prefix = tokenizer.nextToken();
            localName = tokenizer.nextToken();
        } else if (tokenizer.countTokens() == 1) {
            localName = tokenizer.nextToken();
        }

        String namespceURI = defaultNamespace;
        if (prefix != null) {
            namespceURI = namespaces.get(prefix);
        }
        return new QName(namespceURI, localName);
    }

    public static void generateXMLFile(final Element element, final Writer writer) {
        try {
            final Transformer it = newTransformer();

            it.setOutputProperty(OutputKeys.METHOD, "xml");
            it.setOutputProperty(OutputKeys.INDENT, "yes");
            it.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            it.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            it.transform(new DOMSource(element), new StreamResult(writer));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static Element createElementNS(final Node node, final QName name) {
        return createElementNS(node.getOwnerDocument(), name.getNamespaceURI(), name.getLocalPart());
    }

    public static Element createElementNS(final Document root, final QName name) {
        return createElementNS(root, name.getNamespaceURI(), name.getLocalPart());
    }

    public static Element createElementNS(final Document root, final String namespaceURI, final String qualifiedName) {
        return root.createElementNS(namespaceURI, qualifiedName);
    }

    public static Text createTextNode(final Document root, final String data) {
        return root.createTextNode(data);
    }

    public static Text createTextNode(final Node node, final String data) {
        return createTextNode(node.getOwnerDocument(), data);
    }

    public static void removeContents(final Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            parent.removeChild(node);
            node = node.getNextSibling();
        }
    }

    public static InputStream getInputStream(final Document doc) throws Exception {
        DOMImplementationLS impl = null;
        final DOMImplementation docImpl = doc.getImplementation();
        // Try to get the DOMImplementation from doc first before
        // defaulting to the sun implementation.
        if (docImpl != null && docImpl.hasFeature("LS", "3.0")) {
            impl = (DOMImplementationLS) docImpl.getFeature("LS", "3.0");
        } else {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            if (impl == null) {
                System.setProperty(DOMImplementationRegistry.PROPERTY, "com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl");
                registry = DOMImplementationRegistry.newInstance();
                impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            }
        }
        final LSOutput output = impl.createLSOutput();
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        output.setByteStream(byteArrayOutputStream);
        final LSSerializer writer = impl.createLSSerializer();
        writer.write(doc, output);
        final byte[] buf = byteArrayOutputStream.toByteArray();
        return new ByteArrayInputStream(buf);
    }

    public static Element fetchElementByNameAttribute(final Element parent, final String targetName, final String nameValue) {

        final List<Element> elemList = DOMUtils.findAllElementsByTagName(parent, targetName);
        for (final Element elem : elemList) {
            if (elem.getAttribute("name").equals(nameValue)) {
                return elem;
            }
        }
        return null;
    }

    public static QName getQName(final String value, final Node node) {
        if (value == null) {
            return null;
        }

        final int index = value.indexOf(":");

        if (index == -1) {
            return new QName(value);
        }

        final String prefix = value.substring(0, index);
        final String localName = value.substring(index + 1);
        final String ns = node.lookupNamespaceURI(prefix);

        if (ns == null || localName == null) {
            throw new RuntimeException("Invalid QName in mapping: " + value);
        }

        return new QName(ns, localName, prefix);
    }

    public static Node fromSource(final Source src) throws Exception {

        final Transformer trans = TransformerFactory.newInstance().newTransformer();
        final DOMResult res = new DOMResult();
        trans.transform(src, res);
        return res.getNode();
    }

    public static QName convertStringToQName(final String expandedQName) {
        return convertStringToQName(expandedQName, "");
    }

    public static QName convertStringToQName(final String expandedQName, final String prefix) {
        final int ind1 = expandedQName.indexOf('{');
        if (ind1 != 0) {
            return new QName(expandedQName);
        }

        final int ind2 = expandedQName.indexOf('}');
        if (ind2 <= ind1 + 1 || ind2 >= expandedQName.length() - 1) {
            return null;
        }
        final String ns = expandedQName.substring(ind1 + 1, ind2);
        final String localName = expandedQName.substring(ind2 + 1);
        return new QName(ns, localName, prefix);
    }

    public static Set<QName> convertStringsToQNames(final List<String> expandedQNames) {
        Set<QName> dropElements = Collections.emptySet();
        if (expandedQNames != null) {
            dropElements = new LinkedHashSet<QName>(expandedQNames.size());
            for (final String val : expandedQNames) {
                dropElements.add(XMLUtils.convertStringToQName(val));
            }
        }
        return dropElements;
    }

    public static String xmlEncode(final String value) {
        final Matcher m = XML_ESCAPE_CHARS.matcher(value);
        if (!m.find()) {
            return value;
        }
        int i = 0;
        final StringBuilder sb = new StringBuilder();
        do {
            final String replacement = XML_ENCODING_TABLE.get(m.group());
            sb.append(value.substring(i, m.start()));
            sb.append(replacement);
            i = m.end();
        } while (m.find());
        sb.append(value.substring(i, value.length()));
        return sb.toString();
    }

}
