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
package com.openexchange.user.copy.internal.folder.util;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * {@link Node}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Node<T extends Comparable<T>> implements Comparable<Node<T>> {

    private final T value;

    private Node<T> parent;

    private Set<Node<T>> children;


    public Node(final T value, final Node<T> parent) {
        super();
        this.value = value;
        this.parent = parent;
        children = new HashSet<Node<T>>();
    }

    public boolean addChild(final Node<T> node) {
        return children.add(node);
    }

    public Set<Node<T>> getChildren() {
        return new TreeSet<>(children);
    }

    public void setChildren(final Set<Node<T>> children) {
        this.children = new HashSet<Node<T>>();
        this.children.addAll(children);
    }

    public void setParent(final Node<T> node) {
        parent = node;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public T getValue() {
        return value;
    }

    public Node<T> getParent() {
        return parent;
    }

    public Set<Node<T>> getChildren(final Node<T> node) {
        if (this.equals(node)) {
            return getChildren();
        } else {
            if (children.isEmpty()) {
                return null;
            } else {
                Set<Node<T>> found = null;
                for (final Node<T> child : children) {
                    found = child.getChildren(node);
                    if (found != null) {
                        break;
                    }
                }
                return found;
            }
        }
    }

    public boolean removeChild(final Node<T> child) {
        if (children.contains(child)) {
            return children.remove(child);
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        final
        Node<T> other = (Node<T>) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "{" + value.toString() + "}";
    }

    @Override
    public int compareTo(Node<T> o) {
        return this.value.compareTo(o.getValue());
    }

}
