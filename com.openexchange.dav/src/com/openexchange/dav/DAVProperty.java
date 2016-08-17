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

package com.openexchange.dav;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.jdom2.Attribute;
import org.jdom2.Element;
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
        return toString();
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
    public String toString() {
        Writer writer = null;
        try {
            writer = new AllocatingStringWriter();
            new XMLOutputter().output(element, writer);
            return writer.toString();
        } catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(DAVProperty.class);
            return null;
        } finally {
            Streams.close(writer);
        }
    }

}
