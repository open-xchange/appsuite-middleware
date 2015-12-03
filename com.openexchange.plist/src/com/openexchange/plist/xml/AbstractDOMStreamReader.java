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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


/**
 * {@link AbstractDOMStreamReader} - Copy of <code>org.apache.cxf.staxutils.AbstractDOMStreamReader</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public abstract class AbstractDOMStreamReader<T, I> implements XMLStreamReader {

    protected int currentEvent = XMLStreamConstants.START_DOCUMENT;

    private final Map<String, Object> properties = new HashMap<String, Object>();

    private final FastStack<ElementFrame<T, I>> frames = new FastStack<ElementFrame<T, I>>();

    private ElementFrame<T, I> frame;


    /**
     *
     */
    public static class ElementFrame<T, I> {
        T element;
        I currentChild;

        boolean started;
        boolean ended;

        List<String> uris;
        List<String> prefixes;
        List<Object> attributes;

        final ElementFrame<T, I> parent;

        public ElementFrame(final T element, final ElementFrame<T, I> parent) {
            this.element = element;
            this.parent = parent;
        }

        public ElementFrame(final T element, final ElementFrame<T, I> parent, final I ch) {
            this.element = element;
            this.parent = parent;
            this.currentChild = ch;
        }
        public ElementFrame(final T doc, final boolean s) {
            this.element = doc;
            parent = null;
            started = s;
            attributes = Collections.emptyList();
            prefixes = Collections.emptyList();
            uris = Collections.emptyList();
        }
        public ElementFrame(final T doc) {
            this(doc, true);
        }
        public T getElement() {
            return element;
        }

        public I getCurrentChild() {
            return currentChild;
        }
        public void setCurrentChild(final I o) {
            currentChild = o;
        }
        public boolean isDocument() {
            return false;
        }
        public boolean isDocumentFragment() {
            return false;
        }
    }

    /**
     * @param element
     */
    public AbstractDOMStreamReader(final ElementFrame<T, I> frame) {
        this.frame = frame;
        frames.push(this.frame);
    }

    protected ElementFrame<T, I> getCurrentFrame() {
        return frame;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.stream.XMLStreamReader#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(final String key) throws IllegalArgumentException {
        return properties.get(key);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.stream.XMLStreamReader#next()
     */
    @Override
    public int next() throws XMLStreamException {
        if (frame.ended) {
            frames.pop();
            if (!frames.empty()) {
                frame = frames.peek();
            } else {
                currentEvent = END_DOCUMENT;
                return currentEvent;
            }
        }

        if (!frame.started) {
            frame.started = true;
            currentEvent = frame.isDocument() ? START_DOCUMENT : START_ELEMENT;
        } else if (hasMoreChildren()) {
            currentEvent = nextChild();

            if (currentEvent == START_ELEMENT) {
                final ElementFrame<T, I> newFrame = getChildFrame();
                newFrame.started = true;
                frame = newFrame;
                frames.push(this.frame);
                currentEvent = START_ELEMENT;

                newFrame(newFrame);
            }
        } else {
            frame.ended = true;
            if (frame.isDocument()) {
                currentEvent = END_DOCUMENT;
            } else {
                currentEvent = END_ELEMENT;
                endElement();
            }
        }
        return currentEvent;
    }

    protected void newFrame(final ElementFrame<T, I> newFrame) {
    }

    protected void endElement() {
    }

    protected abstract boolean hasMoreChildren();
    protected abstract int nextChild();
    protected abstract ElementFrame<T, I> getChildFrame();

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.stream.XMLStreamReader#require(int, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void require(final int arg0, final String arg1, final String arg2) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.stream.XMLStreamReader#getElementText()
     */
    @Override
    public abstract String getElementText() throws XMLStreamException;

    public void consumeFrame() {
        frame.started = true;
        frame.ended = true;
        if (frame.isDocument()) {
            currentEvent = END_DOCUMENT;
        } else {
            currentEvent = END_ELEMENT;
            endElement();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.stream.XMLStreamReader#nextTag()
     */
    @Override
    public int nextTag() throws XMLStreamException {
        while (hasNext()) {
            if (START_ELEMENT == next()) {
                return START_ELEMENT;
            }
        }

        return currentEvent;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.stream.XMLStreamReader#hasNext()
     */
    @Override
    public boolean hasNext() throws XMLStreamException {

        return !(frame.ended && (frames.size() == 0 || frame.isDocumentFragment()));

    }

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.stream.XMLStreamReader#close()
     */
    @Override
    public void close() throws XMLStreamException {
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.stream.XMLStreamReader#getNamespaceURI(java.lang.String)
     */
    @Override
    public abstract String getNamespaceURI(String prefix);

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.stream.XMLStreamReader#isStartElement()
     */
    @Override
    public boolean isStartElement() {
        return currentEvent == START_ELEMENT;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.stream.XMLStreamReader#isEndElement()
     */
    @Override
    public boolean isEndElement() {
        return currentEvent == END_ELEMENT;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.stream.XMLStreamReader#isCharacters()
     */
    @Override
    public boolean isCharacters() {
        return currentEvent == CHARACTERS;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.stream.XMLStreamReader#isWhiteSpace()
     */
    @Override
    public boolean isWhiteSpace() {
        if (currentEvent == CHARACTERS || currentEvent == CDATA) {
            final String text = getText();
            final int len = text.length();
            for (int i = 0; i < len; ++i) {
                if (text.charAt(i) > 0x0020) {
                    return false;
                }
            }
            return true;
        }
        return currentEvent == SPACE;
    }

    @Override
    public int getEventType() {
        return currentEvent;
    }

    @Override
    public int getTextCharacters(final int sourceStart, final char[] target, final int targetStart, int length)
        throws XMLStreamException {
        final char[] src = getText().toCharArray();

        if (sourceStart + length >= src.length) {
            length = src.length - sourceStart;
        }

        for (int i = 0; i < length; i++) {
            target[targetStart + i] = src[i + sourceStart];
        }

        return length;
    }

    @Override
    public boolean hasText() {
        return currentEvent == CHARACTERS || currentEvent == DTD || currentEvent == ENTITY_REFERENCE
                || currentEvent == COMMENT || currentEvent == SPACE;
    }

    public String getSystemId() {
        return null;
    }
    public String getPublicId() {
        return null;
    }
    @Override
    public Location getLocation() {
        return new Location() {

            @Override
            public int getCharacterOffset() {
                return 0;
            }

            @Override
            public int getColumnNumber() {
                return 0;
            }

            @Override
            public int getLineNumber() {
                return 0;
            }

            @Override
            public String getPublicId() {
                return AbstractDOMStreamReader.this.getPublicId();
            }

            @Override
            public String getSystemId() {
                return AbstractDOMStreamReader.this.getSystemId();
            }

        };
    }

    @Override
    public boolean hasName() {
        return currentEvent == START_ELEMENT || currentEvent == END_ELEMENT;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean isStandalone() {
        return false;
    }

    @Override
    public boolean standaloneSet() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getCharacterEncodingScheme() {
        // TODO Auto-generated method stub
        return null;
    }
}
