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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.soap.cxf;

import java.util.LinkedList;
import java.util.Queue;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * {@link StackXMLStreamReader} - Keeps track of current element depth and manages a stack of elements.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class StackXMLStreamReader implements XMLStreamReader {

    /**
     * The underlying reader instance.
     */
    protected XMLStreamReader reader;

    private int depth;
    private final Queue<QName> elements;

    /**
     * Initializes a new {@link StackXMLStreamReader}.
     * 
     * @param r The delegate reader
     */
    public StackXMLStreamReader(final XMLStreamReader r) {
        super();
        this.reader = r;
        elements = new LinkedList<QName>();
    }

    /**
     * Gets the reader.
     * 
     * @return The reader
     */
    public XMLStreamReader getReader() {
        return this.reader;
    }

    /**
     * Gets the current element stack reference.
     * 
     * @return The element stack
     */
    protected Queue<QName> getElementsDirect() {
        return elements;
    }

    /**
     * Gets the current element stack.
     * <p>
     * <b>Note</b>: Returned queue is a <i>copy</i>. Changes will not be reflected in this reader's collection.
     * 
     * @return The element stack
     */
    public Queue<QName> getElements() {
        return new LinkedList<QName>(elements);
    }

    /**
     * Gets the current depth.
     * 
     * @return The depth.
     */
    public int getDepth() {
        return depth;
    }

    @Override
    public void close() throws XMLStreamException {
        reader.close();
    }

    @Override
    public int getAttributeCount() {
        return reader.getAttributeCount();
    }

    @Override
    public String getAttributeLocalName(final int arg0) {
        return reader.getAttributeLocalName(arg0);
    }

    @Override
    public QName getAttributeName(final int arg0) {
        return reader.getAttributeName(arg0);
    }

    @Override
    public String getAttributeNamespace(final int arg0) {
        return reader.getAttributeNamespace(arg0);
    }

    @Override
    public String getAttributePrefix(final int arg0) {
        return reader.getAttributePrefix(arg0);
    }

    @Override
    public String getAttributeType(final int arg0) {
        return reader.getAttributeType(arg0);
    }

    @Override
    public String getAttributeValue(final int arg0) {
        return reader.getAttributeValue(arg0);
    }

    @Override
    public String getAttributeValue(final String namespace, final String localName) {
        return reader.getAttributeValue(namespace, localName);
    }

    @Override
    public String getCharacterEncodingScheme() {
        return reader.getCharacterEncodingScheme();
    }

    @Override
    public String getElementText() throws XMLStreamException {
        final String ret = reader.getElementText();
        // workaround bugs in some readers that aren't properly advancing to
        // the END_ELEMENT (*cough*jettison*cough*)
        while (reader.getEventType() != XMLStreamReader.END_ELEMENT) {
            reader.next();
        }
        depth--;
        elements.poll();
        return ret;
    }

    @Override
    public String getEncoding() {
        return reader.getEncoding();
    }

    @Override
    public int getEventType() {
        return reader.getEventType();
    }

    @Override
    public String getLocalName() {
        return reader.getLocalName();
    }

    @Override
    public Location getLocation() {
        return reader.getLocation();
    }

    @Override
    public QName getName() {
        return reader.getName();
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return reader.getNamespaceContext();
    }

    @Override
    public int getNamespaceCount() {
        return reader.getNamespaceCount();
    }

    @Override
    public String getNamespacePrefix(final int arg0) {
        return reader.getNamespacePrefix(arg0);
    }

    @Override
    public String getNamespaceURI() {
        return reader.getNamespaceURI();
    }

    @Override
    public String getNamespaceURI(final int arg0) {

        return reader.getNamespaceURI(arg0);
    }

    @Override
    public String getNamespaceURI(final String arg0) {
        return reader.getNamespaceURI(arg0);
    }

    @Override
    public String getPIData() {
        return reader.getPIData();
    }

    @Override
    public String getPITarget() {
        return reader.getPITarget();
    }

    @Override
    public String getPrefix() {
        return reader.getPrefix();
    }

    @Override
    public Object getProperty(final String arg0) throws IllegalArgumentException {

        return reader.getProperty(arg0);
    }

    @Override
    public String getText() {
        return reader.getText();
    }

    @Override
    public char[] getTextCharacters() {
        return reader.getTextCharacters();
    }

    @Override
    public int getTextCharacters(final int arg0, final char[] arg1, final int arg2, final int arg3) throws XMLStreamException {
        return reader.getTextCharacters(arg0, arg1, arg2, arg3);
    }

    @Override
    public int getTextLength() {
        return reader.getTextLength();
    }

    @Override
    public int getTextStart() {
        return reader.getTextStart();
    }

    @Override
    public String getVersion() {
        return reader.getVersion();
    }

    @Override
    public boolean hasName() {
        return reader.hasName();
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        return reader.hasNext();
    }

    @Override
    public boolean hasText() {
        return reader.hasText();
    }

    @Override
    public boolean isAttributeSpecified(final int arg0) {
        return reader.isAttributeSpecified(arg0);
    }

    @Override
    public boolean isCharacters() {
        return reader.isCharacters();
    }

    @Override
    public boolean isEndElement() {
        return reader.isEndElement();
    }

    @Override
    public boolean isStandalone() {
        return reader.isStandalone();
    }

    @Override
    public boolean isStartElement() {
        return reader.isStartElement();
    }

    @Override
    public boolean isWhiteSpace() {
        return reader.isWhiteSpace();
    }

    @Override
    public int next() throws XMLStreamException {
        final int next = reader.next();

        if (next == START_ELEMENT) {
            elements.offer(getName());
            depth++;
        } else if (next == END_ELEMENT) {
            depth--;
            elements.poll();
        }

        return next;
    }

    @Override
    public int nextTag() throws XMLStreamException {
        int eventType = next();
        while ((eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) || (eventType == XMLStreamConstants.CDATA && isWhiteSpace())
        // skip whitespace
        || eventType == XMLStreamConstants.SPACE || eventType == XMLStreamConstants.PROCESSING_INSTRUCTION || eventType == XMLStreamConstants.COMMENT) {
            eventType = next();
        }
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException("expected start or end tag", getLocation());
        }
        return eventType;
    }

    @Override
    public void require(final int arg0, final String arg1, final String arg2) throws XMLStreamException {
        reader.require(arg0, arg1, arg2);
    }

    @Override
    public boolean standaloneSet() {
        return reader.standaloneSet();
    }

    @Override
    public int hashCode() {
        return reader.hashCode();
    }

    @Override
    public boolean equals(final Object arg0) {
        return reader.equals(arg0);
    }

    @Override
    public String toString() {
        return reader.toString();
    }

}
