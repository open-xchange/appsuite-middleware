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

package com.openexchange.plist.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import com.openexchange.java.Strings;

/**
 * {@link StaxUtils}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public final class StaxUtils {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StaxUtils.class);

    /**
     * Initializes a new {@link StaxUtils}.
     */
    private StaxUtils() {
        super();
    }

    private static final BlockingQueue<XMLInputFactory> NS_AWARE_INPUT_FACTORY_POOL;
    private static final XMLInputFactory SAFE_INPUT_FACTORY;
    private static final BlockingQueue<XMLOutputFactory> OUTPUT_FACTORY_POOL;
    private static final XMLOutputFactory SAFE_OUTPUT_FACTORY;

    private static final String XML_NS = "http://www.w3.org/2000/xmlns/";
    private static final String DEF_PREFIXES[] = new String[] {
        "ns1".intern(), "ns2".intern(), "ns3".intern(), "ns4".intern(), "ns5".intern(), "ns6".intern(), "ns7".intern(), "ns8".intern(),
        "ns9".intern() };

    private static int getInteger(final String prop, final int def) {
        try {
            final String s = System.getProperty(prop);
            if (Strings.isEmpty(s)) {
                return def;
            }
            int i = Integer.parseInt(s);
            if (i < 0) {
                i = def;
            }
            return i;
        } catch (final Exception t) {
            // ignore
        }
        return def;
    }

    private static volatile int innerElementLevelThreshold = 100;
    private static volatile int innerElementCountThreshold = 50000;
    private static final int maxAttributeCount = 500;
    private static final int maxAttributeSize = 64 * 1024; // 64K per attribute, likely just "list" will hit
    private static final int maxTextLength = 128 * 1024 * 1024; // 128M - more than this should DEFINITLEY use MTOM
    private static final long maxElementCount = Long.MAX_VALUE;
    private static final long maxXMLCharacters = Long.MAX_VALUE;

    private static final boolean allowInsecureParser = false;

    static {
        final int i = getInteger("org.apache.cxf.staxutils.pool-size", 20);

        NS_AWARE_INPUT_FACTORY_POOL = new ArrayBlockingQueue<XMLInputFactory>(i);
        OUTPUT_FACTORY_POOL = new ArrayBlockingQueue<XMLOutputFactory>(i);

        XMLInputFactory xif = null;
        try {
            xif = createXMLInputFactory(true);
            final String xifClassName = xif.getClass().getName();
            if (!xifClassName.contains("ctc.wstx") && !xifClassName.contains("xml.xlxp") && !xifClassName.contains("xml.xlxp2") && !xifClassName.contains("bea.core")) {
                xif = null;
            }
        } catch (final Throwable t) {
            // ignore, can always drop down to the pooled factories
            xif = null;
        }
        SAFE_INPUT_FACTORY = xif;

        final XMLOutputFactory xof = XMLOutputFactory.newInstance();
        final String xofClassName = xof.getClass().getName();
        if (xofClassName.contains("ctc.wstx") || xofClassName.contains("xml.xlxp") || xofClassName.contains("xml.xlxp2") || xofClassName.contains("bea.core")) {
            SAFE_OUTPUT_FACTORY = xof;
        } else {
            SAFE_OUTPUT_FACTORY = null;
        }

    }

    // ------------------------------------------------------------------------------------------------------------------------ //

    /**
     * Load a class with a given name.
     * <p/>
     * It will try to load the class in the following order:
     * <ul>
     * <li>From Thread.currentThread().getContextClassLoader()
     * <li>Using the basic Class.forName()
     * <li>From ClassLoaderUtil.class.getClassLoader()
     * <li>From the callingClass.getClassLoader()
     * </ul>
     *
     * @param className The name of the class to load
     * @param callingClass The Class object of the calling object
     * @throws ClassNotFoundException If the class cannot be found anywhere.
     */
    public static Class<?> loadClass(final String className, final Class<?> callingClass) throws ClassNotFoundException {
        try {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();

            if (cl != null) {
                return cl.loadClass(className);
            }
        } catch (final ClassNotFoundException e) {
            // ignore
        }
        return loadClass2(className, callingClass);
    }

    public static <T> Class<? extends T> loadClass(final String className, final Class<?> callingClass, final Class<T> type) throws ClassNotFoundException {
        try {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();

            if (cl != null) {
                return cl.loadClass(className).asSubclass(type);
            }
        } catch (final ClassNotFoundException e) {
            // ignore
        }
        return loadClass2(className, callingClass).asSubclass(type);
    }

    private static Class<?> loadClass2(final String className, final Class<?> callingClass) throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException ex) {
            try {
                if (StaxUtils.class.getClassLoader() != null) {
                    return StaxUtils.class.getClassLoader().loadClass(className);
                }
            } catch (final ClassNotFoundException exc) {
                if (callingClass != null && callingClass.getClassLoader() != null) {
                    return callingClass.getClassLoader().loadClass(className);
                }
            }
            throw ex;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------- //

    public static void setInnerElementLevelThreshold(final int i) {
        innerElementLevelThreshold = i;
    }

    public static void setInnerElementCountThreshold(final int i) {
        innerElementCountThreshold = i;
    }

    /**
     * CXF works with multiple STaX parsers. When we can't find any other way to work against the different parsers, this can be used to
     * condition code. Note: if you've got Woodstox in the class path without being the default provider, this will return the wrong answer.
     *
     * @return true if Woodstox is in the classpath.
     */
    public static boolean isWoodstox() {
        try {
            loadClass("org.codehaus.stax2.XMLStreamReader2", StaxUtils.class);
        } catch (final ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * Return a cached, namespace-aware, factory.
     *
     * @return
     */
    private static XMLInputFactory getXMLInputFactory() {
        if (SAFE_INPUT_FACTORY != null) {
            return SAFE_INPUT_FACTORY;
        }
        XMLInputFactory f = NS_AWARE_INPUT_FACTORY_POOL.poll();
        if (f == null) {
            f = createXMLInputFactory(true);
        }
        return f;
    }

    private static void returnXMLInputFactory(final XMLInputFactory factory) {
        if (SAFE_INPUT_FACTORY != factory) {
            NS_AWARE_INPUT_FACTORY_POOL.offer(factory);
        }
    }

    private static XMLOutputFactory getXMLOutputFactory() {
        if (SAFE_OUTPUT_FACTORY != null) {
            return SAFE_OUTPUT_FACTORY;
        }
        XMLOutputFactory f = OUTPUT_FACTORY_POOL.poll();
        if (f == null) {
            f = XMLOutputFactory.newInstance();
        }
        return f;
    }

    private static void returnXMLOutputFactory(final XMLOutputFactory factory) {
        if (SAFE_OUTPUT_FACTORY != factory) {
            OUTPUT_FACTORY_POOL.offer(factory);
        }
    }

    /**
     * Return a new factory so that the caller can set sticky parameters.
     *
     * @param nsAware
     * @throws XMLStreamException
     */
    public static XMLInputFactory createXMLInputFactory(final boolean nsAware) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        if (!setRestrictionProperties(factory)) {
            try {
                factory = createWoodstoxFactory();
            } catch (final Throwable t) {
                // ignore for now
            }
            if (!setRestrictionProperties(factory)) {
                if (allowInsecureParser) {
                    LOGGER.warn("INSECURE_PARSER_DETECTED: {}", factory.getClass().getName());
                } else {
                    throw new RuntimeException("Cannot create a secure XMLInputFactory");
                }
            }
        }
        setProperty(factory, XMLInputFactory.IS_NAMESPACE_AWARE, nsAware);
        setProperty(factory, XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        setProperty(factory, XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        setProperty(factory, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        factory.setXMLResolver(new XMLResolver() {

            @Override
            public Object resolveEntity(final String publicID, final String systemID, final String baseURI, final String namespace) throws XMLStreamException {
                throw new XMLStreamException("Reading external entities is disabled");
            }
        });

        return factory;
    }

    private static XMLInputFactory createWoodstoxFactory() {
        return WoodstoxHelper.createInputFactory();
    }

    private static boolean setRestrictionProperties(final XMLInputFactory factory) {
        // For now, we can only support Woodstox 4.2.x and newer as none of the other
        // stax parsers support these settings
        if (setProperty(factory, "com.ctc.wstx.maxAttributesPerElement", maxAttributeCount) && setProperty(factory, "com.ctc.wstx.maxAttributeSize", maxAttributeSize) && setProperty(factory, "com.ctc.wstx.maxChildrenPerElement", innerElementCountThreshold) && setProperty(factory, "com.ctc.wstx.maxElementCount", maxElementCount) && setProperty(factory, "com.ctc.wstx.maxElementDepth", innerElementLevelThreshold) && setProperty(factory, "com.ctc.wstx.maxCharacters", maxXMLCharacters) && setProperty(factory, "com.ctc.wstx.maxTextLength", maxTextLength)) {
            return true;
        }
        return false;
    }

    private static boolean setProperty(final XMLInputFactory f, final String p, final Object o) {
        try {
            f.setProperty(p, o);
            return true;
        } catch (final Throwable t) {
            // ignore
        }
        return false;
    }

    public static XMLStreamWriter createXMLStreamWriter(final Writer out) {
        final XMLOutputFactory factory = getXMLOutputFactory();
        try {
            return factory.createXMLStreamWriter(out);
        } catch (final XMLStreamException e) {
            throw new RuntimeException("Can't create XMLStreamWriter", e);
        } finally {
            returnXMLOutputFactory(factory);
        }
    }

    public static XMLStreamWriter createXMLStreamWriter(final OutputStream out) {
        return createXMLStreamWriter(out, null);
    }

    public static XMLStreamWriter createXMLStreamWriter(final OutputStream out, String encoding) {
        if (encoding == null) {
            encoding = "UTF-8";
        }
        final XMLOutputFactory factory = getXMLOutputFactory();
        try {
            return factory.createXMLStreamWriter(out, encoding);
        } catch (final XMLStreamException e) {
            throw new RuntimeException("Can't create XMLStreamWriter", e);
        } finally {
            returnXMLOutputFactory(factory);
        }
    }

    public static XMLStreamWriter createXMLStreamWriter(final Result r) {
        if (r instanceof DOMResult) {
            // use our own DOM writer to avoid issues with Sun's
            // version that doesn't support getNamespaceContext
            final DOMResult dr = (DOMResult) r;
            final Node nd = dr.getNode();
            if (nd instanceof Document) {
                return new W3CDOMStreamWriter((Document) nd);
            } else if (nd instanceof Element) {
                return new W3CDOMStreamWriter((Element) nd);
            } else if (nd instanceof DocumentFragment) {
                return new W3CDOMStreamWriter((DocumentFragment) nd);
            }
        }
        final XMLOutputFactory factory = getXMLOutputFactory();
        try {
            return factory.createXMLStreamWriter(r);
        } catch (final XMLStreamException e) {
            throw new RuntimeException("Cant' create XMLStreamWriter", e);
        } finally {
            returnXMLOutputFactory(factory);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------- //

    public static XMLStreamReader createFilteredReader(final XMLStreamReader reader, final StreamFilter filter) {
        final XMLInputFactory factory = getXMLInputFactory();
        try {
            return factory.createFilteredReader(reader, filter);
        } catch (final XMLStreamException e) {
            throw new RuntimeException("Cant' create XMLStreamReader", e);
        } finally {
            returnXMLInputFactory(factory);
        }
    }

    public static XMLStreamReader createXMLStreamReader(final InputSource src) {
        final String sysId = src.getSystemId() == null ? null : new String(src.getSystemId());
        final String pubId = src.getPublicId() == null ? null : new String(src.getPublicId());
        if (src.getByteStream() != null) {
            if (src.getEncoding() == null) {
                final StreamSource ss = new StreamSource(src.getByteStream(), sysId);
                ss.setPublicId(pubId);
                return createXMLStreamReader(ss);
            }
            return createXMLStreamReader(src.getByteStream(), src.getEncoding());
        } else if (src.getCharacterStream() != null) {
            final StreamSource ss = new StreamSource(src.getCharacterStream(), sysId);
            ss.setPublicId(pubId);
            return createXMLStreamReader(ss);
        } else {
            try {
                final URL url = new URL(sysId);
                final StreamSource ss = new StreamSource(url.openStream(), sysId);
                ss.setPublicId(pubId);
                return createXMLStreamReader(ss);
            } catch (final Exception ex) {
                // ignore - not a valid URL
            }
        }
        throw new IllegalArgumentException("InputSource must have a ByteStream or CharacterStream");
    }

    /**
     * @param in
     * @param encoding
     * @param ctx
     * @return
     */
    public static XMLStreamReader createXMLStreamReader(final InputStream in, String encoding) {
        if (encoding == null) {
            encoding = "UTF-8";
        }

        final XMLInputFactory factory = getXMLInputFactory();
        try {
            return factory.createXMLStreamReader(in, encoding);
        } catch (final XMLStreamException e) {
            throw new RuntimeException("Couldn't parse stream.", e);
        } finally {
            returnXMLInputFactory(factory);
        }
    }

    /**
     * @param in
     * @return
     */
    public static XMLStreamReader createXMLStreamReader(final InputStream in) {
        final XMLInputFactory factory = getXMLInputFactory();
        try {
            return factory.createXMLStreamReader(in);
        } catch (final XMLStreamException e) {
            throw new RuntimeException("Couldn't parse stream.", e);
        } finally {
            returnXMLInputFactory(factory);
        }
    }

    public static XMLStreamReader createXMLStreamReader(final String systemId, final InputStream in) {
        final XMLInputFactory factory = getXMLInputFactory();
        try {
            return factory.createXMLStreamReader(systemId, in);
        } catch (final XMLStreamException e) {
            throw new RuntimeException("Couldn't parse stream.", e);
        } finally {
            returnXMLInputFactory(factory);
        }
    }

    public static XMLStreamReader createXMLStreamReader(final Element el) {
        return new W3CDOMStreamReader(el);
    }

    public static XMLStreamReader createXMLStreamReader(final Document doc) {
        return new W3CDOMStreamReader(doc.getDocumentElement());
    }

    public static XMLStreamReader createXMLStreamReader(final Element el, final String sysId) {
        return new W3CDOMStreamReader(el, sysId);
    }

    public static XMLStreamReader createXMLStreamReader(final Document doc, final String sysId) {
        return new W3CDOMStreamReader(doc.getDocumentElement(), sysId);
    }

    public static XMLStreamReader createXMLStreamReader(final Source source) {
        try {
            if (source instanceof DOMSource) {
                final DOMSource ds = (DOMSource) source;
                final Node nd = ds.getNode();
                Element el = null;
                if (nd instanceof Document) {
                    el = ((Document) nd).getDocumentElement();
                } else if (nd instanceof Element) {
                    el = (Element) nd;
                }

                if (null != el) {
                    return new W3CDOMStreamReader(el, source.getSystemId());
                }
            } else if ("javax.xml.transform.stax.StAXSource".equals(source.getClass().getName())) {
                try {
                    return (XMLStreamReader) source.getClass().getMethod("getXMLStreamReader").invoke(source);
                } catch (final Exception ex) {
                    // ignore
                }
            } else if (source instanceof StaxSource) {
                return ((StaxSource) source).getXMLStreamReader();
            } else if (source instanceof SAXSource) {
                return createXMLStreamReader(((SAXSource) source).getInputSource());
            }

            final XMLInputFactory factory = getXMLInputFactory();
            try {
                XMLStreamReader reader = null;

                try {
                    reader = factory.createXMLStreamReader(source);
                } catch (final UnsupportedOperationException e) {
                    // ignore
                }
                if (reader == null && source instanceof StreamSource) {
                    // createXMLStreamReader from Source is optional, we'll try and map it
                    final StreamSource ss = (StreamSource) source;
                    if (ss.getInputStream() != null) {
                        reader = factory.createXMLStreamReader(ss.getSystemId(), ss.getInputStream());
                    } else {
                        reader = factory.createXMLStreamReader(ss.getSystemId(), ss.getReader());
                    }
                }
                return reader;
            } finally {
                returnXMLInputFactory(factory);
            }
        } catch (final XMLStreamException e) {
            throw new RuntimeException("Couldn't parse stream.", e);
        }
    }

    /**
     * @param reader
     * @return
     */
    public static XMLStreamReader createXMLStreamReader(final Reader reader) {
        final XMLInputFactory factory = getXMLInputFactory();
        try {
            return factory.createXMLStreamReader(reader);
        } catch (final XMLStreamException e) {
            throw new RuntimeException("Couldn't parse stream.", e);
        } finally {
            returnXMLInputFactory(factory);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------- //

    public static String toString(final Document doc) throws XMLStreamException {
        final StringWriter sw = new StringWriter(1024);
        XMLStreamWriter writer = null;
        try {
            writer = createXMLStreamWriter(sw);
            copy(doc, writer);
            writer.flush();
        } finally {
            StaxUtils.close(writer);
        }
        return sw.toString();
    }

    public static String toString(final Element el) throws XMLStreamException {
        final StringWriter sw = new StringWriter(1024);
        XMLStreamWriter writer = null;
        try {
            writer = createXMLStreamWriter(sw);
            copy(el, writer);
            writer.flush();
        } finally {
            StaxUtils.close(writer);
        }
        return sw.toString();
    }

    // -------------------------------------------------------------------------------------------------------------------------------- //

    public static void close(final XMLStreamReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (final Exception e) {
                // ignore
            }
        }
    }

    public static void close(final XMLStreamWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (final Exception e) {
                // ignore
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------- //

    public static void copy(final Document doc, final XMLStreamWriter writer) throws XMLStreamException {
        final XMLStreamReader reader = createXMLStreamReader(doc);
        copy(reader, writer);
    }

    public static void copy(final Element node, final XMLStreamWriter writer) throws XMLStreamException {
        final XMLStreamReader reader = createXMLStreamReader(node);
        copy(reader, writer);
    }

    public static void copy(final XMLStreamReader reader, final OutputStream os) throws XMLStreamException {
        final XMLStreamWriter xsw = StaxUtils.createXMLStreamWriter(os);
        StaxUtils.copy(reader, xsw);
        xsw.close();
    }

    /**
     * Copies the reader to the writer. The start and end document methods must be handled on the writer manually. TODO: if the namespace on
     * the reader has been declared previously to where we are in the stream, this probably won't work.
     *
     * @param reader
     * @param writer
     * @throws XMLStreamException
     */
    public static void copy(final XMLStreamReader reader, final XMLStreamWriter writer) throws XMLStreamException {
        copy(reader, writer, false);
    }

    public static void copy(final XMLStreamReader reader, final XMLStreamWriter writer, final boolean fragment) throws XMLStreamException {
        // number of elements read in
        int read = 0;
        int event = reader.getEventType();

        while (reader.hasNext()) {
            switch (event) {
            case XMLStreamConstants.START_ELEMENT:
                read++;
                writeStartElement(reader, writer);
                break;
            case XMLStreamConstants.END_ELEMENT:
                writer.writeEndElement();
                read--;
                if (read <= 0 && !fragment) {
                    return;
                }
                break;
            case XMLStreamConstants.CHARACTERS:
                final String s = reader.getText();
                if (s != null) {
                    writer.writeCharacters(s);
                }
                break;
            case XMLStreamConstants.COMMENT:
                writer.writeComment(reader.getText());
                break;
            case XMLStreamConstants.CDATA:
                writer.writeCData(reader.getText());
                break;
            case XMLStreamConstants.START_DOCUMENT:
            case XMLStreamConstants.END_DOCUMENT:
            case XMLStreamConstants.ATTRIBUTE:
            case XMLStreamConstants.NAMESPACE:
                break;
            default:
                break;
            }
            event = reader.next();
        }
    }

    private static void writeStartElement(final XMLStreamReader reader, final XMLStreamWriter writer) throws XMLStreamException {
        final String uri = reader.getNamespaceURI();
        String prefix = reader.getPrefix();
        final String local = reader.getLocalName();

        if (prefix == null) {
            prefix = "";
        }

        boolean writeElementNS = false;

        if (uri != null) {
            writeElementNS = true;
            final Iterator<String> it = writer.getNamespaceContext().getPrefixes(uri);
            while (it != null && it.hasNext()) {
                String s = it.next();
                if (s == null) {
                    s = "";
                }
                if (s.equals(prefix)) {
                    writeElementNS = false;
                }
            }
        }

        // Write out the element name
        if (uri != null) {
            if (prefix.length() == 0 && Strings.isEmpty(uri)) {
                writer.writeStartElement(local);
            } else {
                writer.writeStartElement(prefix, local, uri);
            }
        } else {
            writer.writeStartElement(local);
        }

        // Write out the namespaces
        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            final String nsURI = reader.getNamespaceURI(i);
            String nsPrefix = reader.getNamespacePrefix(i);
            if (nsPrefix == null) {
                nsPrefix = "";
            }
            if (nsPrefix.length() == 0) {
                writer.writeDefaultNamespace(nsURI);
                writer.setDefaultNamespace(nsURI);
            } else {
                writer.writeNamespace(nsPrefix, nsURI);
                writer.setPrefix(nsPrefix, nsURI);
            }

            if (nsURI.equals(uri) && nsPrefix.equals(prefix)) {
                writeElementNS = false;
            }
        }

        // Check if the namespace still needs to be written.
        // We need this check because namespace writing works
        // different on Woodstox and the RI.
        if (writeElementNS) {
            if (prefix.length() == 0) {
                writer.writeDefaultNamespace(uri);
                writer.setDefaultNamespace(uri);
            } else {
                writer.writeNamespace(prefix, uri);
                writer.setPrefix(prefix, uri);
            }
        }

        // Write out attributes
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final String ns = reader.getAttributeNamespace(i);
            final String nsPrefix = reader.getAttributePrefix(i);
            if (ns == null || ns.length() == 0) {
                writer.writeAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
            } else if (nsPrefix == null || nsPrefix.length() == 0) {
                writer.writeAttribute(reader.getAttributeNamespace(i), reader.getAttributeLocalName(i), reader.getAttributeValue(i));
            } else {
                final Iterator<String> it = writer.getNamespaceContext().getPrefixes(ns);
                boolean writeNs = true;
                while (it != null && it.hasNext()) {
                    String s = it.next();
                    if (s == null) {
                        s = "";
                    }
                    if (s.equals(nsPrefix)) {
                        writeNs = false;
                    }
                }
                if (writeNs) {
                    writer.writeNamespace(nsPrefix, ns);
                    writer.setPrefix(nsPrefix, ns);
                }
                writer.writeAttribute(reader.getAttributePrefix(i), reader.getAttributeNamespace(i), reader.getAttributeLocalName(i), reader.getAttributeValue(i));
            }

        }
    }

}
