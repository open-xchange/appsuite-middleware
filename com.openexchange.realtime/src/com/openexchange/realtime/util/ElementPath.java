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

package com.openexchange.realtime.util;

import java.io.Serializable;

/**
 * {@link ElementPaths} - Identifies elements in a namespace.
 * Structured after Javas Package.Class namespace scheme <code>PathRoot.PathNode1.PathNode2.Element</code>.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ElementPath implements Serializable, Comparable<ElementPath> {

    private static final long serialVersionUID = -3227354270593651700L;

    private String namespace = "";

    private String element = "";

    /**
     * Initializes a new {@link ElementPath} structured after Javas Package.Class namespace scheme
     * <code>PathRoot.PathNode1.PathNode2.Element</code>.
     * 
     * @param namespace The namespace containing elements e.g. "PathRoot.PathNode1.PathNode2" 
     * @param element The name of the element within the namespace e.g. "Element"
     */
    public ElementPath(String namespace, String element) {
        if (namespace != null) {
            this.namespace = namespace;
        }
        if (element == null || element.isEmpty()) {
            throw new IllegalArgumentException("Malformed elementPath");
        }
        this.element = element;
    }

    /**
     * Create a ElementPath from a String representation.
     * Valid input is either just an element or a namespace and an Element joined by a dot. Neither namespaces nor elements must start or
     * end with a dot. So valid input looks like <code>Element</code> or <code>Root.Node.Element</code>
     * Initializes a new {@link ElementPath}.
     *
     * @param elementPath the string represntation to parse
     * @throws IllegalArgumentException for malformed elementPaths
     */
    public ElementPath(String elementPath) {
        int index = elementPath.lastIndexOf(".");
        if (index == -1) { // default namespace
            this.element = elementPath;
        } else {
            String namespace = elementPath.substring(0, index);
            if (namespace.isEmpty()) {
                throw new IllegalArgumentException("Malformed elementPath");
            }
            this.namespace = namespace;

            String element = elementPath.substring(index + 1, elementPath.length());
            if (element.isEmpty()) {
                throw new IllegalArgumentException("Malformed elementPath");
            }
            this.element = element;
        }
    }

    public String getNamespace() {
        return namespace;
    }

    public String getElement() {
        return element;
    }

    public String getElementPath() {
        return namespace + "." + element;
    }

    @Override
    public String toString() {
        if("".equals(namespace)) {
            return "ElementPath [" + element + "]";
        }
        return "ElementPath [" + namespace + "." + element + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((element == null) ? 0 : element.hashCode());
        result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ElementPath)) {
            return false;
        }
        ElementPath other = (ElementPath) obj;
        if (element == null) {
            if (other.element != null) {
                return false;
            }
        } else if (!element.equals(other.element)) {
            return false;
        }
        if (namespace == null) {
            if (other.namespace != null) {
                return false;
            }
        } else if (!namespace.equals(other.namespace)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ElementPath other) {
        return getElementPath().compareTo(other.getElementPath());
    }

}
