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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.cxf.interceptor.transform.TransformInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.staxutils.transform.TransformUtils;
import com.openexchange.java.Streams;

/**
 * {@link RemoveGenericLabelledElementsInterceptor}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RemoveGenericLabelledElementsInterceptor extends TransformInInterceptor {

    /**
     * Initializes a new {@link RemoveGenericLabelledElementsInterceptor}.
     */
    public RemoveGenericLabelledElementsInterceptor() {
        super();
    }

    private static boolean disabled() {
        // TODO: Remove me if implementation is done
        return true;
    }

    private static boolean transformWithinReader() {
        return true;
    }

    @Override
    public void handleMessage(final Message message) {
        if (disabled()) {
            return;
        }
        /*
         * Transform within reader or within stream content
         */
        if (transformWithinReader()) {
            XMLStreamReader reader = message.getContent(XMLStreamReader.class);
            InputStream is = message.getContent(InputStream.class);
            // Create transforming reader
            final XMLStreamReader transformReader = new ReplacingXMLStreamReader(TransformUtils.createNewReaderIfNeeded(reader, is));
            if (transformReader != null) {
                message.setContent(XMLStreamReader.class, transformReader);
                message.removeContent(InputStream.class);
            }
        } else {
            XMLStreamReader reader = message.getContent(XMLStreamReader.class);
            reader = TransformUtils.createNewReaderIfNeeded(reader, message.getContent(InputStream.class));
            ByteArrayOutputStream out = Streams.newByteArrayOutputStream(8192);

            // TODO: Modify by reader's events & write to out

            final XMLStreamReader transformReader = TransformUtils.createNewReaderIfNeeded(null, Streams.newByteArrayInputStream(out.toByteArray()));
            out = null;
            if (transformReader != null) {
                message.setContent(XMLStreamReader.class, transformReader);
                message.removeContent(InputStream.class);
            }
        }
    }

    private static final class ReplacingXMLStreamReader extends StackXMLStreamReader {

        // See org.apache.cxf.staxutils.transform.InTransformReader

        private final Stack<List<ParsingEvent>> pushedAheadEvents;
        private final Stack<ParsingEvent> pushedBackEvents;
        private ParsingEvent currentEvent;
        private QName lastNonGeneric;
        private final NamespaceContext namespaceContext;
        private final List<Integer> attributesIndexes;
        private boolean attributesIndexed;

        protected ReplacingXMLStreamReader(final XMLStreamReader reader) {
            super(reader);
            pushedAheadEvents = new Stack<List<ParsingEvent>>();
            pushedBackEvents = new Stack<ParsingEvent>();
            attributesIndexes = new ArrayList<Integer>();
            namespaceContext = reader.getNamespaceContext();
        }

        @Override
        public int next() throws XMLStreamException {
            if (!pushedBackEvents.empty()) {
                // consume events from the pushed back stack
                currentEvent = pushedBackEvents.pop();
                return currentEvent.getEvent();
            }
            int event = super.next();
            if (XMLStreamConstants.START_ELEMENT == event) {
                attributesIndexed = false;
                final QName theName = super.getName();
                if (isGeneric(theName)) {
                    // {http://soap.admin.openexchange.com}db>
                    QName expected = getExpected(lastNonGeneric); // new QName("http://soap.admin.openexchange.com", "auth", "");
                    String prefix = theName.getPrefix();
                    if (isEmpty(prefix) && isEmpty(theName.getNamespaceURI()) && !isEmpty(expected.getNamespaceURI())) {
                        // prefix = namespaceContext.getPrefix(expected.getNamespaceURI());
                        // if (prefix == null) {
                        // prefix = namespaceContext.findUniquePrefix(expected.getNamespaceURI());
                        // }
                        prefix = "";
                    } else if (prefix.length() > 0 && expected.getNamespaceURI().length() == 0) {
                        prefix = "";
                    }
                    expected = new QName(expected.getNamespaceURI(), expected.getLocalPart(), prefix);
                    if (isEmptyQName(expected)) {
                        // skip the current element (deep drop)
                        final int depth = getDepth();
                        while (depth != getDepth() || super.next() != XMLStreamConstants.END_ELEMENT) {
                            // get to the matching end element event
                        }
                        event = next();
                    } else {
                        currentEvent = createStartElementEvent(expected);
                        if (theName.equals(expected)) {
                            pushedAheadEvents.push(null);
                        } else {
                            pushedAheadEvents.push(Collections.singletonList(createEndElementEvent(expected)));
                        }
                    }
                } else {
                    lastNonGeneric = theName;
                }
            } else if (XMLStreamConstants.END_ELEMENT == event) {
                final QName theName = super.getName();

                final List<ParsingEvent> pe = pushedAheadEvents.pop();
                if (null != pe) {
                    pushedBackEvents.addAll(pe);
                    currentEvent = pushedBackEvents.pop();
                    event = currentEvent.getEvent();
                }
            } else {
                currentEvent = null;
            }

            return event;
        }

        private QName getExpected(final QName lastNonGeneric) {
            return null;
        }

        @Override
        public String getLocalName() {
            if (currentEvent != null) {
                return currentEvent.getName().getLocalPart();
            } else {
                return super.getLocalName();
            }
        }

        @Override
        public String getPrefix() {
            final QName name = readCurrentElement();
            String prefix = name.getPrefix();
            if (prefix.length() == 0 && getNamespaceURI().length() > 0) {
                prefix = getNamespaceContext().getPrefix(getNamespaceURI());
                if (prefix == null) {
                    prefix = "";
                }
            }
            return prefix;
        }

        @Override
        public String getNamespaceURI() {
            if (currentEvent != null) {
                return currentEvent.getName().getNamespaceURI();
            } else {
                return super.getNamespaceURI();
            }
        }

        private QName readCurrentElement() {
            if (currentEvent != null) {
                return currentEvent.getName();
            }
            final String ns = super.getNamespaceURI();
            final String name = super.getLocalName();
            final String prefix = super.getPrefix();
            return new QName(ns, name, prefix == null ? "" : prefix);
        }

        @Override
        public QName getName() {
            return new QName(getNamespaceURI(), getLocalName());
        }

        @Override
        public int getAttributeCount() {
            if (!pushedBackEvents.empty()) {
                return 0;
            }
            checkAttributeIndexRange(-1);
            return attributesIndexes.size();
        }

        @Override
        public String getAttributeLocalName(final int index) {
            if (!pushedBackEvents.empty()) {
                throwIndexException(index, 0);
            }
            checkAttributeIndexRange(index);

            return getAttributeName(index).getLocalPart();
        }

        @Override
        public QName getAttributeName(final int index) {
            if (!pushedBackEvents.empty()) {
                throwIndexException(index, 0);
            }
            checkAttributeIndexRange(index);
            final QName aname = super.getAttributeName(attributesIndexes.get(index).intValue());
            return aname;
        }

        @Override
        public String getAttributeNamespace(final int index) {
            if (!pushedBackEvents.empty()) {
                throwIndexException(index, 0);
            }
            checkAttributeIndexRange(index);

            return getAttributeName(index).getNamespaceURI();
        }

        @Override
        public String getAttributePrefix(final int index) {
            if (!pushedBackEvents.empty()) {
                throwIndexException(index, 0);
            }
            checkAttributeIndexRange(index);

            final QName aname = getAttributeName(index);
            if (XMLConstants.NULL_NS_URI.equals(aname.getNamespaceURI())) {
                return "";
            } else {
                return namespaceContext.getPrefix(aname.getNamespaceURI());
            }
        }

        @Override
        public String getAttributeType(final int index) {
            if (!pushedBackEvents.empty()) {
                throwIndexException(index, 0);
            }
            checkAttributeIndexRange(index);
            return super.getAttributeType(attributesIndexes.get(index).intValue());
        }

        @Override
        public String getAttributeValue(final int index) {
            if (!pushedBackEvents.empty()) {
                throwIndexException(index, 0);
            }
            checkAttributeIndexRange(index);
            return super.getAttributeValue(attributesIndexes.get(index).intValue());
        }

        @Override
        public String getAttributeValue(final String namespace, final String localName) {
            if (!pushedBackEvents.empty()) {
                return null;
            }
            checkAttributeIndexRange(-1);
            // TODO need reverse lookup
            return super.getAttributeValue(namespace, localName);
        }

        @Override
        public String getText() {
            if (currentEvent != null) {
                return currentEvent.getValue();
            }
            return super.getText();
        }

        @Override
        public char[] getTextCharacters() {
            if (currentEvent != null && currentEvent != null) {
                return currentEvent.getValue().toCharArray();
            }
            return super.getTextCharacters();
        }

        @Override
        public int getTextCharacters(final int sourceStart, final char[] target, final int targetStart, final int length) throws XMLStreamException {
            if (currentEvent != null && currentEvent != null) {
                int len = currentEvent.getValue().length() - sourceStart;
                if (len > length) {
                    len = length;
                }
                currentEvent.getValue().getChars(sourceStart, sourceStart + len, target, targetStart);
                return len;
            }

            return super.getTextCharacters(sourceStart, target, targetStart, length);
        }

        @Override
        public int getTextLength() {
            if (currentEvent != null && currentEvent.getValue() != null) {
                return currentEvent.getValue().length();
            }
            return super.getTextLength();
        }

        /**
         * Checks the index range for the current attributes set. If the attributes are not indexed for the current element context, they
         * will be indexed.
         * 
         * @param index
         */
        private void checkAttributeIndexRange(final int index) {
            if (!attributesIndexed) {
                attributesIndexes.clear();
                final int count = super.getAttributeCount();
                for (int i = 0; i < count; i++) {
                    attributesIndexes.add(Integer.valueOf(i));
                }
                attributesIndexed = true;
            }
            if (index >= attributesIndexes.size()) {
                throwIndexException(index, attributesIndexes.size());
            }
        }

        private void throwIndexException(final int index, final int size) {
            throw new IllegalArgumentException("Invalid index " + index + "; current element has only " + size + " attributes");
        }

        // private static final Pattern PATTERN_GENERIC = Pattern.compile(Pattern.quote("c-gensym") + "[0-9]+");

        private static boolean isGeneric(final QName name) {
            final String localPart = name.getLocalPart();
            return null != localPart && localPart.toLowerCase(Locale.US).startsWith("c-gensym");
        }

        private static boolean isEmptyQName(final QName qname) {
            return XMLConstants.NULL_NS_URI.equals(qname.getNamespaceURI()) && "".equals(qname.getLocalPart());
        }

        private static ParsingEvent createStartElementEvent(final QName name) {
            return new ParsingEvent(XMLStreamConstants.START_ELEMENT, name, null);
        }

        private static ParsingEvent createEndElementEvent(final QName name) {
            return new ParsingEvent(XMLStreamConstants.END_ELEMENT, name, null);
        }

        private static boolean isEmpty(final String string) {
            if (null == string) {
                return true;
            }
            final int len = string.length();
            boolean isWhitespace = true;
            for (int i = 0; isWhitespace && i < len; i++) {
                isWhitespace = Character.isWhitespace(string.charAt(i));
            }
            return isWhitespace;
        }
    }

}
