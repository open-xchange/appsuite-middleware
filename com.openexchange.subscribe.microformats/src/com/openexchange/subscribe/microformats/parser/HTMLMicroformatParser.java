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

package com.openexchange.subscribe.microformats.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.microformats.OXMFParser;
import com.openexchange.subscribe.microformats.OXMFSubscriptionErrorMessage;

/**
 * {@link HTMLMicroformatParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class HTMLMicroformatParser implements OXMFParser {

    private static final String IMG = "img";
    private static final String ANCHOR = "a";

    private final Set<String> containerClasses = new HashSet<String>();
    private final Set<String> prefixes = new HashSet<String>();

    @Override
    public void addAttributePrefix(String prefix) {
        prefixes.add(prefix);
    }

    @Override
    public void addContainerElement(String containerElement) {
        containerClasses.add(containerElement);
    }

    @Override
    public List<Map<String, String>> parse(String html) throws OXException {
        return parse(new StringReader(html));
    }

    @Override
    public List<Map<String, String>> parse(Reader html) throws OXException {
        DOMParser parser = new DOMParser();
        ArrayList<Map<String, String>> container = new ArrayList<Map<String, String>>();
        try {

            parser.parse(new InputSource(html));
            Document document = parser.getDocument();

            recurse(document, container);

        } catch (SAXException e) {
            OXMFSubscriptionErrorMessage.ParseException.create(e, e.getMessage());
        } catch (IOException e) {
            OXMFSubscriptionErrorMessage.IOException.create(e, e.getMessage());
        }
        return container;
    }

    private void recurse(Node node, ArrayList<Map<String, String>> container) {
        if(isContainer(node)) {
            Map<String, String> element = new HashMap<String, String>();
            NodeList childNodes = node.getChildNodes();
            for(int i = 0, size = childNodes.getLength(); i < size; i++) {
                Node child = childNodes.item(i);
                recurse(child, element);
            }
            container.add(element);
        } else {
            NodeList childNodes = node.getChildNodes();
            for(int i = 0, size = childNodes.getLength(); i < size; i++) {
                Node child = childNodes.item(i);
                recurse(child, container);
            }
        }
    }

    private void recurse(Node node, Map<String, String> element) {
        List<String> attributeKeys = getAttributeKeys(node);
        if(attributeKeys != null) {
            parse(node, element, attributeKeys);
        } else {
            NodeList childNodes = node.getChildNodes();
            for(int i = 0, size = childNodes.getLength(); i < size; i++) {
                Node child = childNodes.item(i);
                recurse(child, element);
            }
        }
    }

    private void parse(Node node, Map<String, String> element, List<String> attributeKeys) {
        String value = isImageElement(node)? getSrc(node) : isAnchorElement(node) ? getHref(node) : node.getTextContent();
        for (String key : attributeKeys) {
            element.put(key, value.trim());
        }
    }

    private String getHref(Node node) {
        return ((Element)node).getAttribute("href");
    }

    private String getSrc(Node node) {
        return ((Element)node).getAttribute("src");
    }

    private boolean isImageElement(Node node) {
        if(node.getNodeType() == Node.ELEMENT_NODE) {
            return IMG.equalsIgnoreCase(((Element)node).getTagName());
        }
        return false;
    }

    private boolean isAnchorElement(Node node) {
        if(node.getNodeType() == Node.ELEMENT_NODE) {
            return ANCHOR.equalsIgnoreCase(((Element)node).getTagName());
        }
        return false;
    }

    private List<String> getAttributeKeys(Node node) {
        if(node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            String[] classes = element.getAttribute("class").split("\\s+");
            List<String> keys = new ArrayList<String>(classes.length);
            for (String klass : classes) {
                if (hasValidPrefix(klass)) {
                    keys.add(klass);
                }
            }
            if(keys.size() == 0) {
                return null;
            }
            return keys;
        }
        return null;
    }

    private boolean hasValidPrefix(String klass) {
        for(String prefix : prefixes) {
            if(klass.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isContainer(Node node) {
        if(node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            String[] classes = element.getAttribute("class").split("\\s+");
            for (String klass : classes) {
                if (containerClasses.contains(klass)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void reset() {

    }

}
