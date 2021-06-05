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

package com.openexchange.dav;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.java.Streams;
import com.openexchange.webdav.protocol.WebdavProperty;

/**
 * {@link DAVProperty}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class DAVProperty extends WebdavProperty {

    private final Element element;

    /**
     * Initializes a new {@link DAVProperty} based on the supplied XML element.
     *
     * @param element The element
     */
    public DAVProperty(Element element) {
        super();
        this.element = element;
    }

    /**
     * Gets the XML element.
     *
     * @return The element
     */
    public Element getElement() {
        return element;
    }

    @Override
    public String getLanguage() {
        Attribute langAttribute = element.getAttribute("lang");
        return null != langAttribute ? langAttribute.getValue() : null;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getNamespace() {
        return element.getNamespaceURI();
    }

    @Override
    public String getValue() {
        if (null != element.getChildren() && 0 < element.getChildren().size()) {
            return toString();
        }
        return element.getText();
    }

    @Override
    public List<Element> getChildren() {
        return element.getChildren();
    }

    @Override
    public Map<String, String> getAttributes() {
        HashMap<String, String> attributes = new HashMap<String, String>();
        for (Attribute attribute : element.getAttributes()) {
            attributes.put(attribute.getName(), attribute.getValue());
        }
        return attributes;
    }

    @Override
    public boolean isXML() {
        return true;
    }

    @Override
    public boolean isDate() {
        return false;
    }

    @Override
    public void setNamespace(String namespace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDate(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLanguage(String lang) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setXML(boolean xml) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChildren(List<Element> children) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        Writer writer = null;
        try {
            writer = new AllocatingStringWriter();
            new XMLOutputter(Format.getPrettyFormat()).output(element, writer);
            return writer.toString();
        } catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(DAVProperty.class);
            return null;
        } finally {
            Streams.close(writer);
        }
    }

}
