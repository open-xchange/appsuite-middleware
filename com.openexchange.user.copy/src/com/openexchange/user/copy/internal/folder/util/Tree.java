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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * {@link Tree}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Tree<T extends Comparable<T>> {

    private Node<T> root;

    private final Map<T, Node<T>> nodes;

    public Tree(final T rootObject) {
        super();
        root = new Node<T>(rootObject, null);
        nodes = new HashMap<T, Node<T>>();
        nodes.put(rootObject, root);
    }

    public T getRoot() {
        return root.getValue();
    }

    public boolean addChild(final T toAdd, final T parent) {
        final Node<T> pNode = nodes.get(parent);
        if (pNode == null) {
            return false;
        } else {
            final Node<T> node = new Node<T>(toAdd, pNode);
            if (pNode.addChild(node)) {
                nodes.put(toAdd, node);
                return true;
            }

            return false;
        }
    }

    public Set<T> getChildren(final T parent) {
        final Set<T> set = new TreeSet<T>();
        final Node<T> n1 = nodes.get(parent);
        if (n1 != null) {
            final Set<Node<T>> children = root.getChildren(n1);
            if (children != null) {
                for (final Node<T> node : children) {
                    set.add(node.getValue());
                }
            }
        }

        return set;
    }

    public boolean containsChild(final T object) {
        return nodes.containsKey(object);
    }

    public boolean isLeaf(final T object) {
        final Node<T> node = nodes.get(object);
        if (node == null) {
            return true;
            // TODO: throw Exception
        }

        return node.isLeaf();
    }

    public boolean removeChild(final T object) {
        removeChildRecursive(object);

        final Node<T> node = nodes.get(object);
        if (node != null) {
            final Node<T> parent = node.getParent();
            if (parent.removeChild(node)) {
                removeChildRecursive(object);
                return true;
            }
        }

        return false;
    }

    private void removeChildRecursive(final T object) {
        final Node<T> node = nodes.remove(object);
        if (node != null && !node.isLeaf()) {
            final Set<Node<T>> children = node.getChildren();
            for (final Node<T> child : children) {
                removeChildRecursive(child.getValue());
            }
        }
    }

    public boolean exchangeNodes(final T origin, final T newObject) {
        final Node<T> originNode = nodes.remove(origin);
        if (originNode == null) {
            return false;
        }

        final Node<T> parentNode = originNode.getParent();
        final Node<T> newNode = new Node<T>(newObject, parentNode);
        final Set<Node<T>> children = originNode.getChildren();
        for (final Node<T> node : children) {
            node.setParent(newNode);
        }
        newNode.setChildren(children);
        if (parentNode == null) {
            this.root = null;
            this.root = newNode;
            nodes.put(newObject, newNode);
            return true;
        } else if (parentNode.removeChild(originNode) && parentNode.addChild(newNode)) {
            nodes.put(newObject, newNode);
            return true;
        }

        return false;
    }

    public Set<T> getAllNodesAsSet() {
        final Set<T> set = new HashSet<T>();
        set.addAll(nodes.keySet());

        return set;
    }

}
