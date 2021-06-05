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
