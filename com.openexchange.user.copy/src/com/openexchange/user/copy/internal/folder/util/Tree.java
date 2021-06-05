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
