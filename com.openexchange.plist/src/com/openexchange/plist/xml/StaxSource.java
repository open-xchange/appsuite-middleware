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

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;


/**
 * {@link StaxSource}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class StaxSource extends SAXSource implements XMLReader {

    private final XMLStreamReader streamReader;

    private ContentHandler contentHandler;

    private LexicalHandler lexicalHandler;

    public StaxSource(final XMLStreamReader streamReader) {
        this.streamReader = streamReader;
        setInputSource(new InputSource());
    }

    @Override
    public XMLReader getXMLReader() {
        return this;
    }

    public XMLStreamReader getXMLStreamReader() {
        return streamReader;
    }

    protected void parse() throws SAXException {
        try {
            while (true) {
                switch (streamReader.getEventType()) {
                // Attributes are handled in START_ELEMENT
                case XMLStreamConstants.ATTRIBUTE:
                    break;
                case XMLStreamConstants.CDATA:
                {
                    if (lexicalHandler != null) {
                        lexicalHandler.startCDATA();
                    }
                    final int length = streamReader.getTextLength();
                    final int start = streamReader.getTextStart();
                    final char[] chars = streamReader.getTextCharacters();
                    contentHandler.characters(chars, start, length);
                    if (lexicalHandler != null) {
                        lexicalHandler.endCDATA();
                    }
                    break;
                }
                case XMLStreamConstants.CHARACTERS:
                {
                    final int length = streamReader.getTextLength();
                    final int start = streamReader.getTextStart();
                    final char[] chars = streamReader.getTextCharacters();
                    contentHandler.characters(chars, start, length);
                    break;
                }
                case XMLStreamConstants.SPACE:
                {
                    final int length = streamReader.getTextLength();
                    final int start = streamReader.getTextStart();
                    final char[] chars = streamReader.getTextCharacters();
                    contentHandler.ignorableWhitespace(chars, start, length);
                    break;
                }
                case XMLStreamConstants.COMMENT:
                    if (lexicalHandler != null) {
                        final int length = streamReader.getTextLength();
                        final int start = streamReader.getTextStart();
                        final char[] chars = streamReader.getTextCharacters();
                        lexicalHandler.comment(chars, start, length);
                    }
                    break;
                case XMLStreamConstants.DTD:
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    contentHandler.endDocument();
                    return;
                case XMLStreamConstants.END_ELEMENT: {
                    final String uri = streamReader.getNamespaceURI();
                    final String localName = streamReader.getLocalName();
                    final String prefix = streamReader.getPrefix();
                    final String qname = prefix != null && prefix.length() > 0
                        ? prefix + ":" + localName : localName;
                    contentHandler.endElement(uri, localName, qname);
                    break;
                }
                case XMLStreamConstants.ENTITY_DECLARATION:
                case XMLStreamConstants.ENTITY_REFERENCE:
                case XMLStreamConstants.NAMESPACE:
                case XMLStreamConstants.NOTATION_DECLARATION:
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                    contentHandler.startDocument();
                    break;
                case XMLStreamConstants.START_ELEMENT: {
                    final String uri = streamReader.getNamespaceURI();
                    final String localName = streamReader.getLocalName();
                    final String prefix = streamReader.getPrefix();
                    final String qname = prefix != null && prefix.length() > 0
                        ? prefix + ":" + localName : localName;
                    contentHandler.startElement(uri == null ? "" : uri, localName, qname, getAttributes());
                    break;
                }
                default:
                    break;
                }
                if (!streamReader.hasNext()) {
                    return;
                }
                streamReader.next();
            }
        } catch (final XMLStreamException e) {
            SAXParseException spe;
            if (e.getLocation() != null) {
                spe = new SAXParseException(e.getMessage(), null, null,
                                            e.getLocation().getLineNumber(),
                                            e.getLocation().getColumnNumber(), e);
            } else {
                spe = new SAXParseException(e.getMessage(), null, null, -1, -1, e);
            }
            spe.initCause(e);
            throw spe;
        }
    }

    protected String getQualifiedName() {
        final String prefix = streamReader.getPrefix();
        if (prefix != null && prefix.length() > 0) {
            return prefix + ":" + streamReader.getLocalName();
        } else {
            return streamReader.getLocalName();
        }
    }

    protected Attributes getAttributes() {
        final AttributesImpl attrs = new AttributesImpl();
        // Adding namespace declaration as attributes is necessary because
        // the xalan implementation that ships with SUN JDK 1.4 is bugged
        // and does not handle the startPrefixMapping method
        for (int i = 0; i < streamReader.getNamespaceCount(); i++) {
            final String prefix = streamReader.getNamespacePrefix(i);
            String uri = streamReader.getNamespaceURI(i);
            if (uri == null) {
                uri = "";
            }
            // Default namespace
            if (prefix == null || prefix.length() == 0) {
                attrs.addAttribute("",
                                   "",
                                   XMLConstants.XMLNS_ATTRIBUTE,
                                   "CDATA",
                                   uri);
            } else {
                attrs.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                                   prefix,
                                   XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix,
                                   "CDATA",
                                   uri);
            }
        }
        for (int i = 0; i < streamReader.getAttributeCount(); i++) {
            final String uri = streamReader.getAttributeNamespace(i);
            final String localName = streamReader.getAttributeLocalName(i);
            final String prefix = streamReader.getAttributePrefix(i);
            String qName;
            if (prefix != null && prefix.length() > 0) {
                qName = prefix + ':' + localName;
            } else {
                qName = localName;
            }
            final String type = streamReader.getAttributeType(i);
            String value = streamReader.getAttributeValue(i);
            if (value == null) {
                value = "";
            }

            attrs.addAttribute(uri == null ? "" : uri, localName, qName, type, value);
        }
        return attrs;
    }

    @Override
    public boolean getFeature(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    @Override
    public void setFeature(final String name, final boolean value)
        throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    @Override
    public Object getProperty(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    @Override
    public void setProperty(final String name, final Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
            lexicalHandler = (LexicalHandler) value;
        } else {
            throw new SAXNotRecognizedException(name);
        }
    }

    @Override
    public void setEntityResolver(final EntityResolver resolver) {
    }

    @Override
    public EntityResolver getEntityResolver() {
        return null;
    }

    @Override
    public void setDTDHandler(final DTDHandler handler) {
    }

    @Override
    public DTDHandler getDTDHandler() {
        return null;
    }

    @Override
    public void setContentHandler(final ContentHandler handler) {
        contentHandler = handler;
        if (handler instanceof LexicalHandler
            && lexicalHandler == null) {
            lexicalHandler = (LexicalHandler)handler;
        }
    }

    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    @Override
    public void setErrorHandler(final ErrorHandler handler) {
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return null;
    }

    @Override
    public void parse(final InputSource input) throws SAXException {
        StaxSource.this.parse();
    }

    @Override
    public void parse(final String systemId) throws SAXException {
        StaxSource.this.parse();
    }

}
