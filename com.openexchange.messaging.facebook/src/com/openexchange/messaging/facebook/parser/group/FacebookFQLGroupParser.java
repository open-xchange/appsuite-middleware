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

package com.openexchange.messaging.facebook.parser.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.facebook.utility.FacebookGroup;
import com.openexchange.messaging.facebook.utility.FacebookMessagingUtility;
import com.openexchange.messaging.generic.Utility;
import com.openexchange.messaging.generic.internet.MimeMessagingBodyPart;
import com.openexchange.messaging.generic.internet.MimeMultipartContent;

/**
 * {@link FacebookFQLGroupParser} - Parses a given facebook group element.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class FacebookFQLGroupParser {

    private interface ItemHandler {

        void handleItem(Node item, FacebookGroup group) throws OXException;
    }

    private static final Map<String, ItemHandler> ITEM_HANDLERS;

    static {
        {
            final Map<String, ItemHandler> m = new HashMap<String, ItemHandler>();

            m.put("gid", new ItemHandler() {

                @Override
                public void handleItem(final Node item, final FacebookGroup group) throws OXException {
                    group.setGid(FacebookMessagingUtility.parseUnsignedLong(item.getTextContent()));
                }
            });

            m.put("name", new ItemHandler() {

                @Override
                public void handleItem(final Node item, final FacebookGroup group) throws OXException {
                    group.setName(item.getTextContent());
                }
            });

            m.put("pic_small", new ItemHandler() {

                @Override
                public void handleItem(final Node item, final FacebookGroup group) throws OXException {
                    group.setPicSmall(item.getTextContent());
                }
            });

            /*
             * TODO: Add other useful item handler
             */

            ITEM_HANDLERS = Collections.unmodifiableMap(m);
        }
    }

    /**
     * Initializes a new {@link FacebookFQLGroupParser}.
     */
    private FacebookFQLGroupParser() {
        super();
    }

    /**
     * Parses given facebook group element into a user.
     *
     * @param groupElement The facebook group element
     * @return The resulting group
     * @throws OXException If parsing fails
     */
    public static FacebookGroup parseGroupDOMElement(final Element groupElement) throws OXException {
        if (!groupElement.hasChildNodes()) {
            return null;
        }
        final FacebookGroup group = new FacebookGroup();
        /*
         * Iterate child nodes
         */
        final NodeList childNodes = groupElement.getChildNodes();
        final int len = childNodes.getLength();
        for (int i = 0; i < len; i++) {
            final Node item = childNodes.item(i);
            final String localName = item.getLocalName();
            if (null != localName) {
                final ItemHandler itemHandler = ITEM_HANDLERS.get(localName);
                if (null == itemHandler) {
                    com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FacebookFQLGroupParser.class)).warn("Un-handled item: " + localName);
                } else {
                    itemHandler.handleItem(item, group);
                }
            }
        }
        /*
         * Return
         */
        return group.isEmpty() ? null : group;
    }

    /**
     * Gets the first node with specified name occurring as child below given node.
     *
     * @param name The node name to look-up
     * @param node The parent node
     * @return The appropriate node or <code>null</code> if none found
     */
    static Node getNodeByName(final String name, final Node node) {
        if (null == name || null == node || !node.hasChildNodes()) {
            return null;
        }
        final NodeList nodes = node.getChildNodes();
        return getNodeByName(name, nodes, nodes.getLength());
    }

    /**
     * Gets the first node with specified name occurring in given node list.
     *
     * @param name The node name to look-up
     * @param nodes The node list
     * @param len The length of the node list
     * @return The appropriate node or <code>null</code> if none found
     */
    static Node getNodeByName(final String name, final NodeList nodes, final int len) {
        for (int i = 0; i < len; i++) {
            final Node item = nodes.item(i);
            if (name.equals(item.getLocalName())) {
                return item;
            }
        }
        return null;
    }

    /**
     * Gets all nodes with specified name occurring as child below given node.
     *
     * @param name The node name to look-up
     * @param node The parent node
     * @return The appropriate nodes
     */
    static List<Node> getNodesByName(final String name, final Node node) {
        if (null == name || null == node || !node.hasChildNodes()) {
            return Collections.emptyList();
        }
        final NodeList nodes = node.getChildNodes();
        final int len = nodes.getLength();
        final List<Node> ret = new ArrayList<Node>(len);
        for (int i = 0; i < len; i++) {
            final Node item = nodes.item(i);
            if (name.equals(item.getLocalName())) {
                ret.add(item);
            }
        }
        return ret;
    }

    private static final String HTML_SPACE = "&#160;";

    private static final String UTF_8 = "UTF-8";

    private static MimeMultipartContent createAlternative(final String messageText) throws OXException {
        final MimeMultipartContent alt = new MimeMultipartContent("alternative");
        {
            final MimeMessagingBodyPart text = new MimeMessagingBodyPart();
            text.setText(Utility.textFormat(messageText), UTF_8, "plain");
            text.setHeader("MIME-Version", "1.0");
            text.setHeader("Content-Type", "text/plain; charset=UTF-8");
            alt.addBodyPart(text);
        }
        {
            final MimeMessagingBodyPart html = new MimeMessagingBodyPart();
            final String contentType = "text/html; charset=UTF-8";
            if (messageText == null || messageText.length() == 0) {
                html.setContent(new StringContent(Utility.getConformHTML(HTML_SPACE, UTF_8).replaceFirst(HTML_SPACE, "")), contentType);
            } else {
                html.setContent(new StringContent(Utility.getConformHTML(messageText, UTF_8)), contentType);
            }
            html.setHeader("MIME-Version", "1.0");
            html.setHeader("Content-Type", contentType);
            alt.addBodyPart(html);
        }
        return alt;
    }

}
