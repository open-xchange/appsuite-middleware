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

package com.openexchange.configuration.clt.scr;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * {@link Logback} contains various helpers for Logback related SCRs.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.4
 */
public class Logback {

    /**
     * Get all appender-ref child elements from a NodeList.
     * 
     * @param nodeList NodeList that might contain descendant appender-ref elements
     * @return Set of all found appender-ref elements or an empty Set
     */
    public static Set<Element> getAppenderRefs(NodeList nodeList) {
        Set<Element> refs = new HashSet<Element>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element currentElement = Element.class.cast(currentNode);
                refs.addAll(getAppenderRefs(currentElement));
            }
        }

        return refs;
    }

    /**
     * Get all appender-ref child elements from an Element.
     * 
     * @param element Element that might contain descendant appender-ref elements
     * @return Set of all found appender-ref elements or an empty Set
     */
    public static Set<Element> getAppenderRefs(Element element) {
        Set<Element> refs = new HashSet<Element>();

        NodeList appenderRefs = element.getElementsByTagName("appender-ref");
        for (int i = 0; i < appenderRefs.getLength(); i++) {
            Node appenderRefNode = appenderRefs.item(i);
            if (appenderRefNode.getNodeType() == Node.ELEMENT_NODE) {
                refs.add(Element.class.cast(appenderRefNode));
            }
        }

        return refs;
    }

    /**
     * Recursively get all descendent appenders that match the given appender-refs.
     * 
     * @param allAppenders The Set of all known appenders.
     * @param appenderRefs The Set of appender-refs to be looked up after inspecting an Element for appender references
     * @return An empty Map if the referenced appenders don't have any appender-ref Elements or a Map containing all name -> referenced appender entries
     */
    public static Map<String, Element> getMatchingAppenders(Map<String, Element> allAppenders, Set<Element> appenderRefs) {
        if (allAppenders.isEmpty() || appenderRefs.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Element> matchingAppenders = new HashMap<String, Element>();

        for (Element appenderRef : appenderRefs) {
            String refName = appenderRef.getAttribute("ref");
            if (!refName.isEmpty()) {
                Element appender = allAppenders.get(refName);
                if (appender != null && !matchingAppenders.containsKey(refName)) {
                    matchingAppenders.put(refName, appender);
                }
            }
        }

        if (!matchingAppenders.isEmpty()) {
            Map<String, Element> referencedAppenders = new HashMap<String, Element>();
            for (Element appender : matchingAppenders.values()) {
                referencedAppenders.putAll(getMatchingAppenders(allAppenders, getAppenderRefs(appender)));
            }
            matchingAppenders.putAll(referencedAppenders);
        }

        return matchingAppenders;
    }

    /**
     * Transform a list of <node name="a_name"..> nodes to a name -> element mapping. Nodes that aren't Elements or don't have a name are discarded.
     *   
     * @param nodeList The list of nodes to be transformed
     * @return An empty map if no Element nodes with a name attribute can be found, the name -> element mappings otherwise
     */
    public static Map<String, Element> nodeListToNamedElementMap(NodeList nodeList) {
        Map<String, Element> elementMap = new HashMap<String, Element>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node appenderNode = nodeList.item(i);
            if (appenderNode.getNodeType() == Node.ELEMENT_NODE) {
                Element appenderElement = Element.class.cast(appenderNode);
                String appenderName = appenderElement.getAttribute("name");
                if (!appenderName.isEmpty()) {
                    elementMap.put(appenderName, appenderElement);
                }
            }
        }
        return elementMap;
    }

    /**
     * Get the configured logfile for the given appender.
     * 
     * @param appender The appender to inspect for log file configuration
     * @return An empty String or the text content of the file childnode of the given appender
     * @throws Exception if we find a malformed element in logback.xml
     */
    public static String getLogFileFromAppender(Element appender) throws Exception {
        String logfile = "";
        NodeList logfileList = appender.getElementsByTagName("file");
        if (logfileList.getLength() < 1) {
            throw new Exception("Appender should have a logfile configured");
        }
        Node logfileItem = logfileList.item(0);
        String logfileText = logfileItem.getTextContent();
        logfile = logfileText == null ? logfile : logfileText;
        return logfile;
    }
    
    /**
     * Remove a list of nodes from the logback.xml document
     * 
     * @param nodeList The list of nodes to remove
     * @return the number of removed nodes
     */
    public static int removeNodeList(NodeList nodeList) {
    	int removed=0;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node original = nodeList.item(i);
            Node parent = original.getParentNode();
            // remove previous text to remove empty lines
            Node prev = original.getPreviousSibling();
            if (prev != null && prev.getNodeType() == Node.TEXT_NODE && prev.getNodeValue().trim().isEmpty()) {
                parent.removeChild(prev);
            }
            parent.removeChild(original);
            removed++;
        }
        return removed;
    }
}
