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

package com.openexchange.imap.threader;

/**
 * The ThreadContainer object is used to encapsulate an {@code Threadable} object (it holds some intermediate state used while threading.)
 * This is a friendly class that doesn't escape from this module.
 */
final class ThreadContainer {

    Threadable threadable;
    ThreadContainer parent;
    ThreadContainer child;
    ThreadContainer next;

    /**
     * Initializes a new {@link ThreadContainer}.
     */
    ThreadContainer() {
        super();
    }

    // Copy the ThreadContainer tree structure down into the underlying
    // Threadable objects (that is, make the Threadable tree look like
    // the ThreadContainer tree.)
    //
    @SuppressWarnings("null")
    void flush() {
        ThreadContainer tc = this;
        while (tc != null) {
            final Threadable threadable = tc.threadable;
            final boolean hasThreadable = threadable != null;
            if (tc.parent != null && !hasThreadable) {
                // Only the rootNode is allowed to not have a threadable.
                throw new Error("no threadable in " + this.toString());
            }
            // Drop parent reference
            tc.parent = null;
            // Handle child
            final ThreadContainer childContainer = tc.child;
            if (hasThreadable) {
                threadable.setChild(childContainer == null ? null : childContainer.threadable);
            }
            if (childContainer != null) {
                childContainer.flush();
                tc.child = null;
            }
            // Handle next
            final ThreadContainer nextContainer = tc.next;
            if (hasThreadable) {
                threadable.setNext(nextContainer == null ? null : nextContainer.threadable);
            }
            // Drop next reference & point to next sibling
            tc.next = null;
            tc.threadable = null;
            tc = nextContainer;
        }
        /*-
         *
         *

        if (threadable != null) {
            threadable.setChild(child == null ? null : child.threadable);
        }

        if (child != null) {
            child.flush();
            child = null;
        }

        if (threadable != null) {
            threadable.setNext(next == null ? null : next.threadable);
        }

        if (next != null) {
            next.flush();
            next = null;
        }

        threadable = null;
         *
         */
    }

    /**
     * Returns <code>true</code> if child is under self's tree. This is used for detecting circularities in the references header.
     *
     * @param target The target container
     * @return <code>true</code> if child is under self's tree; otherwise <code>false</code>
     */
    boolean findChild(ThreadContainer target) {
        try {
            if (child == null) {
                return false;
            } else if (child == target) {
                return true;
            } else {
                return child.findChild(target);
            }
        } catch (@SuppressWarnings("unused") final StackOverflowError error) {
            return false;
        }
    }

    /**
     * Reverse the children (child through child.next.next.next...)
     */
    void reverseChildren() {
        if (child != null) {
            // reverse the children (child through child.next.next.next...)
            ThreadContainer kid, prev, rest;
            for (prev = null, kid = child, rest = kid.next; kid != null; prev = kid, kid = rest, rest = (rest == null ? null : rest.next)) {
                kid.next = prev;
            }
            child = prev;

            // then do it for the kids
            for (kid = child; kid != null; kid = kid.next) {
                kid.reverseChildren();
            }
        }
    }
}
