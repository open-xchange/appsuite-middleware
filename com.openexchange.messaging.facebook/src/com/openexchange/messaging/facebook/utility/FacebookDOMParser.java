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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.messaging.facebook.utility;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.facebook.FacebookMessagingExceptionCodes;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link FacebookDOMParser} - Parser for Facebook XML results from a FQL query.
 *
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;fql_query_response xmlns="http://api.facebook.com/1.0/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" list="true"&gt;
 *   &lt;user&gt;
 *     &lt;wall_count&gt;6&lt;/wall_count&gt;
 *   &lt;/user&gt;
 * &lt;/fql_query_response&gt;
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FacebookDOMParser {

    /**
     * Initializes a new {@link FacebookDOMParser}.
     */
    private FacebookDOMParser() {
        super();
    }

    public static void main(final String[] args) {
        try {
            parseXMLResponse("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            		"<fql_query_response xmlns=\"http://api.facebook.com/1.0/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" list=\"true\">\n" +
            		"  <stream_post>\n" +
            		"    <updated_time>1297960981</updated_time>\n" +
            		"    <filter_key>nf</filter_key>\n" +
            		"    <message>This is my new, dynamic test-post. Enjoy!</message>\n" +
            		"    <post_id>1551949035_1539050846839</post_id>\n" +
            		"    <attachment>\n" +
            		"      <description/>\n" +
            		"    </attachment>\n" +
            		"    <actor_id>1551949035</actor_id>\n" +
            		"    <created_time>1297960981</created_time>\n" +
            		"  </stream_post>\n" +
            		"</fql_query_response>");
        } catch (final Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static final short ELEMENT_NODE = Node.ELEMENT_NODE;

    /**
     * Parses XML-formatted FQL query response
     *
     * @param xmlReponse The XML-formatted FQL query response
     * @return The parsed list of elements
     * @throws OXException If parsing fails
     */
    public static List<Element> parseXMLResponse(final String xmlReponse) throws OXException {
        try {
            /*
             * Parse to DOM document
             */
            final DOMParser parser = new DOMParser();
            parser.parse(new InputSource(new UnsynchronizedByteArrayInputStream(xmlReponse.getBytes(com.openexchange.java.Charsets.UTF_8))));
            final Document dom = parser.getDocument();
            /*
             * Get document's root element
             */
            final Element rootElement = dom.getDocumentElement();
            final String attribute = rootElement.getAttribute("list");
            if ("true".equalsIgnoreCase(attribute)) {
                final NodeList childNodes = rootElement.getChildNodes();
                final int length = childNodes.getLength();
                if (length <= 0) {
                    return Collections.emptyList();
                }
                final List<Element> list = new ArrayList<Element>(length);
                for (int i = 0; i < length; i++) {
                    final Node node = childNodes.item(i);
                    if (ELEMENT_NODE == node.getNodeType()) {
                        list.add((Element) node);
                    }
                }
                return list;
            }
            /*
             * Return singleton list
             */
            return Collections.singletonList(rootElement);
        } catch (final UnsupportedEncodingException e) {
            throw FacebookMessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final SAXException e) {
            throw FacebookMessagingExceptionCodes.XML_PARSE_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw FacebookMessagingExceptionCodes.XML_PARSE_ERROR.create(e, e.getMessage());
        }
    }

}
